/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
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
        addProperties(propMap);
    }

    /**
     * Constructor which reads configuration data from the command line options.
     * @param options Command-line options
     * @throws OpenStegoException
     */
    public OpenStegoConfig(CmdLineOptions options) throws OpenStegoException
    {
        HashMap map = new HashMap();
        
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
        
        addProperties(map);
    }

    /**
     * Method to add properties from the map to this configuration data
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    protected void addProperties(Map propMap) throws OpenStegoException
    {
        Iterator keys = null;
        String key = null;
        String value = null;

        keys = propMap.keySet().iterator();
        while(keys.hasNext())
        {
            key = (String) keys.next();
            if(key.equals(USE_COMPRESSION))
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
                    throw new OpenStegoException(OpenStego.NAMESPACE, OpenStegoException.INVALID_USE_COMPR_VALUE, value, null);
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
                    throw new OpenStegoException(OpenStego.NAMESPACE, OpenStegoException.INVALID_USE_ENCRYPT_VALUE, value, null);
                }
            }
            else if(key.equals(PASSWORD))
            {
                password = propMap.get(key).toString();
            }
        }
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