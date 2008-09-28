/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.template.imagebit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.util.CmdLineOptions;

/**
 * Class to store configuration data for Image Bit plugin template
 */
public class ImageBitConfig extends OpenStegoConfig
{
    /**
     * Key string for configuration item - maxBitsUsedPerChannel.
     * <p>
     * Maximum bits to use per color channel. Allowing for higher number here might degrade the quality
     * of the image in case the data size is big.
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
    public ImageBitConfig()
    {
    }

    /**
     * Constructor with map of configuration data. Please make sure that only valid keys for configuration
     * items are provided, and the values for those items are also valid.
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    public ImageBitConfig(Map propMap) throws OpenStegoException
    {
        addProperties(propMap);
    }

    /**
     * Constructor which reads configuration data from the command line options.
     * @param options Command-line options
     * @throws OpenStegoException
     */
    public ImageBitConfig(CmdLineOptions options) throws OpenStegoException
    {
        super(options);

        HashMap map = new HashMap();
        if(options.getOption("-b") != null) // maxBitsUsedPerChannel
        {
            map.put(MAX_BITS_USED_PER_CHANNEL, options.getOptionValue("-b"));
        }

        addProperties(map);
    }

    /**
     * Method to add properties from the map to this configuration data
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    protected void addProperties(Map propMap) throws OpenStegoException
    {
        super.addProperties(propMap);

        Iterator keys = null;
        String key = null;
        String value = null;

        keys = propMap.keySet().iterator();
        while(keys.hasNext())
        {
            key = (String) keys.next();
            if(key.equals(MAX_BITS_USED_PER_CHANNEL))
            {
                value = propMap.get(key).toString().trim();
                try
                {
                    maxBitsUsedPerChannel = Integer.parseInt(value);
                }
                catch(NumberFormatException nfEx)
                {
                    throw new OpenStegoException(ImageBitPluginTemplate.NAMESPACE, ImageBitErrors.MAX_BITS_NOT_NUMBER,
                            value, nfEx);
                }

                if(maxBitsUsedPerChannel < 1 || maxBitsUsedPerChannel > 8)
                {
                    throw new OpenStegoException(ImageBitPluginTemplate.NAMESPACE,
                            ImageBitErrors.MAX_BITS_NOT_IN_RANGE, value, null);
                }
            }
        }
    }

    /**
     * Get method for configuration item - maxBitsUsedPerChannel
     * @return maxBitsUsedPerChannel
     */
    public int getMaxBitsUsedPerChannel()
    {
        return maxBitsUsedPerChannel;
    }

    /**
     * Set method for configuration item - maxBitsUsedPerChannel
     * @param maxBitsUsedPerChannel
     */
    public void setMaxBitsUsedPerChannel(int maxBitsUsedPerChannel)
    {
        this.maxBitsUsedPerChannel = maxBitsUsedPerChannel;
    }

    /**
     * Get method for configuration item - imageFileExtension
     * @return imageFileExtension
     */
    public String getImageFileExtension()
    {
        return imageFileExtension;
    }

    /**
     * Set method for configuration item - imageFileExtension
     * @param imageFileExtension
     */
    public void setImageFileExtension(String imageFileExtension)
    {
        this.imageFileExtension = imageFileExtension;
    }
}