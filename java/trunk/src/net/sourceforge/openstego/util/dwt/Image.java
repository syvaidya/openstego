/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.util.dwt;

/**
 * Object to store Image data
 */
public class Image
{
    private double[] data = null;

    int width = 0;

    int height = 0;

    int size = 0;

    int bpp = 0;

    /**
     * Default constructor
     * 
     * @param width Width of the image
     * @param height Height of the image
     */
    public Image(int width, int height)
    {
        this.data = new double[width * height];
        this.width = width;
        this.height = height;
        this.size = width * height;
        this.bpp = 0;
    }

    /**
     * Get method for data
     * 
     * @return data
     */
    public double[] getData()
    {
        return data;
    }

    /**
     * Set method for data
     * 
     * @param data
     */
    public void setData(double[] data)
    {
        this.data = data;
    }

    /**
     * Get method for width
     * 
     * @return width
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Set method for width
     * 
     * @param width
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * Get method for height
     * 
     * @return height
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Set method for height
     * 
     * @param height
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    /**
     * Get method for bpp
     * 
     * @return bpp
     */
    public int getBpp()
    {
        return bpp;
    }

    /**
     * Set method for bpp
     * 
     * @param bpp
     */
    public void setBpp(int bpp)
    {
        this.bpp = bpp;
    }
}