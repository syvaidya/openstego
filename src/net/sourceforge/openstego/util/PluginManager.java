/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.util;

import java.io.InputStream;
import java.util.*;

import net.sourceforge.openstego.*;

/**
 * Utility class to load and manage the available stego plugins
 */
public class PluginManager
{
    /**
     * Name of the default plugin
     */
    private final static String DEFAULT_PLUGIN = "LSB";


    /**
     * Static variable to hold the list of available plugins
     */
    private static List plugins = new ArrayList();

    /**
     * Static variable to hold a map of available plugins
     */
    private static Map pluginsMap = new HashMap();

    /**
     * Method to load the stego plugin classes
     * @throws OpenStegoException
     */
    public static void loadPlugins() throws OpenStegoException
    {
        Properties prop = new Properties();
        OpenStegoPlugin plugin = null;
        InputStream is = null;
        Enumeration en = null;

        try
        {
            // Load external plugins if available
            is = prop.getClass().getResourceAsStream("/OpenStegoPlugins.external");
            if(is != null)
            {
                prop.load(is);
            }

            // Load internal plugins
            prop.load(prop.getClass().getResourceAsStream("/OpenStegoPlugins.internal"));

            en = prop.keys();
            while(en.hasMoreElements())
            {
                plugin = (OpenStegoPlugin) Class.forName((String) en.nextElement()).newInstance();
                plugins.add(plugin);
                pluginsMap.put(plugin.getName().toUpperCase(), plugin);
            }
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(    ex);
        }
    }

    /**
     * Method to get the list of names of the loaded plugins
     */
    public static List getPluginNames()
    {
        List nameList = new ArrayList();

        for(int i = 0; i < plugins.size(); i++)
        {
            nameList.add(((OpenStegoPlugin) plugins.get(i)).getName().toUpperCase());
        }
        
        return nameList;
    }

    /**
     * Method to get the list of the loaded plugins
     */
    public static List getPlugins()
    {
        return plugins;
    }

    /**
     * Method to get the plugin object based on the name of the plugin
     */
    public static OpenStegoPlugin getPluginByName(String name)
    {
        return (OpenStegoPlugin) pluginsMap.get(name.toUpperCase());
    }

    /**
     * Method to get the default plugin
     */
    public static OpenStegoPlugin getDefaultPlugin()
    {
        return getPluginByName(DEFAULT_PLUGIN);
    }
}
