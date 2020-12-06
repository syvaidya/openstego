/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.template.dct;

import java.util.Map;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.util.cmd.CmdLineOptions;

/**
 * Class to store configuration data for DCT plugin template
 */
public class DCTConfig extends OpenStegoConfig {
    /**
     * Image file extension to use for writing
     */
    private String imageFileExtension = "png";

    /**
     * Default Constructor (with default values for configuration items)
     */
    public DCTConfig() {
    }

    /**
     * Constructor with map of configuration data. Please make sure that only valid keys for configuration
     * items are provided, and the values for those items are also valid.
     *
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    public DCTConfig(Map<String, String> propMap) throws OpenStegoException {
        addProperties(propMap);
    }

    /**
     * Constructor which reads configuration data from the command line options.
     *
     * @param options Command-line options
     * @throws OpenStegoException
     */
    public DCTConfig(CmdLineOptions options) throws OpenStegoException {
        super(options);
    }

    /**
     * Method to add properties from the map to this configuration data
     *
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    @Override
    protected void addProperties(Map<String, String> propMap) throws OpenStegoException {
        super.addProperties(propMap);
    }

    /**
     * Get method for configuration item - imageFileExtension
     *
     * @return imageFileExtension
     */
    public String getImageFileExtension() {
        return this.imageFileExtension;
    }

    /**
     * Set method for configuration item - imageFileExtension
     *
     * @param imageFileExtension
     */
    public void setImageFileExtension(String imageFileExtension) {
        this.imageFileExtension = imageFileExtension;
    }
}
