/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego.ui;

import net.sourceforge.openstego.util.LabelUtil;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * Frame class to build the Swing UI for OpenStego. This class includes only graphics rendering
 * code. Listeners are implemented in {@link net.sourceforge.openstego.ui.OpenStegoUI} class.
 */
public class OpenStegoFrame extends JFrame
{
    /**
     * "Source Data" text field
     */
    protected JTextField srcDataTextField = new JTextField();

    /**
     * "Source Data" browse file button
     */
    protected JButton srcDataFileButton = new JButton();

    /**
     * "Source Image" text field
     */
    protected JTextField srcImageTextField = new JTextField();

    /**
     * "Source Image" browse file button
     */
    protected JButton srcImgFileButton = new JButton();

    /**
     * "Target Image" text field
     */
    protected JTextField tgtImageTextField = new JTextField();

    /**
     * "Target Image" browse file button
     */
    protected JButton tgtImgFileButton = new JButton();

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
     * "Image for Extract" text field
     */
    protected JTextField imgForExtractTextField = new JTextField();

    /**
     * "Image for Extract" browse file button
     */
    protected JButton imgForExtractFileButton = new JButton();

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
        label = new JLabel(LabelUtil.getString("gui.label.sourceDataFile"));
        label.setLabelFor(srcDataTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 2;
        label = new JLabel(LabelUtil.getString("gui.label.sourceImgFile"));
        label.setLabelFor(srcImageTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 4;
        label = new JLabel(LabelUtil.getString("gui.label.outputImgFile"));
        label.setLabelFor(tgtImageTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 5, 5);

        srcDataTextField.setColumns(50);
        gridBagConstraints.gridy = 1;
        embedPanel.add(srcDataTextField, gridBagConstraints);

        srcImageTextField.setColumns(50);
        gridBagConstraints.gridy = 3;
        embedPanel.add(srcImageTextField, gridBagConstraints);

        tgtImageTextField.setColumns(50);
        gridBagConstraints.gridy = 5;
        embedPanel.add(tgtImageTextField, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);

        srcDataFileButton.setText("...");
        srcDataFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 1;
        embedPanel.add(srcDataFileButton, gridBagConstraints);

        srcImgFileButton.setText("...");
        srcImgFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 3;
        embedPanel.add(srcImgFileButton, gridBagConstraints);

        tgtImgFileButton.setText("...");
        tgtImgFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 5;
        embedPanel.add(tgtImgFileButton, gridBagConstraints);

        optionPanel.setBorder(new TitledBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5, 5)), new EtchedBorder()),
            " " + LabelUtil.getString("gui.label.option.title") + " "));
        optionPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        gridBagConstraints.gridy = 0;
        label = new JLabel(LabelUtil.getString("gui.label.option.maxBitsPerChannel"));
        label.setLabelFor(maxBitsComboBox);
        optionPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        label = new JLabel(LabelUtil.getString("gui.label.option.useCompression"));
        label.setLabelFor(useCompCheckBox);
        optionPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 2;
        label = new JLabel(LabelUtil.getString("gui.label.option.useEncryption"));
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

        passwordPanel.setBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5, 5)), new EtchedBorder()));
        passwordPanel.setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        gridBagConstraints.gridx = 0;
        label = new JLabel(LabelUtil.getString("gui.label.option.password"));
        label.setLabelFor(passwordTextField);
        passwordPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        passwordTextField.setColumns(20);
        passwordPanel.add(passwordTextField, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        label = new JLabel(LabelUtil.getString("gui.label.option.confPassword"));
        label.setLabelFor(confPasswordTextField);
        passwordPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridx = 3;
        confPasswordTextField.setColumns(20);
        passwordPanel.add(confPasswordTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
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
        extractPanel.add(new JLabel(LabelUtil.getString("gui.label.imgForExtractFile")), gridBagConstraints);

        gridBagConstraints.gridy = 2;
        extractPanel.add(new JLabel(LabelUtil.getString("gui.label.outputDataFolder")), gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 5, 5);

        gridBagConstraints.gridy = 1;
        imgForExtractTextField.setColumns(50);
        extractPanel.add(imgForExtractTextField, gridBagConstraints);

        gridBagConstraints.gridy = 3;
        outputFolderTextField.setColumns(50);
        extractPanel.add(outputFolderTextField, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);

        gridBagConstraints.gridy = 1;
        imgForExtractFileButton.setText("...");
        imgForExtractFileButton.setPreferredSize(new Dimension(22, 22));
        extractPanel.add(imgForExtractFileButton, gridBagConstraints);

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

        mainTabbedPane.addTab(LabelUtil.getString("gui.label.tab.embed"), embedPanel);
        mainTabbedPane.addTab(LabelUtil.getString("gui.label.tab.extract"), extractPanel);

        mainPanel.add(mainTabbedPane);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(new Insets(0, 5, 5, 5)));

        okButton.setText(LabelUtil.getString("gui.button.ok"));
        buttonPanel.add(okButton);

        cancelButton.setText(LabelUtil.getString("gui.button.cancel"));
        buttonPanel.add(cancelButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setTitle(LabelUtil.getString("gui.window.title"));
        pack();
    }

    /**
     * Method to set the action commands for interactive UI items
     */
    private void setActionCommands()
    {
        srcDataFileButton.setActionCommand("BROWSE_SRC_DATA");
        srcImgFileButton.setActionCommand("BROWSE_SRC_IMG");
        tgtImgFileButton.setActionCommand("BROWSE_TGT_IMG");

        imgForExtractFileButton.setActionCommand("BROWSE_IMG_FOR_EXTRACT");
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
}
