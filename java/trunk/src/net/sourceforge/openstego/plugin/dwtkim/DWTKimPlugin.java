/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.dwtkim;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.plugin.template.image.ImagePluginTemplate;
import net.sourceforge.openstego.util.ImageUtil;
import net.sourceforge.openstego.util.LabelUtil;
import net.sourceforge.openstego.util.dwt.DWT;
import net.sourceforge.openstego.util.dwt.DWTUtil;
import net.sourceforge.openstego.util.dwt.ImageTree;

/**
 * Plugin for OpenStego which implements the DWT based algorithm by Kim
 */
public class DWTKimPlugin extends ImagePluginTemplate
{
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
    public DWTKimPlugin()
    {
        LabelUtil.addNamespace(NAMESPACE, "net.sourceforge.openstego.resource.DWTKimPluginLabels");
        new DWTKimErrors(); // Initialize error codes
    }

    /**
     * Gives the name of the plugin
     * 
     * @return Name of the plugin
     */
    public String getName()
    {
        return "DWTKim";
    }

    /**
     * Gives the purpose(s) of the plugin
     * 
     * @return Purpose(s) of the plugin
     */
    public List getPurposes()
    {
        List purposes = new ArrayList();
        purposes.add(PURPOSE_WATERMARKING);
        return purposes;
    }

    /**
     * Gives a short description of the plugin
     * 
     * @return Short description of the plugin
     */
    public String getDescription()
    {
        return labelUtil.getString("plugin.description");
    }

    /**
     * Method to embed the message into the cover data
     * 
     * @param msg Message to be embedded
     * @param msgFileName Name of the message file. If this value is provided, then the filename should be embedded in
     *            the cover data
     * @param cover Cover data into which message needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the message
     * @throws OpenStegoException
     */
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName)
            throws OpenStegoException
    {
        BufferedImage image = null;
        ArrayList yuv = null;
        DWT dwt = null;
        ImageTree dwtTree = null;
        ImageTree p = null;
        Signature sig = null;
        int[][] luminance = null;
        int cols = 0;
        int rows = 0;
        int levels = 0;
        int currLevel = 0;
        int w = 0;
        double maxCoeff = 0.0;
        double alpha = 0.0;

        // Cover file is mandatory
        if(cover == null)
        {
            throw new OpenStegoException(NAMESPACE, 0, null); // TODO
        }
        else
        {
            image = ImageUtil.byteArrayToImage(cover, coverFileName);
        }

        cols = image.getWidth();
        rows = image.getHeight();
        yuv = ImageUtil.getYuvFromImage(image);
        luminance = (int[][]) yuv.get(0);
        sig = new Signature(msg);

        // Check that level is okay
        levels = DWTUtil.findDeepestLevel(cols, rows) - 1;
        if(sig.l > levels)
        {
            throw new OpenStegoException(null); // TODO
        }

        // Wavelet transform
        dwt = new DWT(cols, rows, sig.f, sig.l, sig.e);
        dwtTree = dwt.forwardDWT(luminance);

        p = dwtTree;
        w = 0;

        // process each decomposition level
        while(p.getCoarse() != null)
        {
            double threshold;

            // Get current decomposition level number
            currLevel = p.getHorizontal().getLevel();

            // Find largest absolute coefficient in detail subbands of current decomposition level
            maxCoeff = findLevelMaxCoeff(p);

            // Calculate significance threshold for current decomposition level
            threshold = calcLevelThreshold(maxCoeff);

            // Calculate embedding strength alpha for current decomposition level
            alpha = calcLevelAlphaDetail(sig.a, currLevel);

            // Embed watermark sequence into detail subbands of current decomposition level
            w = markSubBand(p.getHorizontal(), alpha, sig.watermark, threshold, w, sig.n);
            w = markSubBand(p.getVertical(), alpha, sig.watermark, threshold, w, sig.n);
            w = markSubBand(p.getDiagonal(), alpha, sig.watermark, threshold, w, sig.n);

            p = p.getCoarse();
        }

        // Mark approximation image using calculated significance threshold and embedding strength
        w = markSubBand(p, sig.A, sig.watermark, calcLevelThreshold(findSubBandMaxCoeff(p, 1)), w, sig.n);

        dwt.inverseDWT(dwtTree, luminance);
        yuv.set(0, luminance);

        return ImageUtil.imageToByteArray(ImageUtil.getImageFromYuv(yuv), stegoFileName, this);
    }

    /**
     * Method to extract the message file name from the stego data
     * 
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @return Message file name
     * @throws OpenStegoException
     */
    public String extractMsgFileName(byte[] stegoData, String stegoFileName) throws OpenStegoException
    {
        return null;
    }

    /**
     * Method to extract the message from the stego data
     * 
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @return Extracted message
     * @throws OpenStegoException
     */
    public byte[] extractData(byte[] stegoData, String stegoFileName) throws OpenStegoException
    {
        return null;
    }

    /**
     * Method to generate the signature data
     * 
     * @return Signature data
     * @throws OpenStegoException
     */
    public byte[] generateSignature() throws OpenStegoException
    {
        Random rand = new Random();
        Signature sig = new Signature(rand);
        return sig.getSigData();
    }

    /**
     * Method to get the usage details of the plugin
     * 
     * @return Usage details of the plugin
     * @throws OpenStegoException
     */
    public String getUsage() throws OpenStegoException
    {
        return labelUtil.getString("plugin.usage");
    }

    private int markSubBand(ImageTree s, double alpha, double watermark[], double threshold, int w, int n)
    {
        double coeff = 0.0;
        double newCoeff = 0.0;

        for(int i = 5; i < s.getImage().getHeight() - 5; i++)
        {
            for(int j = 5; j < s.getImage().getWidth() - 5; j++)
            {
                coeff = DWTUtil.getPixel(s.getImage(), i, j);
                if(Math.abs(coeff) > threshold)
                {
                    newCoeff = coeff + alpha * coeff * watermark[w % n];
                    DWTUtil.setPixel(s.getImage(), i, j, newCoeff);
                    w++;
                }
            }
        }

        return w;
    }

    private double findSubBandMaxCoeff(ImageTree s, int subband)
    {
        double max = 0.0;
        double coeff = 0.0;

        for(int i = 5; i < s.getImage().getHeight() - 5; i++)
        {
            for(int j = 5; j < s.getImage().getWidth() - 5; j++)
            {
                coeff = Math.abs(DWTUtil.getPixel(s.getImage(), i, j));
                if(coeff > max)
                {
                    max = coeff;
                }
            }
        }

        return max;
    }

    private double findLevelMaxCoeff(ImageTree p)
    {
        double h = 0.0;
        double v = 0.0;
        double d = 0.0;

        h = findSubBandMaxCoeff(p.getHorizontal(), 2);
        v = findSubBandMaxCoeff(p.getVertical(), 3);
        d = findSubBandMaxCoeff(p.getDiagonal(), 4);

        return DWTUtil.max(h, DWTUtil.max(v, d));
    }

    private double calcLevelThreshold(double maxCoeff)
    {
        return Math.pow(2.0, Math.floor(Math.log(maxCoeff) / Math.log(2.0)) - 1.0);
    }

    private double calcLevelAlphaDetail(double alpha, int level)
    {
        return alpha / Math.pow(2.0, level - 1);
    }

    class Signature
    {
        byte[] sig = "KISG".getBytes();

        int n = 1000;

        int l = 1; //TODO

        int e = 2;

        int f = 1;

        double a = 0.1;

        double A = 0.02;

        double[] watermark = null;

        public Signature(Random rand) throws OpenStegoException
        {
            double m = 0.0;
            double d = 1.0;
            double x = 0.0;
            double x1 = 0.0;
            double x2 = 0.0;

            watermark = new double[n];
            for(int cnt = 0; cnt < (n >> 1); cnt = cnt + 2)
            {
                do
                {
                    x1 = 2.0 * ((rand.nextInt() & Integer.MAX_VALUE) / ((double) Integer.MAX_VALUE + 1.0)) - 1.0;
                    x2 = 2.0 * ((rand.nextInt() & Integer.MAX_VALUE) / ((double) Integer.MAX_VALUE + 1.0)) - 1.0;
                    x = x1 * x1 + x2 * x2;
                }
                while(x >= 1.0);

                x1 *= Math.sqrt((-2.0) * Math.log(x) / x);
                x2 *= Math.sqrt((-2.0) * Math.log(x) / x);

                watermark[cnt] = m + (d * x1);
                watermark[cnt + 1] = m + (d * x2);
            }
        }

        public Signature(byte[] sigData) throws OpenStegoException
        {
            ObjectInputStream ois = null;
            byte[] inputSig = new byte[sig.length];

            try
            {
                ois = new ObjectInputStream(new ByteArrayInputStream(sigData));
                ois.read(inputSig, 0, sig.length);
                if(!(new String(sig)).equals(new String(inputSig)))
                {
                    throw new OpenStegoException(null); // TODO
                }

                n = ois.readInt();
                a = ois.readDouble();
                A = ois.readDouble();
                l = ois.readInt();
                e = ois.readInt();
                f = ois.readInt();

                watermark = new double[n];
                for(int i = 0; i < watermark.length; i++)
                {
                    watermark[i] = ois.readDouble();
                }
            }
            catch(IOException ioEx)
            {
                throw new OpenStegoException(ioEx);
            }
        }

        public byte[] getSigData() throws OpenStegoException
        {
            ByteArrayOutputStream baos = null;
            ObjectOutputStream oos = null;

            try
            {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.write(sig);
                oos.writeInt(n);
                oos.writeDouble(a);
                oos.writeDouble(A);
                oos.writeInt(l);
                oos.writeInt(e);
                oos.writeInt(f);

                for(int i = 0; i < watermark.length; i++)
                {
                    oos.writeDouble(watermark[i]);
                }
                oos.flush();
                oos.close();

                return baos.toByteArray();
            }
            catch(IOException ioEx)
            {
                throw new OpenStegoException(ioEx);
            }
        }
    }
}
