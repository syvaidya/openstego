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
     * "Image for Extract" text field
     */
    protected JTextField imgForExtractTextField = new JTextField();

    /**
     * "Image for Extract" browse file button
     */
    protected JButton imgForExtractFileButton = new JButton();

    /**
     * "Output Data" text field
     */
    protected JTextField outputDataTextField = new JTextField();

    /**
     * "Output Data" browse file button
     */
    protected JButton outputDataFileButton = new JButton();

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
        embedPanel.add(new JLabel(LabelUtil.getString("gui.label.sourceDataFile")), gridBagConstraints);

        gridBagConstraints.gridy = 2;
        embedPanel.add(new JLabel(LabelUtil.getString("gui.label.sourceImgFile")), gridBagConstraints);

        gridBagConstraints.gridy = 4;
        embedPanel.add(new JLabel(LabelUtil.getString("gui.label.outputImgFile")), gridBagConstraints);

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
        optionPanel.add(new JLabel(LabelUtil.getString("gui.label.option.useCompression")), gridBagConstraints);

        gridBagConstraints.gridy = 1;
        optionPanel.add(new JLabel(LabelUtil.getString("gui.label.option.maxBitsPerChannel")), gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;

        gridBagConstraints.gridy = 0;
        optionPanel.add(useCompCheckBox, gridBagConstraints);

        for(int i = 1; i <= 8; i++) maxBitsList[i - 1] = new Integer(i);
        maxBitsComboBox = new JComboBox(maxBitsList);
        maxBitsComboBox.setPreferredSize(new Dimension(40, 20));

        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(5, 10, 5, 5);
        optionPanel.add(maxBitsComboBox, gridBagConstraints);

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
        extractPanel.add(new JLabel(LabelUtil.getString("gui.label.outputDataFile")), gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 5, 5);

        gridBagConstraints.gridy = 1;
        imgForExtractTextField.setColumns(50);
        extractPanel.add(imgForExtractTextField, gridBagConstraints);

        gridBagConstraints.gridy = 3;
        outputDataTextField.setColumns(50);
        extractPanel.add(outputDataTextField, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);

        gridBagConstraints.gridy = 1;
        imgForExtractFileButton.setText("...");
        imgForExtractFileButton.setPreferredSize(new Dimension(22, 22));
        extractPanel.add(imgForExtractFileButton, gridBagConstraints);

        gridBagConstraints.gridy = 3;
        outputDataFileButton.setText("...");
        outputDataFileButton.setPreferredSize(new Dimension(22, 22));
        extractPanel.add(outputDataFileButton, gridBagConstraints);

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
        outputDataFileButton.setActionCommand("BROWSE_TGT_DATA");

        okButton.setActionCommand("OK");
        cancelButton.setActionCommand("CANCEL");
    }
}
