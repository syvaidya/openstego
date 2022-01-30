/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.template.dct;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.util.cmd.CmdLineOptions;

import java.util.Map;

/**
 * Class to store configuration data for DCT plugin template
 */
public class DCTConfig extends OpenStegoConfig {
    /**
     * Key string for configuration item - imageFileExtension
     * <p>
     * Image file extension for the output file
     */
    public static final String IMAGE_FILE_EXTENSION = "imageFileExtension";

    /**
     * Image file extension to use for writing
     */
    private String imageFileExtension = "png";

    /**
     * Converts command line options to Map form
     *
     * @param options Command-line options
     * @return Options in Map form
     * @throws OpenStegoException Processing issues
     */
    @Override
    protected Map<String, Object> convertCmdLineOptionsToMap(CmdLineOptions options) throws OpenStegoException {
        Map<String, Object> map = super.convertCmdLineOptionsToMap(options);

        if (options.getOption("-i") != null) { // imageFileExtension
            map.put(IMAGE_FILE_EXTENSION, options.getStringValue("-i"));
        }

        return map;
    }

    /**
     * Processes a configuration item.
     *
     * @param key   Configuration item key
     * @param value Configuration item value
     */
    @Override
    protected void processConfigItem(String key, Object value) throws OpenStegoException {
        super.processConfigItem(key, value);
        if (key.equals(IMAGE_FILE_EXTENSION)) {
            assert value instanceof String;
            this.imageFileExtension = (String) value;
        }
    }

    /**
     * Get method for configuration item - imageFileExtension
     *
     * @return imageFileExtension
     */
    @SuppressWarnings("unused")
    public String getImageFileExtension() {
        return this.imageFileExtension;
    }

    /**
     * Set method for configuration item - imageFileExtension
     *
     * @param imageFileExtension Value to be set
     */
    @SuppressWarnings("unused")
    public void setImageFileExtension(String imageFileExtension) {
        this.imageFileExtension = imageFileExtension;
    }
}
