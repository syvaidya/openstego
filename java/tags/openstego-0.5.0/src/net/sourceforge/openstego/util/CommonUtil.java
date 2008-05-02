/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.util;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
            throw new OpenStegoException(ioEx);
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
            throw new OpenStegoException(ioEx);
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
            throw new OpenStegoException(ioEx);
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

    /**
     * Method to parse a delimiter separated list of files into arraylist of filenames. It supports wildcard characters
     * "*" and "?" within the filenames.
     * @param fileList Delimiter separated list of filenames
     * @param delimiter Delimiter for tokenization
     * @return List of filenames after tokenizing and wildcard expansion
     */
    public static List parseFileList(String fileList, String delimiter)
    {
        int index = 0;
        StringTokenizer tokenizer = null;
        String fileName = null;
        String dirName = null;
        ArrayList output = new ArrayList();
        File fileDir = null;
        File[] arrFile = null;

        if(fileList == null)
        {
            return output;
        }

        tokenizer = new StringTokenizer(fileList, delimiter);
        while(tokenizer.hasMoreTokens())
        {
            fileName = tokenizer.nextToken().trim();
            index = fileName.lastIndexOf(File.separator);

            if(index >= 0)
            {
                dirName = fileName.substring(0, index);
                fileName = fileName.substring(index + 1);
            }
            else
            {
                dirName = ".";
            }
            fileName = replaceWildcards(fileName);

            fileDir = new File(dirName.equals("") ? "." : dirName);
            arrFile = fileDir.listFiles(new WildcardFilenameFilter(fileName));

            for(int i = 0; i < arrFile.length; i++)
            {
                output.add(arrFile[i]);
            }
        }

        return output;
    }

    /**
     * Helper method to replace file wildcard characters with Java regexp wildcard chararcters
     * @param input Input String
     * @return String containing modified wildcard characters
     */
    private static String replaceWildcards(String input)
    {
        StringBuffer buffer = new StringBuffer();
        char[] chars = input.toCharArray();

        for(int i = 0; i < chars.length; i++)
        {
            if(chars[i] == '*')
            {
                buffer.append(".*");
            }
            else if(chars[i] == '?')
            {
                buffer.append(".{1}");
            }
            else if("+()^$.{}[]|\\".indexOf(chars[i]) != -1) // Escape rest of the java regexp wildcards
            {
                buffer.append('\\').append(chars[i]);
            }
            else
            {
                buffer.append(chars[i]);
            }
        }

        return buffer.toString();
    }

    /**
     * Inner class for wildcard filename filter
     */
    static class WildcardFilenameFilter implements FilenameFilter
    {
        /**
         * Variable to hold the filter string
         */
        String filter = null;

        /**
         * Default constructor
         * @param filter Filter string
         */
        public WildcardFilenameFilter(String filter)
        {
            this.filter = filter.toLowerCase();
        }

        /**
         * Implementation of <code>accept</code> method
         * @param dir Directory to traverse
         * @param name Name of the file
         * @return Whether file is accepted by the filter or not
         */
        public boolean accept(File dir, String name)
        {
            return (name.toLowerCase().matches(filter));
        }
    }
}
