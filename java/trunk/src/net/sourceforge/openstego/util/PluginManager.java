/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.OpenStegoPlugin;

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
        List pluginList = null;
        OpenStegoPlugin plugin = null;
        InputStream is = null;

        try
        {
            // Load internal plugins
            is = plugins.getClass().getResourceAsStream("/OpenStegoPlugins.internal");
            pluginList = StringUtil.getStringLines(new String(CommonUtil.getStreamBytes(is)));

            // Load external plugins if available
            is = plugins.getClass().getResourceAsStream("/OpenStegoPlugins.external");
            if(is != null)
            {
                pluginList.addAll(StringUtil.getStringLines(new String(CommonUtil.getStreamBytes(is))));
            }

            for(int i = 0; i < pluginList.size(); i++)
            {
                plugin = (OpenStegoPlugin) Class.forName((String) pluginList.get(i)).newInstance();
                plugins.add(plugin);
                pluginsMap.put(plugin.getName().toUpperCase(), plugin);
            }
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(ex);
        }
    }

    /**
     * Method to get the list of names of the loaded plugins
     * @return List of names of the loaded plugins
     */
    public static List getPluginNames()
    {
        List nameList = new ArrayList();

        for(int i = 0; i < plugins.size(); i++)
        {
            nameList.add(((OpenStegoPlugin) plugins.get(i)).getName());
        }

        return nameList;
    }

    /**
     * Method to get the list of the loaded plugins
     * @return List of the loaded plugins
     */
    public static List getPlugins()
    {
        return plugins;
    }

    /**
     * Method to get the plugin object based on the name of the plugin
     * @param name Name of the plugin
     * @return Plugin object
     */
    public static OpenStegoPlugin getPluginByName(String name)
    {
        return (OpenStegoPlugin) pluginsMap.get(name.toUpperCase());
    }

    /**
     * Method to get the default plugin
     * @return Default plugin object
     */
    public static OpenStegoPlugin getDefaultPlugin()
    {
        return getPluginByName(DEFAULT_PLUGIN);
    }
}
