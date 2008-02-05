/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.UIManager;

import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.util.*;

/**
 * This is the main class for OpenStego. It includes the {@link #main(java.lang.String[])} method which provides the
 * command line interface for the tool. It also has API methods which can be used by external programs
 * when using OpenStego as a library.
 */
public class OpenStego
{
    /**
     * Constant for the namespace for labels
     */
    public static final String NAMESPACE = "OpenStego";

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(NAMESPACE);

    /**
     * Configuration data
     */
    private OpenStegoConfig config = null;

    /**
     * Stego plugin to use for embedding / extracting data
     */
    private OpenStegoPlugin plugin = null;

    static
    {
        LabelUtil.addNamespace(NAMESPACE, "net.sourceforge.openstego.resource.OpenStegoLabels");
    }

    /**
     * Constructor using the default configuration
     * @param plugin Stego plugin to use
     */
    public OpenStego(OpenStegoPlugin plugin)
    {
        this(plugin, (OpenStegoConfig) null);
    }

    /**
     * Constructor using <code>OpenStegoConfig</code> object
     * @param plugin Stego plugin to use
     * @param config OpenStegoConfig object with configuration data
     */
    public OpenStego(OpenStegoPlugin plugin, OpenStegoConfig config)
    {
        if(plugin == null)
        {
            this.plugin = PluginManager.getDefaultPlugin();
        }
        else
        {
            this.plugin = plugin;
        }

        if(config == null)
        {
            this.config = new OpenStegoConfig();
        }
        else
        {
            this.config = config;
        }
    }

    /**
     * Constructor with configuration data in the form of <code>Map<code>
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    public OpenStego(OpenStegoPlugin plugin, Map propMap) throws OpenStegoException
    {
        this(plugin, new OpenStegoConfig(propMap));
    }

    /**
     * Method to embed the message data into the cover data
     * @param msg Message data to be embedded
     * @param msgFileName Name of the message file
     * @param cover Cover data into which message data needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the embedded message
     * @throws OpenStegoException
     */
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName)
        throws OpenStegoException
    {
        try
        {
            // Compress data, if requested
            if(config.isUseCompression())
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream zos = new GZIPOutputStream(bos);
                zos.write(msg);
                zos.finish();
                zos.close();
                bos.close();

                msg = bos.toByteArray();
            }

            // Encrypt data, if requested
            if(config.isUseEncryption())
            {
                OpenStegoCrypto crypto = new OpenStegoCrypto(config.getPassword());
                msg = crypto.encrypt(msg);
            }

            return plugin.embedData(msg, msgFileName, cover, coverFileName, stegoFileName);
        }
        catch(OpenStegoException osEx)
        {
            throw osEx;
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(ex);
        }
    }

    /**
     * Method to embed the message data into the cover data (alternate API)
     * @param msgFile File containing the message data to be embedded
     * @param coverFile Cover file into which data needs to be embedded
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the embedded message
     * @throws OpenStegoException
     */
    public byte[] embedData(File msgFile, File coverFile, String stegoFileName) throws OpenStegoException
    {
        InputStream is = null;
        String filename = null;

        try
        {
            // If no message file is provided, then read the data from stdin
            if(msgFile == null)
            {
                is = System.in;
            }
            else
            {
                is = new FileInputStream(msgFile);
                filename = msgFile.getName();
            }

            return embedData(CommonUtil.getStreamBytes(is), filename,
                             coverFile == null ? null : CommonUtil.getFileBytes(coverFile),
                             coverFile == null ? null : coverFile.getName(),
                             stegoFileName);
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Method to extract the message data from stego data
     * @param stegoData Stego data from which the message needs to be extracted
     * @param stegoFileName Name of the stego file
     * @return Extracted message (List's first element is filename and second element is the message as byte array)
     * @throws OpenStegoException
     */
    public List extractData(byte[] stegoData, String stegoFileName) throws OpenStegoException
    {
        byte[] msg = null;
        List output = new ArrayList();

        try
        {
            // TODO - Determine the plugin to use for extract

            // Add file name as first element of output list
            output.add(plugin.extractMsgFileName(stegoData, stegoFileName));
            msg = plugin.extractData(stegoData, stegoFileName);

            // Decrypt data, if required
            if(config.isUseEncryption())
            {
                OpenStegoCrypto crypto = new OpenStegoCrypto(config.getPassword());
                msg = crypto.decrypt(msg);
            }

            // Decompress data, if required
            if(config.isUseCompression())
            {
                try
                {
                    ByteArrayInputStream bis = new ByteArrayInputStream(msg);
                    GZIPInputStream zis = new GZIPInputStream(bis);
                    msg = CommonUtil.getStreamBytes(zis);
                    zis.close();
                    bis.close();
                }
                catch(IOException ioEx)
                {
                    throw new OpenStegoException(OpenStego.NAMESPACE, OpenStegoException.CORRUPT_DATA, ioEx);
                }
            }

            // Add message as second element of output list
            output.add(msg);
        }
        catch(OpenStegoException osEx)
        {
            throw osEx;
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(ex);
        }

        return output;
    }

    /**
     * Method to extract the message data from stego data (alternate API)
     * @param stegoFile Stego file from which message needs to be extracted
     * @return Extracted message (List's first element is filename and second element is the message as byte array)
     * @throws OpenStegoException
     */
    public List extractData(File stegoFile) throws OpenStegoException
    {
        return extractData(CommonUtil.getFileBytes(stegoFile), stegoFile.getName());
    }

    /**
     * Get method for configuration data
     * @return Configuration data
     */
    public OpenStegoConfig getConfig()
    {
        return config;
    }

    /**
     * Main method for calling openstego from command line.
     *
     * @param args Command line arguments
     * @throws OpenStegoException
     */
    public static void main(String[] args) throws OpenStegoException
    {
        int count = 0;
        int index = 0;
        String msgFileName = null;
        String coverFileName = null;
        String stegoFileName = null;
        String extractDir = null;
        String extractFileName = null;
        String command = null;
        String pluginName = null;
        List stegoData = null;
        OpenStego stego = null;
        CmdLineParser parser = null;
        CmdLineOptions options = null;
        CmdLineOption option = null;
        List optionList = null;
        OpenStegoPlugin plugin = null;

        try
        {
            // First parse of the command-line (without plugin specific options)
            parser = new CmdLineParser(getStdCmdLineOptions(null), args);
            if(!parser.isValid())
            {
                displayUsage();
                return;
            }

            // Load the stego plugins
            PluginManager.loadPlugins();

            if(parser.getNumOfOptions() == 0) // Start GUI
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch(Exception e)
                {
                }
                new OpenStegoUI().setVisible(true);
            }
            else
            {
                pluginName = parser.getParsedOptions().getOptionValue("-a");

                // Get the plugin object, and refresh the command-line parser data
                if(pluginName != null && !pluginName.equals(""))
                {
                    plugin = PluginManager.getPluginByName(pluginName);
                }
                else
                {
                    plugin = PluginManager.getDefaultPlugin();
                }

                // Second parse of the command-line (with plugin specific options)
                parser = new CmdLineParser(getStdCmdLineOptions(plugin), args);

                optionList = parser.getParsedOptionsAsList();
                options = parser.getParsedOptions();

                for(int i = 0; i < optionList.size(); i++)
                {
                    option = (CmdLineOption) optionList.get(i);
                    if(((i == 0) && (option.getType() != CmdLineOption.TYPE_COMMAND))
                        || ((i > 0) && (option.getType() == CmdLineOption.TYPE_COMMAND)))
                    {
                        displayUsage();
                        return;
                    }

                    if(i == 0)
                    {
                        command = option.getName();
                    }
                }

                // Non-standard options are not allowed
                if(parser.getNonStdOptions().size() > 0)
                {
                    displayUsage();
                    return;
                }

                // Create main stego object
                stego = new OpenStego(plugin, plugin.createConfig(parser.getParsedOptions()));

                if(command.equals("embed"))
                {
                    msgFileName = options.getOptionValue("-mf");
                    coverFileName = options.getOptionValue("-cf");
                    stegoFileName = options.getOptionValue("-sf");

                    // Check if we need to prompt for password
                    if(stego.getConfig().isUseEncryption() && stego.getConfig().getPassword() == null)
                    {
                        stego.getConfig().setPassword(PasswordInput.readPassword(
                                labelUtil.getString("cmd.msg.enterPassword") + " "));
                    }

                    CommonUtil.writeFile(stego.embedData(
                                (msgFileName == null || msgFileName.equals("-")) ? null : new File(msgFileName),
                                (coverFileName == null || coverFileName.equals("-")) ? null : new File(coverFileName),
                                (stegoFileName == null || stegoFileName.equals("-")) ? null : stegoFileName),
                            (stegoFileName == null || stegoFileName.equals("-")) ? null : stegoFileName);
                }
                else if(command.equals("extract"))
                {
                    stegoFileName = options.getOptionValue("-sf");
                    extractDir = options.getOptionValue("-xd");

                    if(stegoFileName == null)
                    {
                        displayUsage();
                        return;
                    }

                    try
                    {
                        stegoData = stego.extractData(new File(stegoFileName));
                    }
                    catch(OpenStegoException osEx)
                    {
                        if(osEx.getErrorCode() == OpenStegoException.INVALID_PASSWORD)
                        {
                            if(stego.getConfig().getPassword() == null)
                            {
                                stego.getConfig().setPassword(PasswordInput.readPassword(
                                        labelUtil.getString("cmd.msg.enterPassword") + " "));

                                try
                                {
                                    stegoData = stego.extractData(new File(stegoFileName));
                                }
                                catch(OpenStegoException inEx)
                                {
                                    if(inEx.getErrorCode() == OpenStegoException.INVALID_PASSWORD)
                                    {
                                        System.err.println(inEx.getMessage());
                                        return;
                                    }
                                    else
                                    {
                                        throw inEx;
                                    }
                                }
                            }
                            else
                            {
                                System.err.println(osEx.getMessage());
                                return;
                            }
                        }
                        else
                        {
                            throw osEx;
                        }
                    }
                    extractFileName = options.getOptionValue("-xf");
                    if(extractFileName == null)
                    {
                        extractFileName = (String) stegoData.get(0);
                        if(extractFileName == null || extractFileName.equals(""))
                        {
                            extractFileName = "untitled";
                        }
                    }
                    if(extractDir != null)
                    {
                        extractFileName = extractDir + File.separator + extractFileName;
                    }

                    CommonUtil.writeFile((byte[]) stegoData.get(1), extractFileName);
                    System.out.println(labelUtil.getString("cmd.msg.fileExtracted", new Object[] { extractFileName }));
                }
                else if(command.equals("readformats"))
                {
                    List formats = plugin.getReadableFileExtensions();
                    for(int i = 0; i < formats.size(); i++)
                    {
                        System.out.println(formats.get(i));
                    }
                }
                else if(command.equals("writeformats"))
                {
                    List formats = plugin.getWritableFileExtensions();
                    for(int i = 0; i < formats.size(); i++)
                    {
                        System.out.println(formats.get(i));
                    }
                }
                else if(command.equals("algorithms"))
                {
                    List plugins = PluginManager.getPlugins();
                    for(int i = 0; i < plugins.size(); i++)
                    {
                        plugin = (OpenStegoPlugin) plugins.get(i);
                        System.out.println(plugin.getName() + " - " + plugin.getDescription());
                    }
                }
                else if(command.equals("help"))
                {
                    if(plugin == null)
                    {
                        displayUsage();
                        return;
                    }
                    else // Show plugin-specific help
                    {
                        System.err.println(plugin.getUsage());
                    }
                }
                else
                {
                    displayUsage();
                    return;
                }
            }
        }
        catch(OpenStegoException osEx)
        {
            throw osEx;
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(ex);
        }
    }

    /**
     * Method to display usage for OpenStego
     */
    private static void displayUsage()
    {
        OpenStegoConfig defaultConfig = new OpenStegoConfig();
        System.err.print(labelUtil.getString("versionString"));
        System.err.println(labelUtil.getString("cmd.usage", new Object[] { File.separator,
                                            PluginManager.getDefaultPlugin().getName() }));
    }

    /**
     * Method to generate the standard list of command-line options
     * @param plugin Stego plugin for plugin-specific command-line options
     * @return Standard list of command-line options
     * @throws OpenStegoException
     */
    private static CmdLineOptions getStdCmdLineOptions(OpenStegoPlugin plugin) throws OpenStegoException
    {
        CmdLineOptions options = new CmdLineOptions();

        // Commands
        options.add("embed", "--embed", CmdLineOption.TYPE_COMMAND, false);
        options.add("extract", "--extract", CmdLineOption.TYPE_COMMAND, false);
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

        // Command options
        options.add("-c", "--compress", CmdLineOption.TYPE_OPTION, false);
        options.add("-C", "--nocompress", CmdLineOption.TYPE_OPTION, false);
        options.add("-e", "--encrypt", CmdLineOption.TYPE_OPTION, false);
        options.add("-E", "--noencrypt", CmdLineOption.TYPE_OPTION, false);
        options.add("-p", "--password", CmdLineOption.TYPE_OPTION, true);

        // Plugin-specific options
        if(plugin != null)
        {
            plugin.populateStdCmdLineOptions(options);
        }

        return options;
    }
}