/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util.dwt;

/**
 * Object to store Image data
 */
public class Image {
    /**
     * Image data
     */
    private double[] data = null;

    /**
     * Image width
     */
    int width = 0;

    /**
     * Image height
     */
    int height = 0;

    /**
     * Default constructor
     *
     * @param width Width of the image
     * @param height Height of the image
     */
    public Image(int width, int height) {
        this.data = new double[width * height];
        this.width = width;
        this.height = height;
    }

    /**
     * Get method for data
     *
     * @return data
     */
    public double[] getData() {
        return this.data;
    }

    /**
     * Set method for data
     *
     * @param data
     */
    public void setData(double[] data) {
        this.data = data;
    }

    /**
     * Get method for width
     *
     * @return width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Set method for width
     *
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get method for height
     *
     * @return height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Set method for height
     *
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
    }
}