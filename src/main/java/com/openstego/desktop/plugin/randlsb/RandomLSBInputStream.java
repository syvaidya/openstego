/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.randlsb;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.lsb.LSBDataHeader;
import com.openstego.desktop.plugin.lsb.LSBErrors;
import com.openstego.desktop.plugin.lsb.LSBPlugin;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.StringUtil;

/**
 * InputStream to read embedded data from image file using Random LSB algorithm
 */
public class RandomLSBInputStream extends InputStream {
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
     * Array for bits in the image
     */
    private Set<String> bitRead = new HashSet<>();

    /**
     * Random number generator
     */
    private Random rand = null;

    /**
     * Default constructor
     *
     * @param image Image data to be read
     * @param config Configuration data to use while reading
     * @throws OpenStegoException
     */
    public RandomLSBInputStream(ImageHolder image, OpenStegoConfig config) throws OpenStegoException {
        if (image == null || image.getImage() == null) {
            throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.NULL_IMAGE_ARGUMENT);
        }

        this.image = image;
        this.channelBitsUsed = 1;
        this.config = config;

        this.imgWidth = image.getImage().getWidth();
        this.imgHeight = image.getImage().getHeight();

        // Initialize random number generator with seed generated using password
        this.rand = new Random(StringUtil.passwordHash(config.getPassword()));
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
    }

    /**
     * Implementation of <code>InputStream.read()</code> method
     *
     * @return Byte read from the stream
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        byte[] bitSet = new byte[8];
        int x = 0;
        int y = 0;
        int channel = 0;
        int bit = 0;
        String key;

        for (int i = 0; i < 8; i++) {
            do {
                x = this.rand.nextInt(this.imgWidth);
                y = this.rand.nextInt(this.imgHeight);
                channel = this.rand.nextInt(3);
                bit = this.rand.nextInt(this.channelBitsUsed);
                key = x + "_" + y + "_" + channel + "_" + bit;
            } while (this.bitRead.contains(key));
            this.bitRead.add(key);

            bitSet[i] = (byte) getPixelBit(x, y, channel, bit);
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
     * Gets a particular bit in the image, and puts it into the LSB of an integer.
     *
     * @param x The x position of the pixel on the image
     * @param y The y position of the pixel on the image
     * @param channel The color channel containing the bit
     * @param bit The bit position
     * @return The bit at the given position, as the LSB of an integer
     */
    public int getPixelBit(int x, int y, int channel, int bit) {
        return ((this.image.getImage().getRGB(x, y) >> ((channel * 8) + bit)) & 0x1);
    }
}
