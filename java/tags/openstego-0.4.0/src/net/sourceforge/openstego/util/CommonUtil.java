/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.util;

import java.awt.Color;
import java.io.*;
import javax.swing.JTextField;
import javax.swing.UIManager;

import net.sourceforge.openstego.OpenStegoException;

/**
 * Common utilities for OpenStego
 */
public class CommonUtil
{
    /**
     * Method to get byte array data from given InputStream
     * @param is InputStream to read
     * @return Stream data as byte array
     * @throws OpenStegoException
     */
    public static byte[] getStreamBytes(InputStream is) throws OpenStegoException
    {
        final int BUF_SIZE = 512;
        ByteArrayOutputStream bos = null;
        int bytesRead = 0;
        byte[] data = null;

        try
        {
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
        catch(IOException ioEx)
        {
            throw new OpenStegoException(    ioEx);
        }
    }

    /**
     * Method to get byte array data from given file
     * @param file File to read
     * @return File data as byte array
     * @throws OpenStegoException
     */
    public static byte[] getFileBytes(File file) throws OpenStegoException
    {
        try
        {
            return getStreamBytes(new FileInputStream(file));
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(    ioEx);
        }
    }

    /**
     * Method to write file data to disk
     * @param fileData File data
     * @param fileName File name (If this is <code>null</code>, then data is written to stdout)
     * @throws OpenStegoException
     */
    public static void writeFile(byte[] fileData, String fileName) throws OpenStegoException
    {
        OutputStream os = null;

        try
        {
            // If file name is not provided, then write the data to stdout
            if(fileName == null)
            {
                os = System.out;
            }
            else
            {
                os = new FileOutputStream(fileName);
            }
            os.write(fileData);
            os.close();
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(    ioEx);
        }
    }

    /**
     * Method to enable/disable a Swing JTextField object
     * @param textField Swing JTextField object
     * @param enabled Flag to indicate whether to enable or disable the object
     */
    public static void setEnabled(JTextField textField, boolean enabled)
    {
        if(enabled)
        {
            textField.setEnabled(true);
            textField.setBackground(Color.WHITE);
        }
        else
        {
            textField.setEnabled(false);
            textField.setBackground(UIManager.getColor("Panel.background"));
        }
    }
}
