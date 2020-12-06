/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Localized label handler for OpenStego
 */
public class LabelUtil {
    /**
     * Static variable to hold the map of labels loaded from resource files
     */
    private static Map<String, ResourceBundle> map = new HashMap<String, ResourceBundle>();

    /**
     * Static variable to store the namespace map
     */
    private static Map<String, LabelUtil> namespaceMap = new HashMap<String, LabelUtil>();

    /**
     * Method to add new namespace using resource bundle
     *
     * @param namespace Namespace for the labels
     * @param bundle Resource bundle name
     */
    public static void addNamespace(String namespace, String bundle) {
        map.put(namespace, ResourceBundle.getBundle(bundle, Locale.getDefault()));
    }

    /**
     * Method to get instance of LabelUtil based on the namespace
     *
     * @param namespace Namespace for the labels
     * @return Instance of LabelUtil
     */
    public static LabelUtil getInstance(String namespace) {
        LabelUtil util = null;

        util = namespaceMap.get(namespace);
        if (util == null) {
            util = new LabelUtil(namespace);
            namespaceMap.put(namespace, util);
        }

        return util;
    }

    /**
     * Variable to store the current namespace
     */
    private String namespace = null;

    /**
     * Constructor is protected
     *
     * @param namespace Namespace for the label
     */
    protected LabelUtil(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Method to get label value for the given label key
     *
     * @param key Key for the label
     * @return Display value for the label
     */
    public String getString(String key) {
        return (map.get(this.namespace)).getString(key);
    }

    /**
     * Method to get label value for the given label key (using optional parameters)
     *
     * @param key Key for the label
     * @param parameters Parameters to pass for a parameterized label
     * @return Display value for the label
     */
    public String getString(String key, Object... parameters) {
        return MessageFormat.format(getString(key), parameters);
    }
}
