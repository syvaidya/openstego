/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.openstego.util.*;

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
     * @throws OpenStegoException
     */
    public OpenStegoConfig(Map propMap) throws OpenStegoException
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
                catch(NumberFormatException nfEx)
                {
                    throw new OpenStegoException(OpenStegoException.MAX_BITS_NOT_NUMBER, value, nfEx);
                }

                if(maxBitsUsedPerChannel < 1 || maxBitsUsedPerChannel > 8)
                {
                    throw new OpenStegoException(OpenStegoException.MAX_BITS_NOT_IN_RANGE, value, null);
                }
            }
            else if(key.equals(USE_COMPRESSION))
            {
                value = propMap.get(key).toString().trim();
                if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("y") || value.equals("1"))
                {
                    useCompression = true;
                }
                else if(value.equalsIgnoreCase("false") || value.equalsIgnoreCase("n") || value.equals("0"))
                {
                    useCompression = false;
                }
                else
                {
                    throw new OpenStegoException(OpenStegoException.INVALID_USE_COMPR_VALUE, value, null);
                }
            }
            else if(key.equals(USE_ENCRYPTION))
            {
                value = propMap.get(key).toString().trim();
                if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("y") || value.equals("1"))
                {
                    useEncryption = true;
                }
                else if(value.equalsIgnoreCase("false") || value.equalsIgnoreCase("n") || value.equals("0"))
                {
                    useEncryption = false;
                }
                else
                {
                    throw new OpenStegoException(OpenStegoException.INVALID_USE_ENCRYPT_VALUE, value, null);
                }
            }
            else if(key.equals(PASSWORD))
            {
                password = propMap.get(key).toString();
            }
            else
            {
                throw new OpenStegoException(OpenStegoException.INVALID_KEY_NAME, key, null);
            }
        }
    }

    /**
     * Constructor which reads configuration data from the command line options.
     * @param options Command-line options
     * @throws OpenStegoException
     */
    public OpenStegoConfig(CmdLineOptions options) throws OpenStegoException
    {
        HashMap map = new HashMap();
        
        if(options.getOption("-b") != null) // maxBitsUsedPerChannel
        {
            map.put(MAX_BITS_USED_PER_CHANNEL, options.getOptionValue("-b"));
        }

        if(options.getOption("-c") != null) // compress
        {
            map.put(USE_COMPRESSION, "true");
        }

        if(options.getOption("-C") != null) // nocompress
        {
            map.put(USE_COMPRESSION, "false");
        }

        if(options.getOption("-e") != null) // encrypt
        {
            map.put(USE_ENCRYPTION, "true");
        }

        if(options.getOption("-E") != null) // noencrypt
        {
            map.put(USE_ENCRYPTION, "false");
        }

        if(options.getOption("-p") != null) // password
        {
            map.put(PASSWORD, options.getOptionValue("-p"));
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