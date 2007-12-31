/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.util.Iterator;
import java.util.Map;

import net.sourceforge.openstego.util.LabelUtil;

/**
 * Class to store configuration data for OpenStego
 */
public class OpenStegoConfig
{
    /**
     * Key string for configuration item - maxBitsUsedPerChannel.
     * <p>
     * Maximum bits to use per color channel. Allowing for higher number here might degrade the quality
     * of the image in case the data size is big.
     */
    public static final String MAX_BITS_USED_PER_CHANNEL = "maxBitsUsedPerChannel";

    /**
     * Key string for configuration item - useCompression
     * <p>
     * Flag to indicate whether compression should be used or not
     */
    public static final String USE_COMPRESSION = "useCompression";

    /**
     * Key string for configuration item - useEncryption
     * <p>
     * Flag to indicate whether encryption should be used or not
     */
    public static final String USE_ENCRYPTION = "useEncryption";

    /**
     * Key string for configuration item - password
     * <p>
     * Password for encryption in case "useEncryption" is set to true
     */
    public static final String PASSWORD = "password";

    /**
     * Maximum bits to use per color channel. Allowing for higher number here might degrade the quality
     * of the image in case the data size is big.
     */
    private int maxBitsUsedPerChannel = 3;

    /**
     * Default image file type to use for writing, in case the read image file type is not supported
     */
    private String defaultImageOutputType = "png";

    /**
     * Flag to indicate whether compression should be used or not
     */
    private boolean useCompression = true;

    /**
     * Flag to indicate whether encryption should be used or not
     */
    private boolean useEncryption = false;

    /**
     * Password for encryption in case "useEncryption" is set to true
     */
    private String password = null;

    /**
     * Default Constructor (with default values for configuration items)
     */
    public OpenStegoConfig()
    {
    }

    /**
     * Constructor with map of configuration data. Please make sure that only valid keys for configuration
     * items are provided, and the values for those items are also valid.
     * @param propMap Map containing the configuration data
     */
    public OpenStegoConfig(Map propMap)
    {
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
                catch(NumberFormatException ex)
                {
                    throw new IllegalArgumentException(LabelUtil.getString(
                            "err.config.maxBitsUsedPerChannel.notNumber", new Object[] { value }));
                }

                if(maxBitsUsedPerChannel < 1 || maxBitsUsedPerChannel > 8)
                {
                    throw new IllegalArgumentException(LabelUtil.getString(
                            "err.config.maxBitsUsedPerChannel.notInRange", new Object[] { value }));
                }
            }
            else if(key.equals(USE_COMPRESSION))
            {
                value = propMap.get(key).toString().trim();
                if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("y"))
                {
                    useCompression = true;
                }
                else if(value.equalsIgnoreCase("false") || value.equalsIgnoreCase("n"))
                {
                    useCompression = false;
                }
                else
                {
                    throw new IllegalArgumentException(LabelUtil.getString("err.config.useCompression.invalid",
                            new Object[] { value }));
                }
            }
            else if(key.equals(USE_ENCRYPTION))
            {
                value = propMap.get(key).toString().trim();
                if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("y"))
                {
                    useEncryption = true;
                }
                else if(value.equalsIgnoreCase("false") || value.equalsIgnoreCase("n"))
                {
                    useEncryption = false;
                }
                else
                {
                    throw new IllegalArgumentException(LabelUtil.getString("err.config.useEncryption.invalid",
                            new Object[] { value }));
                }
            }
            else if(key.equals(PASSWORD))
            {
                password = propMap.get(key).toString();
            }
            else
            {
                throw new IllegalArgumentException(LabelUtil.getString("err.config.invalidKey", new Object[] { key }));
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
     * Get method for configuration item - defaultImageOutputType
     * @return defaultImageOutputType
     */
    public String getDefaultImageOutputType()
    {
        return defaultImageOutputType;
    }

    /**
     * Get method for configuration item - useCompression
     * @return useCompression
     */
    public boolean isUseCompression()
    {
        return useCompression;
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
     * Set method for configuration item - useCompression
     * @param useCompression
     */
    public void setUseCompression(boolean useCompression)
    {
        this.useCompression = useCompression;
    }

    /**
     * Get Method for useEncryption
     * @return useEncryption
     */
    public boolean isUseEncryption()
    {
        return useEncryption;
    }

    /**
     * Set Method for useEncryption
     * @param useEncryption
     */
    public void setUseEncryption(boolean useEncryption)
    {
        this.useEncryption = useEncryption;
    }

    /**
     * Get Method for password
     * @return password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Set Method for password
     * @param password
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
}