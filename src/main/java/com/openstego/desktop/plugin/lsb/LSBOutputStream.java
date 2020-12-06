/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.lsb;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.LabelUtil;

/**
 * OutputStream to embed data into image
 */
public class LSBOutputStream extends OutputStream {
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(LSBPlugin.NAMESPACE);

    /**
     * Output Image data
     */
    private ImageHolder image = null;

    /**
     * Number of bits used per color channel
     */
    private int channelBitsUsed = 1;

    /**
     * Length of the data
     */
    private int dataLength = 0;

    /**
     * Name of the source data file
     */
    private String fileName = null;

    /**
     * Current x co-ordinate
     */
    private int x = 0;

    /**
     * Current y co-ordinate
     */
    private int y = 0;

    /**
     * Current bit number to be read
     */
    private int currBit = 0;

    /**
     * Bit set to store three bits per pixel
     */
    private byte[] bitSet = null;

    /**
     * Width of the image
     */
    private int imgWidth = 0;

    /**
     * Height of the image
     */
    private int imgHeight = 0;

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
    public LSBOutputStream(ImageHolder image, int dataLength, String fileName, OpenStegoConfig config) throws OpenStegoException {
        if (image == null || image.getImage() == null) {
            throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.NULL_IMAGE_ARGUMENT);
        }

        this.dataLength = dataLength;
        this.imgWidth = image.getImage().getWidth();
        this.imgHeight = image.getImage().getHeight();
        this.config = config;
        BufferedImage newImg = new BufferedImage(this.imgWidth, this.imgHeight, BufferedImage.TYPE_INT_RGB);
        this.image = new ImageHolder(newImg, image.getMetadata());
        for (int x = 0; x < this.imgWidth; x++) {
            for (int y = 0; y < this.imgHeight; y++) {
                newImg.setRGB(x, y, image.getImage().getRGB(x, y));
            }
        }

        this.channelBitsUsed = 1;
        this.fileName = fileName;
        this.bitSet = new byte[3];
        writeHeader();
    }

    /**
     * Method to write header data to stream
     *
     * @throws OpenStegoException
     */
    private void writeHeader() throws OpenStegoException {
        int channelBits = 1;
        int noOfPixels = 0;
        int headerSize = 0;
        LSBDataHeader header = null;

        try {
            noOfPixels = this.imgWidth * this.imgHeight;
            header = new LSBDataHeader(this.dataLength, channelBits, this.fileName, this.config);
            headerSize = header.getHeaderSize();

            while (true) {
                if ((noOfPixels * 3 * channelBits) / 8.0 < (headerSize + this.dataLength)) {
                    channelBits++;
                    if (channelBits > ((LSBConfig) this.config).getMaxBitsUsedPerChannel()) {
                        throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.IMAGE_SIZE_INSUFFICIENT);
                    }
                } else {
                    break;
                }
            }

            // Update channelBitsUsed in the header, and write to image
            header.setChannelBitsUsed(channelBits);
            write(header.getHeaderData());

            if (this.currBit != 0) {
                this.currBit = 0;
                writeCurrentBitSet();
                nextPixel();
            }

            this.channelBitsUsed = channelBits;
            this.bitSet = new byte[3 * channelBits];
        } catch (OpenStegoException osEx) {
            throw osEx;
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
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
        for (int bit = 0; bit < 8; bit++) {
            this.bitSet[this.currBit] = (byte) ((data >> (7 - bit)) & 1);
            this.currBit++;
            if (this.currBit == this.bitSet.length) {
                this.currBit = 0;
                writeCurrentBitSet();
                nextPixel();
            }
        }
    }

    /**
     * Flushes the stream
     *
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
        writeCurrentBitSet();
    }

    /**
     * Closes the stream
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if (this.currBit != 0) {
            for (int i = this.currBit; i < this.bitSet.length; i++) {
                this.bitSet[i] = 0;
            }
            this.currBit = 0;
            writeCurrentBitSet();
            nextPixel();
        }
        super.close();
    }

    /**
     * Get the image containing the embedded data. Ideally, this should be called after the stream is closed.
     *
     * @return Image data
     * @throws OpenStegoException
     */
    public ImageHolder getImage() throws OpenStegoException {
        try {
            flush();
        } catch (IOException ioEx) {
            throw new OpenStegoException(ioEx);
        }
        return this.image;
    }

    /**
     * Method to write current bit set
     *
     * @throws IOException
     */
    private void writeCurrentBitSet() throws IOException {
        int pixel = 0;
        int offset = 0;
        int mask = 0;
        int maskPerByte = 0;
        int bitOffset = 0;

        if (this.y == this.imgHeight) {
            throw new IOException(labelUtil.getString("err.image.insufficientSize"));
        }

        maskPerByte = (int) (Math.pow(2, this.channelBitsUsed) - 1);
        mask = (maskPerByte << 16) + (maskPerByte << 8) + maskPerByte;
        pixel = this.image.getImage().getRGB(this.x, this.y) & (0xFFFFFFFF - mask);

        for (int bit = 0; bit < 3; bit++) {
            bitOffset = 0;
            for (int i = 0; i < this.channelBitsUsed; i++) {
                bitOffset = (bitOffset << 1) + this.bitSet[(bit * this.channelBitsUsed) + i];
            }
            offset = (offset << 8) + bitOffset;
        }
        this.image.getImage().setRGB(this.x, this.y, pixel + offset);
    }

    /**
     * Method to move on to next pixel
     */
    private void nextPixel() {
        this.x++;
        if (this.x == this.imgWidth) {
            this.x = 0;
            this.y++;
        }
    }
}
