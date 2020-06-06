/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.OpenStegoPlugin;

/**
 * Utility class to load and manage the available stego plugins
 */
public class PluginManager {
    /**
     * Constructor is private so that this class is not instantiated
     */
    private PluginManager() {
    }

    /**
     * Static variable to hold the list of available plugins
     */
    private static List<OpenStegoPlugin> plugins = new ArrayList<OpenStegoPlugin>();

    /**
     * Static variable to hold a map of available plugins
     */
    private static Map<String, OpenStegoPlugin> pluginsMap = new HashMap<String, OpenStegoPlugin>();

    /**
     * Method to load the stego plugin classes
     *
     * @throws OpenStegoException
     */
    public static void loadPlugins() throws OpenStegoException {
        List<String> pluginList = null;
        OpenStegoPlugin plugin = null;
        InputStream is = null;

        try {
            // Load internal plugins
            is = PluginManager.class.getResourceAsStream("/OpenStegoPlugins.internal");
            pluginList = StringUtil.getStringLines(new String(CommonUtil.getStreamBytes(is)));

            // Load external plugins if available
            is = PluginManager.class.getResourceAsStream("/OpenStegoPlugins.external");
            if (is != null) {
                pluginList.addAll(StringUtil.getStringLines(new String(CommonUtil.getStreamBytes(is))));
            }

            for (int i = 0; i < pluginList.size(); i++) {
                plugin = (OpenStegoPlugin) Class.forName(pluginList.get(i)).getDeclaredConstructor().newInstance();
                plugins.add(plugin);
                pluginsMap.put(plugin.getName().toUpperCase(), plugin);
            }
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        }
    }

    /**
     * Method to get the list of names of the loaded plugins
     *
     * @return List of names of the loaded plugins
     */
    public static List<String> getPluginNames() {
        List<String> nameList = new ArrayList<String>();

        for (int i = 0; i < plugins.size(); i++) {
            nameList.add((plugins.get(i)).getName());
        }

        return nameList;
    }

    /**
     * Method to get the list of the loaded plugins
     *
     * @return List of the loaded plugins
     */
    public static List<OpenStegoPlugin> getPlugins() {
        return plugins;
    }

    /**
     * Method to get the list of the data hiding plugins
     *
     * @return List of the data hiding plugins
     */
    public static List<OpenStegoPlugin> getDataHidingPlugins() {
        OpenStegoPlugin plugin = null;
        List<OpenStegoPlugin> dhPlugins = new ArrayList<OpenStegoPlugin>();

        for (int i = 0; i < plugins.size(); i++) {
            plugin = plugins.get(i);
            if (plugin.getPurposes().contains(OpenStegoPlugin.Purpose.DATA_HIDING)) {
                dhPlugins.add(plugin);
            }
        }
        return dhPlugins;
    }

    /**
     * Method to get the list of the watermarking plugins
     *
     * @return List of the watermarking plugins
     */
    public static List<OpenStegoPlugin> getWatermarkingPlugins() {
        OpenStegoPlugin plugin = null;
        List<OpenStegoPlugin> dhPlugins = new ArrayList<OpenStegoPlugin>();

        for (int i = 0; i < plugins.size(); i++) {
            plugin = plugins.get(i);
            if (plugin.getPurposes().contains(OpenStegoPlugin.Purpose.WATERMARKING)) {
                dhPlugins.add(plugin);
            }
        }
        return dhPlugins;
    }

    /**
     * Method to get the plugin object based on the name of the plugin
     *
     * @param name Name of the plugin
     * @return Plugin object
     */
    public static OpenStegoPlugin getPluginByName(String name) {
        return pluginsMap.get(name.toUpperCase());
    }
}
