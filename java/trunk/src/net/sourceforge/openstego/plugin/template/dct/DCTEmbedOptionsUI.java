/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.template.dct;

import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.ui.PluginEmbedOptionsUI;

/**
 * GUI class for the DCT Plugin template
 */
public class DCTEmbedOptionsUI extends PluginEmbedOptionsUI
{
    /**
     * Reference to the parent OpenStegoUI object
     */
    //private OpenStegoUI stegoUI = null;
    /**
     * Default constructor
     * @param stegoUI Reference to the parent UI object
     * @throws OpenStegoException
     */
    public DCTEmbedOptionsUI(OpenStegoUI stegoUI) throws OpenStegoException
    {
        //this.stegoUI = stegoUI;
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
    }

    /**
     * Method to populate the config object based on the GUI data
     * @param config OpenStego configuration data
     * @throws OpenStegoException
     */
    public void setConfigFromGUI(OpenStegoConfig config) throws OpenStegoException
    {
    }
}
