/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.dwtkim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Random;

import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.template.image.WMImagePluginTemplate;
import com.openstego.desktop.util.CommonUtil;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.ImageUtil;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.StringUtil;
import com.openstego.desktop.util.dwt.DWT;
import com.openstego.desktop.util.dwt.DWTUtil;
import com.openstego.desktop.util.dwt.ImageTree;

/**
 * Plugin for OpenStego which implements the DWT based algorithm by Kim.
 * <p>
 * This class is based on the code provided by Peter Meerwald at: <a
 * href="http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/">http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/</a>
 * <p>
 * Refer to his thesis on watermarking: Peter Meerwald, Digital Image Watermarking in the Wavelet Transfer Domain,
 * Master's Thesis, Department of Scientific Computing, University of Salzburg, Austria, January 2001.
 */
public class DWTKimPlugin extends WMImagePluginTemplate {
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(DWTKimPlugin.NAMESPACE);

    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "DWTKIM";

    /**
     * Default constructor
     */
    public DWTKimPlugin() {
        LabelUtil.addNamespace(NAMESPACE, "i18n.DWTKimPluginLabels");
        new DWTKimErrors(); // Initialize error codes
    }

    /**
     * Gives the name of the plugin
     *
     * @return Name of the plugin
     */
    @Override
    public String getName() {
        return "DWTKim";
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
        int[][] luminance = null;
        int imgType = 0;
        int cols = 0;
        int rows = 0;
        int levels = 0;
        int currLevel = 0;
        int w = 0;
        double maxCoeff = 0.0;
        double alpha = 0.0;

        // Cover file is mandatory
        if (cover == null) {
            throw new OpenStegoException(null, NAMESPACE, DWTKimErrors.ERR_NO_COVER_FILE);
        } else {
            image = ImageUtil.byteArrayToImage(cover, coverFileName);
        }

        imgType = image.getImage().getType();
        cols = image.getImage().getWidth();
        rows = image.getImage().getHeight();
        yuv = ImageUtil.getYuvFromImage(image.getImage());
        luminance = yuv.get(0);
        sig = new Signature(msg);

        // Check that level is okay
        levels = DWTUtil.findDeepestLevel(cols, rows) - 1;
        if (sig.decompositionLevel > levels) {
            throw new OpenStegoException(null, NAMESPACE, DWTKimErrors.ERR_DECOMP_LEVEL_NOT_ENOUGH);
        }

        // Wavelet transform
        dwt = new DWT(cols, rows, sig.filterNumber, sig.decompositionLevel, sig.waveletFilterMethod);
        dwtTree = dwt.forwardDWT(luminance);

        p = dwtTree;
        w = 0;

        // process each decomposition level
        while (p.getCoarse() != null) {
            double threshold;

            // Get current decomposition level number
            currLevel = p.getHorizontal().getLevel();

            // Find largest absolute coefficient in detail subbands of current decomposition level
            maxCoeff = findLevelMaxCoeff(p);

            // Calculate significance threshold for current decomposition level
            threshold = calcLevelThreshold(maxCoeff);

            // Calculate embedding strength alpha for current decomposition level
            alpha = calcLevelAlphaDetail(sig.alphaForDetailSubBand, currLevel);

            // Embed watermark sequence into detail subbands of current decomposition level
            w = markSubBand(p.getHorizontal(), alpha, sig.watermark, threshold, w, sig.watermarkLength);
            w = markSubBand(p.getVertical(), alpha, sig.watermark, threshold, w, sig.watermarkLength);
            w = markSubBand(p.getDiagonal(), alpha, sig.watermark, threshold, w, sig.watermarkLength);

            p = p.getCoarse();
        }

        // Mark approximation image using calculated significance threshold and embedding strength
        w = markSubBand(p, sig.alphaForApproxSubBand, sig.watermark, calcLevelThreshold(findSubBandMaxCoeff(p, 1)), w, sig.watermarkLength);

        dwt.inverseDWT(dwtTree, luminance);
        yuv.set(0, luminance);

        image.setImage(ImageUtil.getImageFromYuv(yuv, imgType));
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
        return null;
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
        // TODO
        return 0.0;
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
     * Utility method to mark a wavelet sub-band using the watermark data
     *
     * @param imgTree Image data
     * @param alpha Alpha value
     * @param watermark Watermark data
     * @param threshold Threshold
     * @param w
     * @param n
     * @return
     */
    private int markSubBand(ImageTree imgTree, double alpha, double watermark[], double threshold, int w, int n) {
        double coeff = 0.0;
        double newCoeff = 0.0;

        for (int i = 5; i < imgTree.getImage().getHeight() - 5; i++) {
            for (int j = 5; j < imgTree.getImage().getWidth() - 5; j++) {
                coeff = DWTUtil.getPixel(imgTree.getImage(), i, j);
                if (Math.abs(coeff) > threshold) {
                    newCoeff = coeff + alpha * coeff * watermark[w % n];
                    DWTUtil.setPixel(imgTree.getImage(), i, j, newCoeff);
                    w++;
                }
            }
        }

        return w;
    }

    /**
     * Utility method to find max coefficient for the sub-band
     *
     * @param imgTree Image data
     * @param subBand Sub-band number
     * @return Max coefficient
     */
    private double findSubBandMaxCoeff(ImageTree imgTree, int subBand) {
        double max = 0.0;
        double coeff = 0.0;

        for (int i = 5; i < imgTree.getImage().getHeight() - 5; i++) {
            for (int j = 5; j < imgTree.getImage().getWidth() - 5; j++) {
                coeff = Math.abs(DWTUtil.getPixel(imgTree.getImage(), i, j));
                if (coeff > max) {
                    max = coeff;
                }
            }
        }

        return max;
    }

    /**
     * Utility method to find the level adaptive max coefficient
     *
     * @param imgTree Image data
     * @return Level adaptive max coefficient
     */
    private double findLevelMaxCoeff(ImageTree imgTree) {
        double h = 0.0;
        double v = 0.0;
        double d = 0.0;

        h = findSubBandMaxCoeff(imgTree.getHorizontal(), 2);
        v = findSubBandMaxCoeff(imgTree.getVertical(), 3);
        d = findSubBandMaxCoeff(imgTree.getDiagonal(), 4);

        return CommonUtil.max(h, CommonUtil.max(v, d));
    }

    /**
     * Utility method to calculate level threshold
     *
     * @param maxCoeff Max coefficient
     * @return Level threshold
     */
    private double calcLevelThreshold(double maxCoeff) {
        return Math.pow(2.0, Math.floor(Math.log(maxCoeff) / Math.log(2.0)) - 1.0);
    }

    /**
     * Utility method to calculate level alpha detail
     *
     * @param alpha Alpha value
     * @param level Level number
     * @return Level alpha detail
     */
    private double calcLevelAlphaDetail(double alpha, int level) {
        return alpha / Math.pow(2.0, level - 1);
    }

    /**
     * Private class for the data structure required for the signature
     */
    private class Signature {
        /**
         * Signature stamp
         */
        byte[] sig = "KISG".getBytes();

        /**
         * Length of the watermark
         */
        int watermarkLength = 1000;

        /**
         * Decomposition level
         */
        int decompositionLevel = 4;

        /**
         * Wavelet filter method
         */
        int waveletFilterMethod = 2;

        /**
         * Filter number
         */
        int filterNumber = 1;

        /**
         * Alpha for the detail sub-bands
         */
        double alphaForDetailSubBand = 0.1;

        /**
         * Alpha for the approximation sub-bands
         */
        double alphaForApproxSubBand = 0.02;

        /**
         * Watermark data
         */
        double[] watermark = null;

        /**
         * Constructor which generates the watermark data using the given randomizer
         *
         * @param rand Randomizer to use for generating watermark data
         */
        public Signature(Random rand) {
            double m = 0.0;
            double d = 1.0;
            double x = 0.0;
            double x1 = 0.0;
            double x2 = 0.0;

            this.watermark = new double[this.watermarkLength];
            for (int cnt = 0; cnt < (this.watermarkLength >> 1); cnt = cnt + 2) {
                do {
                    x1 = 2.0 * ((rand.nextInt() & Integer.MAX_VALUE) / (Integer.MAX_VALUE + 1.0)) - 1.0;
                    x2 = 2.0 * ((rand.nextInt() & Integer.MAX_VALUE) / (Integer.MAX_VALUE + 1.0)) - 1.0;
                    x = x1 * x1 + x2 * x2;
                } while (x >= 1.0);

                x1 *= Math.sqrt((-2.0) * Math.log(x) / x);
                x2 *= Math.sqrt((-2.0) * Math.log(x) / x);

                this.watermark[cnt] = m + (d * x1);
                this.watermark[cnt + 1] = m + (d * x2);
            }
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
                    throw new OpenStegoException(null, NAMESPACE, DWTKimErrors.ERR_SIG_NOT_VALID);
                }

                this.watermarkLength = ois.readInt();
                this.alphaForDetailSubBand = ois.readDouble();
                this.alphaForApproxSubBand = ois.readDouble();
                this.decompositionLevel = ois.readInt();
                this.waveletFilterMethod = ois.readInt();
                this.filterNumber = ois.readInt();

                this.watermark = new double[this.watermarkLength];
                for (int i = 0; i < this.watermark.length; i++) {
                    this.watermark[i] = ois.readDouble();
                }
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
                oos.writeDouble(this.alphaForDetailSubBand);
                oos.writeDouble(this.alphaForApproxSubBand);
                oos.writeInt(this.decompositionLevel);
                oos.writeInt(this.waveletFilterMethod);
                oos.writeInt(this.filterNumber);

                for (int i = 0; i < this.watermark.length; i++) {
                    oos.writeDouble(this.watermark[i]);
                }
                oos.flush();
                oos.close();

                return baos.toByteArray();
            } catch (IOException ioEx) {
                throw new OpenStegoException(ioEx);
            }
        }
    }
}
