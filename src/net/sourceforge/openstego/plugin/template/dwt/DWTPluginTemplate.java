/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.template.dwt;

import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.plugin.template.image.ImagePluginTemplate;
import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.ui.PluginEmbedOptionsUI;
import net.sourceforge.openstego.util.CmdLineOptions;
import net.sourceforge.openstego.util.LabelUtil;

/**
 * Template plugin for OpenStego which implements the DWT based steganography for images (wavelet domain)
 */
public abstract class DWTPluginTemplate extends ImagePluginTemplate
{
    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "DWTTEMPLATE";

    static
    {
        LabelUtil.addNamespace(NAMESPACE, "net.sourceforge.openstego.resource.DWTPluginTemplateLabels");
        new DWTErrors(); // Initialize error codes
    }

    /**
     * Method to get the UI object specific to this plugin, which will be embedded inside the main OpenStego GUI
     * 
     * @param stegoUI Reference to the parent OpenStegoUI object
     * @return UI object specific to this plugin
     * @throws OpenStegoException
     */
    public PluginEmbedOptionsUI getEmbedOptionsUI(OpenStegoUI stegoUI) throws OpenStegoException
    {
        return null;
    }

    /**
     * Method to populate the standard command-line options used by this plugin
     * 
     * @param options Existing command-line options. Plugin-specific options will get added to this list
     * @throws OpenStegoException
     */
    public void populateStdCmdLineOptions(CmdLineOptions options) throws OpenStegoException
    {
    }

    /**
     * Method to get the configuration class specific to this plugin
     * @return Configuration class specific to this plugin
     */
    public Class getConfigClass()
    {
        return OpenStegoConfig.class;
    }
}
