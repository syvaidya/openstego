/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2014 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.lsb;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.ui.PluginEmbedOptionsUI;
import net.sourceforge.openstego.util.CommonUtil;
import net.sourceforge.openstego.util.LabelUtil;

/**
 * GUI class for the LSB Plugin
 */
public class LSBEmbedOptionsUI extends PluginEmbedOptionsUI
{
    private static final long serialVersionUID = 6168148599483165215L;

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(LSBPlugin.NAMESPACE);

    /**
     * "Random Image as Source" checkbox
     */
    private JCheckBox randomImgCheckBox = new JCheckBox();

    /**
     * Combobox for "Max Bits Per Color Channel"
     */
    private JComboBox<Integer> maxBitsComboBox = null;

    /**
     * Reference to the parent OpenStegoUI object
     */
    private OpenStegoUI stegoUI = null;

    /**
     * Default constructor
     *
     * @param stegoUI Reference to the parent UI object
     */
    public LSBEmbedOptionsUI(OpenStegoUI stegoUI)
    {
        this.stegoUI = stegoUI;

        GridBagConstraints gridBagConstraints = null;
        JLabel label = null;
        Integer[] maxBitsList = new Integer[8];

        setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        gridBagConstraints.gridy = 0;
        label = new JLabel(labelUtil.getString("gui.label.option.useRandomImage"));
        add(label, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        label = new JLabel(labelUtil.getString("gui.label.option.maxBitsPerChannel"));
        add(label, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;

        gridBagConstraints.gridy = 0;
        add(this.randomImgCheckBox, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        for(int i = 0; i < 8; i++)
        {
            maxBitsList[i] = new Integer(i + 1);
        }
        this.maxBitsComboBox = new JComboBox<Integer>(maxBitsList);
        this.maxBitsComboBox.setPreferredSize(new Dimension(40, 20));
        add(this.maxBitsComboBox, gridBagConstraints);

        ChangeListener changeListener = new ChangeListener()
        {
            public void stateChanged(ChangeEvent changeEvent)
            {
                useRandomImgChanged();
            }
        };
        this.randomImgCheckBox.addChangeListener(changeListener);
        useRandomImgChanged();
    }

    /**
     * Method to handle change event for 'randomImage'
     */
    private void useRandomImgChanged()
    {
        JTextField coverFileTextField = this.stegoUI.getEmbedPanel().getCoverFileTextField();
        JButton coverFileButton = this.stegoUI.getEmbedPanel().getCoverFileButton();

        if(this.randomImgCheckBox.isSelected())
        {
            CommonUtil.setEnabled(coverFileTextField, false);
            coverFileTextField.setText("");
            coverFileButton.setEnabled(false);
        }
        else
        {
            CommonUtil.setEnabled(coverFileTextField, true);
            coverFileButton.setEnabled(true);
            coverFileTextField.requestFocus();
        }
    }

    /**
     * Method to validate plugin options for "Embed" action
     *
     * @return Boolean indicating whether validation was successful or not
     * @throws OpenStegoException
     */
    public boolean validateEmbedAction() throws OpenStegoException
    {
        return true;
    }

    /**
     * Method to populate the plugin GUI options based on the config data
     *
     * @param config OpenStego configuration data
     * @throws OpenStegoException
     */
    public void setGUIFromConfig(OpenStegoConfig config) throws OpenStegoException
    {
        this.maxBitsComboBox.setSelectedItem(new Integer(((LSBConfig) config).getMaxBitsUsedPerChannel()));
    }

    /**
     * Method to populate the config object based on the GUI data
     *
     * @param config OpenStego configuration data
     * @throws OpenStegoException
     */
    public void setConfigFromGUI(OpenStegoConfig config) throws OpenStegoException
    {
        ((LSBConfig) config).setMaxBitsUsedPerChannel(((Integer) this.maxBitsComboBox.getSelectedItem()).intValue());
    }
}
