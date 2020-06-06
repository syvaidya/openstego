/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.dctlsb;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.template.dct.DCTDataHeader;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.ImageUtil;
import com.openstego.desktop.util.StringUtil;
import com.openstego.desktop.util.dct.DCT;

/**
 * InputStream to read embedded data from image file using DCT LSB algorithm
 */
public class DctLSBInputStream extends InputStream {
    /**
     * Data header
     */
    private DCTDataHeader dataHeader = null;

    /**
     * Current message bit number
     */
    private int n = 0;

    /**
     * Width of the image
     */
    private int imgWidth = 0;

    /**
     * Height of the image
     */
    private int imgHeight = 0;

    /**
     * Array to store Y component from YUV colorspace of the image
     */
    private int[][] y = null;

    /**
     * Object to handle DCT transforms
     */
    private DCT dct = null;

    /**
     * Array to store the DCT coefficients for the image
     */
    private double[][] dcts = null;

    /**
     * Coordinate hit check class
     */
    private Coordinates coord = null;

    /**
     * Random number generator
     */
    private Random rand = null;

    /**
     * Configuration data
     */
    private OpenStegoConfig config = null;

    /**
     * Default constructor
     *
     * @param image Image data to be read
     * @param config Configuration data to use while reading
     * @throws OpenStegoException
     */
    public DctLSBInputStream(ImageHolder image, OpenStegoConfig config) throws OpenStegoException {
        if (image == null || image.getImage() == null) {
            throw new IllegalArgumentException("No image file provided");
        }

        BufferedImage imgData = image.getImage();
        this.config = config;
        this.imgWidth = imgData.getWidth();
        this.imgHeight = imgData.getHeight();

        // Calculate widht and height rounded to 8
        this.imgWidth = this.imgWidth - (this.imgWidth % DCT.NJPEG);
        this.imgHeight = this.imgHeight - (this.imgHeight % DCT.NJPEG);

        this.y = ImageUtil.getYuvFromImage(imgData).get(0);

        this.dct = new DCT();
        this.dct.initDct8x8();
        this.dct.initQuantumJpegLumin();
        this.dcts = new double[DCT.NJPEG][DCT.NJPEG];
        this.coord = new Coordinates((this.imgWidth * this.imgHeight * 8) / (DCT.NJPEG * DCT.NJPEG));

        this.rand = new Random(StringUtil.passwordHash(this.config.getPassword()));
        readHeader();
    }

    /**
     * Method to read header data from the input stream
     *
     * @throws OpenStegoException
     */
    private void readHeader() throws OpenStegoException {
        this.dataHeader = new DCTDataHeader(this, this.config);
    }

    /**
     * Implementation of <code>InputStream.read()</code> method
     *
     * @return Byte read from the stream
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        int out = 0;
        int xb = 0;
        int yb = 0;
        int coeffNum = 0;

        for (int count = 0; count < 8; count++) {
            if (this.n >= (this.imgWidth * this.imgHeight * 8)) {
                return -1;
            }

            do {
                xb = Math.abs(this.rand.nextInt()) % (this.imgWidth / DCT.NJPEG);
                yb = Math.abs(this.rand.nextInt()) % (this.imgHeight / DCT.NJPEG);
            } while (!this.coord.add(xb, yb));

            // Do the forward 8x8 DCT of that block
            this.dct.fwdDctBlock8x8(this.y, xb * DCT.NJPEG, yb * DCT.NJPEG, this.dcts);

            // Randomly select a coefficient. Only accept coefficient in the middle frequency range
            do {
                coeffNum = (Math.abs(this.rand.nextInt()) % (DCT.NJPEG * DCT.NJPEG - 2)) + 1;
            } while (this.dct.isMidFreqCoeff8x8(coeffNum) == 0);

            // Quantize block according to quantization quality parameter
            this.dct.quantize8x8(this.dcts);

            // Get the LSB of the coefficient
            out = (out << 1) + (((int) this.dcts[coeffNum / DCT.NJPEG][coeffNum % DCT.NJPEG]) & 1);

            this.n++;
        }

        return out;
    }

    /**
     * Get method for dataHeader
     *
     * @return Data header
     */
    public DCTDataHeader getDataHeader() {
        return this.dataHeader;
    }
}
