/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Modified by: Jordan Fava for experimental purposes
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.randlsb;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.lsb.LSBConfig;
import com.openstego.desktop.plugin.lsb.LSBDataHeader;
import com.openstego.desktop.plugin.lsb.LSBErrors;
import com.openstego.desktop.plugin.lsb.LSBPlugin;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.StringUtil;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * OutputStream to embed data into image
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

        // Initialize random number generator with seed generated using password
        this.rand = new Random(StringUtil.passwordHash(config.getPassword()));

        writeHeader();
    }

    /**
     * Step 1 & Step 2: Removed capacity check and increased bits per channel up to 8
     */
    private void writeHeader() throws OpenStegoException {
        int channelBits = 1;
        int noOfPixels = this.imgWidth * this.imgHeight;
        int headerSize;

        try {
            LSBDataHeader header = new LSBDataHeader(this.dataLength, channelBits, this.fileName, this.config);
            headerSize = header.getHeaderSize();

            // Step 1 & 2: Force embedding even if image normally too small, allow up to 8 bits per channel
            while ((noOfPixels * 3 * channelBits) / 8.0 < (headerSize + this.dataLength)) {
                channelBits++;
                if (channelBits > 8) { // Step 2: max 8 bits per channel
                    break; // allow overflow for experiment
                }
            }

            header.setChannelBitsUsed(channelBits);

            write(header.getHeaderData());
            this.channelBitsUsed = channelBits;
        } catch (OpenStegoException osEx) {
            throw osEx;
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        }
    }

    @Override
    public void write(int data) {
        boolean bitValue;
        int x, y, channel, bit;
        String key;

        for (int i = 0; i < 8; i++) {
            bitValue = ((data >> (7 - i)) & 0x1) == 0x1;

            // Step 3: Prevent overwriting already written bits
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
            newPixel = pixel | 1 << (bit + (channel * 8));
        } else {
            int newColor = 0xfffffffe;
            for (int i = 0; i < (bit + (channel * 8)); i++) {
                newColor = (newColor << 1) | 0x1;
            }
            newPixel = pixel & newColor;
        }

        this.image.getImage().setRGB(x, y, newPixel);
    }
}

