/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * Localized label handler for OpenStego
 */
public class LabelUtil
{
    /**
     * Static variable to hold the labels loaded from resource file
     */
    private static ResourceBundle labels = null;

    static
    {
        labels = ResourceBundle.getBundle("net.sourceforge.openstego.resource.LabelBundle", Locale.getDefault());
    }

    /**
     * Method to get label value for the given label key
     * @param key Key for the label
     * @return Display value for the label
     */
    public static String getString(String key)
    {
        return labels.getString(key);
    }

    /**
     * Method to get label value for the given label key (using optional parameters)
     * @param key Key for the label
     * @param parameters Parameters to pass for a parameterized label
     * @return Display value for the label
     */
    public static String getString(String key, Object[] parameters)
    {
        return MessageFormat.format(labels.getString(key), parameters);
    }
}
