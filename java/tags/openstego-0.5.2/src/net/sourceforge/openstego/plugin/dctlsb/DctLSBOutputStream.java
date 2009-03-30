/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.dctlsb;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.plugin.template.dct.DCTDataHeader;
import net.sourceforge.openstego.util.ImageUtil;
import net.sourceforge.openstego.util.StringUtil;
import net.sourceforge.openstego.util.dct.DCT;

/**
 * OutputStream to embed data into image
 */
public class DctLSBOutputStream extends OutputStream
{
    /**
     * Output Image data
     */
    private BufferedImage image = null;

    /**
     * Length of the data
     */
    private int dataLength = 0;

    /**
     * Name of the source data file
     */
    private String fileName = null;

    /**
     * Current message bit number
     */
    private int n = 0;

    /**
     * Actual width of the image
     */
    private int actualImgWidth = 0;

    /**
     * Actual height of the image
     */
    private int actualImgHeight = 0;

    /**
     * Width of the image rounded to 8 (So that 8x8 blocks can be created from the image)
     */
    private int imgWidth = 0;

    /**
     * Height of the image rounded to 8 (So that 8x8 blocks can be created from the image)
     */
    private int imgHeight = 0;

    /**
     * Array to store Y component from YUV colorspace of the image
     */
    private int[][] y = null;

    /**
     * Array to store U component from YUV colorspace of the image
     */
    private int[][] u = null;

    /**
     * Array to store V component from YUV colorspace of the image
     */
    private int[][] v = null;

    /**
     * Object to handle DCT transforms
     */
    private DCT dct = null;

    /**
     * Array to store the DCT coefficients for the image
     */
    private double[][] dcts = null;

    /**
     * Coordinate hit check class
     */
    private Coordinates coord = null;

    /**
     * Random number generator
     */
    private Random rand = null;

    /**
     * Configuration data
     */
    private OpenStegoConfig config = null;

    /**
     * Default constructor
     * @param image Source image into which data will be embedded
     * @param dataLength Length of the data that would be written to the image
     * @param fileName Name of the source data file
     * @param config Configuration data to use while writing
     * @throws OpenStegoException
     */
    public DctLSBOutputStream(BufferedImage image, int dataLength, String fileName, OpenStegoConfig config)
            throws OpenStegoException
    {
        ArrayList yuv = null;

        if(image == null)
        {
            throw new IllegalArgumentException("No image provided");
        }

        this.dataLength = dataLength;
        this.actualImgWidth = image.getWidth();
        this.actualImgHeight = image.getHeight();
        this.config = config;
        this.fileName = fileName;
        this.image = new BufferedImage(this.actualImgWidth, this.actualImgHeight, BufferedImage.TYPE_INT_RGB);

        // Calculate width and height rounded to 8
        imgWidth = actualImgWidth - (actualImgWidth % DCT.NJPEG);
        imgHeight = actualImgHeight - (actualImgHeight % DCT.NJPEG);

        yuv = ImageUtil.getYuvFromImage(image);
        y = (int[][]) yuv.get(0);
        u = (int[][]) yuv.get(1);
        v = (int[][]) yuv.get(2);
        for(int i = 0; i < actualImgWidth; i++)
        {
            for(int j = 0; j < actualImgHeight; j++)
            {
                this.image.setRGB(i, j, image.getRGB(i, j));
            }
        }

        dct = new DCT();
        dct.initDct8x8();
        dct.initQuantumJpegLumin();
        dcts = new double[DCT.NJPEG][DCT.NJPEG];

        rand = new Random(StringUtil.passwordHash(config.getPassword()));
        writeHeader();
    }

    /**
     * Method to write header data to stream
     * @throws OpenStegoException
     */
    private void writeHeader() throws OpenStegoException
    {
        DCTDataHeader header = null;

        try
        {
            header = new DCTDataHeader(dataLength, fileName, config);

            if(((header.getHeaderSize() + dataLength) * 8) > (imgWidth * imgHeight / (DCT.NJPEG * DCT.NJPEG)))
            {
                throw new OpenStegoException(DctLSBPlugin.NAMESPACE, DctLSBErrors.IMAGE_SIZE_INSUFFICIENT, null);
            }
            coord = new Coordinates((header.getHeaderSize() + dataLength) * 8);
            write(header.getHeaderData());
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(ioEx);
        }
    }

    /**
     * Implementation of <code>OutputStream.write(int)</code> method
     * @param data Byte to be written
     * @throws IOException
     */
    public void write(int data) throws IOException
    {
        int xb = 0;
        int yb = 0;
        int coeffNum = 0;
        int coeff = 0;

        for(int count = 0; count < 8; count++)
        {
            if(n >= (imgWidth * imgHeight * 8))
            {
                throw new IOException("Image size insufficient");
            }

            // Randomly select a block, check to get distinct blocks (don't use a block twice)
            do
            {
                xb = Math.abs(rand.nextInt()) % (imgWidth / DCT.NJPEG);
                yb = Math.abs(rand.nextInt()) % (imgHeight / DCT.NJPEG);
            }
            while(!coord.add(xb, yb));

            // Do the forward 8x8 DCT of that block
            dct.fwdDctBlock8x8(y, xb * DCT.NJPEG, yb * DCT.NJPEG, dcts);

            // Randomly select a coefficient. Only accept coefficient in the middle frequency range
            do
            {
                coeffNum = (Math.abs(rand.nextInt()) % (DCT.NJPEG * DCT.NJPEG - 2)) + 1;
            }
            while(dct.isMidFreqCoeff8x8(coeffNum) == 0);

            // Quantize block according to quantization quality parameter
            dct.quantize8x8(dcts);

            // Read the coefficient value and replace its LSB based on the message bit
            coeff = (int) dcts[coeffNum / DCT.NJPEG][coeffNum % DCT.NJPEG];
            if(((data >> (7 - count)) & 1) == 1)
            {
                coeff |= 1;
            }
            else
            {
                coeff &= ~(1);
            }
            dcts[coeffNum / DCT.NJPEG][coeffNum % DCT.NJPEG] = coeff;

            // Dequantize the block
            dct.dequantize8x8(dcts);

            // Do the inverse DCT on the modified 8x8 block
            dct.invDctBlock8x8(dcts, y, xb * DCT.NJPEG, yb * DCT.NJPEG);

            n++;
        }
    }

    /**
     * Get the image containing the embedded data. Ideally, this should be called after the stream is closed.
     * @return Image data
     */
    public BufferedImage getImage()
    {
        ArrayList yuv = new ArrayList();
        yuv.add(y);
        yuv.add(u);
        yuv.add(v);

        image = ImageUtil.getImageFromYuv(yuv);
        return image;
    }
}
