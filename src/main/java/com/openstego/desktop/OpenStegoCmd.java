/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop;

import com.openstego.desktop.util.CommonUtil;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.PluginManager;
import com.openstego.desktop.util.cmd.CmdLineOption;
import com.openstego.desktop.util.cmd.CmdLineOptions;
import com.openstego.desktop.util.cmd.CmdLineParser;
import com.openstego.desktop.util.cmd.PasswordInput;

import java.io.File;
import java.util.List;

/**
 * This is the main class for OpenStego command line
 */
public class OpenStegoCmd {
    /**
     * LabelUtil instance to retrieve labels
     */
    private static final LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    /**
     * Main method for processing command line
     *
     * @param args Command line arguments
     */
    public static void execute(String[] args) {
        String command = null;
        String pluginName;
        OpenStego stego = null;
        CmdLineParser parser;
        CmdLineOptions options;
        CmdLineOption option;
        List<CmdLineOption> optionList;
        OpenStegoPlugin<?> plugin = null;

        try {
            // First parse of the command-line (without plugin specific options)
            parser = new CmdLineParser(getStdCmdLineOptions(null), args);
            if (!parser.isValid()) {
                displayUsage();
                return;
            }

            pluginName = parser.getParsedOptions().getStringValue("-a");

            // Get the plugin object
            if (pluginName != null && !pluginName.equals("")) {
                plugin = PluginManager.getPluginByName(pluginName);
                if (plugin == null) {
                    throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.PLUGIN_NOT_FOUND, pluginName);
                }
            }
            // Try to auto-select plugin
            else {
                List<OpenStegoPlugin<?>> plugins = PluginManager.getPlugins();
                if (plugins.size() == 1) {
                    plugin = plugins.get(0);
                } else if (plugins.size() > 1) {
                    optionList = parser.getParsedOptionsAsList();
                    if (optionList.size() > 0) {
                        command = (optionList.get(0)).getName();
                        if (command.equals("embed") || command.equals("extract")) {
                            plugins = PluginManager.getDataHidingPlugins();
                            if (plugins.size() == 1) {
                                plugin = plugins.get(0);
                            }
                        } else if (command.equals("gensig") || command.equals("embedmark") || command.equals("checkmark")) {
                            plugins = PluginManager.getWatermarkingPlugins();
                            if (plugins.size() == 1) {
                                plugin = plugins.get(0);
                            }
                        }
                    }
                }
            }

            // Second parse of the command-line (with plugin specific options)
            if (plugin != null) {
                parser = new CmdLineParser(getStdCmdLineOptions(plugin), args);
            }

            optionList = parser.getParsedOptionsAsList();
            options = parser.getParsedOptions();

            for (int i = 0; i < optionList.size(); i++) {
                option = optionList.get(i);
                if (((i == 0) && (option.getType() != CmdLineOption.TYPE_COMMAND)) || ((i > 0) && (option.getType() == CmdLineOption.TYPE_COMMAND))) {
                    displayUsage();
                    return;
                }

                if (i == 0) {
                    command = option.getName();
                }
            }

            // Non-standard options are not allowed
            if (parser.getNonStdOptions().size() > 0) {
                displayUsage();
                return;
            }

            // Check that algorithm is selected
            assert command != null;
            if (!command.equals("help") && !command.equals("algorithms")) {
                if (plugin == null) {
                    throw new OpenStegoException(null, OpenStego.NAMESPACE, OpenStegoErrors.NO_PLUGIN_SPECIFIED);
                } else {
                    // Create main stego object
                    plugin.resetConfig(parser.getParsedOptions());
                    stego = new OpenStego(plugin, plugin.getConfig());
                }
            }

            switch (command) {
                case "embed":
                    executeEmbed(options, stego);
                    break;
                case "embedmark":
                    executeEmbedMark(options, stego);
                    break;
                case "extract":
                    executeExtract(options, stego);
                    break;
                case "checkmark":
                    executeCheckMark(options, stego);
                    break;
                case "gensig":
                    executeGenSig(options, stego);
                    break;
                case "diff":
                    executeDiff(options, stego);
                    break;
                case "readformats": {
                    List<String> formats = plugin.getReadableFileExtensions();
                    formats.forEach(System.out::println);
                    break;
                }
                case "writeformats": {
                    List<String> formats = plugin.getWritableFileExtensions();
                    formats.forEach(System.out::println);
                    break;
                }
                case "algorithms":
                    List<OpenStegoPlugin<?>> plugins = PluginManager.getPlugins();
                    for (OpenStegoPlugin<?> osp : plugins) {
                        System.out.println(osp.getName() + " " + osp.getPurposesLabel() + " - " + osp.getDescription());
                    }
                    break;
                case "help":
                    if (plugin == null) {
                        displayUsage();
                        return;
                    } else { // Show plugin-specific help
                        System.err.println(plugin.getUsage());
                    }
                    break;
                default:
                    displayUsage();
            }
        } catch (OpenStegoException osEx) {
            if (osEx.getErrorCode() == OpenStegoException.UNHANDLED_EXCEPTION) {
                osEx.printStackTrace();
            } else {
                System.err.println(osEx.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method to execute "embed" command
     *
     * @param options Command-line options
     * @param stego   {@link OpenStego} object
     * @throws OpenStegoException Processing issues
     */
    private static void executeEmbed(CmdLineOptions options, OpenStego stego) throws OpenStegoException {
        String msgFileName = options.getStringValue("-mf");
        String coverFileName = options.getStringValue("-cf");
        String stegoFileName = options.getStringValue("-sf");
        List<File> coverFileList;

        // Check if we need to prompt for password
        if (stego.getConfig().isUseEncryption() && stego.getConfig().getPassword() == null) {
            stego.getConfig().setPassword(PasswordInput.readPassword(labelUtil.getString("cmd.msg.enterPassword") + " "));
        }

        File msgFile = (msgFileName == null || msgFileName.equals("-")) ? null : new File(msgFileName);
        coverFileList = CommonUtil.parseFileList(coverFileName, ";");
        // If no coverfile or only one coverfile is provided then use stegofile name given by the user
        if (coverFileList.size() <= 1) {
            if (coverFileList.size() == 0 && coverFileName != null && !coverFileName.equals("-")) {
                System.err.println(labelUtil.getString("cmd.msg.coverFileNotFound", coverFileName));
                return;
            }

            String stegoFile = (stegoFileName == null || stegoFileName.equals("-")) ? null : stegoFileName;
            CommonUtil.writeFile(
                    stego.embedData(msgFile, coverFileList.size() == 0 ? null : coverFileList.get(0), stegoFile),
                    stegoFile);
        }
        // Else loop through all coverfiles and overwrite the same coverfiles with generated stegofiles
        else {
            // If stego file name is provided, then warn user that it will be ignored
            if (stegoFileName != null && !stegoFileName.equals("-")) {
                System.err.println(labelUtil.getString("cmd.warn.stegoFileIgnored"));
            }

            // Loop through all cover files
            for (File file : coverFileList) {
                coverFileName = file.getName();
                CommonUtil.writeFile(stego.embedData(msgFile, file, coverFileName), coverFileName);
                System.err.println(labelUtil.getString("cmd.msg.coverProcessed", coverFileName));
            }
        }
    }

    /**
     * Method to execute "embedmark" command
     *
     * @param options Command-line options
     * @param stego   {@link OpenStego} object
     * @throws OpenStegoException Processing issues
     */
    private static void executeEmbedMark(CmdLineOptions options, OpenStego stego) throws OpenStegoException {
        String sigFileName = options.getStringValue("-gf");
        String coverFileName = options.getStringValue("-cf");
        String stegoFileName = options.getStringValue("-sf");

        File sigFile = (sigFileName == null || sigFileName.equals("-")) ? null : new File(sigFileName);
        List<File> coverFileList = CommonUtil.parseFileList(coverFileName, ";");
        // If no coverfile or only one coverfile is provided then use stegofile name given by the user
        if (coverFileList.size() <= 1) {
            if (coverFileList.size() == 0 && coverFileName != null && !coverFileName.equals("-")) {
                System.err.println(labelUtil.getString("cmd.msg.coverFileNotFound", coverFileName));
                return;
            }

            String stegoFile = (stegoFileName == null || stegoFileName.equals("-")) ? null : stegoFileName;
            CommonUtil.writeFile(
                    stego.embedMark(sigFile, coverFileList.size() == 0 ? null : coverFileList.get(0), stegoFile),
                    stegoFile);
        }
        // Else loop through all coverfiles and overwrite the same coverfiles with generated stegofiles
        else {
            // If stego file name is provided, then warn user that it will be ignored
            if (stegoFileName != null && !stegoFileName.equals("-")) {
                System.err.println(labelUtil.getString("cmd.warn.stegoFileIgnored"));
            }

            // Loop through all cover files
            for (File file : coverFileList) {
                coverFileName = file.getName();
                CommonUtil.writeFile(stego.embedMark(sigFile, file, coverFileName), coverFileName);
                System.err.println(labelUtil.getString("cmd.msg.coverProcessed", coverFileName));
            }
        }
    }

    /**
     * Method to execute "extract" command
     *
     * @param options Command-line options
     * @param stego   {@link OpenStego} object
     * @throws OpenStegoException Processing issues
     */
    private static void executeExtract(CmdLineOptions options, OpenStego stego) throws OpenStegoException {
        String stegoFileName = options.getStringValue("-sf");
        String extractDir = options.getStringValue("-xd");
        String extractFileName;
        List<?> msgData;

        if (stegoFileName == null) {
            displayUsage();
            return;
        }

        try {
            msgData = stego.extractData(new File(stegoFileName));
        } catch (OpenStegoException osEx) {
            if (osEx.getErrorCode() == OpenStegoErrors.INVALID_PASSWORD || osEx.getErrorCode() == OpenStegoErrors.NO_VALID_PLUGIN) {
                if (stego.getConfig().getPassword() == null) {
                    stego.getConfig().setPassword(PasswordInput.readPassword(labelUtil.getString("cmd.msg.enterPassword") + " "));

                    try {
                        msgData = stego.extractData(new File(stegoFileName));
                    } catch (OpenStegoException inEx) {
                        if (inEx.getErrorCode() == OpenStegoErrors.INVALID_PASSWORD) {
                            System.err.println(inEx.getMessage());
                            return;
                        } else {
                            throw inEx;
                        }
                    }
                } else {
                    System.err.println(osEx.getMessage());
                    return;
                }
            } else {
                throw osEx;
            }
        }

        extractFileName = options.getStringValue("-xf");
        if (extractFileName == null) {
            extractFileName = (String) msgData.get(0);
            if (extractFileName == null || extractFileName.equals("")) {
                extractFileName = "untitled";
            }
        }

        if (extractDir != null) {
            extractFileName = extractDir + File.separator + extractFileName;
        }

        CommonUtil.writeFile((byte[]) msgData.get(1), extractFileName);
        System.err.println(labelUtil.getString("cmd.msg.fileExtracted", extractFileName));
    }

    /**
     * Method to execute "checkmark" command
     *
     * @param options Command-line options
     * @param stego   {@link OpenStego} object
     * @throws OpenStegoException Processing issues
     */
    private static void executeCheckMark(CmdLineOptions options, OpenStego stego) throws OpenStegoException {
        String stegoFileName = options.getStringValue("-sf");
        String sigFileName = options.getStringValue("-gf");
        List<File> stegoFileList;

        if (stegoFileName == null || sigFileName == null) {
            displayUsage();
            return;
        }

        stegoFileList = CommonUtil.parseFileList(stegoFileName, ";");
        // If only one stegofile is provided then use stegofile name given by the user
        if (stegoFileList.size() == 1) {
            System.out.println(stego.checkMark(stegoFileList.get(0), new File(sigFileName)));
        }
        // Else loop through all stegofiles and calculate correlation value for each
        else {
            for (File file : stegoFileList) {
                stegoFileName = file.getName();
                System.out.println(stegoFileName + "\t" + stego.checkMark(file, new File(sigFileName)));
            }
        }
    }

    /**
     * Method to execute "gensig" command
     *
     * @param options Command-line options
     * @param stego   {@link OpenStego} object
     * @throws OpenStegoException Processing issues
     */
    private static void executeGenSig(CmdLineOptions options, OpenStego stego) throws OpenStegoException {
        // Check if we need to prompt for password
        if (stego.getConfig().getPassword() == null) {
            stego.getConfig().setPassword(PasswordInput.readPassword(labelUtil.getString("cmd.msg.enterPassword") + " "));
        }

        String signatureFileName = options.getStringValue("-gf");
        CommonUtil.writeFile(stego.generateSignature(), (signatureFileName == null || signatureFileName.equals("-")) ? null : signatureFileName);
    }

    /**
     * Method to execute "diff" command
     *
     * @param options Command-line options
     * @param stego   {@link OpenStego} object
     * @throws OpenStegoException Processing issues
     */
    private static void executeDiff(CmdLineOptions options, OpenStego stego) throws OpenStegoException {
        String coverFileName = options.getStringValue("-cf");
        String stegoFileName = options.getStringValue("-sf");
        String extractDir = options.getStringValue("-xd");
        String extractFileName = options.getStringValue("-xf");

        if (extractDir != null) {
            extractFileName = extractDir + File.separator + extractFileName;
        }

        CommonUtil.writeFile(stego.getDiff(new File(stegoFileName), new File(coverFileName), extractFileName), extractFileName);
    }

    /**
     * Method to display usage for OpenStego
     *
     * @throws OpenStegoException Processing issues
     */
    private static void displayUsage() throws OpenStegoException {
        PluginManager.loadPlugins();

        System.err.print(labelUtil.getString("appName") + " " + labelUtil.getString("appVersion") + ". ");
        System.err.println(labelUtil.getString("copyright") + "\n");
        System.err.println(labelUtil.getString("cmd.usage", File.separator));
    }

    /**
     * Method to generate the standard list of command-line options
     *
     * @param plugin Stego plugin for plugin-specific command-line options
     * @return Standard list of command-line options
     * @throws OpenStegoException Processing issues
     */
    private static CmdLineOptions getStdCmdLineOptions(OpenStegoPlugin<?> plugin) throws OpenStegoException {
        CmdLineOptions options = new CmdLineOptions();

        // Commands
        options.add("embed", "--embed", CmdLineOption.TYPE_COMMAND, false);
        options.add("extract", "--extract", CmdLineOption.TYPE_COMMAND, false);
        options.add("gensig", "--gensig", CmdLineOption.TYPE_COMMAND, false);
        options.add("embedmark", "--embedmark", CmdLineOption.TYPE_COMMAND, false);
        options.add("checkmark", "--checkmark", CmdLineOption.TYPE_COMMAND, false);
        options.add("diff", "--diff", CmdLineOption.TYPE_COMMAND, false);
        options.add("readformats", "--readformats", CmdLineOption.TYPE_COMMAND, false);
        options.add("writeformats", "--writeformats", CmdLineOption.TYPE_COMMAND, false);
        options.add("algorithms", "--algorithms", CmdLineOption.TYPE_COMMAND, false);
        options.add("help", "--help", CmdLineOption.TYPE_COMMAND, false);

        // Plugin options
        options.add("-a", "--algorithm", CmdLineOption.TYPE_OPTION, true);

        // File options
        options.add("-mf", "--messagefile", CmdLineOption.TYPE_OPTION, true);
        options.add("-cf", "--coverfile", CmdLineOption.TYPE_OPTION, true);
        options.add("-sf", "--stegofile", CmdLineOption.TYPE_OPTION, true);
        options.add("-xf", "--extractfile", CmdLineOption.TYPE_OPTION, true);
        options.add("-xd", "--extractdir", CmdLineOption.TYPE_OPTION, true);
        options.add("-gf", "--sigfile", CmdLineOption.TYPE_OPTION, true);

        // Command options
        options.add("-c", "--compress", CmdLineOption.TYPE_OPTION, false);
        options.add("-C", "--nocompress", CmdLineOption.TYPE_OPTION, false);
        options.add("-e", "--encrypt", CmdLineOption.TYPE_OPTION, false);
        options.add("-E", "--noencrypt", CmdLineOption.TYPE_OPTION, false);
        options.add("-p", "--password", CmdLineOption.TYPE_OPTION, true);
        options.add("-A", "--cryptalgo", CmdLineOption.TYPE_OPTION, true);

        // Plugin-specific options
        if (plugin != null) {
            plugin.populateStdCmdLineOptions(options);
        }

        return options;
    }
}
