/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.util.List;
import java.util.Map;

import net.sourceforge.openstego.ui.*;
import net.sourceforge.openstego.util.*;

/**
 * Abstract class for stego plugins for OpenStego. Abstract methods need to be implemented to add support for more
 * steganographic algorithms
 */
public abstract class OpenStegoPlugin
{
    /**
     * Configuration data to be used while embedding / extracting data
     */
    protected OpenStegoConfig config = null;


    // ------------- Metadata Methods -------------

    /**
     * Gives the name of the plugin
     * @return Name of the plugin
     */
    public abstract String getName();

    /**
     * Gives a short description of the plugin
     * @return Short description of the plugin
     */
    public abstract String getDescription();


    // ------------- Core Stego Methods -------------

    /**
     * Method to embed the message into the cover data
     * @param msg Message to be embedded
     * @param msgFileName Name of the message file. If this value is provided, then the filename should be
     *                    embedded in the cover data
     * @param cover Cover data into which message needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the message
     * @throws OpenStegoException
     */
    public abstract byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName)
        throws OpenStegoException;

    /**
     * Method to extract the message file name from the stego data
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @return Message file name
     * @throws OpenStegoException
     */
    public abstract String extractMsgFileName(byte[] stegoData, String stegoFileName) throws OpenStegoException;

    /**
     * Method to extract the message from the stego data
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @return Extracted message
     * @throws OpenStegoException
     */
    public abstract byte[] extractData(byte[] stegoData, String stegoFileName) throws OpenStegoException;

    /**
     * Method to find out whether given stego data can be handled by this plugin or not
     * @param stegoData Stego data containing the message
     * @return Boolean indicating whether the stego data can be handled by this plugin or not
     */
    public abstract boolean canHandle(byte[] stegoData) throws OpenStegoException;

    /**
     * Method to get the list of supported file extensions for reading
     * @return List of supported file extensions for reading
     */
    public abstract List getReadableFileExtensions() throws OpenStegoException;

    /**
     * Method to get the list of supported file extensions for writing
     * @return List of supported file extensions for writing
     */
    public abstract List getWritableFileExtensions() throws OpenStegoException;


    // ------------- Command-line Related Methods -------------

    /**
     * Method to populate the standard command-line options used by this plugin
     * @param options Existing command-line options. Plugin-specific options will get added to this list
     */
    public abstract void populateStdCmdLineOptions(CmdLineOptions options) throws OpenStegoException;

    /**
     * Method to get the usage details of the plugin
     * @return Usage details of the plugin
     */
    public abstract String getUsage() throws OpenStegoException;


    // ------------- GUI Related Methods -------------

    /**
     * Method to get the UI object for "Embed" action specific to this plugin. This UI object will be embedded inside
     * the main OpenStego GUI
     * @param stegoUI Reference to the parent OpenStegoUI object
     * @return UI object specific to this plugin for "Embed" action
     */
    public abstract PluginEmbedOptionsUI getEmbedOptionsUI(OpenStegoUI stegoUI) throws OpenStegoException;


    // ------------- Other Methods -------------

    /**
     * Method to create default configuration data (specific to this plugin)
     * @return Configuration data
     */
    public abstract OpenStegoConfig createConfig() throws OpenStegoException;

    /**
     * Method to create configuration data (specific to this plugin) based on the property map
     * @param propMap Property map
     * @return Configuration data
     */
    public abstract OpenStegoConfig createConfig(Map propMap) throws OpenStegoException;

    /**
     * Method to create configuration data (specific to this plugin) based on the command-line options
     * @param options Command-line options
     * @return Configuration data
     */
    public abstract OpenStegoConfig createConfig(CmdLineOptions options) throws OpenStegoException;

    /**
     * Get method for config
     * @return Configuration data
     */
    public OpenStegoConfig getConfig()
    {
        return config;
    }
}