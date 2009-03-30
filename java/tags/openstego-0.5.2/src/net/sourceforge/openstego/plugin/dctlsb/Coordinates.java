/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.dctlsb;

import java.util.HashMap;

/**
 * Class for storing co-ordinate hits
 */
public class Coordinates
{
    /**
     * Maximum size of the coordinate space
     */
    private int size = 0;

    /**
     * Map to store the hits
     */
    private HashMap map = new HashMap();

    /**
     * Default constructor
     * @param size Maximum size of the coordinate space
     */
    public Coordinates(int size)
    {
        this.size = size;
    }

    /**
     * Add coordinate to the space. If already hit, it returns false
     * @param x X-axis coordinate
     * @param y Y-axis coordinate
     * @return False, if coordinate already hit
     */
    public boolean add(int x, int y)
    {
        if(map.size() >= size)
        {
            throw new IllegalArgumentException("Exhausted the coordinate space");
        }

        String key = x + "," + y;
        if(map.containsKey(key))
        {
            return false;
        }
        else
        {
            map.put(key, key);
            return true;
        }
    }
}
