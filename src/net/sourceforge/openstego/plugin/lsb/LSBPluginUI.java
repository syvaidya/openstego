/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.lsb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.openstego.*;
import net.sourceforge.openstego.ui.*;
import net.sourceforge.openstego.util.*;

/**
 * GUI class for the LSB Plugin
 */
public class LSBPluginUI extends OpenStegoPluginUI
{
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(LSBPlugin.NAMESPACE);

    /**
     * Default constructor
     * @param Reference to the parent UI object
     * @throws OpenStegoException
     */
    public LSBPluginUI(OpenStegoUI stegoUI) throws OpenStegoException
    {
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
     * Method to validate plugin options for "Extract" action
     * @return Boolean indicating whether validation was successful or not
     * @throws OpenStegoException
     */
    public boolean validateExtractAction() throws OpenStegoException
    {
        return true;
    }

    /**
     * Method to populate the plugin GUI options based on the config data
     * @param config OpenStego configuration data
     * @throws OpenStegoException
     */
    public void loadConfig(OpenStegoConfig config) throws OpenStegoException
    {
    }

    /**
     * Method to populate the config object based on the GUI data
     * @param config OpenStego configuration data
     * @throws OpenStegoException
     */
    public void setConfig(OpenStegoConfig config) throws OpenStegoException
    {
    }

    /**
     * This cleanup method is called on close of the application
     * @throws OpenStegoException
     */
    public void cleanup() throws OpenStegoException
    {
    }
}
