/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.lsb;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.ui.OpenStegoFrame;
import com.openstego.desktop.ui.PluginEmbedOptionsUI;
import com.openstego.desktop.util.CommonUtil;
import com.openstego.desktop.util.LabelUtil;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * GUI class for the LSB Plugin
 */
@SuppressWarnings("unused")
public class LSBEmbedOptionsUI extends PluginEmbedOptionsUI {
    private static final long serialVersionUID = 6168148599483165215L;

    /**
     * LabelUtil instance to retrieve labels
     */
    private static final LabelUtil labelUtil = LabelUtil.getInstance(LSBPlugin.NAMESPACE);

    /**
     * "Random Image as Source" checkbox
     */
    private final JCheckBox randomImgCheckBox = new JCheckBox();

    /**
     * Combobox for "Max Bits Per Color Channel"
     */
    private final JComboBox<Integer> maxBitsComboBox;

    /**
     * Reference to the parent OpenStegoUI object
     */
    private final OpenStegoFrame stegoUI;

    /**
     * Default constructor
     *
     * @param stegoUI Reference to the parent UI object
     */
    public LSBEmbedOptionsUI(OpenStegoFrame stegoUI) {
        this.stegoUI = stegoUI;

        GridBagConstraints gridBagConstraints;
        JLabel label;
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
        for (int i = 0; i < 8; i++) {
            maxBitsList[i] = i + 1;
        }
        this.maxBitsComboBox = new JComboBox<>(maxBitsList);
        this.maxBitsComboBox.setPreferredSize(new Dimension(40, 20));
        add(this.maxBitsComboBox, gridBagConstraints);

        ChangeListener changeListener = changeEvent -> useRandomImgChanged();
        this.randomImgCheckBox.addChangeListener(changeListener);
    }

    /**
     * Initialize the UI
     */
    @Override
    public void initialize() {
        useRandomImgChanged();
    }

    /**
     * Method to handle change event for 'randomImage'
     */
    private void useRandomImgChanged() {
        JTextField coverFileTextField = this.stegoUI.getEmbedPanel().getCoverFileTextField();
        JButton coverFileButton = this.stegoUI.getEmbedPanel().getCoverFileButton();

        if (this.randomImgCheckBox.isSelected()) {
            CommonUtil.setEnabled(coverFileTextField, false);
            coverFileTextField.setText("");
            coverFileButton.setEnabled(false);
        } else {
            CommonUtil.setEnabled(coverFileTextField, true);
            coverFileButton.setEnabled(true);
            coverFileTextField.requestFocus();
        }
    }

    /**
     * Method to validate plugin options for "Embed" action
     *
     * @return Boolean indicating whether validation was successful or not
     */
    @Override
    public boolean validateEmbedAction() {
        return true;
    }

    /**
     * Method to populate the plugin GUI options based on the config data
     *
     * @param config OpenStego configuration data
     */
    @Override
    public void setGUIFromConfig(OpenStegoConfig config) {
        this.maxBitsComboBox.setSelectedItem(((LSBConfig) config).getMaxBitsUsedPerChannel());
    }

    /**
     * Method to populate the config object based on the GUI data
     *
     * @param config OpenStego configuration data
     */
    @Override
    public void setConfigFromGUI(OpenStegoConfig config) {
        Integer maxBits = (Integer) this.maxBitsComboBox.getSelectedItem();
        if (maxBits != null) {
            ((LSBConfig) config).setMaxBitsUsedPerChannel(maxBits);
        }
    }
}
