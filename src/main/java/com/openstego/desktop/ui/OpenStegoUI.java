/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */
package com.openstego.desktop.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.OpenStegoPlugin;
import com.openstego.desktop.util.CommonUtil;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.PluginManager;
import com.openstego.desktop.util.ui.WorkerTask;

/**
 * This is the main class for OpenStego GUI and it implements the action and window listeners.
 */
public class OpenStegoUI extends OpenStegoFrame {
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
    public OpenStegoUI() throws OpenStegoException {
        super();
        resetGUI();

        URL iconURL = getClass().getResource("/images/OpenStegoIcon.png");
        if (iconURL != null) {
            this.setIconImage(new ImageIcon(iconURL).getImage());
        }

        Listener listener = new Listener();
        addWindowListener(listener);

        getFileExitMenuItem().addActionListener(listener);
        getHelpAboutMenuItem().addActionListener(listener);

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

        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - (getWidth() / 2), screenSize.height / 2 - (getHeight() / 2));
    }

    /**
     * Method to reset the GUI components from scratch
     */
    protected void resetGUI() {
        pack();

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
    private void embedData() throws OpenStegoException {
        String outputFileName = null;
        String password = null;
        String confPassword = null;
        File outputFile = null;
        List<File> coverFileList = null;

        outputFileName = getEmbedPanel().getStegoFileTextField().getText();
        outputFile = new File(outputFileName);
        coverFileList = CommonUtil.parseFileList(getEmbedPanel().getCoverFileTextField().getText(), ";");
        password = new String(getEmbedPanel().getPasswordTextField().getPassword());
        confPassword = new String(getEmbedPanel().getConfPasswordTextField().getPassword());

        // START: Input Validations
        if (!checkMandatory(getEmbedPanel().getMsgFileTextField(), labelUtil.getString("gui.label.dhEmbed.msgFile"))) {
            return;
        }
        if (!checkMandatory(getEmbedPanel().getCoverFileTextField(), labelUtil.getString("gui.label.dhEmbed.coverFile"))) {
            return;
        }
        if (!checkMandatory(getEmbedPanel().getStegoFileTextField(), labelUtil.getString("gui.label.dhEmbed.stegoFile"))) {
            return;
        }

        // Check if single or multiple cover files are selected
        if (coverFileList.size() <= 1) {
            // If user has provided a wildcard for cover file name, and parser returns zero length, then it means that
            // there are no matching files with that wildcard
            if (coverFileList.size() == 0 && !getEmbedPanel().getCoverFileTextField().getText().trim().equals("")) {
                JOptionPane.showMessageDialog(this,
                    labelUtil.getString("gui.msg.err.dhEmbed.coverFileNotFound", getEmbedPanel().getCoverFileTextField().getText()),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                getEmbedPanel().getCoverFileTextField().requestFocus();
                return;
            }
            // If single cover file is given, then output stego file must not be a directory
            if (outputFile.isDirectory()) {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.dhEmbed.outputShouldBeFile"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                getEmbedPanel().getStegoFileTextField().requestFocus();
                return;
            }
        } else {
            // If multiple cover files are given, then output stego file must be a directory
            if (!outputFile.isDirectory()) {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.dhEmbed.outputShouldBeDir"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                getEmbedPanel().getStegoFileTextField().requestFocus();
                return;
            }
        }

        if (!password.equals(confPassword)) {
            JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.dhEmbed.passwordMismatch"), labelUtil.getString("gui.msg.title.err"),
                JOptionPane.ERROR_MESSAGE);
            getEmbedPanel().getConfPasswordTextField().requestFocus();
            return;
        }
        // END: Input Validations

        WorkerTask task = new WorkerTask(this, coverFileList, coverFileList.size() > 1) {
            @Override
            protected Object doInBackground() throws Exception {
                OpenStego openStego = null;
                OpenStegoConfig config = null;
                OpenStegoPlugin embedPlugin = null;
                String outputFileName = null;
                String dataFileName = null;
                String cryptAlgo = null;
                String password = null;
                File outputFile = null;
                File cvrFile = null;
                int processCount = 0;
                int skipCount = 0;
                byte[] stegoData = null;

                @SuppressWarnings ("unchecked")
                List<File> coverFileList = (List<File>) this.data;

                cryptAlgo = (String) getEmbedPanel().getEncryptionAlgoComboBox().getSelectedItem();
                password = new String(getEmbedPanel().getPasswordTextField().getPassword());
                dataFileName = getEmbedPanel().getMsgFileTextField().getText();
                outputFileName = getEmbedPanel().getStegoFileTextField().getText();
                outputFile = new File(outputFileName);

                embedPlugin = getDefaultPlugin(OpenStegoPlugin.Purpose.DATA_HIDING);
                config = embedPlugin.createConfig();
                config.setUseCompression(true);
                config.setUseEncryption(true);
                config.setEncryptionAlgorithm(cryptAlgo);
                config.setPassword(password);
                openStego = new OpenStego(embedPlugin, config);

                for (int i = 0; i < coverFileList.size(); i++) {
                    setProgress(i * 100 / coverFileList.size());
                    cvrFile = coverFileList.get(i);

                    if (outputFile.isDirectory()) {
                        // Use cover file name as the output file name. Change the folder to given output folder
                        outputFileName = outputFile.getPath() + File.separator + cvrFile.getName();
                    }

                    // If the output filename extension is not supported for writing, then change the same
                    if (!embedPlugin.getWritableFileExtensions()
                            .contains(outputFileName.substring(outputFileName.lastIndexOf('.') + 1).toLowerCase())) {
                        outputFileName = outputFileName + "." + embedPlugin.getWritableFileExtensions().get(0);
                    }

                    if ((new File(outputFileName)).exists()) {
                        if (JOptionPane.showConfirmDialog(this.parent, labelUtil.getString("gui.msg.warn.fileExists", outputFileName),
                            labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                            if (coverFileList.size() == 1) {
                                this.cancel(true);
                                return null;
                            }
                            skipCount++;
                            continue;
                        }
                    }

                    processCount++;
                    stegoData = openStego.embedData(dataFileName == null || dataFileName.equals("") ? null : new File(dataFileName), cvrFile,
                        outputFileName);
                    CommonUtil.writeFile(stegoData, outputFileName);
                }

                return new Integer[] { processCount, skipCount };
            }

            @Override
            public void done() {
                super.done();
                if (isCancelled()) {
                    return;
                }

                Integer[] val = null;
                try {
                    val = (Integer[]) get();
                } catch (InterruptedException exc) {
                    exc.printStackTrace();
                    return;
                } catch (ExecutionException exc) {
                    handleException(exc);
                    return;
                }

                JOptionPane.showMessageDialog(this.parent, labelUtil.getString("gui.msg.success.dhEmbed", val[0], val[1]),
                    labelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

                // Reset configuration
                ((OpenStegoUI) this.parent).resetGUI();
            }
        };
        task.start();
    }

    /**
     * This method extracts data from the selected file
     *
     * @throws OpenStegoException
     */
    private void extractData() throws OpenStegoException {
        // START: Input Validations
        if (!checkMandatory(getExtractPanel().getInputStegoFileTextField(), labelUtil.getString("gui.label.dhExtract.stegoFile"))) {
            return;
        }
        if (!checkMandatory(getExtractPanel().getOutputFolderTextField(), labelUtil.getString("gui.label.dhExtract.outputDir"))) {
            return;
        }
        // END: Input Validations

        WorkerTask task = new WorkerTask(this, null, false) {
            @Override
            protected Object doInBackground() throws Exception {
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

                stegoOutput = openStego.extractData(new File(stegoFileName));
                outputFileName = (String) stegoOutput.get(0);
                file = new File(outputFolder + File.separator + outputFileName);
                if (file.exists()) {
                    if (JOptionPane.showConfirmDialog(this.parent, labelUtil.getString("gui.msg.warn.fileExists", outputFileName),
                        labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                        this.cancel(true);
                    }
                }

                CommonUtil.writeFile((byte[]) stegoOutput.get(1), outputFolder + File.separator + outputFileName);
                return outputFileName;
            }

            @Override
            public void done() {
                super.done();
                if (isCancelled()) {
                    return;
                }

                String outputFileName = null;
                try {
                    outputFileName = (String) get();
                } catch (InterruptedException exc) {
                    exc.printStackTrace();
                    return;
                } catch (ExecutionException exc) {
                    handleException(exc);
                    return;
                }

                JOptionPane.showMessageDialog(this.parent, labelUtil.getString("gui.msg.success.dhExtract", outputFileName),
                    labelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

                // Reset GUI
                getExtractPanel().getInputStegoFileTextField().setText("");
                getExtractPanel().getOutputFolderTextField().setText("");
                getExtractPanel().getExtractPwdTextField().setText("");
                getExtractPanel().getInputStegoFileTextField().requestFocus();
            }
        };
        task.start();
    }

    /**
     * This method generates signature for watermarking
     *
     * @throws OpenStegoException
     */
    private void generateSignature() throws OpenStegoException {
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
        if (!checkMandatory(getGenSigPanel().getInputKeyTextField(), labelUtil.getString("gui.label.wmGenSig.inputKey"))) {
            return;
        }
        if (!checkMandatory(getGenSigPanel().getSignatureFileTextField(), labelUtil.getString("gui.label.wmGenSig.sigFile"))) {
            return;
        }
        // END: Input Validations

        config.setPassword(inputKey);
        openStego = new OpenStego(plugin, config);
        if (sigFile.exists()) {
            if (JOptionPane.showConfirmDialog(this, labelUtil.getString("gui.msg.warn.fileExists", sigFileName),
                labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                return;
            }
        }

        sigData = openStego.generateSignature();
        CommonUtil.writeFile(sigData, sigFile);

        JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.success.wmGenSig"), labelUtil.getString("gui.msg.title.success"),
            JOptionPane.INFORMATION_MESSAGE);

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
    private void embedMark() throws OpenStegoException {
        List<File> inputFileList = null;
        File outputFile = null;

        inputFileList = CommonUtil.parseFileList(getEmbedWmPanel().getFileForWmTextField().getText(), ";");
        outputFile = new File(getEmbedWmPanel().getOutputWmFileTextField().getText());

        // START: Input Validations
        if (!checkMandatory(getEmbedWmPanel().getFileForWmTextField(), labelUtil.getString("gui.label.wmEmbed.fileForWm"))) {
            return;
        }
        if (!checkMandatory(getEmbedWmPanel().getSignatureFileTextField(), labelUtil.getString("gui.label.wmEmbed.sigFile"))) {
            return;
        }
        if (!checkMandatory(getEmbedWmPanel().getOutputWmFileTextField(), labelUtil.getString("gui.label.wmEmbed.outputWmFile"))) {
            return;
        }

        // Check if single or multiple input files are selected
        if (inputFileList.size() <= 1) {
            // If user has provided a wildcard for file name, and parser returns zero length, then it means that
            // there are no matching files with that wildcard
            if (inputFileList.size() == 0 && !getEmbedWmPanel().getFileForWmTextField().getText().trim().equals("")) {
                JOptionPane.showMessageDialog(this,
                    labelUtil.getString("gui.msg.err.wmEmbed.inputFileNotFound", getEmbedWmPanel().getFileForWmTextField().getText()),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                getEmbedWmPanel().getFileForWmTextField().requestFocus();
                return;
            }
            // If single input file is given, then output file must not be a directory
            if (outputFile.isDirectory()) {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.wmEmbed.outputShouldBeFile"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                getEmbedWmPanel().getOutputWmFileTextField().requestFocus();
                return;
            }
        } else {
            // If multiple input files are given, then output file must be a directory
            if (!outputFile.isDirectory()) {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.wmEmbed.outputShouldBeDir"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                getEmbedWmPanel().getOutputWmFileTextField().requestFocus();
                return;
            }
        }
        // END: Input Validations

        WorkerTask task = new WorkerTask(this, inputFileList, inputFileList.size() > 1) {
            @Override
            protected Object doInBackground() throws Exception {
                OpenStego openStego = null;
                byte[] wmData = null;
                String sigFileName = null;
                String outputFileName = null;
                File inputFile = null;
                File outputFile = null;
                int processCount = 0;
                int skipCount = 0;
                OpenStegoConfig config = null;
                OpenStegoPlugin plugin = null;

                @SuppressWarnings ("unchecked")
                List<File> inputFileList = (List<File>) this.data;

                plugin = getDefaultPlugin(OpenStegoPlugin.Purpose.WATERMARKING);
                config = plugin.createConfig();
                openStego = new OpenStego(plugin, config);

                sigFileName = getEmbedWmPanel().getSignatureFileTextField().getText();
                outputFileName = getEmbedWmPanel().getOutputWmFileTextField().getText();
                outputFile = new File(outputFileName);

                for (int i = 0; i < inputFileList.size(); i++) {
                    setProgress(i * 100 / inputFileList.size());
                    inputFile = inputFileList.get(i);

                    if (outputFile.isDirectory()) {
                        // Use input file name as the output file name. Change the folder to given output folder
                        outputFileName = outputFile.getPath() + File.separator + inputFile.getName();
                    }

                    // If the output filename extension is not supported for writing, then change the same
                    if (!plugin.getWritableFileExtensions().contains(outputFileName.substring(outputFileName.lastIndexOf('.') + 1).toLowerCase())) {
                        outputFileName = outputFileName + "." + plugin.getWritableFileExtensions().get(0);
                    }

                    if ((new File(outputFileName)).exists()) {
                        if (JOptionPane.showConfirmDialog(this.parent, labelUtil.getString("gui.msg.warn.fileExists", outputFileName),
                            labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                            if (inputFileList.size() == 1) {
                                this.cancel(true);
                                return null;
                            }
                            skipCount++;
                            continue;
                        }
                    }

                    processCount++;
                    wmData = openStego.embedMark(sigFileName == null || sigFileName.equals("") ? null : new File(sigFileName), inputFile,
                        outputFileName);
                    CommonUtil.writeFile(wmData, outputFileName);
                }

                return new Integer[] { processCount, skipCount };
            }

            @Override
            public void done() {
                super.done();
                if (isCancelled()) {
                    return;
                }

                Integer[] val = null;
                try {
                    val = (Integer[]) get();
                } catch (InterruptedException exc) {
                    exc.printStackTrace();
                    return;
                } catch (ExecutionException exc) {
                    handleException(exc);
                    return;
                }

                JOptionPane.showMessageDialog(this.parent, labelUtil.getString("gui.msg.success.wmEmbed", val[0], val[1]),
                    labelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

                // Reset GUI
                getEmbedWmPanel().getFileForWmTextField().setText("");
                getEmbedWmPanel().getSignatureFileTextField().setText("");
                getEmbedWmPanel().getOutputWmFileTextField().setText("");
                getEmbedWmPanel().getFileForWmTextField().requestFocus();
            }
        };
        task.start();
    }

    /**
     * This method checks for watermark in the selected file
     *
     * @throws OpenStegoException
     */
    private void checkMark() throws OpenStegoException {
        List<File> inputFileList = null;

        // START: Input Validations
        if (!checkMandatory(getVerifyWmPanel().getInputFileTextField(), labelUtil.getString("gui.label.wmVerify.inputWmFile"))) {
            return;
        }
        if (!checkMandatory(getVerifyWmPanel().getSignatureFileTextField(), labelUtil.getString("gui.label.wmVerify.sigFile"))) {
            return;
        }

        // If user has provided a wildcard for file name, and parser returns zero length, then it means that
        // there are no matching files with that wildcard
        inputFileList = CommonUtil.parseFileList(getVerifyWmPanel().getInputFileTextField().getText(), ";");
        if (inputFileList.size() == 0 && !getVerifyWmPanel().getInputFileTextField().getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this,
                labelUtil.getString("gui.msg.err.wmVerify.inputFileNotFound", getVerifyWmPanel().getInputFileTextField().getText()),
                labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
            getVerifyWmPanel().getInputFileTextField().requestFocus();
            return;
        }
        // END: Input Validations

        WorkerTask task = new WorkerTask(this, inputFileList, inputFileList.size() > 1) {
            @Override
            protected Object doInBackground() throws Exception {
                File sigFile = null;
                OpenStego openStego = null;
                OpenStegoConfig config = null;
                OpenStegoPlugin plugin = null;
                NumberFormat formatter = NumberFormat.getPercentInstance();
                double correlation = 0.0;

                @SuppressWarnings ("unchecked")
                List<File> inputFileList = (List<File>) this.data;

                plugin = getDefaultPlugin(OpenStegoPlugin.Purpose.WATERMARKING);
                config = plugin.createConfig();

                openStego = new OpenStego(plugin, config);
                sigFile = new File(getVerifyWmPanel().getSignatureFileTextField().getText());

                Object[][] tblData = new Object[inputFileList.size()][2];
                for (int i = 0; i < inputFileList.size(); i++) {
                    setProgress(i * 100 / inputFileList.size());
                    File inputFile = inputFileList.get(i);
                    correlation = openStego.checkMark(inputFile, sigFile);
                    tblData[i][0] = inputFile.getName();
                    String color = null;
                    if (correlation > plugin.getHighWatermarkLevel()) {
                        color = "green";
                    } else if (correlation > plugin.getLowWatermarkLevel()) {
                        color = "#FFBF00";
                    } else {
                        color = "red";
                    }
                    tblData[i][1] = "<html><span style='color:" + color + "'>\u25cf " + formatter.format(correlation) + "</span></html>";
                }
                setProgress(100);

                return tblData;
            }

            @Override
            public void done() {
                super.done();
                if (isCancelled()) {
                    return;
                }

                Object[][] tblData = null;
                try {
                    tblData = (Object[][]) get();
                } catch (InterruptedException exc) {
                    exc.printStackTrace();
                    return;
                } catch (ExecutionException exc) {
                    handleException(exc);
                    return;
                }

                JTable table = new JTable(tblData, new Object[] { labelUtil.getString("gui.label.wmVerify.result.header.fileName"),
                        labelUtil.getString("gui.label.wmVerify.result.header.strength") }) {
                    /**
                     *
                     */
                    private static final long serialVersionUID = 2555408155856491941L;

                    @Override
                    public boolean isCellEditable(int rowIndex, int colIndex) {
                        return false;
                    }
                };
                JScrollPane pane = new JScrollPane(table);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                table.setDragEnabled(false);
                table.setCellSelectionEnabled(false);
                table.setRowSelectionAllowed(false);
                table.setPreferredScrollableViewportSize(new Dimension(400, 150));

                JPanel panel = new JPanel(new BorderLayout());
                JLabel header = new JLabel(labelUtil.getString("gui.msg.success.wmVerify"));
                header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                panel.add(header, BorderLayout.NORTH);
                panel.add(pane, BorderLayout.CENTER);

                JOptionPane.showMessageDialog(this.parent, panel, labelUtil.getString("gui.msg.title.results"), JOptionPane.INFORMATION_MESSAGE);

                // Reset GUI
                getVerifyWmPanel().getInputFileTextField().setText("");
                getVerifyWmPanel().getSignatureFileTextField().setText("");
                getVerifyWmPanel().getInputFileTextField().requestFocus();
            }
        };
        task.start();
    }

    /**
     * This method shows the file chooser and updates the text field based on the selection
     *
     * @throws OpenStegoException
     */
    private void selectFile(String action) throws OpenStegoException {
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

        if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EMB_MSGFILE)) {
            title = labelUtil.getString("gui.filer.title.dhEmbed.msgFile");
            textField = getEmbedPanel().getMsgFileTextField();
        } else if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EMB_CVRFILE)) {
            title = labelUtil.getString("gui.filer.title.dhEmbed.coverFile");
            filterDesc = labelUtil.getString("gui.filer.filter.coverFiles", getExtensionsString(plugin, READ_EXTENSIONS));
            allowedExts = getExtensionsList(plugin, READ_EXTENSIONS);
            textField = getEmbedPanel().getCoverFileTextField();
            multiSelect = true;
        } else if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EMB_STGFILE)) {
            title = labelUtil.getString("gui.filer.title.dhEmbed.stegoFile");
            if (coverFileListSize > 1) {
                allowFileDir = FileBrowser.ALLOW_DIRECTORY;
            } else {
                filterDesc = labelUtil.getString("gui.filer.filter.stegoFiles", getExtensionsString(plugin, WRITE_EXTENSIONS));
                allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
            }
            textField = getEmbedPanel().getStegoFileTextField();
        } else if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EXT_STGFILE)) {
            title = labelUtil.getString("gui.filer.title.dhExtract.stegoFile");
            filterDesc = labelUtil.getString("gui.filer.filter.stegoFiles", getExtensionsString(plugin, WRITE_EXTENSIONS));
            allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
            textField = getExtractPanel().getInputStegoFileTextField();
        } else if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EXT_OUTDIR)) {
            title = labelUtil.getString("gui.filer.title.dhExtract.outputDir");
            allowFileDir = FileBrowser.ALLOW_DIRECTORY;
            textField = getExtractPanel().getOutputFolderTextField();
        } else if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_GSG_SIGFILE)) {
            title = labelUtil.getString("gui.filer.title.wmGenSig.sigFile");
            filterDesc = labelUtil.getString("gui.filer.filter.sigFiles", "*" + SIG_FILE_EXTENSION);
            allowedExts = Arrays.asList(new String[] { SIG_FILE_EXTENSION });
            textField = getGenSigPanel().getSignatureFileTextField();
        } else if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_EMB_INPFILE)) {
            title = labelUtil.getString("gui.filer.title.wmEmbed.fileForWm");
            filterDesc = labelUtil.getString("gui.filer.filter.filesForWm", getExtensionsString(plugin, READ_EXTENSIONS));
            allowedExts = getExtensionsList(plugin, READ_EXTENSIONS);
            textField = getEmbedWmPanel().getFileForWmTextField();
            multiSelect = true;
        } else if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_EMB_SIGFILE)) {
            title = labelUtil.getString("gui.filer.title.wmEmbed.sigFile");
            filterDesc = labelUtil.getString("gui.filer.filter.sigFiles", "*" + SIG_FILE_EXTENSION);
            allowedExts = Arrays.asList(new String[] { SIG_FILE_EXTENSION });
            textField = getEmbedWmPanel().getSignatureFileTextField();
        } else if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_EMB_OUTFILE)) {
            title = labelUtil.getString("gui.filer.title.wmEmbed.outputWmFile");
            if (wmInputFileListSize > 1) {
                allowFileDir = FileBrowser.ALLOW_DIRECTORY;
            } else {
                filterDesc = labelUtil.getString("gui.filer.filter.wmFiles", getExtensionsString(plugin, WRITE_EXTENSIONS));
                allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
            }
            textField = getEmbedWmPanel().getOutputWmFileTextField();
        } else if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_VER_INPFILE)) {
            title = labelUtil.getString("gui.filer.title.wmExtract.inputWmFile");
            filterDesc = labelUtil.getString("gui.filer.filter.wmFiles", getExtensionsString(plugin, WRITE_EXTENSIONS));
            allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
            textField = getVerifyWmPanel().getInputFileTextField();
            multiSelect = true;
        } else if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_VER_SIGFILE)) {
            title = labelUtil.getString("gui.filer.title.wmExtract.sigFile");
            filterDesc = labelUtil.getString("gui.filer.filter.sigFiles", "*" + SIG_FILE_EXTENSION);
            allowedExts = Arrays.asList(new String[] { SIG_FILE_EXTENSION });
            textField = getVerifyWmPanel().getSignatureFileTextField();
        }

        fileName = browser.getFileName(title, filterDesc, allowedExts, allowFileDir, multiSelect);
        if (fileName != null) {
            // Check for valid extension for output file
            if ((action.equals(OpenStegoFrame.ActionCommands.BROWSE_DH_EMB_STGFILE) && (coverFileListSize <= 1))
                    || (action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_EMB_OUTFILE) && (wmInputFileListSize <= 1))) {
                if (!plugin.getWritableFileExtensions().contains(fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase())) {
                    fileName = fileName + "." + plugin.getWritableFileExtensions().get(0);
                }
            }
            // Check for valid extension for signature file
            if (action.equals(OpenStegoFrame.ActionCommands.BROWSE_WM_GSG_SIGFILE)) {
                if (!fileName.toLowerCase().endsWith(SIG_FILE_EXTENSION)) {
                    fileName = fileName + SIG_FILE_EXTENSION;
                }
            }
            textField.setText(fileName);
        }
    }

    /**
     * This method exits the application.
     */
    private void close() {
        System.exit(0);
    }

    /**
     * This method displays the About dialog box
     */
    private void showHelpAbout() {
        HelpAboutDialog aboutDialog = new HelpAboutDialog(this);
        aboutDialog.setVisible(true);
    }

    /**
     * This method handles all the exceptions in the GUI
     *
     * @param ex Exception to be handled
     */
    private void handleException(Throwable ex) {
        String msg = null;

        if (ex instanceof OutOfMemoryError) {
            msg = labelUtil.getString("err.memory.full");
        } else if (ex instanceof OpenStegoException) {
            msg = ex.getMessage();
        } else {
            Throwable cause = ex.getCause();
            if (cause instanceof OpenStegoException) {
                msg = cause.getMessage();
            } else {
                msg = ex.getMessage();
            }
        }

        if ((msg == null) || (msg.trim().equals(""))) {
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
    private boolean checkMandatory(JTextField textField, String fieldName) {
        if (!textField.isEnabled()) {
            return true;
        }

        String value = textField.getText();
        if (value == null || value.trim().equals("")) {
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
    private String getExtensionsString(OpenStegoPlugin plugin, int flag) throws OpenStegoException {
        List<String> list = null;
        StringBuffer output = new StringBuffer();

        list = getExtensionsList(plugin, flag);
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
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
    private List<String> getExtensionsList(OpenStegoPlugin plugin, int flag) throws OpenStegoException {
        List<String> list = null;
        List<String> output = new ArrayList<String>();

        if (flag == READ_EXTENSIONS) {
            list = plugin.getReadableFileExtensions();
        } else if (flag == WRITE_EXTENSIONS) {
            list = plugin.getWritableFileExtensions();
        }

        for (int i = 0; i < list.size(); i++) {
            output.add("." + list.get(i));
        }
        return output;
    }

    private OpenStegoPlugin getDefaultPlugin(OpenStegoPlugin.Purpose purpose) {
        // TODO
        if (purpose == OpenStegoPlugin.Purpose.DATA_HIDING) {
            return PluginManager.getPluginByName("RandomLSB");
        } else {
            return PluginManager.getPluginByName("DWTDugad");
        }
    }

    /**
     * Common listener class to handlw action and window events
     */
    class Listener implements ActionListener, WindowListener {
        @Override
        public void actionPerformed(ActionEvent ev) {
            try {
                String action = ev.getActionCommand();

                if (action.startsWith("MENU_")) {
                    if (action.equals(OpenStegoFrame.ActionCommands.MENU_FILE_EXIT)) {
                        close();
                    } else if (action.equals(OpenStegoFrame.ActionCommands.MENU_HELP_ABOUT)) {
                        showHelpAbout();
                    }
                } else if (action.startsWith("BROWSE_")) {
                    selectFile(action);
                } else if (action.startsWith("SWITCH_")) {
                    getMainPanel().removeAll();
                    if (action.equals(OpenStegoFrame.ActionCommands.SWITCH_DH_EMBED)) {
                        getMainPanel().add(getEmbedPanel());
                        getHeader().setText(labelUtil.getString("gui.label.panelHeader.dhEmbed"));
                    } else if (action.equals(OpenStegoFrame.ActionCommands.SWITCH_DH_EXTRACT)) {
                        getMainPanel().add(getExtractPanel());
                        getHeader().setText(labelUtil.getString("gui.label.panelHeader.dhExtract"));
                    } else if (action.equals(OpenStegoFrame.ActionCommands.SWITCH_WM_GENSIG)) {
                        getMainPanel().add(getGenSigPanel());
                        getHeader().setText(labelUtil.getString("gui.label.panelHeader.wmGenSig"));
                    } else if (action.equals(OpenStegoFrame.ActionCommands.SWITCH_WM_EMBED)) {
                        getMainPanel().add(getEmbedWmPanel());
                        getHeader().setText(labelUtil.getString("gui.label.panelHeader.wmEmbed"));
                    } else if (action.equals(OpenStegoFrame.ActionCommands.SWITCH_WM_VERIFY)) {
                        getMainPanel().add(getVerifyWmPanel());
                        getHeader().setText(labelUtil.getString("gui.label.panelHeader.wmVerify"));
                    }
                    getMainPanel().revalidate();
                    getMainPanel().repaint();
                } else if (action.startsWith("RUN_")) {
                    try {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        if (action.equals(OpenStegoFrame.ActionCommands.RUN_DH_EMBED)) {
                            embedData();
                        } else if (action.equals(OpenStegoFrame.ActionCommands.RUN_DH_EXTRACT)) {
                            extractData();
                        } else if (action.equals(OpenStegoFrame.ActionCommands.RUN_WM_GENSIG)) {
                            generateSignature();
                        } else if (action.equals(OpenStegoFrame.ActionCommands.RUN_WM_EMBED)) {
                            embedMark();
                        } else if (action.equals(OpenStegoFrame.ActionCommands.RUN_WM_VERIFY)) {
                            checkMark();
                        }
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            } catch (Throwable ex) {
                handleException(ex);
            }
        }

        @Override
        public void windowClosing(WindowEvent ev) {
            close();
        }

        @Override
        public void windowActivated(WindowEvent ev) {
        }

        @Override
        public void windowClosed(WindowEvent ev) {
        }

        @Override
        public void windowDeactivated(WindowEvent ev) {
        }

        @Override
        public void windowDeiconified(WindowEvent ev) {
        }

        @Override
        public void windowIconified(WindowEvent ev) {
        }

        @Override
        public void windowOpened(WindowEvent ev) {
        }
    }

    /**
     * Class to implement File Chooser
     */
    class FileBrowser {
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
        public String getFileName(String dialogTitle, String filterDesc, List<String> allowedExts, int allowFileDir, boolean multiSelect) {
            int retVal = 0;
            String fileName = null;
            File[] files = null;

            JFileChooser chooser = new JFileChooser(lastFolder);
            chooser.setMultiSelectionEnabled(multiSelect);
            switch (allowFileDir) {
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

            if (filterDesc != null) {
                chooser.setFileFilter(new FileBrowserFilter(filterDesc, allowedExts));
            }
            chooser.setDialogTitle(dialogTitle);
            retVal = chooser.showOpenDialog(null);

            if (retVal == JFileChooser.APPROVE_OPTION) {
                if (multiSelect) {
                    StringBuffer fileList = new StringBuffer();
                    files = chooser.getSelectedFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (i != 0) {
                            fileList.append(";");
                        }
                        fileList.append(files[i].getPath());
                    }
                    fileName = fileList.toString();
                } else {
                    fileName = chooser.getSelectedFile().getPath();
                }
                lastFolder = chooser.getSelectedFile().getParent();
            }

            return fileName;
        }

        /**
         * Class to implement filter for file chooser
         */
        class FileBrowserFilter extends FileFilter {
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
            public FileBrowserFilter(String filterDesc, List<String> allowedExts) {
                this.filterDesc = filterDesc;
                this.allowedExts = allowedExts;
            }

            /**
             * Implementation of <code>accept</accept> method of <code>FileFilter</code> class
             *
             * @param file File to check whether it is acceptable by this filter or not
             * @return Flag to indicate whether file is acceptable or not
             */
            @Override
            public boolean accept(File file) {
                if (file != null) {
                    if (this.allowedExts == null || this.allowedExts.size() == 0 || file.isDirectory()) {
                        return true;
                    }

                    for (int i = 0; i < this.allowedExts.size(); i++) {
                        if (file.getName().toLowerCase().endsWith(this.allowedExts.get(i).toString())) {
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
            @Override
            public String getDescription() {
                return this.filterDesc;
            }
        }
    }
}
