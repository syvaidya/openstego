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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.OpenStegoCrypto;
import com.openstego.desktop.util.LabelUtil;

/**
 * Panel for "Embed"
 */
public class EmbedPanel extends JPanel {
    private static final long serialVersionUID = 5812035848879719995L;

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    private JPanel optionPanel;
    private JTextField msgFileTextField;
    private JButton msgFileButton;
    private JTextField coverFileTextField;
    private JButton coverFileButton;
    private JTextField stegoFileTextField;
    private JButton stegoFileButton;
    private JComboBox<String> encryptionAlgoComboBox;
    private JPasswordField passwordTextField;
    private JPasswordField confPasswordTextField;
    private JButton runEmbedButton;

    /**
     * Default constructor
     */
    public EmbedPanel() {
        super();
        initialize();
    }

    /**
     * Getter method for optionPanel
     *
     * @return optionPanel
     */
    public JPanel getOptionPanel() {
        if (this.optionPanel == null) {
            JLabel label;
            this.optionPanel = new JPanel();
            this.optionPanel.setBorder(new TitledBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5, 5)), new EtchedBorder()),
                    " " + labelUtil.getString("gui.label.dhEmbed.option.title") + " "));
            this.optionPanel.setLayout(new GridBagLayout());

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            label = new JLabel(labelUtil.getString("gui.label.dhEmbed.option.cryptalgo"));
            label.setLabelFor(getEncryptionAlgoComboBox());
            this.optionPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new Insets(5, 5, 5, 30);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            this.optionPanel.add(getEncryptionAlgoComboBox(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            label = new JLabel(labelUtil.getString("gui.label.dhEmbed.option.password"));
            label.setLabelFor(getPasswordTextField());
            this.optionPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            this.optionPanel.add(getPasswordTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            label = new JLabel(labelUtil.getString("gui.label.dhEmbed.option.confPassword"));
            label.setLabelFor(getConfPasswordTextField());
            this.optionPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            this.optionPanel.add(getConfPasswordTextField(), gridBagConstraints);
        }
        return this.optionPanel;
    }

    /**
     * Get method for "Message File" text field
     *
     * @return msgFileTextField
     */
    public JTextField getMsgFileTextField() {
        if (this.msgFileTextField == null) {
            this.msgFileTextField = new JTextField();
            this.msgFileTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.msgFileTextField;
    }

    /**
     * Get method for "Message File" browse file button
     *
     * @return msgFileButton
     */
    public JButton getMsgFileButton() {
        if (this.msgFileButton == null) {
            this.msgFileButton = new JButton();
            this.msgFileButton.setText("...");
        }
        return this.msgFileButton;
    }

    /**
     * Get method for "Cover File" text field
     *
     * @return coverFileTextField
     */
    public JTextField getCoverFileTextField() {
        if (this.coverFileTextField == null) {
            this.coverFileTextField = new JTextField();
            this.coverFileTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.coverFileTextField;
    }

    /**
     * Get method for "Cover File" browse file button
     *
     * @return coverFileButton
     */
    public JButton getCoverFileButton() {
        if (this.coverFileButton == null) {
            this.coverFileButton = new JButton();
            this.coverFileButton.setText("...");
        }
        return this.coverFileButton;
    }

    /**
     * Get method for "Stego File" text field
     *
     * @return stegoFileTextField
     */
    public JTextField getStegoFileTextField() {
        if (this.stegoFileTextField == null) {
            this.stegoFileTextField = new JTextField();
            this.stegoFileTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.stegoFileTextField;
    }

    /**
     * Get method for "Stego File" browse file button
     *
     * @return stegoFileButton
     */
    public JButton getStegoFileButton() {
        if (this.stegoFileButton == null) {
            this.stegoFileButton = new JButton();
            this.stegoFileButton.setText("...");
        }
        return this.stegoFileButton;
    }

    /**
     * Get method for "Encryption Algorithm" combo box
     *
     * @return encryptionAlgoComboBox
     */
    public JComboBox<String> getEncryptionAlgoComboBox() {
        if (this.encryptionAlgoComboBox == null) {
            this.encryptionAlgoComboBox = new JComboBox<String>(new String[] { OpenStegoCrypto.ALGO_AES128, OpenStegoCrypto.ALGO_AES256 });
        }
        return this.encryptionAlgoComboBox;
    }

    /**
     * Get method for "Password" text field
     *
     * @return passwordTextField
     */
    public JPasswordField getPasswordTextField() {
        if (this.passwordTextField == null) {
            this.passwordTextField = new JPasswordField();
            this.passwordTextField.setColumns(OpenStegoFrame.PWD_FIELD_SIZE);
        }
        return this.passwordTextField;
    }

    /**
     * Get method for "Confirm Password" text field
     *
     * @return confPasswordTextField
     */
    public JPasswordField getConfPasswordTextField() {
        if (this.confPasswordTextField == null) {
            this.confPasswordTextField = new JPasswordField();
            this.confPasswordTextField.setColumns(OpenStegoFrame.PWD_FIELD_SIZE);
        }
        return this.confPasswordTextField;
    }

    /**
     * Get method for Embed "OK" button
     *
     * @return runEmbedButton
     */
    public JButton getRunEmbedButton() {
        if (this.runEmbedButton == null) {
            this.runEmbedButton = new JButton();
            this.runEmbedButton.setText(labelUtil.getString("gui.button.dhEmbed.run"));
        }
        return this.runEmbedButton;
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
        label = new JLabel(labelUtil.getString("gui.label.dhEmbed.msgFile"));
        label.setLabelFor(getMsgFileTextField());
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.dhEmbed.coverFile"));
        label.setLabelFor(getCoverFileTextField());
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.dhEmbed.stegoFile"));
        label.setLabelFor(getStegoFileTextField());
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.dhEmbed.coverFileMsg"));
        label.setFont(label.getFont().deriveFont(Font.ITALIC));
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getMsgFileTextField(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getCoverFileTextField(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getStegoFileTextField(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        add(getMsgFileButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        add(getCoverFileButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        add(getStegoFileButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getOptionPanel(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getRunEmbedButton(), gridBagConstraints);

        // Dummy padding
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        this.add(new JLabel(" "), gridBagConstraints);
    }
}
