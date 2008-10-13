/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.util.dwt;

import java.util.ArrayList;

/**
 * Class to handle Discrete Wavelet Transforms (DWT).
 * 
 * This class is conversion of C to Java for the file "dwt.c" file provided by Peter Meerwald at:
 * http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/
 * 
 * Refer to his thesis on watermarking: Peter Meerwald, Digital Image Watermarking in the Wavelet Transfer Domain,
 * Master's Thesis, Department of Scientific Computing, University of Salzburg, Austria, January 2001.
 */
public class DWT
{
    private static ArrayList filterGHList = null;

    private String filterFile = "/net/sourceforge/openstego/plugin/template/dwt/filters.xml";

    private FilterGH[] filters = null;

    private int method = 0;

    private int cols = 0;

    private int rows = 0;

    private int level = 0;

    public DWT(int cols, int rows, int filter, int level, int method)
    {
        // Read the master filter file if it is not already loaded
        if(filterGHList == null)
        {
            filterGHList = FilterXMLReader.parse(filterFile);
        }

        filters = new FilterGH[level + 1];
        for(int i = 0; i <= level; i++)
        {
            filters[i] = (FilterGH) filterGHList.get(filter);
        }

        this.level = level;
        this.method = method;
        this.cols = cols;
        this.rows = rows;
    }

    public ImageTree forwardDWT(int[][] pixels)
    {
        Image image = null;
        ImageTree tree = null;

        image = new Image(cols, rows);

        for(int i = 0; i < rows; i++)
        {
            for(int j = 0; j < cols; j++)
            {
                DWTUtil.setPixel(image, j, i, pixels[i][j]);
            }
        }

        tree = DWTUtil.waveletTransform(image, level, filters, method);
        return tree;
    }

    public ImageTree forwardDWTwp(int[][] pixels)
    {
        Image image = null;
        ImageTree tree = null;

        image = new Image(cols, rows);

        for(int i = 0; i < rows; i++)
        {
            for(int j = 0; j < cols; j++)
            {
                DWTUtil.setPixel(image, j, i, pixels[i][j]);
            }
        }

        tree = DWTUtil.waveletTransformWp(image, 0, level, filters, method);
        return tree;
    }

    public void inverseDWT(ImageTree dwts, int[][] pixels)
    {
        Image image = null;

        image = DWTUtil.inverseTransform(dwts, filters, method + 1);

        for(int i = 0; i < rows; i++)
        {
            for(int j = 0; j < cols; j++)
            {
                pixels[i][j] = DWTUtil.pixelRange((int) (DWTUtil.getPixel(image, j, i) + 0.5));
            }
        }
    }
}
