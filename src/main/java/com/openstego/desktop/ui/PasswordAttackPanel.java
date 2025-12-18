/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */
package com.openstego.desktop.ui;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.pwdattack.PasswordAttack;
import com.openstego.desktop.util.LabelUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Panel for "Password Attack"
 */
public class PasswordAttackPanel extends JPanel {
    private static final long serialVersionUID = 8641250538698716006L;

    /**
     * LabelUtil instance to retrieve labels
     */
    private static final LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    private JTextField stegoFileTextField;
    private JButton stegoFileButton;
    private JTextField outputFolderTextField;
    private JButton outputFolderButton;
    private JComboBox<String> attackModeComboBox;
    private JTextField wordlistTextField;
    private JButton wordlistButton;
    private JTextField wordlist2TextField;
    private JButton wordlist2Button;
    private JTextField maskTextField;
    private JTextField rulesFileTextField;
    private JButton rulesFileButton;
    private JCheckBox useDefaultRulesCheckBox;
    private JTextField custom1TextField;
    private JTextField custom2TextField;
    private JTextField custom3TextField;
    private JTextField custom4TextField;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel attemptsLabel;
    private JLabel speedLabel;
    private JLabel currentPwdLabel;
    private JButton startButton;
    private JButton stopButton;

    private JPanel wordlistPanel;
    private JPanel wordlist2Panel;
    private JPanel maskPanel;
    private JPanel rulesPanel;
    private JPanel customCharsetsPanel;

    /**
     * Default constructor
     */
    public PasswordAttackPanel() {
        super();
        initialize();
    }

    public JTextField getStegoFileTextField() {
        if (this.stegoFileTextField == null) {
            this.stegoFileTextField = new JTextField();
            this.stegoFileTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.stegoFileTextField;
    }

    public JButton getStegoFileButton() {
        if (this.stegoFileButton == null) {
            this.stegoFileButton = new JButton("...");
        }
        return this.stegoFileButton;
    }

    public JTextField getOutputFolderTextField() {
        if (this.outputFolderTextField == null) {
            this.outputFolderTextField = new JTextField();
            this.outputFolderTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.outputFolderTextField;
    }

    public JButton getOutputFolderButton() {
        if (this.outputFolderButton == null) {
            this.outputFolderButton = new JButton("...");
        }
        return this.outputFolderButton;
    }

    public JComboBox<String> getAttackModeComboBox() {
        if (this.attackModeComboBox == null) {
            this.attackModeComboBox = new JComboBox<>();
            this.attackModeComboBox.addItem(labelUtil.getString("gui.pwdAttack.mode.dictionary"));
            this.attackModeComboBox.addItem(labelUtil.getString("gui.pwdAttack.mode.mask"));
            this.attackModeComboBox.addItem(labelUtil.getString("gui.pwdAttack.mode.combinator"));
            this.attackModeComboBox.addItem(labelUtil.getString("gui.pwdAttack.mode.hybrid_wm"));
            this.attackModeComboBox.addItem(labelUtil.getString("gui.pwdAttack.mode.hybrid_mw"));
            this.attackModeComboBox.addItem(labelUtil.getString("gui.pwdAttack.mode.rulebased"));
        }
        return this.attackModeComboBox;
    }

    public JTextField getWordlistTextField() {
        if (this.wordlistTextField == null) {
            this.wordlistTextField = new JTextField();
            this.wordlistTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.wordlistTextField;
    }

    public JButton getWordlistButton() {
        if (this.wordlistButton == null) {
            this.wordlistButton = new JButton("...");
        }
        return this.wordlistButton;
    }

    public JTextField getWordlist2TextField() {
        if (this.wordlist2TextField == null) {
            this.wordlist2TextField = new JTextField();
            this.wordlist2TextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.wordlist2TextField;
    }

    public JButton getWordlist2Button() {
        if (this.wordlist2Button == null) {
            this.wordlist2Button = new JButton("...");
        }
        return this.wordlist2Button;
    }

    public JTextField getMaskTextField() {
        if (this.maskTextField == null) {
            this.maskTextField = new JTextField();
            this.maskTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
            this.maskTextField.setToolTipText(labelUtil.getString("gui.label.pwdAttack.maskHelp"));
        }
        return this.maskTextField;
    }

    public JTextField getRulesFileTextField() {
        if (this.rulesFileTextField == null) {
            this.rulesFileTextField = new JTextField();
            this.rulesFileTextField.setColumns(OpenStegoFrame.TEXTFIELD_SIZE);
        }
        return this.rulesFileTextField;
    }

    public JButton getRulesFileButton() {
        if (this.rulesFileButton == null) {
            this.rulesFileButton = new JButton("...");
        }
        return this.rulesFileButton;
    }

    public JCheckBox getUseDefaultRulesCheckBox() {
        if (this.useDefaultRulesCheckBox == null) {
            this.useDefaultRulesCheckBox = new JCheckBox(labelUtil.getString("gui.label.pwdAttack.useDefaultRules"));
            this.useDefaultRulesCheckBox.setSelected(true);
        }
        return this.useDefaultRulesCheckBox;
    }

    public JTextField getCustom1TextField() {
        if (this.custom1TextField == null) {
            this.custom1TextField = new JTextField(10);
        }
        return this.custom1TextField;
    }

    public JTextField getCustom2TextField() {
        if (this.custom2TextField == null) {
            this.custom2TextField = new JTextField(10);
        }
        return this.custom2TextField;
    }

    public JTextField getCustom3TextField() {
        if (this.custom3TextField == null) {
            this.custom3TextField = new JTextField(10);
        }
        return this.custom3TextField;
    }

    public JTextField getCustom4TextField() {
        if (this.custom4TextField == null) {
            this.custom4TextField = new JTextField(10);
        }
        return this.custom4TextField;
    }

    public JProgressBar getProgressBar() {
        if (this.progressBar == null) {
            this.progressBar = new JProgressBar(0, 100);
            this.progressBar.setStringPainted(true);
            this.progressBar.setString("0%");
        }
        return this.progressBar;
    }

    public JLabel getStatusLabel() {
        if (this.statusLabel == null) {
            this.statusLabel = new JLabel("Ready");
        }
        return this.statusLabel;
    }

    public JLabel getAttemptsLabel() {
        if (this.attemptsLabel == null) {
            this.attemptsLabel = new JLabel("0");
        }
        return this.attemptsLabel;
    }

    public JLabel getSpeedLabel() {
        if (this.speedLabel == null) {
            this.speedLabel = new JLabel("0 pwd/sec");
        }
        return this.speedLabel;
    }

    public JLabel getCurrentPwdLabel() {
        if (this.currentPwdLabel == null) {
            this.currentPwdLabel = new JLabel("-");
        }
        return this.currentPwdLabel;
    }

    public JButton getStartButton() {
        if (this.startButton == null) {
            this.startButton = new JButton(labelUtil.getString("gui.button.pwdAttack.start"));
        }
        return this.startButton;
    }

    public JButton getStopButton() {
        if (this.stopButton == null) {
            this.stopButton = new JButton(labelUtil.getString("gui.button.pwdAttack.stop"));
            this.stopButton.setEnabled(false);
        }
        return this.stopButton;
    }

    public JPanel getWordlistPanel() {
        return wordlistPanel;
    }

    public JPanel getWordlist2Panel() {
        return wordlist2Panel;
    }

    public JPanel getMaskPanel() {
        return maskPanel;
    }

    public JPanel getRulesPanel() {
        return rulesPanel;
    }

    public JPanel getCustomCharsetsPanel() {
        return customCharsetsPanel;
    }

    /**
     * Gets the selected attack mode
     *
     * @return The attack mode
     */
    public PasswordAttack.AttackMode getSelectedAttackMode() {
        int index = getAttackModeComboBox().getSelectedIndex();
        switch (index) {
            case 0:
                return PasswordAttack.AttackMode.DICTIONARY;
            case 1:
                return PasswordAttack.AttackMode.MASK;
            case 2:
                return PasswordAttack.AttackMode.COMBINATOR;
            case 3:
                return PasswordAttack.AttackMode.HYBRID_WORDLIST_MASK;
            case 4:
                return PasswordAttack.AttackMode.HYBRID_MASK_WORDLIST;
            case 5:
                return PasswordAttack.AttackMode.RULE_BASED;
            default:
                return PasswordAttack.AttackMode.DICTIONARY;
        }
    }

    /**
     * Gets the custom character sets
     *
     * @return Array of custom character sets
     */
    public String[] getCustomCharsets() {
        return new String[]{
                getCustom1TextField().getText(),
                getCustom2TextField().getText(),
                getCustom3TextField().getText(),
                getCustom4TextField().getText()
        };
    }

    /**
     * Updates the panel visibility based on selected attack mode
     */
    public void updatePanelVisibility() {
        PasswordAttack.AttackMode mode = getSelectedAttackMode();

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

        boolean needsCustomCharsets = mode == PasswordAttack.AttackMode.MASK ||
                mode == PasswordAttack.AttackMode.HYBRID_WORDLIST_MASK ||
                mode == PasswordAttack.AttackMode.HYBRID_MASK_WORDLIST;

        wordlistPanel.setVisible(needsWordlist);
        wordlist2Panel.setVisible(needsWordlist2);
        maskPanel.setVisible(needsMask);
        rulesPanel.setVisible(needsRules);
        customCharsetsPanel.setVisible(needsCustomCharsets);

        revalidate();
        repaint();
    }

    private void initialize() {
        setLayout(new GridBagLayout());
        int gridy = 0;
        GridBagConstraints gbc;

        // Stego file label
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 0, 5);
        gbc.weightx = 1.0;
        add(new JLabel(labelUtil.getString("gui.label.pwdAttack.stegoFile")), gbc);

        // Stego file text field and button
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.weightx = 1.0;
        add(getStegoFileTextField(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = gridy++;
        gbc.insets = new Insets(0, 0, 5, 5);
        add(getStegoFileButton(), gbc);

        // Output folder label
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 0, 5);
        gbc.weightx = 1.0;
        add(new JLabel(labelUtil.getString("gui.label.pwdAttack.outputDir")), gbc);

        // Output folder text field and button
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.weightx = 1.0;
        add(getOutputFolderTextField(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = gridy++;
        gbc.insets = new Insets(0, 0, 5, 5);
        add(getOutputFolderButton(), gbc);

        // Attack mode
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 0, 5);
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        modePanel.add(new JLabel(labelUtil.getString("gui.label.pwdAttack.attackMode") + ": "));
        modePanel.add(getAttackModeComboBox());
        add(modePanel, gbc);

        // Wordlist panel
        wordlistPanel = createFileInputPanel(
                labelUtil.getString("gui.label.pwdAttack.wordlist"),
                getWordlistTextField(), getWordlistButton());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 0, 5);
        gbc.weightx = 1.0;
        add(wordlistPanel, gbc);

        // Wordlist 2 panel (for combinator)
        wordlist2Panel = createFileInputPanel(
                labelUtil.getString("gui.label.pwdAttack.wordlist2"),
                getWordlist2TextField(), getWordlist2Button());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 0, 5);
        gbc.weightx = 1.0;
        add(wordlist2Panel, gbc);

        // Mask panel
        maskPanel = new JPanel(new BorderLayout(5, 0));
        maskPanel.add(new JLabel(labelUtil.getString("gui.label.pwdAttack.mask") + ": "), BorderLayout.WEST);
        maskPanel.add(getMaskTextField(), BorderLayout.CENTER);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 0, 5);
        gbc.weightx = 1.0;
        add(maskPanel, gbc);

        // Mask help label
        JLabel maskHelpLabel = new JLabel("<html><small>" + labelUtil.getString("gui.label.pwdAttack.maskHelp") + "</small></html>");
        maskHelpLabel.setForeground(Color.GRAY);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        add(maskHelpLabel, gbc);

        // Rules panel
        rulesPanel = new JPanel(new BorderLayout(5, 5));
        JPanel rulesInputPanel = createFileInputPanel(
                labelUtil.getString("gui.label.pwdAttack.rulesFile"),
                getRulesFileTextField(), getRulesFileButton());
        rulesPanel.add(rulesInputPanel, BorderLayout.CENTER);
        rulesPanel.add(getUseDefaultRulesCheckBox(), BorderLayout.SOUTH);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 0, 5);
        gbc.weightx = 1.0;
        add(rulesPanel, gbc);

        // Custom charsets panel
        customCharsetsPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        customCharsetsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), labelUtil.getString("gui.label.pwdAttack.customCharsets"),
                TitledBorder.LEFT, TitledBorder.TOP));
        customCharsetsPanel.add(new JLabel(labelUtil.getString("gui.label.pwdAttack.custom1")));
        customCharsetsPanel.add(getCustom1TextField());
        customCharsetsPanel.add(new JLabel(labelUtil.getString("gui.label.pwdAttack.custom2")));
        customCharsetsPanel.add(getCustom2TextField());
        customCharsetsPanel.add(new JLabel(labelUtil.getString("gui.label.pwdAttack.custom3")));
        customCharsetsPanel.add(getCustom3TextField());
        customCharsetsPanel.add(new JLabel(labelUtil.getString("gui.label.pwdAttack.custom4")));
        customCharsetsPanel.add(getCustom4TextField());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        gbc.weightx = 1.0;
        add(customCharsetsPanel, gbc);

        // Progress panel
        JPanel progressPanel = new JPanel(new GridBagLayout());
        progressPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), labelUtil.getString("gui.label.pwdAttack.progress"),
                TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.fill = GridBagConstraints.HORIZONTAL;
        pgbc.gridx = 0;
        pgbc.gridy = 0;
        pgbc.gridwidth = 4;
        pgbc.weightx = 1.0;
        pgbc.insets = new Insets(5, 5, 5, 5);
        progressPanel.add(getProgressBar(), pgbc);

        pgbc = new GridBagConstraints();
        pgbc.anchor = GridBagConstraints.WEST;
        pgbc.gridx = 0;
        pgbc.gridy = 1;
        pgbc.insets = new Insets(5, 5, 5, 5);
        progressPanel.add(new JLabel(labelUtil.getString("gui.label.pwdAttack.status") + ":"), pgbc);
        pgbc.gridx = 1;
        pgbc.weightx = 1.0;
        progressPanel.add(getStatusLabel(), pgbc);

        pgbc = new GridBagConstraints();
        pgbc.anchor = GridBagConstraints.WEST;
        pgbc.gridx = 2;
        pgbc.gridy = 1;
        pgbc.insets = new Insets(5, 15, 5, 5);
        progressPanel.add(new JLabel(labelUtil.getString("gui.label.pwdAttack.speed") + ":"), pgbc);
        pgbc.gridx = 3;
        pgbc.weightx = 1.0;
        progressPanel.add(getSpeedLabel(), pgbc);

        pgbc = new GridBagConstraints();
        pgbc.anchor = GridBagConstraints.WEST;
        pgbc.gridx = 0;
        pgbc.gridy = 2;
        pgbc.insets = new Insets(5, 5, 5, 5);
        progressPanel.add(new JLabel(labelUtil.getString("gui.label.pwdAttack.attempts") + ":"), pgbc);
        pgbc.gridx = 1;
        pgbc.weightx = 1.0;
        progressPanel.add(getAttemptsLabel(), pgbc);

        pgbc = new GridBagConstraints();
        pgbc.anchor = GridBagConstraints.WEST;
        pgbc.gridx = 2;
        pgbc.gridy = 2;
        pgbc.insets = new Insets(5, 15, 5, 5);
        progressPanel.add(new JLabel(labelUtil.getString("gui.label.pwdAttack.currentPwd") + ":"), pgbc);
        pgbc.gridx = 3;
        pgbc.weightx = 1.0;
        progressPanel.add(getCurrentPwdLabel(), pgbc);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        gbc.weightx = 1.0;
        add(progressPanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(getStartButton());
        buttonPanel.add(getStopButton());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(buttonPanel, gbc);

        // Dummy padding
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.weighty = 1.0;
        add(new JLabel(), gbc);

        // Set initial visibility
        updatePanelVisibility();
    }

    private JPanel createFileInputPanel(String label, JTextField textField, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(new JLabel(label + ": "), BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }
}
