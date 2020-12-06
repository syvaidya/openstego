/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.dwtxie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.template.image.WMImagePluginTemplate;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.ImageUtil;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.StringUtil;
import com.openstego.desktop.util.dwt.DWT;
import com.openstego.desktop.util.dwt.DWTUtil;
import com.openstego.desktop.util.dwt.ImageTree;

/**
 * Plugin for OpenStego which implements the DWT based algorithm by Xie.
 * <p>
 * This class is based on the code provided by Peter Meerwald at: <a
 * href="http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/">http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/</a>
 * <p>
 * Refer to his thesis on watermarking: Peter Meerwald, Digital Image Watermarking in the Wavelet Transfer Domain,
 * Master's Thesis, Department of Scientific Computing, University of Salzburg, Austria, January 2001.
 */
public class DWTXiePlugin extends WMImagePluginTemplate {
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(DWTXiePlugin.NAMESPACE);

    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "DWTXIE";

    /**
     * Default constructor
     */
    public DWTXiePlugin() {
        LabelUtil.addNamespace(NAMESPACE, "i18n.DWTXiePluginLabels");
        new DWTXieErrors(); // Initialize error codes
    }

    /**
     * Gives the name of the plugin
     *
     * @return Name of the plugin
     */
    @Override
    public String getName() {
        return "DWTXie";
    }

    /**
     * Gives a short description of the plugin
     *
     * @return Short description of the plugin
     */
    @Override
    public String getDescription() {
        return labelUtil.getString("plugin.description");
    }

    /**
     * Method to embed the message into the cover data
     *
     * @param msg Message to be embedded
     * @param msgFileName Name of the message file. If this value is provided, then the filename should be embedded in
     *        the cover data
     * @param cover Cover data into which message needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the message
     * @throws OpenStegoException
     */
    @Override
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName) throws OpenStegoException {
        ImageHolder image = null;
        List<int[][]> yuv = null;
        DWT dwt = null;
        ImageTree dwtTree = null;
        ImageTree p = null;
        Signature sig = null;
        Pixel pixel1 = null;
        Pixel pixel2 = null;
        Pixel pixel3 = null;
        int[][] luminance = null;
        int imgType = 0;
        int origWidth = 0;
        int origHeight = 0;
        int cols = 0;
        int rows = 0;
        int n = 0;
        double temp = 0.0;

        // Cover file is mandatory
        if (cover == null) {
            throw new OpenStegoException(null, NAMESPACE, DWTXieErrors.ERR_NO_COVER_FILE);
        } else {
            image = ImageUtil.byteArrayToImage(cover, coverFileName);
        }

        imgType = image.getImage().getType();
        origWidth = image.getImage().getWidth();
        origHeight = image.getImage().getHeight();
        ImageUtil.makeImageSquare(image);

        cols = image.getImage().getWidth();
        rows = image.getImage().getHeight();
        yuv = ImageUtil.getYuvFromImage(image.getImage());
        luminance = yuv.get(0);
        sig = new Signature(msg);

        // Wavelet transform
        dwt = new DWT(cols, rows, sig.filterID, sig.embeddingLevel, sig.waveletFilterMethod);
        dwtTree = dwt.forwardDWT(luminance);

        p = dwtTree;
        // Consider each resolution level
        while (p.getLevel() < sig.embeddingLevel) {
            // Descend one level
            p = p.getCoarse();
        }

        // Repeat binary watermark by sliding a 3-pixel window of approximation image
        for (int row = 0; row < p.getImage().getHeight(); row++) {
            for (int col = 0; col < p.getImage().getWidth() - 3; col += 3) {
                // Get all three approximation pixels in window
                pixel1 = new Pixel(0, DWTUtil.getPixel(p.getImage(), col + 0, row));
                pixel2 = new Pixel(1, DWTUtil.getPixel(p.getImage(), col + 1, row));
                pixel3 = new Pixel(2, DWTUtil.getPixel(p.getImage(), col + 2, row));

                // Bring selected pixels in ascending order
                if (pixel1.value > pixel2.value) {
                    swapPix(pixel1, pixel2);
                }
                if (pixel2.value > pixel3.value) {
                    swapPix(pixel2, pixel3);
                }
                if (pixel1.value > pixel2.value) {
                    swapPix(pixel1, pixel2);
                }

                // Apply watermarking transformation (modify median pixel)
                temp = wmTransform(sig.embeddingStrength, pixel1.value, pixel2.value, pixel3.value,
                    getWatermarkBit(sig.watermark, n % (sig.watermarkLength * 8)));

                // Write modified pixel
                DWTUtil.setPixel(p.getImage(), col + pixel2.pos, row, temp);

                n++;
            }
        }

        dwt.inverseDWT(dwtTree, luminance);
        yuv.set(0, luminance);
        image.setImage(ImageUtil.getImageFromYuv(yuv, imgType));
        ImageUtil.cropImage(image, origWidth, origHeight);

        return ImageUtil.imageToByteArray(image, stegoFileName, this);
    }

    /**
     * Method to extract the message from the stego data
     *
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @param origSigData Optional signature data file for watermark
     * @return Extracted message
     * @throws OpenStegoException
     */
    @Override
    public byte[] extractData(byte[] stegoData, String stegoFileName, byte[] origSigData) throws OpenStegoException {
        List<Integer> sigBitList = new ArrayList<Integer>();
        ImageHolder image = null;
        DWT dwt = null;
        ImageTree dwtTree = null;
        ImageTree p = null;
        Signature sig = null;
        Pixel pixel1 = null;
        Pixel pixel2 = null;
        Pixel pixel3 = null;
        int[][] luminance = null;
        int cols = 0;
        int rows = 0;
        // int n = 0;

        image = ImageUtil.byteArrayToImage(stegoData, stegoFileName);
        ImageUtil.makeImageSquare(image);

        cols = image.getImage().getWidth();
        rows = image.getImage().getHeight();
        luminance = ImageUtil.getYuvFromImage(image.getImage()).get(0);
        sig = new Signature(origSigData);

        // Wavelet transform
        dwt = new DWT(cols, rows, sig.filterID, sig.embeddingLevel, sig.waveletFilterMethod);
        dwtTree = dwt.forwardDWT(luminance);

        p = dwtTree;
        // Consider each resolution level
        while (p.getLevel() < sig.embeddingLevel) {
            // Descend one level
            p = p.getCoarse();
        }

        // Repeat binary watermark by sliding a 3-pixel window of approximation image
        for (int row = 0; row < p.getImage().getHeight(); row++) {
            for (int col = 0; col < p.getImage().getWidth() - 3; col += 3) {
                // Get all three approximation pixels in window
                pixel1 = new Pixel(0, DWTUtil.getPixel(p.getImage(), col + 0, row));
                pixel2 = new Pixel(1, DWTUtil.getPixel(p.getImage(), col + 1, row));
                pixel3 = new Pixel(2, DWTUtil.getPixel(p.getImage(), col + 2, row));

                // Bring selected pixels in ascending order
                if (pixel1.value > pixel2.value) {
                    swapPix(pixel1, pixel2);
                }
                if (pixel2.value > pixel3.value) {
                    swapPix(pixel2, pixel3);
                }
                if (pixel1.value > pixel2.value) {
                    swapPix(pixel1, pixel2);
                }

                // Apply inverse watermarking transformation to get the bit value
                sigBitList.add(invWmTransform(sig.embeddingStrength, pixel1.value, pixel2.value, pixel3.value));
                // n++;
            }
        }
        sig.setWatermark(convertBitListToByteArray(sigBitList));

        return sig.getSigData();
    }

    /**
     * Method to generate the signature data
     *
     * @return Signature data
     * @throws OpenStegoException
     */
    @Override
    public byte[] generateSignature() throws OpenStegoException {
        Random rand = null;
        Signature sig = null;

        rand = new Random(StringUtil.passwordHash(this.config.getPassword()));
        sig = new Signature(rand);

        return sig.getSigData();
    }

    /**
     * Method to check the correlation between original signature and the extracted watermark
     *
     * @param origSigData Original signature data
     * @param watermarkData Extracted watermark data
     * @return Correlation
     * @throws OpenStegoException
     */
    @Override
    public double getWatermarkCorrelation(byte[] origSigData, byte[] watermarkData) throws OpenStegoException {
        int corr = 0;
        Signature orig = new Signature(origSigData);
        Signature wm = new Signature(watermarkData);

        for (int i = 0; i < (wm.watermarkLength * 8); i++) {
            if (getWatermarkBit(orig.watermark, i % (orig.watermarkLength * 8)) == getWatermarkBit(wm.watermark, i)) {
                corr++;
            } else {
                corr--;
            }
        }

        return 0.5 + ((double) corr / (double) (wm.watermarkLength * 8)) / 2;
    }

    /**
     * Method to get the usage details of the plugin
     *
     * @return Usage details of the plugin
     * @throws OpenStegoException
     */
    @Override
    public String getUsage() throws OpenStegoException {
        return labelUtil.getString("plugin.usage");
    }

    /**
     * Watermarking transformation, set median pixel to quantization boundary
     */
    private double wmTransform(double alpha, double f1, double f2, double f3, int x) {
        double s = alpha * Math.abs(f3 - f1) / 2.0;
        double l = (x != 0) ? (f1 + s) : f1;

        while ((l + 2 * s) < f2) {
            l += 2 * s;
        }

        return ((f2 - l) < (l + 2 * s - f2)) ? l : (l + 2 * s);
    }

    /**
     * Inverse watermarking transformation, extract embedded bit, check quantization boundaries
     */
    private int invWmTransform(double alpha, double f1, double f2, double f3) {
        double s = alpha * Math.abs(f3 - f1) / 2.0;
        double l = f1;
        int x = 0;

        while (l < f2) {
            l += s;
            x++;
        }

        if (Math.abs(l - s - f2) < Math.abs(l - f2)) {
            return (x + 1) % 2;
        } else {
            return x % 2;
        }
    }

    /**
     * Method to get a bit value from the watermark
     *
     * @param watermark Watermark data
     * @param n Bit number
     * @return Bit value
     */
    private int getWatermarkBit(byte[] watermark, int n) {
        int byteNum = n >> 3;
        int bit = n & 7;

        return (watermark[byteNum] & (1 << bit)) >> bit;
    }

    /**
     * Method to set a bit value in the watermark
     *
     * @param watermark Watermark data
     * @param n Bit number
     * @param v Bit value
     */
    private void setWatermarkBit(byte[] watermark, int n, int v) {
        int byteNum = n >> 3;
        int bit = n & 7;

        if (v == 1) {
            watermark[byteNum] |= (1 << bit);
        } else {
            watermark[byteNum] &= ~(1 << bit);
        }
    }

    /**
     * Method to convert list of bits into byte array
     *
     * @param bitList List of bits
     * @return Byte array
     */
    private byte[] convertBitListToByteArray(List<Integer> bitList) {
        byte[] data = null;

        data = new byte[bitList.size() >> 3];
        for (int i = 0; i < ((bitList.size() >> 3) << 3); i++) {
            setWatermarkBit(data, i, (bitList.get(i)).intValue());
        }

        return data;
    }

    private void swapPix(Pixel pixel1, Pixel pixel2) {
        int tmpPixPos;
        double tmpPixVal;

        tmpPixPos = pixel1.pos;
        pixel1.pos = pixel2.pos;
        pixel2.pos = tmpPixPos;

        tmpPixVal = pixel1.value;
        pixel1.value = pixel2.value;
        pixel2.value = tmpPixVal;
    }

    /**
     * Private class for the data structure required for the signature
     */
    private class Signature {
        /**
         * Signature stamp
         */
        byte[] sig = "XESG".getBytes();

        /**
         * Length of the watermark (in bytes)
         */
        int watermarkLength = 64;

        /**
         * Embedding strength
         */
        double embeddingStrength = 0.5;

        /**
         * Wavelet filter method
         */
        int waveletFilterMethod = 2;

        /**
         * Filter number
         */
        int filterID = 1;

        /**
         * Embedding level
         */
        int embeddingLevel = 5;

        /**
         * Watermark data
         */
        byte[] watermark = null;

        /**
         * Constructor which generates the watermark data using the given randomizer
         *
         * @param rand Randomizer to use for generating watermark data
         */
        public Signature(Random rand) {
            this.watermark = new byte[this.watermarkLength];
            rand.nextBytes(this.watermark);
        }

        /**
         * Constructor that takes existing the signature data
         *
         * @param sigData Existing signature data
         * @throws OpenStegoException
         */
        public Signature(byte[] sigData) throws OpenStegoException {
            ObjectInputStream ois = null;
            byte[] inputSig = new byte[this.sig.length];

            try {
                ois = new ObjectInputStream(new ByteArrayInputStream(sigData));
                ois.read(inputSig, 0, this.sig.length);
                if (!(new String(this.sig)).equals(new String(inputSig))) {
                    throw new OpenStegoException(null, NAMESPACE, DWTXieErrors.ERR_SIG_NOT_VALID);
                }

                this.watermarkLength = ois.readInt();
                this.embeddingStrength = ois.readDouble();
                this.waveletFilterMethod = ois.readInt();
                this.filterID = ois.readInt();
                this.embeddingLevel = ois.readInt();

                this.watermark = new byte[this.watermarkLength];
                ois.read(this.watermark);
            } catch (IOException ioEx) {
                throw new OpenStegoException(ioEx);
            }
        }

        /**
         * Get the signature data generated
         *
         * @return Signature data
         * @throws OpenStegoException
         */
        public byte[] getSigData() throws OpenStegoException {
            ByteArrayOutputStream baos = null;
            ObjectOutputStream oos = null;

            try {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.write(this.sig);
                oos.writeInt(this.watermarkLength);
                oos.writeDouble(this.embeddingStrength);
                oos.writeInt(this.waveletFilterMethod);
                oos.writeInt(this.filterID);
                oos.writeInt(this.embeddingLevel);
                oos.write(this.watermark);
                oos.flush();
                oos.close();

                return baos.toByteArray();
            } catch (IOException ioEx) {
                throw new OpenStegoException(ioEx);
            }
        }

        /**
         * Method to replace the watermark data
         *
         * @param watermark Watermark data
         */
        public void setWatermark(byte[] watermark) {
            this.watermark = watermark;
            this.watermarkLength = watermark.length;
        }
    }

    private class Pixel {
        int pos = 0;
        double value = 0.0;

        public Pixel(int pos, double value) {
            this.pos = pos;
            this.value = value;
        }
    }
}