/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */
package com.openstego.desktop.ui;

import com.openstego.desktop.*;
import com.openstego.desktop.pwdattack.*;
import com.openstego.desktop.util.CommonUtil;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.ui.WorkerTask;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
    private static final LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    /**
     * Static variable to holds path to last selected folder
     */
    private static String lastFolder = null;

    private final OpenStegoPlugin<?> dhPlugin;
    private final OpenStegoPlugin<?> wmPlugin;

    // Password attack related fields
    private PasswordAttack currentAttack;
    private SwingWorker<PasswordAttackResult, Void> attackWorker;

    /**
     * Default constructor
     */
    public OpenStegoUI(OpenStegoPlugin<?> dhPlugin, OpenStegoPlugin<?> wmPlugin) {
        super(dhPlugin, wmPlugin);
        this.dhPlugin = dhPlugin;
        this.wmPlugin = wmPlugin;
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

        getPwdAttackButton().addActionListener(listener);
        getPwdAttackPanel().getStegoFileButton().addActionListener(listener);
        getPwdAttackPanel().getOutputFolderButton().addActionListener(listener);
        getPwdAttackPanel().getWordlistButton().addActionListener(listener);
        getPwdAttackPanel().getWordlist2Button().addActionListener(listener);
        getPwdAttackPanel().getRulesFileButton().addActionListener(listener);
        getPwdAttackPanel().getStartButton().addActionListener(listener);
        getPwdAttackPanel().getStopButton().addActionListener(listener);
        getPwdAttackPanel().getAttackModeComboBox().addActionListener(e -> getPwdAttackPanel().updatePanelVisibility());

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

        try {
            dhPlugin.resetConfig();
            if (getEmbedPanel().getPluginOptionPanel() != null) {
                getEmbedPanel().getPluginOptionPanel().setGUIFromConfig(dhPlugin.getConfig());
            }
        } catch (OpenStegoException e) {
            handleException(e);
        }
    }

    /**
     * This method embeds the selected data file into selected file
     */
    private void embedData() {
        String outputFileName;
        String password;
        String confPassword;
        File outputFile;
        List<File> coverFileList;

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

        // Plugin specific validations
        if (getEmbedPanel().getPluginOptionPanel() != null &&
                !getEmbedPanel().getPluginOptionPanel().validateEmbedAction()) {
            return;
        }

        WorkerTask task = new WorkerTask(this, coverFileList, coverFileList.size() > 1) {
            @Override
            protected Object doInBackground() throws Exception {
                OpenStego openStego;
                OpenStegoConfig config;
                String outputFileName;
                String dataFileName;
                String cryptAlgo;
                String password;
                File outputFile;
                File cvrFile;
                int processCount = 0;
                int skipCount = 0;
                byte[] stegoData;

                @SuppressWarnings("unchecked")
                List<File> coverFileList = (List<File>) this.data;

                cryptAlgo = (String) getEmbedPanel().getEncryptionAlgoComboBox().getSelectedItem();
                password = new String(getEmbedPanel().getPasswordTextField().getPassword());
                dataFileName = getEmbedPanel().getMsgFileTextField().getText();
                outputFileName = getEmbedPanel().getStegoFileTextField().getText();
                outputFile = new File(outputFileName);

                dhPlugin.resetConfig();
                config = dhPlugin.getConfig();
                config.setUseCompression(true);
                config.setUseEncryption(true);
                config.setEncryptionAlgorithm(cryptAlgo);
                config.setPassword(password);
                if (getEmbedPanel().getPluginOptionPanel() != null) {
                    getEmbedPanel().getPluginOptionPanel().setConfigFromGUI(config);
                }
                openStego = new OpenStego(dhPlugin, config);

                // Add null entry for coverfile if not provided
                if (coverFileList.isEmpty()) {
                    coverFileList.add(null);
                }

                OpenStegoBulkException bulkException = new OpenStegoBulkException();
                for (int i = 0; i < coverFileList.size(); i++) {
                    setProgress(i * 100 / coverFileList.size());
                    cvrFile = coverFileList.get(i);

                    if (outputFile.isDirectory()) {
                        // Use cover file name as the output file name. Change the folder to given output folder
                        outputFileName = outputFile.getPath() + File.separator + (cvrFile == null ? "Output" : cvrFile.getName());
                    }

                    // If the output filename extension is not supported for writing, then change the same
                    if (!dhPlugin.getWritableFileExtensions()
                            .contains(outputFileName.substring(outputFileName.lastIndexOf('.') + 1).toLowerCase())) {
                        outputFileName = outputFileName + "." + dhPlugin.getWritableFileExtensions().get(0);
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
                    try {
                        stegoData = openStego.embedData(
                                dataFileName == null || dataFileName.equals("") ? null : new File(dataFileName),
                                cvrFile, outputFileName);
                        CommonUtil.writeFile(stegoData, outputFileName);
                    } catch (OpenStegoException e) {
                        bulkException.add(cvrFile == null ? "-" : cvrFile.getName(), e);
                    }
                }
                bulkException.throwIfRequired();

                return new Integer[]{processCount, skipCount};
            }

            @Override
            public void done() {
                super.done();
                if (isCancelled()) {
                    return;
                }

                Integer[] val;
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
     */
    private void extractData() {
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
                OpenStego openStego;
                OpenStegoConfig config;
                String stegoFileName;
                String outputFolder;
                String outputFileName;
                File file;
                List<?> stegoOutput;

                dhPlugin.resetConfig();
                config = dhPlugin.getConfig();
                openStego = new OpenStego(dhPlugin, config);
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

                String outputFileName;
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
     * @throws OpenStegoException Processing issues
     */
    private void generateSignature() throws OpenStegoException {
        OpenStego openStego;
        byte[] sigData;
        String inputKey;
        String sigFileName;
        File sigFile;
        OpenStegoConfig config;

        wmPlugin.resetConfig();
        config = wmPlugin.getConfig();

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
        openStego = new OpenStego(wmPlugin, config);
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
     */
    private void embedMark() {
        List<File> inputFileList;
        File outputFile;

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
                OpenStego openStego;
                byte[] wmData;
                String sigFileName;
                String outputFileName;
                File inputFile;
                File outputFile;
                int processCount = 0;
                int skipCount = 0;

                @SuppressWarnings("unchecked")
                List<File> inputFileList = (List<File>) this.data;

                wmPlugin.resetConfig();
                openStego = new OpenStego(wmPlugin, wmPlugin.getConfig());

                sigFileName = getEmbedWmPanel().getSignatureFileTextField().getText();
                outputFileName = getEmbedWmPanel().getOutputWmFileTextField().getText();
                outputFile = new File(outputFileName);

                OpenStegoBulkException bulkException = new OpenStegoBulkException();
                for (int i = 0; i < inputFileList.size(); i++) {
                    setProgress(i * 100 / inputFileList.size());
                    inputFile = inputFileList.get(i);

                    if (outputFile.isDirectory()) {
                        // Use input file name as the output file name. Change the folder to given output folder
                        outputFileName = outputFile.getPath() + File.separator + inputFile.getName();
                    }

                    // If the output filename extension is not supported for writing, then change the same
                    if (!wmPlugin.getWritableFileExtensions().contains(outputFileName.substring(outputFileName.lastIndexOf('.') + 1).toLowerCase())) {
                        outputFileName = outputFileName + "." + wmPlugin.getWritableFileExtensions().get(0);
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
                    try {
                        wmData = openStego.embedMark(sigFileName == null || sigFileName.equals("") ? null : new File(sigFileName), inputFile,
                                outputFileName);
                        CommonUtil.writeFile(wmData, outputFileName);
                    } catch (OpenStegoException e) {
                        bulkException.add(inputFile.getName(), e);
                    }
                }
                bulkException.throwIfRequired();

                return new Integer[]{processCount, skipCount};
            }

            @Override
            public void done() {
                super.done();
                if (isCancelled()) {
                    return;
                }

                Integer[] val;
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
     */
    private void checkMark() {
        List<File> inputFileList;

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
                File sigFile;
                OpenStego openStego;
                NumberFormat formatter = NumberFormat.getPercentInstance();
                double correlation;

                @SuppressWarnings("unchecked")
                List<File> inputFileList = (List<File>) this.data;

                wmPlugin.resetConfig();
                openStego = new OpenStego(wmPlugin, wmPlugin.getConfig());
                sigFile = new File(getVerifyWmPanel().getSignatureFileTextField().getText());

                Object[][] tblData = new Object[inputFileList.size()][2];
                for (int i = 0; i < inputFileList.size(); i++) {
                    setProgress(i * 100 / inputFileList.size());
                    File inputFile = inputFileList.get(i);
                    correlation = openStego.checkMark(inputFile, sigFile);
                    tblData[i][0] = inputFile.getName();
                    String color;
                    if (correlation > wmPlugin.getHighWatermarkLevel()) {
                        color = "green";
                    } else if (correlation > wmPlugin.getLowWatermarkLevel()) {
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

                Object[][] tblData;
                try {
                    tblData = (Object[][]) get();
                } catch (InterruptedException exc) {
                    exc.printStackTrace();
                    return;
                } catch (ExecutionException exc) {
                    handleException(exc);
                    return;
                }

                JTable table = new JTable(tblData, new Object[]{labelUtil.getString("gui.label.wmVerify.result.header.fileName"),
                        labelUtil.getString("gui.label.wmVerify.result.header.strength")}) {
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
     * Starts a password attack on the selected stego file
     */
    private void startPasswordAttack() {
        PasswordAttackPanel panel = getPwdAttackPanel();

        // Validate inputs
        String stegoFileName = panel.getStegoFileTextField().getText();
        String outputFolder = panel.getOutputFolderTextField().getText();

        if (stegoFileName == null || stegoFileName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.pwdAttack.noStegoFile"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
            panel.getStegoFileTextField().requestFocus();
            return;
        }

        if (outputFolder == null || outputFolder.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.pwdAttack.noOutputDir"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
            panel.getOutputFolderTextField().requestFocus();
            return;
        }

        File stegoFile = new File(stegoFileName);
        if (!stegoFile.exists()) {
            JOptionPane.showMessageDialog(this, labelUtil.getString("cmd.pwdattack.err.stegoFileNotFound", stegoFileName),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        PasswordAttack.AttackMode mode = panel.getSelectedAttackMode();

        // Validate mode-specific inputs
        File wordlist1 = null;
        File wordlist2 = null;
        File rulesFile = null;
        String mask = panel.getMaskTextField().getText();
        String[] customCharsets = panel.getCustomCharsets();

        boolean needsWordlist = mode == PasswordAttack.AttackMode.DICTIONARY ||
                mode == PasswordAttack.AttackMode.COMBINATOR ||
                mode == PasswordAttack.AttackMode.HYBRID_WORDLIST_MASK ||
                mode == PasswordAttack.AttackMode.HYBRID_MASK_WORDLIST ||
                mode == PasswordAttack.AttackMode.RULE_BASED;

        boolean needsWordlist2 = mode == PasswordAttack.AttackMode.COMBINATOR;
        boolean needsMask = mode == PasswordAttack.AttackMode.MASK ||
                mode == PasswordAttack.AttackMode.HYBRID_WORDLIST_MASK ||
                mode == PasswordAttack.AttackMode.HYBRID_MASK_WORDLIST;
        boolean needsRules = mode == PasswordAttack.AttackMode.RULE_BASED;

        if (needsWordlist) {
            String wordlistPath = panel.getWordlistTextField().getText();
            if (wordlistPath == null || wordlistPath.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.pwdAttack.noWordlist"),
                        labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            wordlist1 = new File(wordlistPath);
            if (!wordlist1.exists()) {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.pwdAttack.noWordlist"),
                        labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (needsWordlist2) {
            String wordlist2Path = panel.getWordlist2TextField().getText();
            if (wordlist2Path == null || wordlist2Path.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.pwdAttack.noWordlist2"),
                        labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            wordlist2 = new File(wordlist2Path);
            if (!wordlist2.exists()) {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.pwdAttack.noWordlist2"),
                        labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (needsMask && (mask == null || mask.trim().isEmpty())) {
            JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.pwdAttack.noMask"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (needsRules && !panel.getUseDefaultRulesCheckBox().isSelected()) {
            String rulesPath = panel.getRulesFileTextField().getText();
            if (rulesPath != null && !rulesPath.trim().isEmpty()) {
                rulesFile = new File(rulesPath);
            }
        }

        // Create the attack
        final File finalWordlist1 = wordlist1;
        final File finalWordlist2 = wordlist2;
        final File finalRulesFile = rulesFile;
        final String finalMask = mask;
        final String finalOutputFolder = outputFolder;

        // Update UI state
        panel.getStartButton().setEnabled(false);
        panel.getStopButton().setEnabled(true);
        panel.getProgressBar().setValue(0);
        panel.getStatusLabel().setText("Running...");
        panel.getAttemptsLabel().setText("0");
        panel.getSpeedLabel().setText("0 pwd/sec");
        panel.getCurrentPwdLabel().setText("-");

        // Create a SwingWorker to run the attack in the background
        attackWorker = new SwingWorker<PasswordAttackResult, Void>() {
            @Override
            protected PasswordAttackResult doInBackground() {
                PasswordAttack attack;

                if (mode == PasswordAttack.AttackMode.RULE_BASED && panel.getUseDefaultRulesCheckBox().isSelected()) {
                    attack = new RuleBasedAttack(stegoFile, dhPlugin, finalWordlist1,
                            RuleBasedAttack.getDefaultRules());
                } else {
                    attack = PasswordAttack.create(mode, stegoFile, dhPlugin,
                            finalWordlist1, finalWordlist2, finalMask, finalRulesFile, customCharsets);
                }

                currentAttack = attack;

                // Set up the listener for progress updates
                attack.setListener(new PasswordAttackListener() {
                    @Override
                    public void onProgress(long attemptCount, String currentPassword, long elapsedTimeMs, long estimatedTotal) {
                        SwingUtilities.invokeLater(() -> {
                            double speed = elapsedTimeMs > 0 ? (attemptCount * 1000.0) / elapsedTimeMs : 0;
                            panel.getAttemptsLabel().setText(String.format("%,d", attemptCount));
                            panel.getSpeedLabel().setText(String.format("%.0f pwd/sec", speed));
                            panel.getCurrentPwdLabel().setText(truncate(currentPassword, 30));

                            if (estimatedTotal > 0) {
                                int pct = (int) ((attemptCount * 100) / estimatedTotal);
                                panel.getProgressBar().setValue(Math.min(pct, 100));
                                panel.getProgressBar().setString(pct + "%");
                            } else {
                                panel.getProgressBar().setIndeterminate(true);
                            }
                        });
                    }

                    @Override
                    public void onComplete(PasswordAttackResult result) {
                        // Handled in done()
                    }

                    @Override
                    public void onError(Exception error) {
                        SwingUtilities.invokeLater(() -> handleException(error));
                    }

                    @Override
                    public boolean isCancelled() {
                        return attackWorker.isCancelled();
                    }

                    private String truncate(String s, int maxLen) {
                        if (s == null) return "-";
                        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
                    }
                });

                return attack.execute();
            }

            @Override
            protected void done() {
                panel.getStartButton().setEnabled(true);
                panel.getStopButton().setEnabled(false);
                panel.getProgressBar().setIndeterminate(false);

                if (isCancelled()) {
                    panel.getStatusLabel().setText("Cancelled");
                    JOptionPane.showMessageDialog(OpenStegoUI.this,
                            labelUtil.getString("gui.msg.info.pwdAttack.cancelled"),
                            labelUtil.getString("gui.msg.title.results"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                try {
                    PasswordAttackResult result = get();

                    if (result.isFound()) {
                        panel.getProgressBar().setValue(100);
                        panel.getProgressBar().setString("100%");
                        panel.getStatusLabel().setText("Password found!");

                        // Save the extracted file
                        String extractFileName = result.getExtractedFileName();
                        if (extractFileName == null || extractFileName.isEmpty()) {
                            extractFileName = "extracted_data";
                        }
                        String outputPath = finalOutputFolder + File.separator + extractFileName;

                        // Check if file exists
                        File outputFile = new File(outputPath);
                        if (outputFile.exists()) {
                            int overwrite = JOptionPane.showConfirmDialog(OpenStegoUI.this,
                                    labelUtil.getString("gui.msg.warn.fileExists", outputPath),
                                    labelUtil.getString("gui.msg.title.warn"),
                                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (overwrite == JOptionPane.NO_OPTION) {
                                return;
                            }
                        }

                        CommonUtil.writeFile(result.getExtractedData(), outputPath);

                        JOptionPane.showMessageDialog(OpenStegoUI.this,
                                labelUtil.getString("gui.msg.success.pwdAttack.found", result.getPassword()) + "\n" +
                                        labelUtil.getString("gui.msg.success.pwdAttack.extracted", outputPath),
                                labelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        panel.getStatusLabel().setText("Not found");
                        JOptionPane.showMessageDialog(OpenStegoUI.this,
                                labelUtil.getString("gui.msg.info.pwdAttack.notFound"),
                                labelUtil.getString("gui.msg.title.results"), JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    handleException(e);
                }
            }
        };

        attackWorker.execute();
    }

    /**
     * Stops the current password attack
     */
    private void stopPasswordAttack() {
        if (attackWorker != null && !attackWorker.isDone()) {
            attackWorker.cancel(true);
            if (currentAttack != null) {
                currentAttack.cancel();
            }
        }
        getPwdAttackPanel().getStartButton().setEnabled(true);
        getPwdAttackPanel().getStopButton().setEnabled(false);
        getPwdAttackPanel().getStatusLabel().setText("Stopped");
    }

    /**
     * This method shows the file chooser and updates the text field based on the selection
     *
     * @throws OpenStegoException Processing issues
     */
    private void selectFile(String action) throws OpenStegoException {
        FileBrowser browser = new FileBrowser();
        String fileName;
        String title;
        String filterDesc = null;
        List<String> allowedExts = null;
        int allowFileDir = FileBrowser.ALLOW_FILE;
        boolean multiSelect = false;
        int coverFileListSize;
        int wmInputFileListSize;
        JTextField textField;
        OpenStegoPlugin<?> plugin;

        plugin = action.startsWith("BROWSE_DH_") ? dhPlugin : wmPlugin;

        coverFileListSize = CommonUtil.parseFileList(getEmbedPanel().getCoverFileTextField().getText(), ";").size();
        wmInputFileListSize = CommonUtil.parseFileList(getEmbedWmPanel().getFileForWmTextField().getText(), ";").size();

        switch (action) {
            case ActionCommands.BROWSE_DH_EMB_MSGFILE:
                title = labelUtil.getString("gui.filer.title.dhEmbed.msgFile");
                textField = getEmbedPanel().getMsgFileTextField();
                break;
            case ActionCommands.BROWSE_DH_EMB_CVRFILE:
                title = labelUtil.getString("gui.filer.title.dhEmbed.coverFile");
                filterDesc = labelUtil.getString("gui.filer.filter.coverFiles", getExtensionsString(plugin, READ_EXTENSIONS));
                allowedExts = getExtensionsList(plugin, READ_EXTENSIONS);
                textField = getEmbedPanel().getCoverFileTextField();
                multiSelect = true;
                break;
            case ActionCommands.BROWSE_DH_EMB_STGFILE:
                title = labelUtil.getString("gui.filer.title.dhEmbed.stegoFile");
                if (coverFileListSize > 1) {
                    allowFileDir = FileBrowser.ALLOW_DIRECTORY;
                } else {
                    filterDesc = labelUtil.getString("gui.filer.filter.stegoFiles", getExtensionsString(plugin, WRITE_EXTENSIONS));
                    allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
                }
                textField = getEmbedPanel().getStegoFileTextField();
                break;
            case ActionCommands.BROWSE_DH_EXT_STGFILE:
                title = labelUtil.getString("gui.filer.title.dhExtract.stegoFile");
                filterDesc = labelUtil.getString("gui.filer.filter.stegoFiles", getExtensionsString(plugin, WRITE_EXTENSIONS));
                allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
                textField = getExtractPanel().getInputStegoFileTextField();
                break;
            case ActionCommands.BROWSE_DH_EXT_OUTDIR:
                title = labelUtil.getString("gui.filer.title.dhExtract.outputDir");
                allowFileDir = FileBrowser.ALLOW_DIRECTORY;
                textField = getExtractPanel().getOutputFolderTextField();
                break;
            case ActionCommands.BROWSE_WM_GSG_SIGFILE:
                title = labelUtil.getString("gui.filer.title.wmGenSig.sigFile");
                filterDesc = labelUtil.getString("gui.filer.filter.sigFiles", "*" + SIG_FILE_EXTENSION);
                allowedExts = Collections.singletonList(SIG_FILE_EXTENSION);
                textField = getGenSigPanel().getSignatureFileTextField();
                break;
            case ActionCommands.BROWSE_WM_EMB_INPFILE:
                title = labelUtil.getString("gui.filer.title.wmEmbed.fileForWm");
                filterDesc = labelUtil.getString("gui.filer.filter.filesForWm", getExtensionsString(plugin, READ_EXTENSIONS));
                allowedExts = getExtensionsList(plugin, READ_EXTENSIONS);
                textField = getEmbedWmPanel().getFileForWmTextField();
                multiSelect = true;
                break;
            case ActionCommands.BROWSE_WM_EMB_SIGFILE:
                title = labelUtil.getString("gui.filer.title.wmEmbed.sigFile");
                filterDesc = labelUtil.getString("gui.filer.filter.sigFiles", "*" + SIG_FILE_EXTENSION);
                allowedExts = Collections.singletonList(SIG_FILE_EXTENSION);
                textField = getEmbedWmPanel().getSignatureFileTextField();
                break;
            case ActionCommands.BROWSE_WM_EMB_OUTFILE:
                title = labelUtil.getString("gui.filer.title.wmEmbed.outputWmFile");
                if (wmInputFileListSize > 1) {
                    allowFileDir = FileBrowser.ALLOW_DIRECTORY;
                } else {
                    filterDesc = labelUtil.getString("gui.filer.filter.wmFiles", getExtensionsString(plugin, WRITE_EXTENSIONS));
                    allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
                }
                textField = getEmbedWmPanel().getOutputWmFileTextField();
                break;
            case ActionCommands.BROWSE_WM_VER_INPFILE:
                title = labelUtil.getString("gui.filer.title.wmExtract.inputWmFile");
                filterDesc = labelUtil.getString("gui.filer.filter.wmFiles", getExtensionsString(plugin, WRITE_EXTENSIONS));
                allowedExts = getExtensionsList(plugin, WRITE_EXTENSIONS);
                textField = getVerifyWmPanel().getInputFileTextField();
                multiSelect = true;
                break;
            case ActionCommands.BROWSE_WM_VER_SIGFILE:
                title = labelUtil.getString("gui.filer.title.wmExtract.sigFile");
                filterDesc = labelUtil.getString("gui.filer.filter.sigFiles", "*" + SIG_FILE_EXTENSION);
                allowedExts = Collections.singletonList(SIG_FILE_EXTENSION);
                textField = getVerifyWmPanel().getSignatureFileTextField();
                break;
            case ActionCommands.BROWSE_PWD_STGFILE:
                title = labelUtil.getString("gui.filer.title.pwdAttack.stegoFile");
                filterDesc = labelUtil.getString("gui.filer.filter.stegoFiles", getExtensionsString(dhPlugin, WRITE_EXTENSIONS));
                allowedExts = getExtensionsList(dhPlugin, WRITE_EXTENSIONS);
                textField = getPwdAttackPanel().getStegoFileTextField();
                break;
            case ActionCommands.BROWSE_PWD_OUTDIR:
                title = labelUtil.getString("gui.filer.title.pwdAttack.outputDir");
                allowFileDir = FileBrowser.ALLOW_DIRECTORY;
                textField = getPwdAttackPanel().getOutputFolderTextField();
                break;
            case ActionCommands.BROWSE_PWD_WORDLIST:
                title = labelUtil.getString("gui.filer.title.pwdAttack.wordlist");
                filterDesc = labelUtil.getString("gui.filer.filter.wordlist");
                allowedExts = java.util.Arrays.asList(".txt", ".lst", ".dict");
                textField = getPwdAttackPanel().getWordlistTextField();
                break;
            case ActionCommands.BROWSE_PWD_WORDLIST2:
                title = labelUtil.getString("gui.filer.title.pwdAttack.wordlist2");
                filterDesc = labelUtil.getString("gui.filer.filter.wordlist");
                allowedExts = java.util.Arrays.asList(".txt", ".lst", ".dict");
                textField = getPwdAttackPanel().getWordlist2TextField();
                break;
            case ActionCommands.BROWSE_PWD_RULES:
                title = labelUtil.getString("gui.filer.title.pwdAttack.rulesFile");
                filterDesc = labelUtil.getString("gui.filer.filter.rulesFile");
                allowedExts = java.util.Arrays.asList(".rule", ".txt");
                textField = getPwdAttackPanel().getRulesFileTextField();
                break;
            default:
                throw new OpenStegoException(new RuntimeException("Unknown action: " + action));
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
        Object msg;

        if (ex instanceof OutOfMemoryError) {
            msg = labelUtil.getString("err.memory.full");
        } else if (ex instanceof OpenStegoException) {
            msg = ex.getMessage();
        } else if (ex instanceof OpenStegoBulkException) {
            msg = getBulkMessage((OpenStegoBulkException) ex);
        } else {
            Throwable cause = ex.getCause();
            if (cause instanceof OpenStegoException) {
                msg = cause.getMessage();
            } else if (cause instanceof OpenStegoBulkException) {
                msg = getBulkMessage((OpenStegoBulkException) cause);
            } else {
                msg = ex.getMessage();
            }
        }

        if (msg == null || (msg instanceof String && ((String) msg).trim().equals(""))) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            msg = writer.toString();
        }

        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, msg, labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Helper method to build single output component out of all exceptions within a Bulk Exception
     *
     * @param ex Bulk Exception
     * @return Output message component
     */
    private Object getBulkMessage(OpenStegoBulkException ex) {
        int len = ex.getExceptions().size();
        if (len == 1) {
            return ex.getExceptions().get(0);
        }

        String prefix = "<html><head><style type='text/css'>" +
                "table, th, td { border: 1px solid #333; }" +
                "table { border-width: 0 0 1px 1px }" +
                "th, td { border-width: 1px 1px 0 0 }" +
                "td { background-color: white }" +
                "</style></head><body><table border='0' cellspacing='0' cellpadding='5'>";
        StringBuilder sb = new StringBuilder(prefix).append("<tr><th>")
                .append(labelUtil.getString("gui.msg.err.header.file")).append("</th><th>")
                .append(labelUtil.getString("gui.msg.err.header.error")).append("</th></tr>");
        for (int i = 0; i < ex.getKeys().size(); i++) {
            sb.append("<tr><td>").append(ex.getKeys().get(i)).append("</td><td>")
                    .append(ex.getExceptions().get(i).getMessage()).append("</td></tr>");
        }
        sb.append("</table></body></html>");
        return new JLabel(sb.toString());
    }

    /**
     * Method to check whether value is provided or not; and display message box in case it is not provided
     *
     * @param textField Text field to be checked for value
     * @param fieldName Name of the field
     * @return Flag whether value exists or not
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
     * @param flag   Flag to indicate whether readable (READ_EXTENSIONS) or writeable (WRITE_EXTENSIONS) extensions are
     *               required
     * @return List of extensions (as string)
     * @throws OpenStegoException Processing issues
     */
    private String getExtensionsString(OpenStegoPlugin<?> plugin, int flag) throws OpenStegoException {
        List<String> list;
        StringBuilder output = new StringBuilder();

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
     * @param flag   Flag to indicate whether readable (READ_EXTENSIONS) or writeable (WRITE_EXTENSIONS) extensions are
     *               required
     * @return List of extensions (as list)
     * @throws OpenStegoException Processing issues
     */
    private List<String> getExtensionsList(OpenStegoPlugin<?> plugin, int flag) throws OpenStegoException {
        List<String> list;
        List<String> output = new ArrayList<>();

        if (flag == READ_EXTENSIONS) {
            list = plugin.getReadableFileExtensions();
        } else if (flag == WRITE_EXTENSIONS) {
            list = plugin.getWritableFileExtensions();
        } else {
            throw new OpenStegoException(new RuntimeException("Unknown flag: " + flag));
        }

        for (String s : list) {
            output.add("." + s);
        }
        return output;
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
                    switch (action) {
                        case ActionCommands.SWITCH_DH_EMBED:
                            getMainPanel().add(getEmbedPanel());
                            getHeader().setText(labelUtil.getString("gui.label.panelHeader.dhEmbed"));
                            break;
                        case ActionCommands.SWITCH_DH_EXTRACT:
                            getMainPanel().add(getExtractPanel());
                            getHeader().setText(labelUtil.getString("gui.label.panelHeader.dhExtract"));
                            break;
                        case ActionCommands.SWITCH_WM_GENSIG:
                            getMainPanel().add(getGenSigPanel());
                            getHeader().setText(labelUtil.getString("gui.label.panelHeader.wmGenSig"));
                            break;
                        case ActionCommands.SWITCH_WM_EMBED:
                            getMainPanel().add(getEmbedWmPanel());
                            getHeader().setText(labelUtil.getString("gui.label.panelHeader.wmEmbed"));
                            break;
                        case ActionCommands.SWITCH_WM_VERIFY:
                            getMainPanel().add(getVerifyWmPanel());
                            getHeader().setText(labelUtil.getString("gui.label.panelHeader.wmVerify"));
                            break;
                        case ActionCommands.SWITCH_PWD_ATTACK:
                            getMainPanel().add(getPwdAttackPanel());
                            getHeader().setText(labelUtil.getString("gui.label.panelHeader.pwdAttack"));
                            break;
                    }
                    getMainPanel().revalidate();
                    getMainPanel().repaint();
                } else if (action.startsWith("RUN_")) {
                    try {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        switch (action) {
                            case ActionCommands.RUN_DH_EMBED:
                                embedData();
                                break;
                            case ActionCommands.RUN_DH_EXTRACT:
                                extractData();
                                break;
                            case ActionCommands.RUN_WM_GENSIG:
                                generateSignature();
                                break;
                            case ActionCommands.RUN_WM_EMBED:
                                embedMark();
                                break;
                            case ActionCommands.RUN_WM_VERIFY:
                                checkMark();
                                break;
                            case ActionCommands.RUN_PWD_START:
                                startPasswordAttack();
                                break;
                            case ActionCommands.RUN_PWD_STOP:
                                stopPasswordAttack();
                                break;
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
    static class FileBrowser {
        public static final int ALLOW_FILE = 1;
        public static final int ALLOW_DIRECTORY = 2;
        public static final int ALLOW_FILE_AND_DIR = 3;

        /**
         * Method to get the display file chooser and return the selected file name
         *
         * @param dialogTitle  Title for the file chooser dialog box
         * @param filterDesc   Description to be displayed for the filter in file chooser
         * @param allowedExts  Allowed file extensions for the filter
         * @param allowFileDir Type of objects allowed to be selected (FileBrowser.ALLOW_FILE,
         *                     FileBrowser.ALLOW_DIRECTORY or FileBrowser.ALLOW_FILE_AND_DIR)
         * @param multiSelect  Flag to indicate whether multiple file selection is allowed or not
         * @return Name of the selected file (null if no file was selected)
         */
        public String getFileName(String dialogTitle, String filterDesc, List<String> allowedExts, int allowFileDir, boolean multiSelect) {
            int retVal;
            String fileName = null;
            File[] files;

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
                    StringBuilder fileList = new StringBuilder();
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
        static class FileBrowserFilter extends FileFilter {
            /**
             * Description of the filter
             */
            private final String filterDesc;

            /**
             * List of allowed file extensions
             */
            private final List<String> allowedExts;

            /**
             * Default constructor
             *
             * @param filterDesc  Description of the filter
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

                    for (String allowedExt : this.allowedExts) {
                        if (file.getName().toLowerCase().endsWith(allowedExt)) {
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
