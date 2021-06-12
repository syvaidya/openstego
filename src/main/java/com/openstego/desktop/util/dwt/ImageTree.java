/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util.dwt;

/**
 * Object to store Image tree data
 */
public class ImageTree {
    private ImageTree coarse = null;

    private ImageTree horizontal = null;

    private ImageTree vertical = null;

    private ImageTree diagonal = null;

    private Image image = null;

    private int level = 0;

    private int flag = 0;

    /**
     * Get method for coarse
     *
     * @return coarse
     */
    public ImageTree getCoarse() {
        return this.coarse;
    }

    /**
     * Set method for coarse
     *
     * @param coarse Value to be set
     */
    public void setCoarse(ImageTree coarse) {
        this.coarse = coarse;
    }

    /**
     * Get method for horizontal
     *
     * @return horizontal
     */
    public ImageTree getHorizontal() {
        return this.horizontal;
    }

    /**
     * Set method for horizontal
     *
     * @param horizontal Value to be set
     */
    public void setHorizontal(ImageTree horizontal) {
        this.horizontal = horizontal;
    }

    /**
     * Get method for vertical
     *
     * @return vertical
     */
    public ImageTree getVertical() {
        return this.vertical;
    }

    /**
     * Set method for vertical
     *
     * @param vertical Value to be set
     */
    public void setVertical(ImageTree vertical) {
        this.vertical = vertical;
    }

    /**
     * Get method for diagonal
     *
     * @return diagonal
     */
    public ImageTree getDiagonal() {
        return this.diagonal;
    }

    /**
     * Set method for diagonal
     *
     * @param diagonal Value to be set
     */
    public void setDiagonal(ImageTree diagonal) {
        this.diagonal = diagonal;
    }

    /**
     * Get method for image
     *
     * @return image
     */
    public Image getImage() {
        return this.image;
    }

    /**
     * Set method for image
     *
     * @param image Value to be set
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * Get method for level
     *
     * @return level
     */
    public int getLevel() {
        return this.level;
    }

    /**
     * Set method for level
     *
     * @param level Value to be set
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Get method for flag
     *
     * @return flag
     */
    public int getFlag() {
        return this.flag;
    }

    /**
     * Set method for flag
     *
     * @param flag Value to be set
     */
    public void setFlag(int flag) {
        this.flag = flag;
    }
}