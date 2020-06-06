/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.dctlsb;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.template.dct.DCTDataHeader;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.ImageUtil;
import com.openstego.desktop.util.StringUtil;
import com.openstego.desktop.util.dct.DCT;

/**
 * OutputStream to embed data into image
 */
public class DctLSBOutputStream extends OutputStream {
    /**
     * Output Image data
     */
    private ImageHolder image = null;

    /**
     * Length of the data
     */
    private int dataLength = 0;

    /**
     * Name of the source data file
     */
    private String fileName = null;

    /**
     * Current message bit number
     */
    private int n = 0;

    /**
     * Actual width of the image
     */
    private int actualImgWidth = 0;

    /**
     * Actual height of the image
     */
    private int actualImgHeight = 0;

    /**
     * Width of the image rounded to 8 (So that 8x8 blocks can be created from the image)
     */
    private int imgWidth = 0;

    /**
     * Height of the image rounded to 8 (So that 8x8 blocks can be created from the image)
     */
    private int imgHeight = 0;

    /**
     * Array to store Y component from YUV colorspace of the image
     */
    private int[][] y = null;

    /**
     * Array to store U component from YUV colorspace of the image
     */
    private int[][] u = null;

    /**
     * Array to store V component from YUV colorspace of the image
     */
    private int[][] v = null;

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
     * @param image Source image into which data will be embedded
     * @param dataLength Length of the data that would be written to the image
     * @param fileName Name of the source data file
     * @param config Configuration data to use while writing
     * @throws OpenStegoException
     */
    public DctLSBOutputStream(ImageHolder image, int dataLength, String fileName, OpenStegoConfig config) throws OpenStegoException {
        List<int[][]> yuv = null;

        if (image == null) {
            throw new IllegalArgumentException("No image provided");
        }

        this.dataLength = dataLength;
        this.actualImgWidth = image.getImage().getWidth();
        this.actualImgHeight = image.getImage().getHeight();
        this.config = config;
        this.fileName = fileName;
        BufferedImage newImg = new BufferedImage(this.actualImgWidth, this.actualImgHeight, BufferedImage.TYPE_INT_RGB);
        this.image = new ImageHolder(newImg, image.getMetadata());

        // Calculate width and height rounded to 8
        this.imgWidth = this.actualImgWidth - (this.actualImgWidth % DCT.NJPEG);
        this.imgHeight = this.actualImgHeight - (this.actualImgHeight % DCT.NJPEG);

        yuv = ImageUtil.getYuvFromImage(image.getImage());
        this.y = yuv.get(0);
        this.u = yuv.get(1);
        this.v = yuv.get(2);
        for (int i = 0; i < this.actualImgWidth; i++) {
            for (int j = 0; j < this.actualImgHeight; j++) {
                this.image.getImage().setRGB(i, j, image.getImage().getRGB(i, j));
            }
        }

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
     * @throws OpenStegoException
     */
    private void writeHeader() throws OpenStegoException {
        DCTDataHeader header = null;

        try {
            header = new DCTDataHeader(this.dataLength, this.fileName, this.config);

            if (((header.getHeaderSize() + this.dataLength) * 8) > (this.imgWidth * this.imgHeight / (DCT.NJPEG * DCT.NJPEG))) {
                throw new OpenStegoException(null, DctLSBPlugin.NAMESPACE, DctLSBErrors.IMAGE_SIZE_INSUFFICIENT);
            }
            this.coord = new Coordinates((header.getHeaderSize() + this.dataLength) * 8);
            write(header.getHeaderData());
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Implementation of <code>OutputStream.write(int)</code> method
     *
     * @param data Byte to be written
     * @throws IOException
     */
    @Override
    public void write(int data) throws IOException {
        int xb = 0;
        int yb = 0;
        int coeffNum = 0;
        int coeff = 0;

        for (int count = 0; count < 8; count++) {
            if (this.n >= (this.imgWidth * this.imgHeight * 8)) {
                throw new IOException("Image size insufficient");
            }

            // Randomly select a block, check to get distinct blocks (don't use a block twice)
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

            // Read the coefficient value and replace its LSB based on the message bit
            coeff = (int) this.dcts[coeffNum / DCT.NJPEG][coeffNum % DCT.NJPEG];
            if (((data >> (7 - count)) & 1) == 1) {
                coeff |= 1;
            } else {
                coeff &= ~(1);
            }
            this.dcts[coeffNum / DCT.NJPEG][coeffNum % DCT.NJPEG] = coeff;

            // Dequantize the block
            this.dct.dequantize8x8(this.dcts);

            // Do the inverse DCT on the modified 8x8 block
            this.dct.invDctBlock8x8(this.dcts, this.y, xb * DCT.NJPEG, yb * DCT.NJPEG);

            this.n++;
        }
    }

    /**
     * Get the image containing the embedded data. Ideally, this should be called after the stream is closed.
     *
     * @param imgType Type of image
     * @return Image data
     */
    public ImageHolder getImage(int imgType) {
        List<int[][]> yuv = new ArrayList<int[][]>();
        yuv.add(this.y);
        yuv.add(this.u);
        yuv.add(this.v);

        this.image.setImage(ImageUtil.getImageFromYuv(yuv, imgType));
        return this.image;
    }
}
