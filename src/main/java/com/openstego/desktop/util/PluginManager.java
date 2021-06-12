/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.OpenStegoPlugin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static final List<OpenStegoPlugin<?>> plugins = new ArrayList<>();

    /**
     * Static variable to hold a map of available plugins
     */
    private static final Map<String, OpenStegoPlugin<?>> pluginsMap = new HashMap<>();

    /**
     * Method to load the stego plugin classes
     *
     * @throws OpenStegoException Processing issues
     */
    public static void loadPlugins() throws OpenStegoException {
        List<String> pluginList = new ArrayList<>();
        OpenStegoPlugin<?> plugin;

        // Load internal plugins
        try (InputStream is = PluginManager.class.getResourceAsStream("/OpenStegoPlugins.internal");
             InputStream isExt = PluginManager.class.getResourceAsStream("/OpenStegoPlugins.external")) {

            if (is != null) {
                pluginList.addAll(StringUtil.getStringLines(new String(CommonUtil.streamToBytes(is))));
            }

            // Load external plugins if available
            if (isExt != null) {
                pluginList.addAll(StringUtil.getStringLines(new String(CommonUtil.streamToBytes(isExt))));
            }

            for (String pluginClass : pluginList) {
                plugin = (OpenStegoPlugin<?>) Class.forName(pluginClass).getDeclaredConstructor().newInstance();
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
    @SuppressWarnings("unused")
    public static List<String> getPluginNames() {
        return plugins.stream().map(OpenStegoPlugin::getName).collect(Collectors.toList());
    }

    /**
     * Method to get the list of the loaded plugins
     *
     * @return List of the loaded plugins
     */
    public static List<OpenStegoPlugin<?>> getPlugins() {
        return plugins;
    }

    /**
     * Method to get the list of the data hiding plugins
     *
     * @return List of the data hiding plugins
     */
    public static List<OpenStegoPlugin<?>> getDataHidingPlugins() {
        List<OpenStegoPlugin<?>> dhPlugins = new ArrayList<>();

        for (OpenStegoPlugin<?> plugin : plugins) {
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
    public static List<OpenStegoPlugin<?>> getWatermarkingPlugins() {
        List<OpenStegoPlugin<?>> dhPlugins = new ArrayList<>();

        for (OpenStegoPlugin<?> plugin : plugins) {
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
    public static OpenStegoPlugin<?> getPluginByName(String name) {
        return pluginsMap.get(name.toUpperCase());
    }
}
