/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Modified by Jordan Fava
 */

package com.openstego.desktop.plugin.randlsb;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.lsb.LSBConfig;
import com.openstego.desktop.plugin.lsb.LSBDataHeader;
import com.openstego.desktop.plugin.lsb.LSBPlugin;
import com.openstego.desktop.plugin.lsb.LSBErrors;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.StringUtil;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * OutputStream to embed data into image using random LSB method
 */
public class RandomLSBOutputStream extends OutputStream {
    private final ImageHolder image;
    private int channelBitsUsed;
    private final int dataLength;
    private final String fileName;
    private final int imgWidth;
    private final int imgHeight;
    private final OpenStegoConfig config;
    private final Set<String> bitWritten = new HashSet<>();
    private final Random rand;

    public RandomLSBOutputStream(ImageHolder image, int dataLength, String fileName, OpenStegoConfig config) throws OpenStegoException {
        if (image == null || image.getImage() == null) {
            throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.NULL_IMAGE_ARGUMENT);
        }

        this.dataLength = dataLength;
        this.imgWidth = image.getImage().getWidth();
        this.imgHeight = image.getImage().getHeight();
        this.config = config;

        // Convert to RGB if necessary
        if (image.getImage().getType() == BufferedImage.TYPE_INT_RGB) {
            this.image = image;
        } else {
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

        // Initialize random generator with password hash as seed
        this.rand = new Random(StringUtil.passwordHash(config.getPassword()));

        // Do NOT call writeHeader() in constructor to prevent recursion
    }

    // Call this method explicitly after constructing the object
    public void writeHeader() throws OpenStegoException {
        int channelBits = 1;
        int noOfPixels;
        int headerSize;
        LSBDataHeader header;

        try {
            noOfPixels = this.imgWidth * this.imgHeight;
            header = new LSBDataHeader(this.dataLength, channelBits, this.fileName, this.config);
            headerSize = header.getHeaderSize();

            // Increase bits per channel if needed
            while ((noOfPixels * 3 * channelBits) / 8.0 < (headerSize + this.dataLength)) {
                channelBits++;
                if (channelBits > ((LSBConfig) this.config).getMaxBitsUsedPerChannel()) {
                    throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.IMAGE_SIZE_INSUFFICIENT);
                }
            }

            header.setChannelBitsUsed(channelBits);
            writeHeaderData(header.getHeaderData()); // Use private method to avoid calling overridden write()
            this.channelBitsUsed = channelBits;
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        }
    }

    // Internal method to write header bytes directly
    private void writeHeaderData(byte[] headerData) {
        for (byte b : headerData) {
            writeByteDirect(b);
        }
    }

    // Write a byte directly to image without using bitWritten set
    private void writeByteDirect(int data) {
        for (int i = 0; i < 8; i++) {
            boolean bitValue = ((data >> (7 - i)) & 0x1) == 0x1;
            int x, y, channel, bit;
            do {
                x = this.rand.nextInt(this.imgWidth);
                y = this.rand.nextInt(this.imgHeight);
                channel = this.rand.nextInt(3);
                bit = this.rand.nextInt(this.channelBitsUsed);
            } while (false); // no bitWritten check for header

            setPixelBit(x, y, channel, bit, bitValue);
        }
    }

    @Override
    public void write(int data) {
        for (int i = 0; i < 8; i++) {
            boolean bitValue = ((data >> (7 - i)) & 0x1) == 0x1;

            int x, y, channel, bit;
            String key;
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

    public ImageHolder getImage() {
        return this.image;
    }

    private void setPixelBit(int x, int y, int channel, int bit, boolean bitValue) {
        int pixel = this.image.getImage().getRGB(x, y);
        int newPixel;

        if (bitValue) {
            newPixel = pixel | (1 << (bit + (channel * 8)));
        } else {
            int mask = ~(1 << (bit + (channel * 8)));
            newPixel = pixel & mask;
        }

        this.image.getImage().setRGB(x, y, newPixel);
    }
}

