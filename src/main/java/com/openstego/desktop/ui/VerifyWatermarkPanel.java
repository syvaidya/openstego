/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */
package com.openstego.desktop.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.util.LabelUtil;

/**
 * Panel for "Verify Watermark"
 */
public class VerifyWatermarkPanel extends JPanel {
    private static final long serialVersionUID = -7725729404460755224L;

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    private JTextField inputFileTextField;
    private JButton inputFileButton;
    private JTextField signatureFileTextField;
    private JButton signatureFileButton;
    private JButton runVerifyWmButton;

    /**
     * Default constructor
     */
    public VerifyWatermarkPanel() {
        super();
        initialize();
    }

    /**
     * Get method for "Input File" text field
     *
     * @return inputFileTextField
     */
    public JTextField getInputFileTextField() {
        if (this.inputFileTextField == null) {
            this.inputFileTextField = new JTextField();
            this.inputFileTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.inputFileTextField;
    }

    /**
     * Get method for "Input Stego File" browse file button
     *
     * @return inputFileButton
     */
    public JButton getInputFileButton() {
        if (this.inputFileButton == null) {
            this.inputFileButton = new JButton();
            this.inputFileButton.setText("...");
        }
        return this.inputFileButton;
    }

    /**
     * Get method for "Signature File" text field
     *
     * @return signatureFileTextField
     */
    public JTextField getSignatureFileTextField() {
        if (this.signatureFileTextField == null) {
            this.signatureFileTextField = new JTextField();
            this.signatureFileTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.signatureFileTextField;
    }

    /**
     * Get method for "Signature File" browse file button
     *
     * @return signatureFileButton
     */
    public JButton getSignatureFileButton() {
        if (this.signatureFileButton == null) {
            this.signatureFileButton = new JButton();
            this.signatureFileButton.setText("...");
        }
        return this.signatureFileButton;
    }

    /**
     * Get method for "Verify Watermark" button
     *
     * @return runVerifyWmButton
     */
    public JButton getRunVerifyWmButton() {
        if (this.runVerifyWmButton == null) {
            this.runVerifyWmButton = new JButton();
            this.runVerifyWmButton.setText(labelUtil.getString("gui.button.wmVerify.run"));
        }
        return this.runVerifyWmButton;
    }

    private void initialize() {
        JLabel label;
        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.wmVerify.inputWmFile"));
        label.setLabelFor(getInputFileTextField());
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.wmVerify.inputWmFileMsg"));
        label.setFont(label.getFont().deriveFont(Font.ITALIC));
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.wmVerify.sigFile"));
        label.setLabelFor(getSignatureFileTextField());
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getInputFileTextField(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getSignatureFileTextField(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        add(getInputFileButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        add(getSignatureFileButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 0.0;
        add(getRunVerifyWmButton(), gridBagConstraints);

        // Dummy padding
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        add(new JLabel(" "), gridBagConstraints);
    }
}
