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
    private double[] data;

    /**
     * Image width
     */
    int width;

    /**
     * Image height
     */
    int height;

    /**
     * Default constructor
     *
     * @param width  Width of the image
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
     * @param data Value to be set
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
     * @param width Value to be set
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
     * @param height Value to be set
     */
    public void setHeight(int height) {
        this.height = height;
    }
}