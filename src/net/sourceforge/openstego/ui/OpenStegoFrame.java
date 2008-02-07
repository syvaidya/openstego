/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.util.*;

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
     * Combobox for "Stego Algorithm" for "Embed"
     */
    protected JComboBox embedAlgoComboBox = new JComboBox();

    /**
     * Combobox for "Stego Algorithm" for "Extract"
     */
    protected JComboBox extractAlgoComboBox = new JComboBox();

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
     * Panel for plugin specific options for "Embed" action
     */
    protected JPanel pluginEmbedOptionsPanel = new JPanel();

    /**
     * Password panel handle (for enable/disable)
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
     * Get method for "Message File" text field
     */
    public JTextField getMsgFileTextField()
    {
        return msgFileTextField;
    }

    /**
     * Get method for "Message File" browse file button
     */
    public JButton getMsgFileButton()
    {
        return msgFileButton;
    }

    /**
     * Get method for "Cover File" text field
     */
    public JTextField getCoverFileTextField()
    {
        return coverFileTextField;
    }

    /**
     * Get method for "Cover File" browse file button
     */
    public JButton getCoverFileButton()
    {
        return coverFileButton;
    }

    /**
     * Get method for "Stego File" text field
     */
    public JTextField getStegoFileTextField()
    {
        return stegoFileTextField;
    }

    /**
     * Get method for "Stego File" browse file button
     */
    public JButton getStegoFileButton()
    {
        return stegoFileButton;
    }

    /**
     * Get method for Checkbox for "Use Compression"
     */
    public JCheckBox getUseCompCheckBox()
    {
        return useCompCheckBox;
    }

    /**
     * Get method for Checkbox for "Use Encryption"
     */
    public JCheckBox getUseEncryptCheckBox()
    {
        return useEncryptCheckBox;
    }

    /**
     * Get method for "Password" text field
     */
    public JPasswordField getPasswordTextField()
    {
        return passwordTextField;
    }

    /**
     * Get method for "Confirm Password" text field
     */
    public JPasswordField getConfPasswordTextField()
    {
        return confPasswordTextField;
    }

    /**
     * Get method for "Input Stego File" text field
     */
    public JTextField getInputStegoFileTextField()
    {
        return inputStegoFileTextField;
    }

    /**
     * Get method for "Input Stego File" browse file button
     */
    public JButton getInputStegoFileButton()
    {
        return inputStegoFileButton;
    }

    /**
     * Get method for "Output Folder" text field
     */
    public JTextField getOutputFolderTextField()
    {
        return outputFolderTextField;
    }

    /**
     * Get method for "Output Folder" browse file button
     */
    public JButton getOutputFolderButton()
    {
        return outputFolderButton;
    }

    /**
     * Get method for "OK" button
     */
    public JButton getOkButton()
    {
        return okButton;
    }

    /**
     * Get method for "Cancel" button
     */
    public JButton getCancelButton()
    {
        return cancelButton;
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
        JPanel embedAlgoPanel = new JPanel();
        JPanel extractAlgoPanel = new JPanel();
        JPanel optionPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        JLabel label = null;

        mainPanel.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
        embedPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        embedAlgoPanel.add(new JLabel(labelUtil.getString("gui.label.algorithmList")));
        embedAlgoPanel.add(embedAlgoComboBox);

        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        gridBagConstraints.gridy = 0;
        embedPanel.add(embedAlgoPanel, gridBagConstraints);

        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.gridy = 1;
        label = new JLabel(labelUtil.getString("gui.label.msgFile"));
        label.setLabelFor(msgFileTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 3;
        label = new JLabel(labelUtil.getString("gui.label.coverFile"));
        label.setLabelFor(coverFileTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 5;
        label = new JLabel(labelUtil.getString("gui.label.outputStegoFile"));
        label.setLabelFor(stegoFileTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 5, 5);

        msgFileTextField.setColumns(57);
        gridBagConstraints.gridy = 2;
        embedPanel.add(msgFileTextField, gridBagConstraints);

        coverFileTextField.setColumns(57);
        gridBagConstraints.gridy = 4;
        embedPanel.add(coverFileTextField, gridBagConstraints);

        stegoFileTextField.setColumns(57);
        gridBagConstraints.gridy = 6;
        embedPanel.add(stegoFileTextField, gridBagConstraints);


        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);

        msgFileButton.setText("...");
        msgFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 2;
        embedPanel.add(msgFileButton, gridBagConstraints);

        coverFileButton.setText("...");
        coverFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 4;
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
        label = new JLabel(labelUtil.getString("gui.label.option.useCompression"));
        label.setLabelFor(useCompCheckBox);
        optionPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        label = new JLabel(labelUtil.getString("gui.label.option.useEncryption"));
        label.setLabelFor(useEncryptCheckBox);
        optionPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 10, 5, 5);
        optionPanel.add(useCompCheckBox, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        optionPanel.add(useEncryptCheckBox, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
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

        pluginEmbedOptionsPanel.setBorder(new TitledBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5, 5)),
            new EtchedBorder()), " " + labelUtil.getString("gui.label.pluginOption.title") + " "));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        embedPanel.add(pluginEmbedOptionsPanel, gridBagConstraints);

        extractPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;

        extractAlgoPanel.add(new JLabel(labelUtil.getString("gui.label.algorithmList")));
        extractAlgoPanel.add(extractAlgoComboBox);

        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        gridBagConstraints.gridy = 0;
        extractPanel.add(extractAlgoPanel, gridBagConstraints);

        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.gridy = 1;
        extractPanel.add(new JLabel(labelUtil.getString("gui.label.inputStegoFile")), gridBagConstraints);

        gridBagConstraints.gridy = 3;
        extractPanel.add(new JLabel(labelUtil.getString("gui.label.outputDataFolder")), gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 5, 5);

        gridBagConstraints.gridy = 2;
        inputStegoFileTextField.setColumns(57);
        extractPanel.add(inputStegoFileTextField, gridBagConstraints);

        gridBagConstraints.gridy = 4;
        outputFolderTextField.setColumns(57);
        extractPanel.add(outputFolderTextField, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);

        gridBagConstraints.gridy = 2;
        inputStegoFileButton.setText("...");
        inputStegoFileButton.setPreferredSize(new Dimension(22, 22));
        extractPanel.add(inputStegoFileButton, gridBagConstraints);

        gridBagConstraints.gridy = 4;
        outputFolderButton.setText("...");
        outputFolderButton.setPreferredSize(new Dimension(22, 22));
        extractPanel.add(outputFolderButton, gridBagConstraints);

        // Dummy padding
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
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

        ActionListener actionListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    embedAlgoChanged();
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        };
        embedAlgoComboBox.addActionListener(actionListener);
    }

    /**
     * Method to handle change event for 'useEncryption'
     */
    private void useEncryptionChanged()
    {
        if(useEncryptCheckBox.isSelected())
        {
            CommonUtil.setEnabled(passwordTextField, true);
            CommonUtil.setEnabled(confPasswordTextField, true);
            passwordTextField.requestFocus();
        }
        else
        {
            CommonUtil.setEnabled(passwordTextField, false);
            CommonUtil.setEnabled(confPasswordTextField, false);
        }
    }

    /**
     * Method to handle change event for 'embedAlgoComboBox'
     * @throws OpenStegoException
     */
    protected void embedAlgoChanged() throws OpenStegoException
    {
    }
}
