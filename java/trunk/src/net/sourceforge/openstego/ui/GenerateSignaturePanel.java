/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2011-2014 Samir Vaidya
 */
package net.sourceforge.openstego.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.util.LabelUtil;

/**
 * Panel for "Generate Signature"
 */
public class GenerateSignaturePanel extends JPanel
{
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    private JTextField inputKeyTextField;
    private JTextField signatureFileTextField;
    private JButton signatureFileButton;
    private JButton runGenSigButton;

    /**
     * Default constructor
     */
    public GenerateSignaturePanel()
    {
        super();
        initialize();
    }

    /**
     * Get method for "Input Key" text field
     * 
     * @return inputKeyTextField
     */
    public JTextField getInputKeyTextField()
    {
        if(this.inputKeyTextField == null)
        {
            this.inputKeyTextField = new JTextField();
        }
        return this.inputKeyTextField;
    }

    /**
     * Getter method for signatureFileTextField
     * 
     * @return signatureFileTextField
     */
    public JTextField getSignatureFileTextField()
    {
        if(this.signatureFileTextField == null)
        {
            this.signatureFileTextField = new JTextField();
            this.signatureFileTextField.setColumns(57);
        }
        return this.signatureFileTextField;
    }

    /**
     * Getter method for signatureFileButton
     * 
     * @return signatureFileButton
     */
    public JButton getSignatureFileButton()
    {
        if(this.signatureFileButton == null)
        {
            this.signatureFileButton = new JButton();
            this.signatureFileButton.setText("...");
            this.signatureFileButton.setPreferredSize(new Dimension(22, 22));
        }
        return this.signatureFileButton;
    }

    /**
     * Get method for GenSig "OK" button
     * 
     * @return runGenSigButton
     */
    public JButton getRunGenSigButton()
    {
        if(this.runGenSigButton == null)
        {
            this.runGenSigButton = new JButton();
            this.runGenSigButton.setText(labelUtil.getString("gui.button.wmGenSig.run"));
        }
        return this.runGenSigButton;
    }

    private void initialize()
    {
        JLabel label;
        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.wmGenSig.inputKey"));
        label.setLabelFor(getInputKeyTextField());
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weighty = 0.0;
        add(getInputKeyTextField(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.weighty = 0.0;
        label = new JLabel(labelUtil.getString("gui.label.wmGenSig.sigFile"));
        label.setLabelFor(getSignatureFileTextField());
        add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getSignatureFileTextField(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        add(getSignatureFileButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.weighty = 0.0;
        add(getRunGenSigButton(), gridBagConstraints);

        // Dummy padding
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.weighty = 1.0;
        add(new JLabel(" "), gridBagConstraints);
    }
}
