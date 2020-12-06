/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.lsb;

import java.io.IOException;
import java.io.InputStream;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.util.ImageHolder;

/**
 * InputStream to read embedded data from image file using LSB algorithm
 */
public class LSBInputStream extends InputStream {
    /**
     * Image data
     */
    private ImageHolder image = null;

    /**
     * Data header
     */
    private LSBDataHeader dataHeader = null;

    /**
     * Number of bits used per color channel
     */
    private int channelBitsUsed = 1;

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
     * @param image Image data to be read
     * @param config Configuration data to use while reading
     * @throws OpenStegoException
     */
    public LSBInputStream(ImageHolder image, OpenStegoConfig config) throws OpenStegoException {
        if (image == null || image.getImage() == null) {
            throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.NULL_IMAGE_ARGUMENT);
        }

        this.image = image;
        this.channelBitsUsed = 1;
        this.config = config;

        this.imgWidth = image.getImage().getWidth();
        this.imgHeight = image.getImage().getHeight();
        readHeader();
    }

    /**
     * Method to read header data from the input stream
     *
     * @throws OpenStegoException
     */
    private void readHeader() throws OpenStegoException {
        this.dataHeader = new LSBDataHeader(this, this.config);
        this.channelBitsUsed = this.dataHeader.getChannelBitsUsed();

        if (this.currBit != 0) {
            this.currBit = 0;
            this.x++;
            if (this.x == this.imgWidth) {
                this.x = 0;
                this.y++;
            }
        }
    }

    /**
     * Implementation of <code>InputStream.read()</code> method
     *
     * @return Byte read from the stream
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        int pixel = 0;
        byte[] bitSet = new byte[8];

        if (this.y == this.imgHeight) {
            return -1;
        }

        for (int i = 0; i < bitSet.length; i++) {
            pixel = this.image.getImage().getRGB(this.x, this.y);
            bitSet[i] = getCurrBitFromPixel(pixel);

            this.currBit++;
            if (this.currBit == (3 * this.channelBitsUsed)) {
                this.currBit = 0;
                this.x++;
                if (this.x == this.imgWidth) {
                    this.x = 0;
                    this.y++;
                    if (this.y == this.imgHeight) {
                        return -1;
                    }
                }
            }
        }
        return ((bitSet[0] << 7) + (bitSet[1] << 6) + (bitSet[2] << 5) + (bitSet[3] << 4) + (bitSet[4] << 3) + (bitSet[5] << 2) + (bitSet[6] << 1)
                + (bitSet[7] << 0));
    }

    /**
     * Get method for dataHeader
     *
     * @return Data header
     */
    public LSBDataHeader getDataHeader() {
        return this.dataHeader;
    }

    /**
     * Gets the bit from pixel based on the current bit
     *
     * @param pixel
     * @return Bit
     */
    private byte getCurrBitFromPixel(int pixel) {
        int group = 0;
        int groupBit = 0;

        group = this.currBit / this.channelBitsUsed;
        groupBit = this.currBit % this.channelBitsUsed;

        return (byte) (((pixel >> (16 - (group * 8))) >> (this.channelBitsUsed - groupBit - 1)) & 1);
    }
}
