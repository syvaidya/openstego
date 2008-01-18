/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego;

import java.awt.image.BufferedImage;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.UIManager;

import net.sourceforge.openstego.ui.OpenStegoUI;
import net.sourceforge.openstego.util.*;

/**
 * This is the main class for OpenStego. It includes the {@link #main(java.lang.String[])} method which provides the
 * command line interface for the tool. It also has API methods which can be used by external programs
 * when using OpenStego as a library.
 */
public class OpenStego
{
    /**
     * Static list of supported read formats
     */
    private static List readFormats = null;

    /**
     * Static list of supported write formats
     */
    private static List writeFormats = null;

    /**
     * Configuration data
     */
    private OpenStegoConfig config = null;

    /**
     * Constructor using the default configuration
     */
    public OpenStego()
    {
        this.config = new OpenStegoConfig();
    }

    /**
     * Constructor using <code>OpenStegoConfig</code> object
     * @param config OpenStegoConfig object with configuration data
     */
    public OpenStego(OpenStegoConfig config)
    {
        this.config = config;
    }

    /**
     * Constructor with configuration data in the form of <code>Map<code>
     * @param propMap Map containing the configuration data
     * @throws OpenStegoException
     */
    public OpenStego(Map propMap) throws OpenStegoException
    {
        this.config = new OpenStegoConfig(propMap);
    }

    /**
     * Method to embed the data into an image
     * @param data Data to be embedded
     * @param dataFileName Name of the data file
     * @param image Source image data into which data needs to be embedded. If <code>null</code> then random image
     *              is generated and used as the cover file
     * @return Image with embedded data
     * @throws OpenStegoException
     */
    public BufferedImage embedData(byte[] data, String dataFileName, BufferedImage image) throws OpenStegoException
    {
        StegoOutputStream stegoOS = null;

        try
        {
            // Compress data, if requested
            if(config.isUseCompression())
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream zos = new GZIPOutputStream(bos);
                zos.write(data);
                zos.finish();
                zos.close();
                bos.close();

                data = bos.toByteArray();
            }

            // Encrypt data, if requested
            if(config.isUseEncryption())
            {
                OpenStegoCrypto crypto = new OpenStegoCrypto(config.getPassword());
                data = crypto.encrypt(data);
            }

            // Generate random image, if input image is not provided
            if(image == null)
            {
                image = generateRandomImage(data.length);
            }

            stegoOS = new StegoOutputStream(image, data.length, dataFileName, config);
            stegoOS.write(data);
            stegoOS.close();

            return stegoOS.getImage();
        }
        catch(OpenStegoException osEx)
        {
            throw osEx;
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(OpenStegoException.UNHANDLED_EXCEPTION, ex);
        }
    }

    /**
     * Method to embed the data into an image (alternate API)
     * @param dataFile File containing the data to be embedded
     * @param imageFile Source image file into which data needs to be embedded. If <code>null</code> then random image
     *                  is generated and used as the cover file
     * @return Image with embedded data
     * @throws OpenStegoException
     */
    public BufferedImage embedData(File dataFile, File imageFile) throws OpenStegoException
    {
        InputStream is = null;
        String filename = null;
        BufferedImage image = null;

        try
        {
            // If no data file is provided, then read the data from stdin
            if(dataFile == null)
            {
                is = System.in;
            }
            else
            {
                is = new FileInputStream(dataFile);
                filename = dataFile.getName();
            }

            image = embedData(getStreamBytes(is), filename, readImage(imageFile));
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(OpenStegoException.UNHANDLED_EXCEPTION, ioEx);
        }

        return image;
    }

    /**
     * Method to extract the data from an image
     * @param image Image from which data needs to be extracted
     * @return Extracted data (List's first element is the file name and second element is byte array of data)
     * @throws OpenStegoException
     */
    public List extractData(BufferedImage image) throws OpenStegoException
    {
        int bytesRead = 0;
        byte[] data = null;
        List output = new ArrayList();
        DataHeader header = null;
        StegoInputStream stegoIS = null;

        try
        {
            stegoIS = new StegoInputStream(image, config);
            header = stegoIS.getDataHeader();

            // Add file name as first element of output list
            output.add(header.getFileName());
            data = new byte[header.getDataLength()];

            bytesRead = stegoIS.read(data, 0, data.length);
            if(bytesRead != data.length)
            {
                throw new OpenStegoException(OpenStegoException.ERR_IMAGE_DATA_READ, null);
            }
            stegoIS.close();

            // Decrypt data, if required
            if(config.isUseEncryption())
            {
                OpenStegoCrypto crypto = new OpenStegoCrypto(config.getPassword());
                data = crypto.decrypt(data);
            }

            // Decompress data, if required
            if(config.isUseCompression())
            {
                try
                {
                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    GZIPInputStream zis = new GZIPInputStream(bis);
                    data = getStreamBytes(zis);
                    zis.close();
                    bis.close();
                }
                catch(IOException ioEx)
                {
                    throw new OpenStegoException(OpenStegoException.CORRUPT_DATA, ioEx);
                }
            }

            // Add data as second element of output list
            output.add(data);
        }
        catch(OpenStegoException osEx)
        {
            throw osEx;
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(OpenStegoException.UNHANDLED_EXCEPTION, ex);
        }

        return output;
    }

    /**
     * Method to extract the data from an image (alternate API)
     * @param imageFile Image file from which data needs to be extracted
     * @return Extracted data (List's first element is the file name and second element is byte array of data)
     * @throws OpenStegoException
     */
    public List extractData(File imageFile) throws OpenStegoException
    {
        return extractData(readImage(imageFile));
    }

    /**
     * Helper method to get byte array data from given InputStream
     * @param is InputStream to read
     * @return Stream data as byte array
     * @throws IOException
     */
    private byte[] getStreamBytes(InputStream is) throws IOException
    {
        final int BUF_SIZE = 512;
        ByteArrayOutputStream bos = null;
        int bytesRead = 0;
        byte[] data = null;

        data = new byte[BUF_SIZE];
        bos = new ByteArrayOutputStream();

        while((bytesRead = is.read(data, 0, BUF_SIZE)) >= 0)
        {
            bos.write(data, 0, bytesRead);
        }

        is.close();
        bos.close();

        return bos.toByteArray();
    }

    /**
     * Method to load the image file
     * @param imageFile Image file
     * @return Buffered image
     * @throws OpenStegoException
     */
    public BufferedImage readImage(File imageFile) throws OpenStegoException
    {
        BufferedImage image = null;
        try
        {
            if(imageFile == null)
            {
                return null;
            }

            image = ImageIO.read(imageFile);
            if(image == null)
            {
                throw new OpenStegoException(OpenStegoException.IMAGE_FILE_INVALID, imageFile.getName(), null);
            }
            return image;
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(OpenStegoException.UNHANDLED_EXCEPTION, ioEx);
        }
    }

    /**
     * Method to generate a random image filled with noise. The size of the image will be calculated based on the
     * length of data (after compression) that needs to be embedded, and the 'maxBitsUsedPerChannel' parameter.
     * @param dataLength Length of data in bytes which the image should be able to accommodate
     * @return Random image filled with noise
     * @throws OpenStegoException
     */
    public BufferedImage generateRandomImage(int dataLength) throws OpenStegoException
    {
        final double ASPECT_RATIO = 4.0 / 3.0;
        int numOfPixels = 0;
        int width = 0;
        int height = 0;
        byte[] rgbValue = new byte[3];
        BufferedImage image = null;
        SecureRandom random = null;

        try
        {
            random = SecureRandom.getInstance("SHA1PRNG");

            numOfPixels = (int) ((DataHeader.getMaxHeaderSize() * 8 / 3.0)
                            + (dataLength * 8 / (3.0 * config.getMaxBitsUsedPerChannel())));
            width = (int) Math.ceil(Math.sqrt(numOfPixels * ASPECT_RATIO));
            height = (int) Math.ceil(numOfPixels / (double) width);

            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for(int x = 0; x < width; x++)
            {
                for(int y = 0; y < height; y++)
                {
                    random.nextBytes(rgbValue);
                    image.setRGB(x, y, DataHeader.byteToInt(rgbValue[0])
                                    + (DataHeader.byteToInt(rgbValue[1]) << 8)
                                    + (DataHeader.byteToInt(rgbValue[2]) << 16));
                }
            }

            return image;
        }
        catch(NoSuchAlgorithmException nsaEx)
        {
            throw new OpenStegoException(OpenStegoException.UNHANDLED_EXCEPTION, nsaEx);
        }
    }

    /**
     * Method to write the image file
     * @param image Image data
     * @param imageFileName Image file name
     * @throws OpenStegoException
     */
    public void writeImage(BufferedImage image, String imageFileName) throws OpenStegoException
    {
        String imageType = null;
        try
        {
            if(imageFileName != null)
            {
                imageType = imageFileName.substring(imageFileName.lastIndexOf('.') + 1).toLowerCase();
                if(!getSupportedWriteFormats().contains(imageType))
                {
                    throw new OpenStegoException(OpenStegoException.IMAGE_TYPE_INVALID, imageType, null);
                }
                if(imageType.equals("jp2"))
                {
                    imageType = "jpeg 2000";
                }
                ImageIO.write(image, imageType, new File(imageFileName));
            }
            // If file name is not provided then write the image data to System.out
            else
            {
                ImageIO.write(image, "png", System.out);
            }
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(OpenStegoException.UNHANDLED_EXCEPTION, ioEx);
        }
    }

    /**
     * Get method for configuration data
     * @return Configuration data
     */
    public OpenStegoConfig getConfig()
    {
        return config;
    }

    /**
     * Main method for calling openstego from command line.
     *
     * @param args Command line arguments
     * @throws OpenStegoException
     */
    public static void main(String[] args) throws OpenStegoException
    {
        int count = 0;
        int index = 0;
        String msgFileName = null;
        String coverFileName = null;
        String stegoFileName = null;
        String extractDir = null;
        String extractFileName = null;
        String command = null;
        List stegoData = null;
        OpenStego stego = null;
        FileOutputStream fos = null;
        CmdLineParser parser = null;
        CmdLineOptions options = null;
        CmdLineOption option = null;
        List optionList = null;

        try
        {
            // Parse the command-line
            parser = new CmdLineParser(getStdCmdLineOptions(), args);
            if(!parser.isValid())
            {
                displayUsage();
                return;
            }

            if(parser.getNumOfOptions() == 0) // Start GUI
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch(Exception e)
                {
                }
                new OpenStegoUI().setVisible(true);
            }
            else
            {
                optionList = parser.getParsedOptionsAsList();
                options = parser.getParsedOptions();

                for(int i = 0; i < optionList.size(); i++)
                {
                    option = (CmdLineOption) optionList.get(i);
                    if(((i == 0) && (option.getType() != CmdLineOption.TYPE_COMMAND))
                        || ((i > 0) && (option.getType() == CmdLineOption.TYPE_COMMAND)))
                    {
                        displayUsage();
                        return;
                    }

                    if(i == 0)
                    {
                        command = option.getName();
                    }
                }
                
                // Non-standard options are not allowed
                if(parser.getNonStdOptions().size() > 0)
                {
                    displayUsage();
                    return;
                }

                // Create main stego object
                stego = new OpenStego(new OpenStegoConfig(parser.getParsedOptions()));

                if(command.equals("embed"))
                {
                    msgFileName = options.getOptionValue("-mf");
                    coverFileName = options.getOptionValue("-cf");
                    stegoFileName = options.getOptionValue("-sf");

                    // Check if we need to prompt for password
                    if(stego.getConfig().isUseEncryption() && stego.getConfig().getPassword() == null)
                    {
                        stego.getConfig().setPassword(PasswordInput.readPassword(
                                LabelUtil.getString("cmd.msg.enterPassword") + " "));
                    }

                    stego.writeImage(stego.embedData(
                                (msgFileName == null || msgFileName.equals("-")) ? null : new File(msgFileName),
                                (coverFileName == null || coverFileName.equals("-")) ? null : new File(coverFileName)),
                                (stegoFileName == null || stegoFileName.equals("-")) ? null : stegoFileName);
                }
                else if(command.equals("extract"))
                {
                    stegoFileName = options.getOptionValue("-sf");
                    extractDir = options.getOptionValue("-xd");

                    if(stegoFileName == null)
                    {
                        displayUsage();
                        return;
                    }

                    try
                    {
                        stegoData = stego.extractData(new File(stegoFileName));
                    }
                    catch(OpenStegoException osEx)
                    {
                        if(osEx.getErrorCode() == OpenStegoException.INVALID_PASSWORD)
                        {
                            if(stego.getConfig().getPassword() == null)
                            {
                                stego.getConfig().setPassword(PasswordInput.readPassword(
                                        LabelUtil.getString("cmd.msg.enterPassword") + " "));

                                try
                                {
                                    stegoData = stego.extractData(new File(stegoFileName));
                                }
                                catch(OpenStegoException inEx)
                                {
                                    if(inEx.getErrorCode() == OpenStegoException.INVALID_PASSWORD)
                                    {
                                        System.err.println(inEx.getMessage());
                                        return;
                                    }
                                    else
                                    {
                                        throw inEx;
                                    }
                                }
                            }
                            else
                            {
                                System.err.println(osEx.getMessage());
                                return;
                            }
                        }
                        else
                        {
                            throw osEx;
                        }
                    }
                    extractFileName = options.getOptionValue("-xf");
                    if(extractFileName == null)
                    {
                        extractFileName = (String) stegoData.get(0);
                        if(extractFileName == null || extractFileName.equals(""))
                        {
                            extractFileName = "untitled";
                        }
                    }
                    if(extractDir != null)
                    {
                        extractFileName = extractDir + File.separator + extractFileName;
                    }

                    fos = new FileOutputStream(extractFileName);
                    fos.write((byte[]) stegoData.get(1));
                    fos.close();

                    System.out.println(LabelUtil.getString("cmd.msg.fileExtracted", new Object[] { extractFileName }));
                }
                else if(command.equals("readformats"))
                {
                    List formats = getSupportedReadFormats();
                    for(int i = 0; i < formats.size(); i++)
                    {
                        System.out.println(formats.get(i));
                    }
                }
                else if(command.equals("writeformats"))
                {
                    List formats = getSupportedWriteFormats();
                    for(int i = 0; i < formats.size(); i++)
                    {
                        System.out.println(formats.get(i));
                    }
                }
                else
                {
                    displayUsage();
                    return;
                }
            }
        }
        catch(OpenStegoException osEx)
        {
            throw osEx;
        }
        catch(Exception ex)
        {
            throw new OpenStegoException(OpenStegoException.UNHANDLED_EXCEPTION, ex);
        }
    }

    /**
     * Method to display usage for OpenStego
     */
    private static void displayUsage()
    {
        OpenStegoConfig defaultConfig = new OpenStegoConfig();
        System.err.print(LabelUtil.getString("versionString"));
        System.err.println(LabelUtil.getString("cmd.usage.main", new Object[] {
                                                    File.separator,
                                                    new Integer(defaultConfig.getMaxBitsUsedPerChannel()) }));
    }

    /**
     * Method to get the list of supported image formats for reading
     * @return List of supported image formats for reading
     */
    public static List getSupportedReadFormats()
    {
        if(readFormats != null)
        {
            return readFormats;
        }

        String format = null;
        String[] formats = null;
        List formatList = new ArrayList();

        formats = ImageIO.getReaderFormatNames();
        for(int i = 0; i < formats.length; i++)
        {
            format = formats[i].toLowerCase();
            if(format.indexOf("jpeg") >= 0 && format.indexOf("2000") >= 0)
            {
                format = "jp2";
            }
            if(!formatList.contains(format))
            {
                formatList.add(format);
            }
        }

        Collections.sort(formatList);
        readFormats = formatList;
        return readFormats;
    }

    /**
     * Method to get the list of supported image formats for writing
     * @return List of supported image formats for writing
     */
    public static List getSupportedWriteFormats()
    {
        if(writeFormats != null)
        {
            return writeFormats;
        }

        String format = null;
        String[] formats = null;
        String[] compTypes = null;
        List formatList = new ArrayList();
        Iterator iter = null;
        ImageWriteParam writeParam = null;

        formats = ImageIO.getWriterFormatNames();
        for(int i = 0; i < formats.length; i++)
        {
            format = formats[i].toLowerCase();
            if(format.indexOf("jpeg") >= 0 && format.indexOf("2000") >= 0)
            {
                format = "jp2";
            }
            if(!formatList.contains(format))
            {
                iter = ImageIO.getImageWritersBySuffix(format);
                while(iter.hasNext())
                {
                    writeParam = ((ImageWriter) iter.next()).getDefaultWriteParam();
                    try
                    {
                        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        compTypes = writeParam.getCompressionTypes();
                        if(compTypes.length > 0)
                        {
                            writeParam.setCompressionType(compTypes[0]);
                        }
                    }
                    catch(UnsupportedOperationException uoEx) // Compression not supported
                    {
                        formatList.add(format);
                        break;
                    }

                    // Only lossless image compression is supported
                    if(writeParam.isCompressionLossless())
                    {
                        formatList.add(format);
                        break;
                    }
                }
            }
        }

        //Expicilty removing GIF and WBMP formats, as they use unsupported color models
        formatList.remove("gif");
        formatList.remove("wbmp");
        Collections.sort(formatList);

        writeFormats = formatList;
        return writeFormats;
    }

    /**
     * Method to generate the standard list of command-line options
     * @return Standard list of command-line options
     */
    private static CmdLineOptions getStdCmdLineOptions()
    {
        CmdLineOptions options = new CmdLineOptions();

        // Commands
        options.add("embed", "--embed", CmdLineOption.TYPE_COMMAND, false);
        options.add("extract", "--extract", CmdLineOption.TYPE_COMMAND, false);
        options.add("readformats", "--readformats", CmdLineOption.TYPE_COMMAND, false);
        options.add("writeformats", "--writeformats", CmdLineOption.TYPE_COMMAND, false);

        // File options
        options.add("-mf", "--messagefile", CmdLineOption.TYPE_OPTION, true);
        options.add("-cf", "--coverfile", CmdLineOption.TYPE_OPTION, true);
        options.add("-sf", "--stegofile", CmdLineOption.TYPE_OPTION, true);
        options.add("-xf", "--extractfile", CmdLineOption.TYPE_OPTION, true);
        options.add("-xd", "--extractdir", CmdLineOption.TYPE_OPTION, true);

        // Command options
        options.add("-b", "--maxBitsUsedPerChannel", CmdLineOption.TYPE_OPTION, true);
        options.add("-c", "--compress", CmdLineOption.TYPE_OPTION, false);
        options.add("-C", "--nocompress", CmdLineOption.TYPE_OPTION, false);
        options.add("-e", "--encrypt", CmdLineOption.TYPE_OPTION, false);
        options.add("-E", "--noencrypt", CmdLineOption.TYPE_OPTION, false);
        options.add("-p", "--password", CmdLineOption.TYPE_OPTION, true);

        return options;
    }
}