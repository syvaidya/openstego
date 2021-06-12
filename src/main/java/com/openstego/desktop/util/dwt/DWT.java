/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util.dwt;

import com.openstego.desktop.util.ImageUtil;

import java.util.Map;

/**
 * Class to handle Discrete Wavelet Transforms (DWT).
 * <p>
 * This class is conversion of C to Java for the file "dwt.c" file provided by Peter Meerwald at:<a
 * href="http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/">http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/</a>
 * <p>
 * Refer to his thesis on watermarking: Peter Meerwald, Digital Image Watermarking in the Wavelet Transfer Domain,
 * Master's Thesis, Department of Scientific Computing, University of Salzburg, Austria, January 2001.
 */
public class DWT {
    /**
     * Master map of filters
     */
    private static Map<Integer, FilterGH> filterGHMap = null;

    /**
     * URI for the filter file
     */
    private static final String FILTER_FILE = "/dwt/filters.xml";

    /**
     * List of loaded filters
     */
    private final FilterGH[] filters;

    /**
     * Wavelet filtering method
     */
    private final int method;

    /**
     * No. of columns in the image
     */
    private final int cols;

    /**
     * No. of rows in the image
     */
    private final int rows;

    /**
     * Wavelet decomposition level
     */
    private final int level;

    /**
     * Default constructor
     *
     * @param cols     Image width
     * @param rows     Image height
     * @param filterID Filter ID to use
     * @param level    Decomposition level
     * @param method   Wavelet filtering method
     */
    public DWT(int cols, int rows, int filterID, int level, int method) {
        // Read the master filter file if it is not already loaded
        if (filterGHMap == null) {
            filterGHMap = FilterXMLReader.parse(FILTER_FILE);
        }

        this.filters = new FilterGH[level + 1];
        for (int i = 0; i <= level; i++) {
            this.filters[i] = filterGHMap.get(filterID);
        }

        this.level = level;
        this.method = method;
        this.cols = cols;
        this.rows = rows;
    }

    /**
     * Method to perform forward DWT on the pixel data
     *
     * @param pixels Image pixel data
     * @return Image tree data after DWT
     */
    public ImageTree forwardDWT(int[][] pixels) {
        Image image;
        ImageTree tree;

        image = new Image(this.cols, this.rows);

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                DWTUtil.setPixel(image, j, i, pixels[i][j]);
            }
        }

        tree = DWTUtil.waveletTransform(image, this.level, this.filters, this.method);
        return tree;
    }

    /**
     * Method to perform inverse DWT to get back the pixel data
     *
     * @param dwts   DWT data as image tree
     * @param pixels Image pixel data
     */
    public void inverseDWT(ImageTree dwts, int[][] pixels) {
        Image image;

        image = DWTUtil.inverseTransform(dwts, this.filters, this.method + 1);

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                pixels[i][j] = ImageUtil.pixelRange((int) (DWTUtil.getPixel(image, j, i) + 0.5));
            }
        }
    }
}
