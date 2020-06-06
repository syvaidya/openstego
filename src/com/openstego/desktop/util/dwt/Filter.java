/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util.dwt;

/**
 * Object to store Filter data
 */
public class Filter {
    /**
     * Constant for filter type = NoSymm
     */
    public static final int TYPE_NOSYMM = 0;

    /**
     * Constant for filter type = Symm
     */
    public static final int TYPE_SYMM = 1;

    /**
     * Constant for filter type = AntiSymm
     */
    public static final int TYPE_ANTISYMM = 2;

    /**
     * Constant for filter method = cutoff
     */
    public static final int METHOD_CUTOFF = 0;

    /**
     * Constant for filter method = inv_cutoff
     */
    public static final int METHOD_INVCUTOFF = 1;

    /**
     * Constant for filter method = periodical
     */
    public static final int METHOD_PERIODICAL = 2;

    /**
     * Constant for filter method = inv_periodical
     */
    public static final int METHOD_INVPERIODICAL = 3;

    /**
     * Constant for filter method = mirror,inv_mirror
     */
    public static final int METHOD_MIRROR = 4;

    /**
     * Constant for filter method = inv_mirror
     */
    public static final int METHOD_INVMIRROR = 5;

    /**
     * Type of the filter
     */
    private int type = -1;

    /**
     * Start value of the filter
     */
    private int start = 0;

    /**
     * End value of the filter
     */
    private int end = 0;

    /**
     * Flag to indicate whether this is hi-pass filter or not
     */
    private boolean hiPass = false;

    /**
     * List of associated data
     */
    private double[] data = null;

    /**
     * Get method for type
     *
     * @return type
     */
    public int getType() {
        return this.type;
    }

    /**
     * Set method for type
     *
     * @param type
     */
    public void setType(String type) {
        if (type.equalsIgnoreCase("nosymm")) {
            this.type = TYPE_NOSYMM;
        } else if (type.equalsIgnoreCase("symm")) {
            this.type = TYPE_SYMM;
        } else if (type.equalsIgnoreCase("antisymm")) {
            this.type = TYPE_ANTISYMM;
        } else {
            this.type = -1;
        }
    }

    /**
     * Get method for start
     *
     * @return start
     */
    public int getStart() {
        return this.start;
    }

    /**
     * Set method for start
     *
     * @param start
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Get method for end
     *
     * @return end
     */
    public int getEnd() {
        return this.end;
    }

    /**
     * Set method for end
     *
     * @param end
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Get method for hiPass
     *
     * @return hiPass
     */
    public boolean isHiPass() {
        return this.hiPass;
    }

    /**
     * Set method for hiPass
     *
     * @param hiPass
     */
    public void setHiPass(boolean hiPass) {
        this.hiPass = hiPass;
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
}