/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.ui;

import com.openstego.desktop.OpenStegoConfig;

import javax.swing.*;

/**
 * Abstract class for GUI for OpenStego plugin for "Embed" action
 */
public abstract class PluginEmbedOptionsUI extends JPanel {
    private static final long serialVersionUID = 6932223460790839609L;

    /**
     * Method to initialized the plugin options UI
     */
    public abstract void initialize();

    /**
     * Method to validate plugin options for "Embed" action
     *
     * @return Boolean indicating whether validation was successful or not
     */
    public abstract boolean validateEmbedAction();

    /**
     * Method to populate the plugin GUI options based on the config data
     *
     * @param config OpenStego configuration data
     */
    public abstract void setGUIFromConfig(OpenStegoConfig config);

    /**
     * Method to populate the config object based on the GUI data
     *
     * @param config OpenStego configuration data
     */
    public abstract void setConfigFromGUI(OpenStegoConfig config);
}
