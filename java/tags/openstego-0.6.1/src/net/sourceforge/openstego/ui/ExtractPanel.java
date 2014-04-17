/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2011-2014 Samir Vaidya
 */
package net.sourceforge.openstego.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.util.LabelUtil;

/**
 * Panel for "Extract"
 */
public class ExtractPanel extends JPanel
{
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    private JTextField inputStegoFileTextField;
    private JButton inputStegoFileButton;
    private JTextField outputFolderTextField;
    private JButton outputFolderButton;
    private JPasswordField extractPwdTextField;
    private JButton runExtractButton;
    private JPanel extractPwdPanel;

    /**
     * Default constructor
     */
    public ExtractPanel()
    {
        super();
        initialize();
    }

    /**
     * Get method for "Input Stego File" text field
     * 
     * @return inputStegoFileTextField
     */
    public JTextField getInputStegoFileTextField()
    {
        if(this.inputStegoFileTextField == null)
        {
            this.inputStegoFileTextField = new JTextField();
            this.inputStegoFileTextField.setColumns(57);
        }
        return this.inputStegoFileTextField;
    }

    /**
     * Get method for "Input Stego File" browse file button
     * 
     * @return inputStegoFileButton
     */
    public JButton getInputStegoFileButton()
    {
        if(this.inputStegoFileButton == null)
        {
            this.inputStegoFileButton = new JButton();
            this.inputStegoFileButton.setText("...");
            this.inputStegoFileButton.setPreferredSize(new Dimension(22, 22));
        }
        return this.inputStegoFileButton;
    }

    /**
     * Get method for "Output Folder" text field
     * 
     * @return outputFolderTextField
     */
    public JTextField getOutputFolderTextField()
    {
        if(this.outputFolderTextField == null)
        {
            this.outputFolderTextField = new JTextField();
            this.outputFolderTextField.setColumns(57);
        }
        return this.outputFolderTextField;
    }

    /**
     * Get method for "Output Folder" browse file button
     * 
     * @return outputFolderButton
     */
    public JButton getOutputFolderButton()
    {
        if(this.outputFolderButton == null)
        {
            this.outputFolderButton = new JButton();
            this.outputFolderButton.setText("...");
            this.outputFolderButton.setPreferredSize(new Dimension(22, 22));
        }
        return this.outputFolderButton;
    }

    /**
     * Get method for "Password for Extract" text field
     * 
     * @return extractPwdTextField
     */
    public JPasswordField getExtractPwdTextField()
    {
        if(this.extractPwdTextField == null)
        {
            this.extractPwdTextField = new JPasswordField();
            this.extractPwdTextField.setColumns(20);
        }
        return this.extractPwdTextField;
    }

    /**
     * Get method for Extract "OK" button
     * 
     * @return runExtractButton
     */
    public JButton getRunExtractButton()
    {
        if(this.runExtractButton == null)
        {
            this.runExtractButton = new JButton();
            this.runExtractButton.setText(labelUtil.getString("gui.button.dhExtract.run"));
        }
        return this.runExtractButton;
    }

    /**
     * Getter method for extractPwdPanel
     * 
     * @return extractPwdPanel
     */
    public JPanel getExtractPwdPanel()
    {
        if(this.extractPwdPanel == null)
        {
            this.extractPwdPanel = new JPanel();
            ((FlowLayout) this.extractPwdPanel.getLayout()).setAlignment(FlowLayout.LEFT);
            this.extractPwdPanel.add(new JLabel(labelUtil.getString("gui.label.dhEmbed.option.password")));
            this.extractPwdPanel.add(getExtractPwdTextField());
        }
        return this.extractPwdPanel;
    }

    private void initialize()
    {
        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(new JLabel(labelUtil.getString("gui.label.dhExtract.stegoFile")), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(new JLabel(labelUtil.getString("gui.label.dhExtract.outputDir")), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getInputStegoFileTextField(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getOutputFolderTextField(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(20, 5, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getExtractPwdPanel(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 0.0;
        add(getInputStegoFileButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 0.0;
        add(getOutputFolderButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 0.0;
        add(getRunExtractButton(), gridBagConstraints);

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
