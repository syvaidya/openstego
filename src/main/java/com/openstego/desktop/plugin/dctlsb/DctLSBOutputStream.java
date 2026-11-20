/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.dctlsb;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.template.dct.DCTDataHeader;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.ImageUtil;
import com.openstego.desktop.util.StringUtil;
import com.openstego.desktop.util.dct.DCT;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * OutputStream to embed data into image
 */
public class DctLSBOutputStream extends OutputStream {
    private static final int MAX_ATTEMPTS = 33;
    private final ImageHolder image;
    private final int[][] blockY = new int[DCT.NJPEG][DCT.NJPEG];
    private final int[][] blockU = new int[DCT.NJPEG][DCT.NJPEG];
    private final int[][] blockV = new int[DCT.NJPEG][DCT.NJPEG];
    private final List<int[][]> block = Arrays.asList(this.blockY, this.blockU, this.blockV);
    private final double[][] original = new double[DCT.NJPEG][DCT.NJPEG];
    private final int[] pixels = new int[DCT.NJPEG * DCT.NJPEG];

    /**
     * Length of the data
     */
    private final int dataLength;

    /**
     * Name of the source data file
     */
    private final String fileName;

    /**
     * Current message bit number
     */
    private int n = 0;
    private int expectedBits;
    private boolean failed;

    /**
     * Width of the image rounded to 8 (So that 8x8 blocks can be created from the image)
     */
    private final int imgWidth;

    /**
     * Height of the image rounded to 8 (So that 8x8 blocks can be created from the image)
     */
    private final int imgHeight;

    /**
     * Array to store Y component from YUV colorspace of the image
     */
    private final int[][] y;

    /**
     * Array to store U component from YUV colorspace of the image
     */
    private final int[][] u;

    /**
     * Array to store V component from YUV colorspace of the image
     */
    private final int[][] v;

    /**
     * Object to handle DCT transforms
     */
    private final DCT dct;

    /**
     * Array to store the DCT coefficients for the image
     */
    private final double[][] dcts;

    /**
     * Coordinate hit check class
     */
    private Coordinates coord;

    /**
     * Random number generator
     */
    private final Random rand;

    /**
     * Configuration data
     */
    private final OpenStegoConfig config;

    /**
     * Default constructor
     *
     * @param image      Source image into which data will be embedded
     * @param dataLength Length of the data that would be written to the image
     * @param fileName   Name of the source data file
     * @param config     Configuration data to use while writing
     * @param imgType    Type of image to produce
     * @throws OpenStegoException Processing issues
     */
    public DctLSBOutputStream(ImageHolder image, int dataLength, String fileName, OpenStegoConfig config, int imgType)
            throws OpenStegoException {
        List<int[][]> yuv;

        if (image == null) {
            throw new IllegalArgumentException("No image provided");
        }

        this.dataLength = dataLength;
        // Actual width of the image
        int actualImgWidth = image.getImage().getWidth();
        // Actual height of the image
        int actualImgHeight = image.getImage().getHeight();
        this.config = config;
        this.fileName = fileName;

        // Calculate width and height rounded to 8
        this.imgWidth = actualImgWidth - (actualImgWidth % DCT.NJPEG);
        this.imgHeight = actualImgHeight - (actualImgHeight % DCT.NJPEG);

        yuv = ImageUtil.getYuvFromImage(image.getImage());
        this.y = yuv.get(0);
        this.u = yuv.get(1);
        this.v = yuv.get(2);
        this.image = new ImageHolder(ImageUtil.getImageFromYuv(yuv.subList(0, 3), imgType), image.getMetadata());
        this.dct = new DCT();
        this.dct.initDct8x8();
        this.dct.initQuantumJpegLumin();
        this.dcts = new double[DCT.NJPEG][DCT.NJPEG];

        this.rand = new Random(StringUtil.passwordHash(config.getPassword()));
        writeHeader();
    }

    /**
     * Method to write header data to stream
     *
     * @throws OpenStegoException Writing issues
     */
    private void writeHeader() throws OpenStegoException {
        DCTDataHeader header;

        try {
            header = new DCTDataHeader(this.dataLength, this.fileName, this.config);

            long bits = (header.getHeaderSize() + (long) this.dataLength) * 8;
            if (this.dataLength < 0 || bits > (long) this.imgWidth * this.imgHeight / (DCT.NJPEG * DCT.NJPEG)
                    || bits > Integer.MAX_VALUE) {
                throw new OpenStegoException(null, DctLSBPlugin.NAMESPACE, DctLSBErrors.IMAGE_SIZE_INSUFFICIENT);
            }
            this.expectedBits = (int) bits;
            this.coord = new Coordinates(this.expectedBits);
            write(header.getHeaderData());
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Implementation of <code>OutputStream.write(int)</code> method
     *
     * @param data Byte to be written
     * @throws IOException Writing issues
     */
    @Override
    public void write(int data) throws IOException {
        int xb;
        int yb;
        int coeffNum;

        if (this.failed) {
            throw new IOException("DctLSB output is incomplete after a write failure");
        }
        if (this.n >= this.expectedBits) {
            throw new IOException("Declared DctLSB data length exceeded");
        }

        for (int count = 0; count < 8; count++) {
            // Randomly select a block, check to get distinct blocks (don't use a block twice)
            do {
                xb = Math.abs(this.rand.nextInt()) % (this.imgWidth / DCT.NJPEG);
                yb = Math.abs(this.rand.nextInt()) % (this.imgHeight / DCT.NJPEG);
            } while (!this.coord.add(xb, yb));

            // Randomly select a coefficient. Only accept coefficient in the middle frequency range
            do {
                coeffNum = (Math.abs(this.rand.nextInt()) % (DCT.NJPEG * DCT.NJPEG - 2)) + 1;
            } while (this.dct.isMidFreqCoeff8x8(coeffNum) == 0);

            writeBit(xb * DCT.NJPEG, yb * DCT.NJPEG, coeffNum, (data >>> (7 - count)) & 1);
            this.n++;
        }
    }

    /**
     * Get the image containing the embedded data. Ideally, this should be called after the stream is closed.
     *
     * @return Image data
     * @throws OpenStegoException Processing issues
     */
    public ImageHolder getImage() throws OpenStegoException {
        if (this.failed || this.n != this.expectedBits) {
            throw new OpenStegoException(new IOException("DctLSB output is incomplete"));
        }
        return this.image;
    }

    private void writeBit(int row, int column, int coefficient, int value) throws IOException {
        int size = DCT.NJPEG;
        this.dct.fwdDctBlock8x8(this.y, row, column, this.original);
        this.dct.quantize8x8(this.original);
        for (int i = 0; i < size; i++) {
            System.arraycopy(this.u[row + i], column, this.blockU[i], 0, size);
            System.arraycopy(this.v[row + i], column, this.blockV[i], 0, size);
        }

        int coefficientRow = coefficient / size;
        int coefficientColumn = coefficient % size;
        int first = ((int) this.original[coefficientRow][coefficientColumn] & ~1) | value;
        BufferedImage raster = this.image.getImage().getSubimage(column, row, size, size);
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            for (int i = 0; i < size; i++) {
                System.arraycopy(this.original[i], 0, this.dcts[i], 0, size);
            }

            // Try the original value first, then +/-2, +/-4, and so on without changing parity.
            int offset = (attempt % 2 == 0) ? -attempt : attempt + 1;
            this.dcts[coefficientRow][coefficientColumn] = first + offset;
            this.dct.dequantize8x8(this.dcts);
            this.dct.invDct8x8(this.dcts, this.blockY);

            BufferedImage rgb = ImageUtil.getImageFromYuv(this.block, BufferedImage.TYPE_INT_RGB);
            rgb.getRGB(0, 0, size, size, this.pixels, 0, size);
            raster.setRGB(0, 0, size, size, this.pixels, 0, size);
            this.dct.fwdDct8x8(ImageUtil.getYuvFromImage(raster).get(0), this.dcts);
            this.dct.quantize8x8(this.dcts);
            if (((int) this.dcts[coefficientRow][coefficientColumn] & 1) == value) {
                return;
            }
        }
        this.failed = true;
        throw new IOException("Cannot preserve a DctLSB bit in the requested image type");
    }
}
