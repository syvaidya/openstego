/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2011-2014 Samir Vaidya
 */
package net.sourceforge.openstego.ui;

import java.awt.Dimension;
import java.awt.Font;
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
 * Panel for "Embed Watermark"
 */
public class EmbedWatermarkPanel extends JPanel
{
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
    public EmbedWatermarkPanel()
    {
        super();
        initialize();
    }

    /**
     * Getter method for fileForWmTextField
     * 
     * @return fileForWmTextField
     */
    public JTextField getFileForWmTextField()
    {
        if(this.fileForWmTextField == null)
        {
            this.fileForWmTextField = new JTextField();
            this.fileForWmTextField.setColumns(57);
        }
        return this.fileForWmTextField;
    }

    /**
     * Getter method for fileForWmButton
     * 
     * @return fileForWmButton
     */
    public JButton getFileForWmButton()
    {
        if(this.fileForWmButton == null)
        {
            this.fileForWmButton = new JButton();
            this.fileForWmButton.setText("...");
            this.fileForWmButton.setPreferredSize(new Dimension(22, 22));
        }
        return this.fileForWmButton;
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
     * Getter method for outputWmFileTextField
     * 
     * @return outputWmFileTextField
     */
    public JTextField getOutputWmFileTextField()
    {
        if(this.outputWmFileTextField == null)
        {
            this.outputWmFileTextField = new JTextField();
            this.outputWmFileTextField.setColumns(57);
        }
        return this.outputWmFileTextField;
    }

    /**
     * Getter method for outputWmFileButton
     * 
     * @return outputWmFileButton
     */
    public JButton getOutputWmFileButton()
    {
        if(this.outputWmFileButton == null)
        {
            this.outputWmFileButton = new JButton();
            this.outputWmFileButton.setText("...");
            this.outputWmFileButton.setPreferredSize(new Dimension(22, 22));
        }
        return this.outputWmFileButton;
    }

    /**
     * Getter method for runEmbedWmButton
     * 
     * @return runEmbedWmButton
     */
    public JButton getRunEmbedWmButton()
    {
        if(this.runEmbedWmButton == null)
        {
            this.runEmbedWmButton = new JButton();
            this.runEmbedWmButton.setText(labelUtil.getString("gui.button.wmEmbed.run"));
        }
        return this.runEmbedWmButton;
    }

    private void initialize()
    {
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
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getFileForWmButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        add(getSignatureFileButton(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 1.0;
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
