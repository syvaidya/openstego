/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.lsb;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.util.ImageHolder;

import java.io.InputStream;

/**
 * InputStream to read embedded data from image file using LSB algorithm
 */
public class LSBInputStream extends InputStream {
    /**
     * Image data
     */
    private final ImageHolder image;

    /**
     * Data header
     */
    private LSBDataHeader dataHeader = null;

    /**
     * Number of bits used per color channel
     */
    private int channelBitsUsed;

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
    private final int imgWidth;

    /**
     * Height of the image
     */
    private final int imgHeight;

    /**
     * Configuration data
     */
    private final OpenStegoConfig config;

    /**
     * Default constructor
     *
     * @param image  Image data to be read
     * @param config Configuration data to use while reading
     * @throws OpenStegoException Processing issues
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
     * @throws OpenStegoException Processing issues
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
     */
    @Override
    public int read() {
        int pixel;
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
        return ((bitSet[0] << 7) + (bitSet[1] << 6) + (bitSet[2] << 5) + (bitSet[3] << 4) + (bitSet[4] << 3)
                + (bitSet[5] << 2) + (bitSet[6] << 1) + bitSet[7]);
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
     * @param pixel Pixel value
     * @return Bit
     */
    private byte getCurrBitFromPixel(int pixel) {
        int group;
        int groupBit;

        group = this.currBit / this.channelBitsUsed;
        groupBit = this.currBit % this.channelBitsUsed;

        return (byte) (((pixel >> (16 - (group * 8))) >> (this.channelBitsUsed - groupBit - 1)) & 1);
    }
}
