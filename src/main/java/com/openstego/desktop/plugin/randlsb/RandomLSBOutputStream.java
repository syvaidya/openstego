/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.randlsb;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.lsb.LSBConfig;
import com.openstego.desktop.plugin.lsb.LSBDataHeader;
import com.openstego.desktop.plugin.lsb.LSBErrors;
import com.openstego.desktop.plugin.lsb.LSBPlugin;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.StringUtil;

/**
 * OutputStream to embed data into image
 */
public class RandomLSBOutputStream extends OutputStream {
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
    private Set<String> bitWritten = new HashSet<>();

    /**
     * Random number generator
     */
    private Random rand = null;

    /**
     * Default constructor
     *
     * @param image Source image into which data will be embedded
     * @param dataLength Length of the data that would be written to the image
     * @param fileName Name of the source data file
     * @param config Configuration data to use while writing
     * @throws OpenStegoException
     */
    public RandomLSBOutputStream(ImageHolder image, int dataLength, String fileName, OpenStegoConfig config) throws OpenStegoException {
        if (image == null || image.getImage() == null) {
            throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.NULL_IMAGE_ARGUMENT);
        }

        this.dataLength = dataLength;
        this.imgWidth = image.getImage().getWidth();
        this.imgHeight = image.getImage().getHeight();
        this.config = config;

        switch (image.getImage().getType()) {
            case BufferedImage.TYPE_INT_RGB:
                this.image = image;
                break;

            default:
                BufferedImage newImg = new BufferedImage(this.imgWidth, this.imgHeight, BufferedImage.TYPE_INT_RGB);
                this.image = new ImageHolder(newImg, image.getMetadata());
                for (int x = 0; x < this.imgWidth; x++) {
                    for (int y = 0; y < this.imgHeight; y++) {
                        newImg.setRGB(x, y, image.getImage().getRGB(x, y));
                    }
                }
        }

        this.channelBitsUsed = 1;
        this.fileName = fileName;

        // Initialize random number generator with seed generated using password
        this.rand = new Random(StringUtil.passwordHash(config.getPassword()));
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
            this.channelBitsUsed = channelBits;
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
        boolean bitValue = false;
        int x = 0;
        int y = 0;
        int channel = 0;
        int bit = 0;
        String key;

        for (int i = 0; i < 8; i++) {
            bitValue = ((data >> (7 - i)) & 0x1) == 0x1;

            do {
                x = this.rand.nextInt(this.imgWidth);
                y = this.rand.nextInt(this.imgHeight);
                channel = this.rand.nextInt(3);
                bit = this.rand.nextInt(this.channelBitsUsed);
                key = x + "_" + y + "_" + channel + "_" + bit;
            } while (this.bitWritten.contains(key));
            this.bitWritten.add(key);

            setPixelBit(x, y, channel, bit, bitValue);
        }
    }

    /**
     * Get the image containing the embedded data. Ideally, this should be called after the stream is closed.
     *
     * @return Image data
     */
    public ImageHolder getImage() {
        return this.image;
    }

    /**
     * Sets the pixel bit at the given location to the new value.
     *
     * @param x The x position of the pixel
     * @param y The y position of the pixel
     * @param channel The color channel of the bit
     * @param bit The position of the bit
     * @param bitValue The new bit value for the pixel
     */
    private void setPixelBit(int x, int y, int channel, int bit, boolean bitValue) {
        int pixel = 0;
        int newColor = 0;
        int newPixel = 0;

        // Get the pixel value
        pixel = this.image.getImage().getRGB(x, y);

        // Set the bit value
        if (bitValue) {
            newPixel = pixel | 1 << (bit + (channel * 8));
        } else {
            newColor = 0xfffffffe;
            for (int i = 0; i < (bit + (channel * 8)); i++) {
                newColor = (newColor << 1) | 0x1;
            }
            newPixel = pixel & newColor;
        }

        // Set the pixel value back in image
        this.image.getImage().setRGB(x, y, newPixel);
    }
}
