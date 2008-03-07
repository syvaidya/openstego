/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.plugin.randlsb;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import net.sourceforge.openstego.*;
import net.sourceforge.openstego.util.LabelUtil;
import net.sourceforge.openstego.plugin.template.imagebit.*;

/**
 * OutputStream to embed data into image
 */
public class RandomLSBOutputStream extends OutputStream
{
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(RandomLSBPlugin.NAMESPACE);

    /**
     * Output Image data
     */
    private BufferedImage image = null;

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
    private boolean bitWritten[][][][] = null;

    /**
     * Random number generator
     */
    private Random rand = null;

    /**
     * Default constructor
     * @param image Source image into which data will be embedded
     * @param dataLength Length of the data that would be written to the image
     * @param fileName Name of the source data file
     * @param config Configuration data to use while writing
     * @throws OpenStegoException
     */
    public RandomLSBOutputStream(BufferedImage image, int dataLength, String fileName, OpenStegoConfig config) throws OpenStegoException
    {
        if(image == null)
        {
            throw new OpenStegoException(RandomLSBPlugin.NAMESPACE, RandomLSBErrors.NULL_IMAGE_ARGUMENT, null);
        }

        this.dataLength = dataLength;
        this.imgWidth = image.getWidth();
        this.imgHeight = image.getHeight();
        this.config = config;
        this.image = new BufferedImage(this.imgWidth, this.imgHeight, BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < imgWidth; x++)
        {
            for(int y = 0; y < imgHeight; y++)
            {
                this.image.setRGB(x, y, image.getRGB(x, y));
            }
        }

        this.channelBitsUsed = 1;
        this.fileName = fileName;
		this.bitWritten = new boolean[this.imgWidth][this.imgHeight][3][8];
		for(int i = 0; i < this.imgWidth; i++)
        {
			for(int j = 0; j < this.imgHeight; j++)
            {
				for(int k = 0; k < 8; k++)
                {
					bitWritten[i][j][0][k] = false;
					bitWritten[i][j][1][k] = false;
					bitWritten[i][j][2][k] = false;
				}
			}
		}

        //Initialize random number generator with seed generated using password - TODO
        rand = new Random(StringUtils.passwordHash(config.getPassword()));
        writeHeader();
    }

    /**
     * Method to write header data to stream
     * @throws OpenStegoException
     */
    private void writeHeader() throws OpenStegoException
    {
        int channelBits = 1;
        int noOfPixels = 0;
        int headerSize = 0;
        ImageBitDataHeader header = null;

        try
        {
            noOfPixels = imgWidth * imgHeight;
            header = new ImageBitDataHeader(dataLength, channelBits, fileName, config);
            headerSize = header.getHeaderSize();

            while(true)
            {
                if((noOfPixels * 3 * channelBits) / 8.0 < (headerSize + dataLength))
                {
                    channelBits++;
                    if(channelBits > ((ImageBitConfig) config).getMaxBitsUsedPerChannel())
                    {
                        throw new OpenStegoException(RandomLSBPlugin.NAMESPACE, RandomLSBErrors.IMAGE_SIZE_INSUFFICIENT,
                                                     null);
                    }
                }
                else
                {
                    break;
                }
            }

            // Update channelBitsUsed in the header, and write to image
            header.setChannelBitsUsed(channelBits);
            write(header.getHeaderData());

            this.channelBitsUsed = channelBits;
        }
        catch(OpenStegoException osEx)
        {
            throw osEx;
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(ex);
        }
    }

    /**
     * Implementation of <code>OutputStream.write(int)</code> method
     * @param data Byte to be written
     * @throws IOException
     */
    public void write(int data) throws IOException
    {
        boolean bitValue = false;
        int x = 0;
        int y = 0;
        int channel = 0;
        int bit = 0;

        for(int i = 0; i < 8; i++)
        {
            bitValue = ((data >> (7 - i)) & 0x1) == 0x1;

            do
            {
                x = rand.nextInt(this.imgWidth);
                y = rand.nextInt(this.imgHeight);
                channel = rand.nextInt(3);
                bit = rand.nextInt(channelBitsUsed);
            }
            while(bitWritten[x][y][channel][bit]);
            bitWritten[x][y][channel][bit] = true;

            setPixelBit(x, y, channel, bit, bitValue);
        }
    }

    /**
     * Get the image containing the embedded data. Ideally, this should be called after the stream is closed.
     * @return Image data
     * @throws OpenStegoException
     */
    public BufferedImage getImage() throws OpenStegoException
    {
        return image;
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
	private void setPixelBit(int x, int y, int channel, int bit, boolean bitValue)
	{
		int pixel = 0;
		int newColor = 0;
        int newPixel = 0;

		//Get the pixel value
		pixel = this.image.getRGB(x, y);

		//Set the bit value
		if(bitValue)
        {
			newPixel = pixel | 1 << (bit + (channel * 8));
		}
        else
        {
			newColor = 0xfffffffe;
			for(int i = 0; i < (bit + (channel * 8)); i++)
            {
				newColor = (newColor << 1) | 0x1;
			}
			newPixel = pixel & newColor;
		}

		//Set the pixel value back in image
		this.image.setRGB(x, y, newPixel);
	}
}