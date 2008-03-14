/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.template.imagebit;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import net.sourceforge.openstego.*;
import net.sourceforge.openstego.ui.*;
import net.sourceforge.openstego.util.*;

/**
 * GUI class for the Image Bit Plugin template
 */
public class ImageBitEmbedOptionsUI extends PluginEmbedOptionsUI
{
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(ImageBitPluginTemplate.NAMESPACE);

    /**
     * "Random Image as Source" checkbox
     */
    private JCheckBox randomImgCheckBox = new JCheckBox();

    /**
     * Combobox for "Max Bits Per Color Channel"
     */
    private JComboBox maxBitsComboBox = null;

    /**
     * Reference to the parent OpenStegoUI object
     */
    private OpenStegoUI stegoUI = null;

    /**
     * Default constructor
     * @param Reference to the parent UI object
     * @throws OpenStegoException
     */
    public ImageBitEmbedOptionsUI(OpenStegoUI stegoUI) throws OpenStegoException
    {
        this.stegoUI = stegoUI;

        GridBagConstraints gridBagConstraints = null;
        JLabel label = null;
        Object[] maxBitsList = new Object[8];

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
        add(randomImgCheckBox, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        for(int i = 0; i < 8; i++) maxBitsList[i] = new Integer(i + 1);
        maxBitsComboBox = new JComboBox(maxBitsList);
        maxBitsComboBox.setPreferredSize(new Dimension(40, 20));
        add(maxBitsComboBox, gridBagConstraints);

        ChangeListener changeListener = new ChangeListener()
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
     * Method to handle change event for 'randomImage'
     */
    private void useRandomImgChanged()
    {
        JTextField coverFileTextField = stegoUI.getCoverFileTextField();
        JButton coverFileButton = stegoUI.getCoverFileButton();

        if(randomImgCheckBox.isSelected())
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
     * @return Boolean indicating whether validation was successful or not
     * @throws OpenStegoException
     */
    public boolean validateEmbedAction() throws OpenStegoException
    {
        return true;
    }

    /**
     * Method to populate the plugin GUI options based on the config data
     * @param config OpenStego configuration data
     * @throws OpenStegoException
     */
    public void setGUIFromConfig(OpenStegoConfig config) throws OpenStegoException
    {
        maxBitsComboBox.setSelectedItem(new Integer(((ImageBitConfig) config).getMaxBitsUsedPerChannel()));
    }

    /**
     * Method to populate the config object based on the GUI data
     * @param config OpenStego configuration data
     * @throws OpenStegoException
     */
    public void setConfigFromGUI(OpenStegoConfig config) throws OpenStegoException
    {
        ((ImageBitConfig) config).setMaxBitsUsedPerChannel(((Integer) maxBitsComboBox.getSelectedItem()).intValue());
    }
}
