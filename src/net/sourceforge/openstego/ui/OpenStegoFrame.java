/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.plugin.lsb.LSBPlugin;
import net.sourceforge.openstego.util.LabelUtil;

/**
 * Frame class to build the Swing UI for OpenStego. This class includes only graphics rendering
 * code. Listeners are implemented in {@link net.sourceforge.openstego.ui.OpenStegoUI} class.
 */
public class OpenStegoFrame extends JFrame
{
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    /**
     * "Message File" text field
     */
    protected JTextField msgFileTextField = new JTextField();

    /**
     * "Message File" browse file button
     */
    protected JButton msgFileButton = new JButton();

    /**
     * "Cover File" text field
     */
    protected JTextField coverFileTextField = new JTextField();

    /**
     * "Cover File" browse file button
     */
    protected JButton coverFileButton = new JButton();

    /**
     * "Random Image as Source" checkbox
     */
    protected JCheckBox randomImgCheckBox = null;

    /**
     * "Stego File" text field
     */
    protected JTextField stegoFileTextField = new JTextField();

    /**
     * "Stego File" browse file button
     */
    protected JButton stegoFileButton = new JButton();

    /**
     * Checkbox for "Use Compression"
     */
    protected JCheckBox useCompCheckBox = new JCheckBox();

    /**
     * Combobox for "Max Bits Per Color Channel"
     */
    protected JComboBox maxBitsComboBox = null;

    /**
     * Checkbox for "Use Encryption"
     */
    protected JCheckBox useEncryptCheckBox = new JCheckBox();

    /**
     * "Password" text field
     */
    protected JPasswordField passwordTextField = new JPasswordField();

    /**
     * "Confirm Password" text field
     */
    protected JPasswordField confPasswordTextField = new JPasswordField();

    /**
     * "Input Stego File" text field
     */
    protected JTextField inputStegoFileTextField = new JTextField();

    /**
     * "Input Stego File" browse file button
     */
    protected JButton inputStegoFileButton = new JButton();

    /**
     * "Output Folder" text field
     */
    protected JTextField outputFolderTextField = new JTextField();

    /**
     * "Output Folder" browse file button
     */
    protected JButton outputFolderButton = new JButton();

    /**
     * "OK" button
     */
    protected JButton okButton = new JButton();

    /**
     * "Cancel" button
     */
    protected JButton cancelButton = new JButton();

    /**
     * Tabbed pane for embed/extract tabs
     */
    protected JTabbedPane mainTabbedPane = new JTabbedPane();

    /**
     * Password panel handle (for show/hide)
     */
    private JPanel passwordPanel = new JPanel();

    /**
     * Default constructor
     */
    public OpenStegoFrame()
    {
        initComponents();
        setActionCommands();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents()
    {
        GridBagConstraints gridBagConstraints = null;
        JPanel mainPanel = new JPanel();
        JPanel embedPanel = new JPanel();
        JPanel extractPanel = new JPanel();
        JPanel optionPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        JLabel label = null;
        Object[] maxBitsList = new Object[8];

        mainPanel.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
        embedPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);

        gridBagConstraints.gridy = 0;
        label = new JLabel(labelUtil.getString("gui.label.msgFile"));
        label.setLabelFor(msgFileTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 2;
        label = new JLabel(labelUtil.getString("gui.label.coverFile"));
        label.setLabelFor(coverFileTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 5;
        label = new JLabel(labelUtil.getString("gui.label.outputStegoFile"));
        label.setLabelFor(stegoFileTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 5, 5);

        msgFileTextField.setColumns(57);
        gridBagConstraints.gridy = 1;
        embedPanel.add(msgFileTextField, gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        coverFileTextField.setColumns(57);
        gridBagConstraints.gridy = 3;
        embedPanel.add(coverFileTextField, gridBagConstraints);

        stegoFileTextField.setColumns(57);
        gridBagConstraints.gridy = 6;
        embedPanel.add(stegoFileTextField, gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        randomImgCheckBox = new JCheckBox(LabelUtil.getInstance(LSBPlugin.NAMESPACE).getString("gui.label.option.useRandomImage"));
        gridBagConstraints.gridy = 4;
        embedPanel.add(randomImgCheckBox, gridBagConstraints);


        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);

        msgFileButton.setText("...");
        msgFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 1;
        embedPanel.add(msgFileButton, gridBagConstraints);

        coverFileButton.setText("...");
        coverFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 3;
        embedPanel.add(coverFileButton, gridBagConstraints);

        stegoFileButton.setText("...");
        stegoFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 6;
        embedPanel.add(stegoFileButton, gridBagConstraints);

        optionPanel.setBorder(new TitledBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5, 5)),
            new EtchedBorder()), " " + labelUtil.getString("gui.label.option.title") + " "));
        optionPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        gridBagConstraints.gridy = 0;
        label = new JLabel(LabelUtil.getInstance(LSBPlugin.NAMESPACE).getString("gui.label.option.maxBitsPerChannel"));
        label.setLabelFor(maxBitsComboBox);
        optionPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        label = new JLabel(labelUtil.getString("gui.label.option.useCompression"));
        label.setLabelFor(useCompCheckBox);
        optionPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 2;
        label = new JLabel(labelUtil.getString("gui.label.option.useEncryption"));
        label.setLabelFor(useEncryptCheckBox);
        optionPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;

        for(int i = 0; i < 8; i++) maxBitsList[i] = new Integer(i + 1);
        maxBitsComboBox = new JComboBox(maxBitsList);
        maxBitsComboBox.setPreferredSize(new Dimension(40, 20));

        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 10, 5, 5);
        optionPanel.add(maxBitsComboBox, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        optionPanel.add(useCompCheckBox, gridBagConstraints);

        gridBagConstraints.gridy = 2;
        optionPanel.add(useEncryptCheckBox, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        optionPanel.add(passwordPanel, gridBagConstraints);

        passwordPanel.setBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5, 5)),
                new EtchedBorder()));
        passwordPanel.setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        gridBagConstraints.gridx = 0;
        label = new JLabel(labelUtil.getString("gui.label.option.password"));
        label.setLabelFor(passwordTextField);
        passwordPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        passwordTextField.setColumns(15);
        passwordPanel.add(passwordTextField, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        label = new JLabel(labelUtil.getString("gui.label.option.confPassword"));
        label.setLabelFor(confPasswordTextField);
        passwordPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridx = 3;
        confPasswordTextField.setColumns(15);
        passwordPanel.add(confPasswordTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        embedPanel.add(optionPanel, gridBagConstraints);

        extractPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);

        gridBagConstraints.gridy = 0;
        extractPanel.add(new JLabel(labelUtil.getString("gui.label.inputStegoFile")), gridBagConstraints);

        gridBagConstraints.gridy = 2;
        extractPanel.add(new JLabel(labelUtil.getString("gui.label.outputDataFolder")), gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 5, 5);

        gridBagConstraints.gridy = 1;
        inputStegoFileTextField.setColumns(57);
        extractPanel.add(inputStegoFileTextField, gridBagConstraints);

        gridBagConstraints.gridy = 3;
        outputFolderTextField.setColumns(57);
        extractPanel.add(outputFolderTextField, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);

        gridBagConstraints.gridy = 1;
        inputStegoFileButton.setText("...");
        inputStegoFileButton.setPreferredSize(new Dimension(22, 22));
        extractPanel.add(inputStegoFileButton, gridBagConstraints);

        gridBagConstraints.gridy = 3;
        outputFolderButton.setText("...");
        outputFolderButton.setPreferredSize(new Dimension(22, 22));
        extractPanel.add(outputFolderButton, gridBagConstraints);

        // Dummy padding
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        extractPanel.add(new JLabel(" "), gridBagConstraints);

        mainTabbedPane.addTab(labelUtil.getString("gui.label.tab.embed"), new ImageIcon(getClass().getResource(
                "/image/EmbedIcon.png")), embedPanel);
        mainTabbedPane.addTab(labelUtil.getString("gui.label.tab.extract"), new ImageIcon(getClass().getResource(
                "/image/ExtractIcon.png")), extractPanel);

        mainPanel.add(mainTabbedPane);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(new Insets(0, 5, 5, 5)));

        okButton.setText(labelUtil.getString("gui.button.ok"));
        buttonPanel.add(okButton);

        cancelButton.setText(labelUtil.getString("gui.button.cancel"));
        buttonPanel.add(cancelButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setTitle(labelUtil.getString("gui.window.title"));
        pack();
    }

    /**
     * Method to set the action commands for interactive UI items
     */
    private void setActionCommands()
    {
        msgFileButton.setActionCommand("BROWSE_SRC_DATA");
        coverFileButton.setActionCommand("BROWSE_SRC_IMG");
        stegoFileButton.setActionCommand("BROWSE_TGT_IMG");

        inputStegoFileButton.setActionCommand("BROWSE_IMG_FOR_EXTRACT");
        outputFolderButton.setActionCommand("BROWSE_TGT_DATA");

        okButton.setActionCommand("OK");
        cancelButton.setActionCommand("CANCEL");

        ChangeListener changeListener = new ChangeListener()
        {
            public void stateChanged(ChangeEvent changeEvent)
            {
                useEncryptionChanged();
            }
        };
        useEncryptCheckBox.addChangeListener(changeListener);
        useEncryptionChanged();

        changeListener = new ChangeListener()
        {
            public void stateChanged(ChangeEvent changeEvent)
            {
                useRandomImgChanged();
            }
        };
        randomImgCheckBox.addChangeListener(changeListener);
        useRandomImgChanged();
    }

    /**
     * Method to handle change event for 'useEncryption'
     */
    private void useEncryptionChanged()
    {
        if(useEncryptCheckBox.isSelected())
        {
            passwordTextField.setEnabled(true);
            passwordTextField.setBackground(Color.WHITE);
            confPasswordTextField.setEnabled(true);
            confPasswordTextField.setBackground(Color.WHITE);
            passwordTextField.requestFocus();
        }
        else
        {
            passwordTextField.setEnabled(false);
            passwordTextField.setBackground(UIManager.getColor("Panel.background"));
            confPasswordTextField.setEnabled(false);
            confPasswordTextField.setBackground(UIManager.getColor("Panel.background"));
        }
    }

    /**
     * Method to handle change event for 'randomImage'
     */
    private void useRandomImgChanged()
    {
        if(randomImgCheckBox.isSelected())
        {
            coverFileTextField.setEnabled(false);
            coverFileTextField.setBackground(UIManager.getColor("Panel.background"));
            coverFileButton.setEnabled(false);
        }
        else
        {
            coverFileTextField.setEnabled(true);
            coverFileTextField.setBackground(Color.WHITE);
            coverFileButton.setEnabled(true);
            coverFileTextField.requestFocus();
        }
    }
}
