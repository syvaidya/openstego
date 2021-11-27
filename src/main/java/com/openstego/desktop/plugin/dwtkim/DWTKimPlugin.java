/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.plugin.dwtkim;

import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.plugin.template.image.WMImagePluginTemplate;
import com.openstego.desktop.util.ImageHolder;
import com.openstego.desktop.util.ImageUtil;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.StringUtil;
import com.openstego.desktop.util.dwt.DWT;
import com.openstego.desktop.util.dwt.DWTUtil;
import com.openstego.desktop.util.dwt.ImageTree;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

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
    private static final LabelUtil labelUtil = LabelUtil.getInstance(DWTKimPlugin.NAMESPACE);

    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "DWTKIM";

    /**
     * Default constructor
     */
    public DWTKimPlugin() {
        LabelUtil.addNamespace(NAMESPACE, "i18n.DWTKimPluginLabels");
        DWTKimErrors.init(); // Initialize error codes
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
     * @param msg           Message to be embedded
     * @param msgFileName   Name of the message file. If this value is provided, then the filename should be embedded in
     *                      the cover data
     * @param cover         Cover data into which message needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the message
     * @throws OpenStegoException Processing issues
     */
    @Override
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName) throws OpenStegoException {
        ImageHolder image;
        List<int[][]> yuv;
        DWT dwt;
        ImageTree dwtTree;
        ImageTree p;
        Signature sig;
        int[][] luminance;
        int imgType;
        int cols;
        int rows;
        int levels;
        int currLevel;
        int w;
        double maxCoeff;
        double alpha;

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
        markSubBand(p, sig.alphaForApproxSubBand, sig.watermark, calcLevelThreshold(findSubBandMaxCoeff(p)), w, sig.watermarkLength);

        dwt.inverseDWT(dwtTree, luminance);
        yuv.set(0, luminance);

        image.setImage(ImageUtil.getImageFromYuv(yuv, imgType));
        return ImageUtil.imageToByteArray(image, stegoFileName, this);
    }

    /**
     * Method to extract the message from the stego data
     *
     * @param stegoData     Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @param origSigData   Optional signature data file for watermark
     * @return Extracted message
     */
    @Override
    public byte[] extractData(byte[] stegoData, String stegoFileName, byte[] origSigData) {
        return null;
    }

    /**
     * Method to generate the signature data
     *
     * @return Signature data
     * @throws OpenStegoException Processing issues
     */
    @Override
    public byte[] generateSignature() throws OpenStegoException {
        Random rand;
        Signature sig;

        rand = new Random(StringUtil.passwordHash(this.config.getPassword()));
        sig = new Signature(rand);

        return sig.getSigData();
    }

    /**
     * Method to check the correlation between original signature and the extracted watermark
     *
     * @param origSigData   Original signature data
     * @param watermarkData Extracted watermark data
     * @return Correlation
     */
    @Override
    public double getWatermarkCorrelation(byte[] origSigData, byte[] watermarkData) {
        // TODO
        return 0.0;
    }

    /**
     * Method to get the usage details of the plugin
     *
     * @return Usage details of the plugin
     */
    @Override
    public String getUsage() {
        return labelUtil.getString("plugin.usage");
    }

    /**
     * Utility method to mark a wavelet sub-band using the watermark data
     *
     * @param imgTree   Image data
     * @param alpha     Alpha value
     * @param watermark Watermark data
     * @param threshold Threshold
     * @param w         Band count
     * @param n         Watermark length
     * @return Updated band count
     */
    private int markSubBand(ImageTree imgTree, double alpha, double[] watermark, double threshold, int w, int n) {
        double coeff;
        double newCoeff;

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
     * @return Max coefficient
     */
    private double findSubBandMaxCoeff(ImageTree imgTree) {
        double max = 0.0;
        double coeff;

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
        double h;
        double v;
        double d;

        h = findSubBandMaxCoeff(imgTree.getHorizontal());
        v = findSubBandMaxCoeff(imgTree.getVertical());
        d = findSubBandMaxCoeff(imgTree.getDiagonal());

        return Math.max(h, Math.max(v, d));
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
    private static class Signature {
        /**
         * Signature stamp
         */
        private final byte[] sig = "KISG".getBytes(StandardCharsets.UTF_8);

        /**
         * Length of the watermark
         */
        private int watermarkLength = 1000;

        /**
         * Decomposition level
         */
        private int decompositionLevel = 4;

        /**
         * Wavelet filter method
         */
        private int waveletFilterMethod = 2;

        /**
         * Filter number
         */
        private int filterNumber = 1;

        /**
         * Alpha for the detail sub-bands
         */
        private double alphaForDetailSubBand = 0.1;

        /**
         * Alpha for the approximation sub-bands
         */
        private double alphaForApproxSubBand = 0.02;

        /**
         * Watermark data
         */
        private final double[] watermark;

        /**
         * Constructor which generates the watermark data using the given randomizer
         *
         * @param rand Randomizer to use for generating watermark data
         */
        public Signature(Random rand) {
            double m = 0.0;
            double d = 1.0;
            double x;
            double x1;
            double x2;
            double r;

            this.watermark = new double[this.watermarkLength];
            for (int cnt = 0; cnt < (this.watermarkLength >> 1); cnt = cnt + 2) {
                do {
                    x1 = 2.0 * ((rand.nextInt() & Integer.MAX_VALUE) / (Integer.MAX_VALUE + 1.0)) - 1.0;
                    x2 = 2.0 * ((rand.nextInt() & Integer.MAX_VALUE) / (Integer.MAX_VALUE + 1.0)) - 1.0;
                    x = x1 * x1 + x2 * x2;
                } while (x >= 1.0);

                r = Math.sqrt((-2.0) * Math.log(x) / x);
                x1 *= r;
                x2 *= r;

                this.watermark[cnt] = m + (d * x1);
                this.watermark[cnt + 1] = m + (d * x2);
            }
        }

        /**
         * Constructor that takes existing the signature data
         *
         * @param sigData Existing signature data
         * @throws OpenStegoException Processing issues
         */
        public Signature(byte[] sigData) throws OpenStegoException {
            ObjectInputStream ois;
            int n;
            byte[] inputSig = new byte[this.sig.length];

            try {
                ois = new ObjectInputStream(new ByteArrayInputStream(sigData));
                n = ois.read(inputSig, 0, this.sig.length);
                if (n == -1 || !(new String(this.sig)).equals(new String(inputSig))) {
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
         * @throws OpenStegoException Processing issues
         */
        public byte[] getSigData() throws OpenStegoException {
            try (
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos)
            ) {
                oos.write(this.sig);
                oos.writeInt(this.watermarkLength);
                oos.writeDouble(this.alphaForDetailSubBand);
                oos.writeDouble(this.alphaForApproxSubBand);
                oos.writeInt(this.decompositionLevel);
                oos.writeInt(this.waveletFilterMethod);
                oos.writeInt(this.filterNumber);

                for (double v : this.watermark) {
                    oos.writeDouble(v);
                }

                oos.flush();
                return baos.toByteArray();
            } catch (IOException ioEx) {
                throw new OpenStegoException(ioEx);
            }
        }
    }

}
