/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.ui;

import javax.swing.JPanel;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;

/**
 * Abstract class for GUI for OpenStego plugin for "Embed" action
 */
public abstract class PluginEmbedOptionsUI extends JPanel {
    private static final long serialVersionUID = 6932223460790839609L;

    /**
     * Method to validate plugin options for "Embed" action
     *
     * @return Boolean indicating whether validation was successful or not
     * @throws OpenStegoException
     */
    public abstract boolean validateEmbedAction() throws OpenStegoException;

    /**
     * Method to populate the plugin GUI options based on the config data
     *
     * @param config OpenStego configuration data
     * @throws OpenStegoException
     */
    public abstract void setGUIFromConfig(OpenStegoConfig config) throws OpenStegoException;

    /**
     * Method to populate the config object based on the GUI data
     *
     * @param config OpenStego configuration data
     * @throws OpenStegoException
     */
    public abstract void setConfigFromGUI(OpenStegoConfig config) throws OpenStegoException;
}
