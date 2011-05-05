/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2011 Samir Vaidya
 */
package net.sourceforge.openstego.ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.OpenStegoPlugin;
import net.sourceforge.openstego.util.CommonUtil;
import net.sourceforge.openstego.util.LabelUtil;
import net.sourceforge.openstego.util.PluginManager;

/**
 * This is the main class for OpenStego GUI and it implements the action and window listeners.
 */
public class OpenStegoUI extends OpenStegoFrame
{
    private static final long serialVersionUID = -7485426167074985636L;

    private static final int READ_EXTENSIONS = 1;
    private static final int WRITE_EXTENSIONS = 2;
    private static final String SIG_FILE_EXTENSION = ".sig";

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    /**
     * Static variable to holds path to last selected folder
     */
    private static String lastFolder = null;

    /**
     * Default constructor
     * 
     * @throws OpenStegoException
     */
    public OpenStegoUI() throws OpenStegoException
    {
        super();
        resetGUI();

        URL iconURL = getClass().getResource("/image/OpenStegoIcon.png");
        if(iconURL != null)
        {
            this.setIconImage(new ImageIcon(iconURL).getImage());
        }

        Listener listener = new Listener();
        addWindowListener(listener);

        getEmbedButton().addActionListener(listener);
        getExtractButton().addActionListener(listener);
        getGenSigButton().addActionListener(listener);
        getSignWmButton().addActionListener(listener);
        getVerifyWmButton().addActionListener(listener);

        getEmbedPanel().getMsgFileButton().addActionListener(listener);
        getEmbedPanel().getCoverFileButton().addActionListener(listener);
        getEmbedPanel().getStegoFileButton().addActionListener(listener);
        getEmbedPanel().getRunEmbedButton().addActionListener(listener);

        getExtractPanel().getInputStegoFileButton().addActionListener(listener);
        getExtractPanel().getOutputFolderButton().addActionListener(listener);
        getExtractPanel().getRunExtractButton().addActionListener(listener);

        getGenSigPanel().getSignatureFileButton().addActionListener(listener);
        getGenSigPanel().getRunGenSigButton().addActionListener(listener);

        getEmbedWmPanel().getFileForWmButton().addActionListener(listener);
        getEmbedWmPanel().getSignatureFileButton().addActionListener(listener);
        getEmbedWmPanel().getOutputWmFileButton().addActionListener(listener);
        getEmbedWmPanel().getRunEmbedWmButton().addActionListener(listener);

        getVerifyWmPanel().getInputFileButton().addActionListener(listener);
        getVerifyWmPanel().getSignatureFileButton().addActionListener(listener);
        getVerifyWmPanel().getRunVerifyWmButton().addActionListener(listener);

        // "Esc" key handling
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            private static final long serialVersionUID = -4890560722044735566L;

            public void actionPerformed(ActionEvent ev)
            {
                close();
            }
        };

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - (getWidth() / 2), screenSize.height / 2 - (getHeight() / 2));
    }

    /**
     * Method to reset the GUI components from scratch
     * 
     * @throws OpenStegoException
     */
    private void resetGUI() throws OpenStegoException
    {
        pack();
        setResizable(false);

        getEmbedPanel().getMsgFileTextField().setText("");
        getEmbedPanel().getCoverFileTextField().setText("");
        getEmbedPanel().getStegoFileTextField().setText("");
        getEmbedPanel().getPasswordTextField().setText("");
        getEmbedPanel().getConfPasswordTextField().setText("");
        getEmbedPanel().getMsgFileTextField().requestFocus();
    }

    /**
     * This method embeds the selected data file into selected file
     * 
     * @throws OpenStegoException
     */
    private void embedData() throws OpenStegoException
    {
        OpenStego openStego = null;
        byte[] stegoData = null;
        String dataFileName = null;
        String outputFileName = null;
        String password = null;
        String confPassword = null;
        List<File> coverFileList = null;
        File cvrFile = null;
        File outputFile = null;
        int processCount = 0;
        int skipCount = 0;
        OpenStegoConfig config = null;
        OpenStegoPlugin embedPlugin = null;

        embedPlugin = getDefaultPlugin(OpenStegoPlugin.Purpose.DATA_HIDING);
        config = embedPlugin.createConfig();

        dataFileName = getEmbedPanel().getMsgFileTextField().getText();
        coverFileList = CommonUtil.parseFileList(getEmbedPanel().getCoverFileTextField().getText(), ";");
        outputFileName = getEmbedPanel().getStegoFileTextField().getText();
        outputFile = new File(outputFileName);
        password = new String(getEmbedPanel().getPasswordTextField().getPassword());
        confPassword = new String(getEmbedPanel().getConfPasswordTextField().getPassword());

        // START: Input Validations
        if(!checkMandatory(getEmbedPanel().getMsgFileTextField(), labelUtil.getString("gui.label.dhEmbed.msgFile")))
        {
            return;
        }
        if(!checkMandatory(getEmbedPanel().getCoverFileTextField(), labelUtil.getString("gui.label.dhEmbed.coverFile")))
        {
            return;
        }
        if(!checkMandatory(getEmbedPanel().getStegoFileTextField(), labelUtil.getString("gui.label.dhEmbed.stegoFile")))
        {
            return;
        }

        // Check if single or multiple cover files are selected
        if(coverFileList.size() <= 1)
        {
            // If user has provided a wildcard for cover file name, and parser returns zero length, then it means that
            // there are no matching files with that wildcard
            if(coverFileList.size() == 0 && !getEmbedPanel().getCoverFileTextField().getText().trim().equals(""))
            {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.dhEmbed.coverFileNotFound",
                    getEmbedPanel().getCoverFileTextField().getText()), labelUtil.getString("gui.msg.title.err"),
                    JOptionPane.ERROR_MESSAGE);
                getEmbedPanel().getCoverFileTextField().requestFocus();
                return;
            }
            // If single cover file is given, then output stego file must not be a directory
            if(outputFile.isDirectory())
            {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.dhEmbed.outputShouldBeFile"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                getEmbedPanel().getStegoFileTextField().requestFocus();
                return;
            }
        }
        else
        {
            // If multiple cover files are given, then output stego file must be a directory
            if(!outputFile.isDirectory())
            {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.dhEmbed.outputShouldBeDir"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                getEmbedPanel().getStegoFileTextField().requestFocus();
                return;
            }
        }

        if(!password.equals(confPassword))
        {
            JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.dhEmbed.passwordMismatch"),
                labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
            getEmbedPanel().getConfPasswordTextField().requestFocus();
            return;
        }
        // END: Input Validations

        config.setUseCompression(true);
        config.setUseEncryption(true);
        config.setPassword(password);
        openStego = new OpenStego(embedPlugin, config);
        if(coverFileList.size() <= 1)
        {
            if(coverFileList.size() == 1)
            {
                cvrFile = coverFileList.get(0);
            }

            if(outputFile.exists())
            {
                if(JOptionPane.showConfirmDialog(this, labelUtil.getString("gui.msg.warn.fileExists", outputFileName),
                    labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
                {
                    return;
                }
            }

            processCount++;
            stegoData = openStego.embedData(dataFileName == null || dataFileName.equals("") ? null : new File(
                    dataFileName), cvrFile, outputFileName);
            CommonUtil.writeFile(stegoData, outputFile);
        }
        else
        {
            for(int i = 0; i < coverFileList.size(); i++)
            {
                cvrFile = coverFileList.get(i);

                // Use cover file name as the output file name. Change the folder to given output folder
                outputFileName = outputFile.getPath() + File.separator + cvrFile.getName();

                // If the output filename extension is not supported for writing, then change the same
                if(!embedPlugin.getWritableFileExtensions().contains(
                    outputFileName.substring(outputFileName.lastIndexOf('.') + 1).toLowerCase()))
                {
                    outputFileName = outputFileName + "." + embedPlugin.getWritableFileExtensions().get(0);
                }

                if((new File(outputFileName)).exists())
                {
                    if(JOptionPane.showConfirmDialog(this,
                        labelUtil.getString("gui.msg.warn.fileExists", outputFileName),
                        labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
                    {
                        skipCount++;
                        continue;
                    }
                }

                processCount++;
                stegoData = openStego.embedData(dataFileName == null || dataFileName.equals("") ? null : new File(
                        dataFileName), cvrFile, outputFileName);
                CommonUtil.writeFile(stegoData, outputFileName);
            }
        }

        JOptionPane.showMessageDialog(this,
            labelUtil.getString("gui.msg.success.dhEmbed", new Integer(processCount), new Integer(skipCount)),
            labelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

        // Reset configuration
        resetGUI();
    }

    /**
     * This method extracts data from the selected file
     * 
     * @throws OpenStegoException
     */
    private void extractData() throws OpenStegoException
    {
        OpenStego openStego = null;
        OpenStegoConfig config = null;
        OpenStegoPlugin extractPlugin = null;
        String stegoFileName = null;
        String outputFolder = null;
        String outputFileName = null;
        File file = null;
        List<?> stegoOutput = null;

        extractPlugin = getDefaultPlugin(OpenStegoPlugin.Purpose.DATA_HIDING);
        config = extractPlugin.createConfig();

        openStego = new OpenStego(extractPlugin, config);
        config = openStego.getConfig();
        config.setPassword(new String(getExtractPanel().getExtractPwdTextField().getPassword()));
        stegoFileName = getExtractPanel().getInputStegoFileTextField().getText();
        outputFolder = getExtractPanel().getOutputFolderTextField().getText();

        // START: Input Validations
        if(!checkMandatory(getExtractPanel().getInputStegoFileTextField(),
            labelUtil.getString("gui.label.dhExtract.stegoFile")))
        {
            return;
        }
        if(!checkMandatory(getExtractPanel().getOutputFolderTextField(),
            labelUtil.getString("gui.label.dhExtract.outputDir")))
        {
            return;
        }
        // END: Input Validations

        stegoOutput = openStego.extractData(new File(stegoFileName));
        outputFileName = (String) stegoOutput.get(0);
        file = new File(outputFolder + File.separator + outputFileName);
        if(file.exists())
        {
            if(JOptionPane.showConfirmDialog(this, labelUtil.getString("gui.msg.warn.fileExists", outputFileName),
                labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
            {
                return;
            }
        }

        CommonUtil.writeFile((byte[]) stegoOutput.get(1), outputFolder + File.separator + outputFileName);
        JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.success.dhExtract", outputFileName),
            labelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

        // Reset GUI
        getExtractPanel().getInputStegoFileTextField().setText("");
        getExtractPanel().getOutputFolderTextField().setText("");
        getExtractPanel().getExtractPwdTextField().setText("");
        getExtractPanel().getInputStegoFileTextField().requestFocus();
    }

    /**
     * This method generates signature for watermarking
     * 
     * @throws OpenStegoException
     */
    private void generateSignature() throws OpenStegoException
    {
        OpenStego openStego = null;
        byte[] sigData = null;
        String inputKey = null;
        String sigFileName = null;
        File sigFile = null;
        OpenStegoConfig config = null;
        OpenStegoPlugin plugin = null;

        plugin = getDefaultPlugin(OpenStegoPlugin.Purpose.WATERMARKING);
        config = plugin.createConfig();

        inputKey = getGenSigPanel().getInputKeyTextField().getText();
        sigFileName = getGenSigPanel().getSignatureFileTextField().getText();
        sigFile = new File(sigFileName);

        // START: Input Validations
        if(!checkMandatory(getGenSigPanel().getInputKeyTextField(), labelUtil.getString("gui.label.wmGenSig.inputKey")))
        {
            return;
        }
        if(!checkMandatory(getGenSigPanel().getSignatureFileTextField(),
            labelUtil.getString("gui.label.wmGenSig.sigFile")))
        {
            return;
        }
        // END: Input Validations

        config.setPassword(inputKey);
        openStego = new OpenStego(plugin, config);
        if(sigFile.exists())
        {
            if(JOptionPane.showConfirmDialog(this, labelUtil.getString("gui.msg.warn.fileExists", sigFileName),
                labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
            {
                return;
            }
        }

        sigData = openStego.generateSignature();
        CommonUtil.writeFile(sigData, sigFile);

        JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.success.wmGenSig"),
            labelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

        // Reset GUI
        getGenSigPanel().getInputKeyTextField().setText("");
        getGenSigPanel().getSignatureFileTextField().setText("");
        getGenSigPanel().getInputKeyTextField().requestFocus();
    }

    /**
     * This method embeds the watermark into selected file
     * 
     * @throws OpenStegoException
     */
    private void embedMark() throws OpenStegoException
    {
        OpenStego openStego = null;
        byte[] wmData = null;
        String sigFileName = null;
        String outputFileName = null;
        List<File> inputFileList = null;
        File inputFile = null;
        File outputFile = null;
        int processCount = 0;
        int skipCount = 0;
        OpenStegoConfig config = null;
        OpenStegoPlugin plugin = null;

        plugin = getDefaultPlugin(OpenStegoPlugin.Purpose.WATERMARKING);
        config = plugin.createConfig();

        inputFileList = CommonUtil.parseFileList(getEmbedWmPanel().getFileForWmTextField().getText(), ";");
        sigFileName = getEmbedWmPanel().getSignatureFileTextField().getText();
        outputFileName = getEmbedWmPanel().getOutputWmFileTextField().getText();
        outputFile = new File(outputFileName);

        // START: Input Validations
        if(!checkMandatory(getEmbedWmPanel().getFileForWmTextField(),
            labelUtil.getString("gui.label.wmEmbed.fileForWm")))
        {
            return;
        }
        if(!checkMandatory(getEmbedWmPanel().getSignatureFileTextField(),
            labelUtil.getString("gui.label.wmEmbed.sigFile")))
        {
            return;
        }
        if(!checkMandatory(getEmbedWmPanel().getOutputWmFileTextField(),
            labelUtil.getString("gui.label.wmEmbed.outputWmFile")))
        {
            return;
        }

        // Check if single or multiple input files are selected
        if(inputFileList.size() <= 1)
        {
            // If user has provided a wildcard for file name, and parser returns zero length, then it means that
            // there are no matching files with that wildcard
            if(inputFileList.size() == 0 && !getEmbedWmPanel().getFileForWmTextField().getText().trim().equals(""))
            {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.wmEmbed.inputFileNotFound",
                    getEmbedWmPanel().getFileForWmTextField().getText()), labelUtil.getString("gui.msg.title.err"),
                    JOptionPane.ERROR_MESSAGE);
                getEmbedWmPanel().getFileForWmTextField().requestFocus();
                return;
            }
            // If single input file is given, then output file must not be a directory
            if(outputFile.isDirectory())
            {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.wmEmbed.outputShouldBeFile"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                getEmbedWmPanel().getOutputWmFileTextField().requestFocus();
                return;
            }
        }
        else
        {
            // If multiple input files are given, then output file must be a directory
            if(!outputFile.isDirectory())
            {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.wmEmbed.outputShouldBeDir"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                getEmbedWmPanel().getOutputWmFileTextField().requestFocus();
                return;
            }
        }
        // END: Input Validations

        openStego = new OpenStego(plugin, config);
        if(inputFileList.size() <= 1)
        {
            if(inputFileList.size() == 1)
            {
                inputFile = inputFileList.get(0);
            }

            if(outputFile.exists())
            {
                if(JOptionPane.showConfirmDialog(this, labelUtil.getString("gui.msg.warn.fileExists", outputFileName),
                    labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
                {
                    return;
                }
            }

            processCount++;
            wmData = openStego.embedMark(sigFileName == null || sigFileName.equals("") ? null : new File(sigFileName),
                inputFile, outputFileName);
            CommonUtil.writeFile(wmData, outputFile);
        }
        else
        {
            for(int i = 0; i < inputFileList.size(); i++)
            {
                inputFile = inputFileList.get(i);

                // Use input file name as the output file name. Change the folder to given output folder
                outputFileName = outputFile.getPath() + File.separator + inputFile.getName();

                // If the output filename extension is not supported for writing, then change the same
                if(!plugin.getWritableFileExtensions().contains(
                    outputFileName.substring(outputFileName.lastIndexOf('.') + 1).toLowerCase()))
                {
                    outputFileName = outputFileName + "." + plugin.getWritableFileExtensions().get(0);
                }

                if((new File(outputFileName)).exists())
                {
                    if(JOptionPane.showConfirmDialog(this,
                        labelUtil.getString("gui.msg.warn.fileExists", outputFileName),
                        labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
                    {
                        skipCount++;
                        continue;
                    }
                }

                processCount++;
                wmData = openStego.embedMark(sigFileName == null || sigFileName.equals("") ? null : new File(
                        sigFileName), inputFile, outputFileName);
                CommonUtil.writeFile(wmData, outputFileName);
            }
        }

        JOptionPane.showMessageDialog(this,
            labelUtil.getString("gui.msg.success.wmEmbed", new Integer(processCount), new Integer(skipCount)),
            labelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

        // Reset GUI
        getEmbedWmPanel().getFileForWmTextField().setText("");
        getEmbedWmPanel().getSignatureFileTextField().setText("");
        getEmbedWmPanel().getOutputWmFileTextField().setText("");
        getEmbedWmPanel().getFileForWmTextField().requestFocus();
    }

    /**
     * This method checks for watermark in the selected file
     * 
     * @throws OpenStegoException
     */
    private void checkMark() throws OpenStegoException
    {
        OpenStego openStego = null;
        OpenStegoConfig config = null;
        OpenStegoPlugin plugin = null;
        List<File> inputFileList = null;
        File sigFile = null;
        StringBuffer resultMsg = new StringBuffer();
        double correlation = 0.0;

        inputFileList = CommonUtil.parseFileList(getVerifyWmPanel().getInputFileTextField().getText(), ";");
        plugin = getDefaultPlugin(OpenStegoPlugin.Purpose.WATERMARKING);
        config = plugin.createConfig();

        // START: Input Validations
        if(!checkMandatory(getVerifyWmPanel().getInputFileTextField(),
            labelUtil.getString("gui.label.wmVerify.inputWmFile")))
        {
            return;
        }
        if(!checkMandatory(getVerifyWmPanel().getSignatureFileTextField(),
            labelUtil.getString("gui.label.wmVerify.sigFile")))
        {
            return;
        }

        // If user has provided a wildcard for file name, and parser returns zero length, then it means that
        // there are no matching files with that wildcard
        if(inputFileList.size() == 0 && !getVerifyWmPanel().getInputFileTextField().getText().trim().equals(""))
        {
            JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.wmVerify.inputFileNotFound",
                getVerifyWmPanel().getInputFileTextField().getText()), labelUtil.getString("gui.msg.title.err"),
                JOptionPane.ERROR_MESSAGE);
            getVerifyWmPanel().getInputFileTextField().requestFocus();
            return;
        }
        // END: Input Validations

        openStego = new OpenStego(plugin, config);
        sigFile = new File(getVerifyWmPanel().getSignatureFileTextField().getText());

        resultMsg.append("<html><p>").append(labelUtil.getString("gui.msg.success.wmVerify")).append("</p><br/>");
        resultMsg.append("<table cellspacing=1 cellpadding=1 style='background-color:#444444' width='80%'>");
        resultMsg.append("<tr style='color:white'><th align=left width=99%>");
        resultMsg.append(labelUtil.getString("gui.label.wmVerify.result.header.fileName"));
        resultMsg.append("</th><th align=left nowrap>");
        resultMsg.append(labelUtil.getString("gui.label.wmVerify.result.header.strength"));
        resultMsg.append("</th></tr>");
        for(File inputFile : inputFileList)
        {
            correlation = openStego.checkMark(inputFile, sigFile);
            resultMsg.append("<tr style='background-color:white'><td>").append(inputFile.getName());
            resultMsg.append("</td><td nowrap style='color:");
            if(correlation > 0.5)
            {
                resultMsg.append("green'>\u25cf High");
            }
            else if(correlation > 0.3)
            {
                resultMsg.append("#FFBF00'>\u25cf Med");
            }
            else
            {
                resultMsg.append("red'>\u25cf Low");
            }

            resultMsg.append("</td></tr>");
        }
        resultMsg.append("</table></html>");

        JOptionPane.showMessageDialog(this, resultMsg.toString(), labelUtil.getString("gui.msg.title.results"),
            JOptionPane.INFORMATION_MESSAGE);

        // Reset GUI
        getVerifyWmPanel().getInputFileTextField().setText("");
        getVerifyWmPanel().getSignatureFileTextField().setText("");
        getVerifyWmPanel().getInputFileTextField().requestFocus();
    }

    /**
     * This method shows the file chooser and updates the text field based on the selection
     * 
     * @throws OpenStegoException
     */
    private void selectFile(String action) throws OpenStegoException
    {
        FileBrowser browser = new FileBrowser();
        String fileName = null;
        String title = null;
        String filterDesc = null;
        List<String> allowedExts = null;
        int allowFileDir = FileBrowser.ALLOW_FILE;
        boolean multiSelect = false;
        int coverFileListSize = 0;
        int wmInputFileListSize = 0;
        JTextField textField = null;
        OpenStegoPlugin plugin;

        plugin = action.startsWith("BROWSE_DH_") ? getDefaultPlugin(OpenStegoPlugin.Purpose.DATA_HIDING)
                : getDefaultPlugin(OpenStegoPlugin.Purpose.WATERMARKING);

        coverFileListSize = CommonUtil.parseFileList(getEmbedPanel().getCoverFileTextField().getText(), ";").size();
        wmInputFileListSize = CommonUtil.parseFileList(getEmbedWmPanel().getFileForWmTextField().getText(), ";").size();

        if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EMB_MSGFILE))
        {
            title = labelUtil.getString("gui.filer.title.dhEmbed.msgFile");
            textField = getEmbedPanel().getMsgFileTextField();
        }
        else if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EMB_CVRFILE))
        {
            title = labelUtil.getString("gui.filer.title.dhEmbed.coverFile");
            filterDesc = labelUtil.getString("gui.filer.filter.coverFiles",
                getExtensionsString(plugin, READ_EXTENSIONS));
            allowedExts = getExtensionsList(plugin, READ_EXTENSIONS);
            textField = getEmbedPanel().getCoverFileTextField();
            multiSelect = true;
        }
        else if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EMB_STGFILE))
        {
            title = labelUtil.getString("gui.filer.title.dhEmbed.stegoFile");
            if(coverFileListSize > 1)
            {
                allowFileDir = FileBrowser.ALLOW_DIRECTORY;
            }
            else
            {
                filterDesc = labelUtil.getString("gui.filer.filter.stegoFiles",
                    getExtensionsString(plugin, WRITE_EXTENSIONS));
                allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
            }
            textField = getEmbedPanel().getStegoFileTextField();
        }
        else if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EXT_STGFILE))
        {
            title = labelUtil.getString("gui.filer.title.dhExtract.stegoFile");
            filterDesc = labelUtil.getString("gui.filer.filter.stegoFiles",
                getExtensionsString(plugin, WRITE_EXTENSIONS));
            allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
            textField = getExtractPanel().getInputStegoFileTextField();
        }
        else if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EXT_OUTDIR))
        {
            title = labelUtil.getString("gui.filer.title.dhExtract.outputDir");
            allowFileDir = FileBrowser.ALLOW_DIRECTORY;
            textField = getExtractPanel().getOutputFolderTextField();
        }
        else if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_GSG_SIGFILE))
        {
            title = labelUtil.getString("gui.filer.title.wmGenSig.sigFile");
            filterDesc = labelUtil.getString("gui.filer.filter.sigFiles", "*" + SIG_FILE_EXTENSION);
            allowedExts = Arrays.asList(new String[] { SIG_FILE_EXTENSION });
            textField = getGenSigPanel().getSignatureFileTextField();
        }
        else if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_EMB_INPFILE))
        {
            title = labelUtil.getString("gui.filer.title.wmEmbed.fileForWm");
            filterDesc = labelUtil.getString("gui.filer.filter.filesForWm",
                getExtensionsString(plugin, READ_EXTENSIONS));
            allowedExts = getExtensionsList(plugin, READ_EXTENSIONS);
            textField = getEmbedWmPanel().getFileForWmTextField();
            multiSelect = true;
        }
        else if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_EMB_SIGFILE))
        {
            title = labelUtil.getString("gui.filer.title.wmEmbed.sigFile");
            filterDesc = labelUtil.getString("gui.filer.filter.sigFiles", "*" + SIG_FILE_EXTENSION);
            allowedExts = Arrays.asList(new String[] { SIG_FILE_EXTENSION });
            textField = getEmbedWmPanel().getSignatureFileTextField();
        }
        else if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_EMB_OUTFILE))
        {
            title = labelUtil.getString("gui.filer.title.wmEmbed.outputWmFile");
            if(wmInputFileListSize > 1)
            {
                allowFileDir = FileBrowser.ALLOW_DIRECTORY;
            }
            else
            {
                filterDesc = labelUtil.getString("gui.filer.filter.wmFiles",
                    getExtensionsString(plugin, WRITE_EXTENSIONS));
                allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
            }
            textField = getEmbedWmPanel().getOutputWmFileTextField();
        }
        else if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_VER_INPFILE))
        {
            title = labelUtil.getString("gui.filer.title.wmExtract.inputWmFile");
            filterDesc = labelUtil.getString("gui.filer.filter.wmFiles", getExtensionsString(plugin, WRITE_EXTENSIONS));
            allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
            textField = getVerifyWmPanel().getInputFileTextField();
            multiSelect = true;
        }
        else if(action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_VER_SIGFILE))
        {
            title = labelUtil.getString("gui.filer.title.wmExtract.sigFile");
            filterDesc = labelUtil.getString("gui.filer.filter.sigFiles", "*" + SIG_FILE_EXTENSION);
            allowedExts = Arrays.asList(new String[] { SIG_FILE_EXTENSION });
            textField = getVerifyWmPanel().getSignatureFileTextField();
        }

        fileName = browser.getFileName(title, filterDesc, allowedExts, allowFileDir, multiSelect);
        if(fileName != null)
        {
            // Check for valid extension for output file
            if((action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EMB_STGFILE) && (coverFileListSize <= 1))
                    || (action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_EMB_OUTFILE) && (wmInputFileListSize <= 1)))
            {
                if(!plugin.getWritableFileExtensions().contains(
                    fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()))
                {
                    fileName = fileName + "." + plugin.getWritableFileExtensions().get(0);
                }
            }
            textField.setText(fileName);
        }
    }

    /**
     * This method exits the application.
     */
    private void close()
    {
        System.exit(0);
    }

    /**
     * This method handles all the exceptions in the GUI
     * 
     * @param ex Exception to be handled
     */
    private void handleException(Throwable ex)
    {
        String msg = null;

        if(ex instanceof OutOfMemoryError)
        {
            msg = labelUtil.getString("err.memory.full");
        }
        else
        {
            msg = ex.getMessage();
        }

        if((msg == null) || (msg.trim().equals("")))
        {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            msg = writer.toString();
        }

        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, msg, labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Method to check whether value is provided or not; and display message box in case it is not provided
     * 
     * @param textField Text field to be checked for value
     * @param fieldName Name of the field
     * @return Flag whether value exists or not
     */
    private boolean checkMandatory(JTextField textField, String fieldName)
    {
        if(!textField.isEnabled())
        {
            return true;
        }

        String value = textField.getText();
        if(value == null || value.trim().equals(""))
        {
            JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.mandatoryCheck", fieldName),
                labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);

            textField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Method to get the list of extensions as a single string
     * 
     * @param plugin Plugin
     * @param flag Flag to indicate whether readable (READ_EXTENSIONS) or writeable (WRITE_EXTENSIONS) extensions are
     *        required
     * @return List of extensions (as string)
     * @throws OpenStegoException
     */
    private String getExtensionsString(OpenStegoPlugin plugin, int flag) throws OpenStegoException
    {
        List<String> list = null;
        StringBuffer output = new StringBuffer();

        list = getExtensionsList(plugin, flag);
        for(int i = 0; i < list.size(); i++)
        {
            if(i > 0)
            {
                output.append(", ");
            }
            output.append("*").append(list.get(i));
        }
        return output.toString();
    }

    /**
     * Method to get the list of extensions as a list
     * 
     * @param plugin Plugin
     * @param flag Flag to indicate whether readable (READ_EXTENSIONS) or writeable (WRITE_EXTENSIONS) extensions are
     *        required
     * @return List of extensions (as list)
     * @throws OpenStegoException
     */
    private List<String> getExtensionsList(OpenStegoPlugin plugin, int flag) throws OpenStegoException
    {
        List<String> list = null;
        List<String> output = new ArrayList<String>();

        if(flag == READ_EXTENSIONS)
        {
            list = plugin.getReadableFileExtensions();
        }
        else if(flag == WRITE_EXTENSIONS)
        {
            list = plugin.getWritableFileExtensions();
        }

        for(int i = 0; i < list.size(); i++)
        {
            output.add("." + list.get(i));
        }
        return output;
    }

    private OpenStegoPlugin getDefaultPlugin(OpenStegoPlugin.Purpose purpose)
    {
        // TODO
        if(purpose == OpenStegoPlugin.Purpose.DATA_HIDING)
        {
            return PluginManager.getPluginByName("RandomLSB");
        }
        else
        {
            return PluginManager.getPluginByName("DWTDugad");
        }
    }

    /**
     * Common listener class to handlw action and window events
     */
    class Listener implements ActionListener, WindowListener
    {
        public void actionPerformed(ActionEvent ev)
        {
            try
            {
                String action = ev.getActionCommand();

                if(action.startsWith("BROWSE_"))
                {
                    selectFile(action);
                }
                else if(action.startsWith("SWITCH_"))
                {
                    getMainPanel().removeAll();
                    if(action.equals(OpenStegoFrame.ActionCommands.SWITCH_DH_EMBED))
                    {
                        getMainPanel().add(getEmbedPanel());
                    }
                    else if(action.equals(OpenStegoFrame.ActionCommands.SWITCH_DH_EXTRACT))
                    {
                        getMainPanel().add(getExtractPanel());
                    }
                    else if(action.equals(OpenStegoFrame.ActionCommands.SWITCH_WM_GENSIG))
                    {
                        getMainPanel().add(getGenSigPanel());
                    }
                    else if(action.equals(OpenStegoFrame.ActionCommands.SWITCH_WM_EMBED))
                    {
                        getMainPanel().add(getEmbedWmPanel());
                    }
                    else if(action.equals(OpenStegoFrame.ActionCommands.SWITCH_WM_VERIFY))
                    {
                        getMainPanel().add(getVerifyWmPanel());
                    }
                    getMainPanel().revalidate();
                    getMainPanel().repaint();
                }
                else if(action.startsWith("RUN_"))
                {
                    try
                    {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        if(action.equals(OpenStegoFrame.ActionCommands.RUN_DH_EMBED))
                        {
                            embedData();
                        }
                        else if(action.equals(OpenStegoFrame.ActionCommands.RUN_DH_EXTRACT))
                        {
                            extractData();
                        }
                        else if(action.equals(OpenStegoFrame.ActionCommands.RUN_WM_GENSIG))
                        {
                            generateSignature();
                        }
                        else if(action.equals(OpenStegoFrame.ActionCommands.RUN_WM_EMBED))
                        {
                            embedMark();
                        }
                        else if(action.equals(OpenStegoFrame.ActionCommands.RUN_WM_VERIFY))
                        {
                            checkMark();
                        }
                    }
                    finally
                    {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
            catch(Throwable ex)
            {
                handleException(ex);
            }
        }

        public void windowClosing(WindowEvent ev)
        {
            close();
        }

        public void windowActivated(WindowEvent ev)
        {
        }

        public void windowClosed(WindowEvent ev)
        {
        }

        public void windowDeactivated(WindowEvent ev)
        {
        }

        public void windowDeiconified(WindowEvent ev)
        {
        }

        public void windowIconified(WindowEvent ev)
        {
        }

        public void windowOpened(WindowEvent ev)
        {
        }
    }

    /**
     * Class to implement File Chooser
     */
    class FileBrowser
    {
        public static final int ALLOW_FILE = 1;
        public static final int ALLOW_DIRECTORY = 2;
        public static final int ALLOW_FILE_AND_DIR = 3;

        /**
         * Method to get the display file chooser and return the selected file name
         * 
         * @param dialogTitle Title for the file chooser dialog box
         * @param filterDesc Description to be displayed for the filter in file chooser
         * @param allowedExts Allowed file extensions for the filter
         * @param allowFileDir Type of objects allowed to be selected (FileBrowser.ALLOW_FILE,
         *        FileBrowser.ALLOW_DIRECTORY or FileBrowser.ALLOW_FILE_AND_DIR)
         * @param multiSelect Flag to indicate whether multiple file selection is allowed or not
         * @return Name of the selected file (null if no file was selected)
         */
        public String getFileName(String dialogTitle, String filterDesc, List<String> allowedExts, int allowFileDir,
                boolean multiSelect)
        {
            int retVal = 0;
            String fileName = null;
            File[] files = null;

            JFileChooser chooser = new JFileChooser(lastFolder);
            chooser.setMultiSelectionEnabled(multiSelect);
            switch(allowFileDir)
            {
                case ALLOW_FILE:
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    break;
                case ALLOW_DIRECTORY:
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    break;
                case ALLOW_FILE_AND_DIR:
                    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    break;
            }

            if(filterDesc != null)
            {
                chooser.setFileFilter(new FileBrowserFilter(filterDesc, allowedExts));
            }
            chooser.setDialogTitle(dialogTitle);
            retVal = chooser.showOpenDialog(null);

            if(retVal == JFileChooser.APPROVE_OPTION)
            {
                if(multiSelect)
                {
                    StringBuffer fileList = new StringBuffer();
                    files = chooser.getSelectedFiles();
                    for(int i = 0; i < files.length; i++)
                    {
                        if(i != 0)
                        {
                            fileList.append(";");
                        }
                        fileList.append(files[i].getPath());
                    }
                    fileName = fileList.toString();
                }
                else
                {
                    fileName = chooser.getSelectedFile().getPath();
                }
                lastFolder = chooser.getSelectedFile().getParent();
            }

            return fileName;
        }

        /**
         * Class to implement filter for file chooser
         */
        class FileBrowserFilter extends FileFilter
        {
            /**
             * Description of the filter
             */
            private String filterDesc = null;

            /**
             * List of allowed file extensions
             */
            private List<String> allowedExts = null;

            /**
             * Default constructor
             * 
             * @param filterDesc Description of the filter
             * @param allowedExts List of allowed file extensions
             */
            public FileBrowserFilter(String filterDesc, List<String> allowedExts)
            {
                this.filterDesc = filterDesc;
                this.allowedExts = allowedExts;
            }

            /**
             * Implementation of <code>accept</accept> method of <code>FileFilter</code> class
             * 
             * @param file File to check whether it is acceptable by this filter or not
             * @return Flag to indicate whether file is acceptable or not
             */
            public boolean accept(File file)
            {
                if(file != null)
                {
                    if(this.allowedExts == null || this.allowedExts.size() == 0 || file.isDirectory())
                    {
                        return true;
                    }

                    for(int i = 0; i < this.allowedExts.size(); i++)
                    {
                        if(file.getName().toLowerCase().endsWith(this.allowedExts.get(i).toString()))
                        {
                            return true;
                        }
                    }
                }

                return false;
            }

            /**
             * Implementation of <code>getDescription</accept> method of <code>FileFilter</code> class
             * 
             * @return Description of the filter
             */
            public String getDescription()
            {
                return this.filterDesc;
            }
        }
    }
}
