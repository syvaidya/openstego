/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.randlsb;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.lsb.LSBDataHeader;
import com.openstego.desktop.plugin.lsb.LSBErrors;
import com.openstego.desktop.plugin.lsb.LSBPlugin;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.StringUtil;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * InputStream to read embedded data from image file using Random LSB algorithm
 */
public class RandomLSBInputStream extends InputStream {
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
     * Array for bits in the image
     */
    private final Set<String> bitRead = new HashSet<>();

    /**
     * Random number generator
     */
    private final Random rand;

    /**
     * Default constructor
     *
     * @param image  Image data to be read
     * @param config Configuration data to use while reading
     * @throws OpenStegoException Processing issues
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
     * @throws OpenStegoException Processing issues
     */
    private void readHeader() throws OpenStegoException {
        this.dataHeader = new LSBDataHeader(this, this.config);
        this.channelBitsUsed = this.dataHeader.getChannelBitsUsed();
    }

    /**
     * Implementation of <code>InputStream.read()</code> method
     *
     * @return Byte read from the stream
     */
    @Override
    public int read() {
        byte[] bitSet = new byte[8];
        int x;
        int y;
        int channel;
        int bit;
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
     * Gets a particular bit in the image, and puts it into the LSB of an integer.
     *
     * @param x       The x position of the pixel on the image
     * @param y       The y position of the pixel on the image
     * @param channel The color channel containing the bit
     * @param bit     The bit position
     * @return The bit at the given position, as the LSB of an integer
     */
    private int getPixelBit(int x, int y, int channel, int bit) {
        return ((this.image.getImage().getRGB(x, y) >> ((channel * 8) + bit)) & 0x1);
    }
}
