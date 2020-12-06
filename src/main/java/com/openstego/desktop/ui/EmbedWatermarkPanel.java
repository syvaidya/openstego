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
 * Panel for "Embed Watermark"
 */
public class EmbedWatermarkPanel extends JPanel {
    private static final long serialVersionUID = -6077376566714959827L;

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    private JTextField fileForWmTextField;
    private JButton fileForWmButton;
    private JTextField signatureFileTextField;
    private JButton signatureFileButton;
    private JTextField outputWmFileTextField;
    private JButton outputWmFileButton;
    private JButton runEmbedWmButton;

    /**
     * Default constructor
     */
    public EmbedWatermarkPanel() {
        super();
        initialize();
    }

    /**
     * Getter method for fileForWmTextField
     *
     * @return fileForWmTextField
     */
    public JTextField getFileForWmTextField() {
        if (this.fileForWmTextField == null) {
            this.fileForWmTextField = new JTextField();
            this.fileForWmTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.fileForWmTextField;
    }

    /**
     * Getter method for fileForWmButton
     *
     * @return fileForWmButton
     */
    public JButton getFileForWmButton() {
        if (this.fileForWmButton == null) {
            this.fileForWmButton = new JButton();
            this.fileForWmButton.setText("...");
        }
        return this.fileForWmButton;
    }

    /**
     * Getter method for signatureFileTextField
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
     * Getter method for signatureFileButton
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
     * Getter method for outputWmFileTextField
     *
     * @return outputWmFileTextField
     */
    public JTextField getOutputWmFileTextField() {
        if (this.outputWmFileTextField == null) {
            this.outputWmFileTextField = new JTextField();
            this.outputWmFileTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.outputWmFileTextField;
    }

    /**
     * Getter method for outputWmFileButton
     *
     * @return outputWmFileButton
     */
    public JButton getOutputWmFileButton() {
        if (this.outputWmFileButton == null) {
            this.outputWmFileButton = new JButton();
            this.outputWmFileButton.setText("...");
        }
        return this.outputWmFileButton;
    }

    /**
     * Getter method for runEmbedWmButton
     *
     * @return runEmbedWmButton
     */
    public JButton getRunEmbedWmButton() {
        if (this.runEmbedWmButton == null) {
            this.runEmbedWmButton = new JButton();
            this.runEmbedWmButton.setText(labelUtil.getString("gui.button.wmEmbed.run"));
        }
        return this.runEmbedWmButton;
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
        label = new JLabel(labelUtil.getString("gui.label.wmEmbed.fileForWm"));
        label.setLabelFor(getFileForWmTextField());
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.wmEmbed.fileForWmMsg"));
        label.setFont(label.getFont().deriveFont(Font.ITALIC));
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.wmEmbed.sigFile"));
        label.setLabelFor(getSignatureFileTextField());
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.wmEmbed.outputWmFile"));
        label.setLabelFor(getOutputWmFileTextField());
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getFileForWmTextField(), gridBagConstraints);

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
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getOutputWmFileTextField(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        add(getFileForWmButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        add(getSignatureFileButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        add(getOutputWmFileButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getRunEmbedWmButton(), gridBagConstraints);

        // Dummy padding
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        add(new JLabel(" "), gridBagConstraints);
    }
}
