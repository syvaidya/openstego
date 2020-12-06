/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.lsb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.util.cmd.CmdLineOptions;

/**
 * Class to store configuration data for LSB plugin
 */
public class LSBConfig extends OpenStegoConfig {
    /**
     * Key string for configuration item - maxBitsUsedPerChannel.
     * <p>
     * Maximum bits to use per color channel. Allowing for higher number here might degrade the quality of the image in
     * case the data size is big.
     */
    public static final String MAX_BITS_USED_PER_CHANNEL = "maxBitsUsedPerChannel";

    /**
     * Maximum bits to use per color channel. Allowing for higher number here might degrade the quality
     * of the image in case the data size is big.
     */
    private int maxBitsUsedPerChannel = 3;

    /**
     * Image file extension to use for writing
     */
    private String imageFileExtension = "png";

    /**
     * Default Constructor (with default values for configuration items)
     */
    public LSBConfig() {
    }

    /**
     * Constructor with map of configuration data. Please make sure that only valid keys for configuration
     * items are provided, and the values for those items are also valid.
     *
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    public LSBConfig(Map<String, String> propMap) throws OpenStegoException {
        addProperties(propMap);
    }

    /**
     * Constructor which reads configuration data from the command line options.
     *
     * @param options Command-line options
     * @throws OpenStegoException
     */
    public LSBConfig(CmdLineOptions options) throws OpenStegoException {
        super(options);

        Map<String, String> map = new HashMap<String, String>();
        if (options.getOption("-b") != null) // maxBitsUsedPerChannel
        {
            map.put(MAX_BITS_USED_PER_CHANNEL, options.getOptionValue("-b"));
        }

        addProperties(map);
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

        Iterator<String> keys = null;
        String key = null;
        String value = null;

        keys = propMap.keySet().iterator();
        while (keys.hasNext()) {
            key = keys.next();
            if (key.equals(MAX_BITS_USED_PER_CHANNEL)) {
                value = propMap.get(key).toString().trim();
                try {
                    this.maxBitsUsedPerChannel = Integer.parseInt(value);
                } catch (NumberFormatException nfEx) {
                    throw new OpenStegoException(nfEx, LSBPlugin.NAMESPACE, LSBErrors.MAX_BITS_NOT_NUMBER, value);
                }

                if (this.maxBitsUsedPerChannel < 1 || this.maxBitsUsedPerChannel > 8) {
                    throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.MAX_BITS_NOT_IN_RANGE, value);
                }
            }
        }
    }

    /**
     * Get method for configuration item - maxBitsUsedPerChannel
     *
     * @return maxBitsUsedPerChannel
     */
    public int getMaxBitsUsedPerChannel() {
        return this.maxBitsUsedPerChannel;
    }

    /**
     * Set method for configuration item - maxBitsUsedPerChannel
     *
     * @param maxBitsUsedPerChannel
     */
    public void setMaxBitsUsedPerChannel(int maxBitsUsedPerChannel) {
        this.maxBitsUsedPerChannel = maxBitsUsedPerChannel;
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
