/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.lsb;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.util.CommonUtil;

/**
 * This class holds the header data for the data that needs to be embedded in the image.
 * First, the header data gets written inside the image, and then the actual data is written.
 */
public class LSBDataHeader {
    /**
     * Magic string at the start of the header to identify OpenStego embedded data
     */
    public static final byte[] DATA_STAMP = "OPENSTEGO".getBytes();

    /**
     * Header version to distinguish between various versions of data embedding. This should be changed to next
     * version, in case the logic of embedding data is changed.
     */
    public static final byte[] HEADER_VERSION = new byte[] { (byte) 2 };

    /**
     * Length of the fixed portion of the header
     */
    private static final int FIXED_HEADER_LENGTH = 8;

    /**
     * Length of the encryption algorithm string
     */
    private static final int CRYPT_ALGO_LENGTH = 8;

    /**
     * Length of the data embedded in the image (excluding the header data)
     */
    private int dataLength = 0;

    /**
     * Number of bits used per color channel for embedding the data
     */
    private int channelBitsUsed = 0;

    /**
     * Name of the file being embedded in the image (as byte array)
     */
    private byte[] fileName = null;

    /**
     * OpenStegoConfig instance to hold the configuration data
     */
    private OpenStegoConfig config = null;

    /**
     * This constructor should normally be used when writing the data.
     *
     * @param dataLength Length of the data embedded in the image (excluding the header data)
     * @param channelBitsUsed Number of bits used per color channel for embedding the data
     * @param fileName Name of the file of data being embedded
     * @param config OpenStegoConfig instance to hold the configuration data
     */
    public LSBDataHeader(int dataLength, int channelBitsUsed, String fileName, OpenStegoConfig config) {
        this.dataLength = dataLength;
        this.channelBitsUsed = channelBitsUsed;
        this.config = config;

        if (fileName == null) {
            this.fileName = new byte[0];
        } else {
            try {
                this.fileName = fileName.getBytes("UTF-8");
            } catch (UnsupportedEncodingException unEx) {
                this.fileName = fileName.getBytes();
            }
        }
    }

    /**
     * This constructor should be used when reading embedded data from an InputStream.
     *
     * @param dataInStream Data input stream containing the embedded data
     * @param config OpenStegoConfig instance to hold the configuration data
     * @throws OpenStegoException
     */
    public LSBDataHeader(InputStream dataInStream, OpenStegoConfig config) throws OpenStegoException {
        int stampLen = 0;
        int versionLen = 0;
        int fileNameLen = 0;
        int channelBits = 0;
        byte[] header = null;
        byte[] cryptAlgo = null;
        byte[] stamp = null;
        byte[] version = null;

        stampLen = DATA_STAMP.length;
        versionLen = HEADER_VERSION.length;
        header = new byte[FIXED_HEADER_LENGTH];
        cryptAlgo = new byte[CRYPT_ALGO_LENGTH];
        stamp = new byte[stampLen];
        version = new byte[versionLen];

        try {
            dataInStream.read(stamp, 0, stampLen);
            if (!(new String(stamp)).equals(new String(DATA_STAMP))) {
                throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.INVALID_STEGO_HEADER);
            }

            dataInStream.read(version, 0, versionLen);
            if (!(new String(version)).equals(new String(HEADER_VERSION))) {
                throw new OpenStegoException(null, LSBPlugin.NAMESPACE, LSBErrors.INVALID_HEADER_VERSION);
            }

            dataInStream.read(header, 0, FIXED_HEADER_LENGTH);
            this.dataLength = (CommonUtil.byteToInt(header[0]) + (CommonUtil.byteToInt(header[1]) << 8) + (CommonUtil.byteToInt(header[2]) << 16)
                    + (CommonUtil.byteToInt(header[3]) << 32));
            channelBits = header[4];
            fileNameLen = header[5];
            config.setUseCompression(header[6] == 1);
            config.setUseEncryption(header[7] == 1);

            dataInStream.read(cryptAlgo, 0, CRYPT_ALGO_LENGTH);
            config.setEncryptionAlgorithm(new String(cryptAlgo).trim());

            if (fileNameLen == 0) {
                this.fileName = new byte[0];
            } else {
                this.fileName = new byte[fileNameLen];
                dataInStream.read(this.fileName, 0, fileNameLen);
            }
        } catch (OpenStegoException osEx) {
            throw osEx;
        } catch (Exception ex) {
            throw new OpenStegoException(ex);
        }

        this.channelBitsUsed = channelBits;
        this.config = config;
    }

    /**
     * This method generates the header in the form of byte array based on the parameters provided in the constructor.
     *
     * @return Header data
     */
    public byte[] getHeaderData() {
        byte[] out = null;
        int stampLen = 0;
        int versionLen = 0;
        int currIndex = 0;

        stampLen = DATA_STAMP.length;
        versionLen = HEADER_VERSION.length;
        out = new byte[stampLen + versionLen + FIXED_HEADER_LENGTH + CRYPT_ALGO_LENGTH + this.fileName.length];

        System.arraycopy(DATA_STAMP, 0, out, currIndex, stampLen);
        currIndex += stampLen;

        System.arraycopy(HEADER_VERSION, 0, out, currIndex, versionLen);
        currIndex += versionLen;

        out[currIndex++] = (byte) ((this.dataLength & 0x000000FF));
        out[currIndex++] = (byte) ((this.dataLength & 0x0000FF00) >> 8);
        out[currIndex++] = (byte) ((this.dataLength & 0x00FF0000) >> 16);
        out[currIndex++] = (byte) ((this.dataLength & 0xFF000000) >> 32);
        out[currIndex++] = (byte) this.channelBitsUsed;
        out[currIndex++] = (byte) this.fileName.length;
        out[currIndex++] = (byte) (this.config.isUseCompression() ? 1 : 0);
        out[currIndex++] = (byte) (this.config.isUseEncryption() ? 1 : 0);

        if (this.config.getEncryptionAlgorithm() != null) {
            System.arraycopy(this.config.getEncryptionAlgorithm().getBytes(), 0, out, currIndex,
                this.config.getEncryptionAlgorithm().getBytes().length);
        }
        currIndex += CRYPT_ALGO_LENGTH;

        if (this.fileName.length > 0) {
            System.arraycopy(this.fileName, 0, out, currIndex, this.fileName.length);
            currIndex += this.fileName.length;
        }

        return out;
    }

    /**
     * Get Method for channelBitsUsed
     *
     * @return channelBitsUsed
     */
    public int getChannelBitsUsed() {
        return this.channelBitsUsed;
    }

    /**
     * Set Method for channelBitsUsed
     *
     * @param channelBitsUsed
     */
    public void setChannelBitsUsed(int channelBitsUsed) {
        this.channelBitsUsed = channelBitsUsed;
    }

    /**
     * Get Method for dataLength
     *
     * @return dataLength
     */
    public int getDataLength() {
        return this.dataLength;
    }

    /**
     * Get Method for fileName
     *
     * @return fileName
     */
    public String getFileName() {
        String name = null;

        try {
            name = new String(this.fileName, "UTF-8");
        } catch (UnsupportedEncodingException unEx) {
            name = new String(this.fileName);
        }
        return name;
    }

    /**
     * Method to get size of the current header
     *
     * @return Header size
     */
    public int getHeaderSize() {
        return DATA_STAMP.length + HEADER_VERSION.length + FIXED_HEADER_LENGTH + CRYPT_ALGO_LENGTH + this.fileName.length;
    }

    /**
     * Method to get the maximum possible size of the header
     *
     * @return Maximum possible header size
     */
    public static int getMaxHeaderSize() {
        // Max file name length assumed to be 256
        return DATA_STAMP.length + HEADER_VERSION.length + FIXED_HEADER_LENGTH + CRYPT_ALGO_LENGTH + 256;
    }
}
