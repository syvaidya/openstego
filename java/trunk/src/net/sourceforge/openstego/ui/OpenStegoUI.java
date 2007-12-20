/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego.ui;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.StegoConfig;
import net.sourceforge.openstego.util.LabelUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

/**
 * This is the main class for OpenStego GUI and it implements the action and window listeners.
 */
public class OpenStegoUI extends OpenStegoFrame
{
    /**
     * Icon data
     */
    private int[] windowIcon = new int[]
    {
        0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x10, 0x00, 0x10, 0x00, 0xf7, 0x00, 0x00, 0xac, 0x2a, 0x04, 0xbc,
        0x9a, 0x54, 0x94, 0x96, 0xbc, 0x7c, 0x56, 0x6c, 0xec, 0xd6, 0xb4, 0x84, 0x72, 0x6c, 0xfc, 0xbe, 0x34,
        0xdc, 0xda, 0xd4, 0x6c, 0x72, 0xa4, 0xfc, 0xf2, 0xd4, 0x6c, 0x66, 0x74, 0xdc, 0x9e, 0x8c, 0xbc, 0xb6,
        0xbc, 0xb4, 0x5e, 0x4c, 0xd4, 0x8a, 0x74, 0x8c, 0x7e, 0x84, 0xdc, 0xae, 0x44, 0x4c, 0x4e, 0x8c, 0xf4,
        0xca, 0x64, 0xdc, 0xda, 0xf4, 0xfc, 0xf6, 0xec, 0xac, 0xb2, 0xcc, 0x9c, 0x82, 0x64, 0xe4, 0x82, 0x1c,
        0x8c, 0x8a, 0x9c, 0x5c, 0x62, 0x94, 0xd4, 0xaa, 0x44, 0x8c, 0x7a, 0x6c, 0x5c, 0x5a, 0x84, 0xfc, 0xfa,
        0xec, 0xcc, 0xc6, 0xd4, 0x7c, 0x3a, 0x3c, 0xa4, 0xa6, 0xcc, 0xdc, 0xd2, 0xcc, 0x84, 0x72, 0x74, 0xfc,
        0xca, 0x4c, 0xec, 0xe6, 0xdc, 0x7c, 0x82, 0xac, 0xcc, 0xb6, 0x8c, 0xfc, 0xd6, 0x7c, 0xec, 0xea, 0xf4,
        0xa4, 0x86, 0x5c, 0x5c, 0x5e, 0x94, 0x9c, 0x9e, 0xbc, 0x64, 0x5e, 0x7c, 0x84, 0x76, 0x6c, 0xfc, 0xee,
        0xe4, 0x74, 0x6a, 0x74, 0xbc, 0xaa, 0x94, 0xe4, 0xb6, 0xac, 0xc4, 0x6e, 0x5c, 0xf4, 0xba, 0x3c, 0xec,
        0xee, 0xfc, 0x6c, 0x6e, 0x9c, 0xd4, 0xa6, 0x4c, 0x6c, 0x5e, 0x84, 0xc4, 0xca, 0xe4, 0xa4, 0x8a, 0x5c,
        0xb4, 0x42, 0x24, 0xcc, 0xa2, 0x4c, 0x94, 0x9a, 0xbc, 0x5c, 0x52, 0x7c, 0xfc, 0xe2, 0xa4, 0xfc, 0xc2,
        0x34, 0xd4, 0xd6, 0xe4, 0x74, 0x76, 0xa4, 0x64, 0x62, 0x7c, 0xdc, 0xa2, 0x94, 0x94, 0x7e, 0x84, 0xe4,
        0xb2, 0x44, 0xfc, 0xd2, 0x64, 0xdc, 0xde, 0xf4, 0xb4, 0xb6, 0xd4, 0x94, 0x7e, 0x6c, 0x94, 0x92, 0xbc,
        0x64, 0x66, 0x94, 0xdc, 0xaa, 0x44, 0x64, 0x5e, 0x84, 0xfc, 0xfe, 0xfc, 0xc4, 0xc6, 0xdc, 0x7c, 0x3a,
        0x44, 0xf4, 0xe6, 0xcc, 0xf4, 0xca, 0x5c, 0xf4, 0xe6, 0xe4, 0x7c, 0x82, 0xb4, 0xfc, 0xde, 0x8c, 0xec,
        0xea, 0xfc, 0x8c, 0x76, 0x6c, 0xfc, 0xf2, 0xe4, 0xe4, 0xba, 0xac, 0xc4, 0x72, 0x5c, 0xf4, 0xf6, 0xfc,
        0xac, 0x8a, 0x5c, 0x00, 0x41, 0xff, 0x00, 0x91, 0xff, 0x00, 0x7c, 0xff, 0x7e, 0x00, 0xff, 0x00, 0x90,
        0xff, 0x00, 0xfd, 0xff, 0xc0, 0x7f, 0x7f, 0x00, 0x05, 0x01, 0x00, 0x10, 0x00, 0x00, 0x90, 0x00, 0x00,
        0x7c, 0x00, 0xff, 0x98, 0x00, 0xff, 0xe9, 0x00, 0xff, 0x12, 0x00, 0xff, 0x00, 0x00, 0xff, 0x00, 0xf4,
        0xff, 0x00, 0x01, 0xff, 0x00, 0x00, 0xff, 0x00, 0x00, 0x00, 0x68, 0x98, 0x00, 0xea, 0x01, 0x00, 0x12,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x18, 0x32, 0x00, 0xee, 0x00, 0x00, 0x90, 0x00, 0x00, 0x7c, 0x00, 0x00,
        0x70, 0x14, 0x00, 0x09, 0x00, 0x15, 0x91, 0x00, 0x00, 0x7c, 0x00, 0x4e, 0xc0, 0x60, 0x9e, 0xe4, 0x9e,
        0x00, 0x97, 0x80, 0x00, 0x7c, 0x7c, 0x04, 0x6f, 0xf0, 0xe9, 0x3e, 0x93, 0x12, 0x91, 0x17, 0x00, 0x7c,
        0x00, 0x9f, 0x62, 0x00, 0xeb, 0x3e, 0x00, 0x81, 0x91, 0x00, 0x7c, 0x7c, 0x00, 0x4a, 0x08, 0x07, 0xe3,
        0x02, 0x00, 0x81, 0x00, 0x00, 0x7c, 0x00, 0x00, 0xa0, 0x84, 0x00, 0x77, 0xed, 0x00, 0x50, 0x12, 0x00,
        0x00, 0x00, 0x00, 0xc8, 0x00, 0x00, 0x5f, 0x00, 0x90, 0x01, 0x00, 0x17, 0x00, 0x00, 0x00, 0x6c, 0x4e,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, 0x4e, 0x00, 0xe8, 0x9e, 0x00, 0x12,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x34, 0x1c, 0x00, 0x00, 0xea, 0x00, 0x00, 0x12, 0x00, 0xc0, 0x00, 0x00,
        0xac, 0x85, 0x00, 0xfb, 0x2b, 0x00, 0x12, 0x83, 0x00, 0x00, 0x7c, 0x00, 0x18, 0x00, 0x68, 0xee, 0x00,
        0x9e, 0x90, 0x00, 0x80, 0x7c, 0x00, 0x7c, 0x70, 0x00, 0xff, 0x05, 0x00, 0xff, 0x91, 0x00, 0xff, 0x7c,
        0x00, 0xff, 0xff, 0x00, 0x60, 0xff, 0x00, 0x9e, 0xff, 0x00, 0x80, 0xff, 0x00, 0x7c, 0x6d, 0x00, 0x3a,
        0x05, 0x01, 0x00, 0x91, 0x00, 0x00, 0x7c, 0x00, 0x00, 0x4a, 0xe9, 0x3a, 0xf4, 0x2b, 0x00, 0x80, 0x83,
        0x00, 0x7c, 0x7c, 0x00, 0x00, 0x40, 0x08, 0x00, 0xea, 0xf4, 0x15, 0x12, 0x12, 0x00, 0x00, 0x00, 0x00,
        0xc4, 0xff, 0x00, 0x2b, 0xff, 0x00, 0x83, 0xff, 0x00, 0x7c, 0xff, 0xc8, 0x00, 0x00, 0x5f, 0x00, 0x00,
        0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xf0, 0x48, 0x01, 0x93, 0xeb, 0x00, 0x17, 0x12, 0x00, 0x00,
        0x00, 0x00, 0x34, 0xf6, 0x00, 0x64, 0x38, 0x00, 0x83, 0x4c, 0x00, 0x7c, 0x00, 0x57, 0xff, 0x28, 0xf4,
        0xff, 0xeb, 0x80, 0xff, 0x12, 0x7c, 0xff, 0x00, 0x2c, 0x44, 0x77, 0xea, 0xeb, 0x10, 0x12, 0x12, 0x4f,
        0x00, 0x00, 0x00, 0xc8, 0x6d, 0x5c, 0x5f, 0x64, 0xeb, 0x15, 0x83, 0x12, 0x00, 0x7c, 0x00, 0x00, 0x68,
        0x34, 0x00, 0x2c, 0x64, 0x00, 0x4f, 0x83, 0x00, 0x00, 0x7c, 0x06, 0x80, 0x0e, 0xea, 0x35, 0xec, 0x12,
        0x4f, 0x12, 0x00, 0x00, 0x00, 0x01, 0x00, 0xc4, 0x00, 0x01, 0xff, 0x00, 0x00, 0xff, 0x00, 0x00, 0x7f,
        0x68, 0x40, 0xd4, 0xe9, 0xea, 0xeb, 0x12, 0x12, 0x12, 0x00, 0x00, 0x00, 0xe6, 0x00, 0xf0, 0x86, 0x01,
        0x93, 0x01, 0x00, 0x17, 0x00, 0x00, 0x00, 0x05, 0x88, 0x34, 0x00, 0x64, 0x64, 0x00, 0x83, 0x83, 0x00,
        0x7c, 0x7c, 0x98, 0x01, 0xf0, 0xe8, 0x00, 0x93, 0x12, 0x00, 0x17, 0x00, 0x00, 0x00, 0x03, 0x00, 0x4e,
        0x00, 0x00, 0x9e, 0x00, 0x00, 0x00, 0x00, 0x5c, 0x00, 0x80, 0x00, 0x00, 0xe9, 0x3f, 0x00, 0x12, 0x17,
        0x00, 0x00, 0x43, 0x00, 0x18, 0x00, 0x8d, 0xee, 0x00, 0xe2, 0x90, 0x00, 0x47, 0x7c, 0x00, 0x00, 0x21,
        0xf9, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2c, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x10, 0x00, 0x07,
        0x08, 0xdd, 0x00, 0x9d, 0x08, 0x1c, 0x58, 0x61, 0x40, 0x83, 0x29, 0x03, 0x13, 0x26, 0x1c, 0x12, 0x01,
        0x41, 0x04, 0x0e, 0x15, 0x14, 0x26, 0x94, 0x01, 0x45, 0x85, 0x95, 0x09, 0x20, 0x54, 0x44, 0x88, 0x98,
        0x70, 0x8a, 0x0e, 0x19, 0x1c, 0x40, 0x94, 0x50, 0x01, 0xc2, 0x0a, 0x88, 0x8d, 0x03, 0x63, 0x00, 0x18,
        0x32, 0x25, 0xc2, 0x04, 0x93, 0x2a, 0x4a, 0x94, 0x08, 0xa2, 0x62, 0x85, 0x13, 0x19, 0x3a, 0x62, 0x60,
        0x21, 0x82, 0x00, 0x49, 0x04, 0x25, 0x2f, 0x1b, 0x22, 0x80, 0xe2, 0x00, 0x00, 0xc2, 0x0b, 0x3d, 0x40,
        0xe0, 0xc0, 0x51, 0xe3, 0x27, 0x08, 0x24, 0x08, 0xa6, 0x00, 0x00, 0x50, 0xa5, 0x83, 0x8b, 0x0c, 0x38,
        0x1a, 0x2e, 0xa5, 0x82, 0x63, 0x0b, 0x89, 0x2a, 0x53, 0x7f, 0xfc, 0xa8, 0xba, 0xe5, 0x88, 0x92, 0x08,
        0x56, 0xb6, 0x50, 0xa8, 0xf2, 0xc3, 0x40, 0x58, 0xb1, 0x63, 0x3b, 0x6c, 0xd9, 0x82, 0x85, 0x2d, 0xdc,
        0xb7, 0x1a, 0x98, 0x48, 0x08, 0x11, 0x25, 0x41, 0x08, 0x29, 0x3f, 0x34, 0x68, 0x70, 0x0b, 0x40, 0x6c,
        0x81, 0x1a, 0x28, 0x96, 0x5c, 0xd1, 0x10, 0x01, 0xc8, 0x83, 0x02, 0x29, 0x72, 0x84, 0x1d, 0x01, 0x03,
        0xc3, 0x09, 0x16, 0x0a, 0x5e, 0xb0, 0x30, 0x11, 0x81, 0x40, 0x8e, 0x1d, 0x53, 0x8d, 0x50, 0x60, 0xb0,
        0xe1, 0x07, 0x8b, 0x00, 0x29, 0x14, 0xd8, 0xe0, 0xe0, 0x41, 0x40, 0x92, 0xa9, 0x4e, 0x7c, 0x14, 0x28,
        0xc0, 0xc4, 0xc2, 0x8c, 0x1f, 0x29, 0x20, 0x68, 0x28, 0x20, 0xe2, 0xc9, 0xd4, 0xdf, 0xc0, 0x83, 0xff,
        0x0e, 0x08, 0x00, 0x3b
    };

    /**
     * Class variable to store OpenStego config data
     */
    private StegoConfig config = new StegoConfig();

    /**
     * Default constructor
     */
    public OpenStegoUI()
    {
        super();
        setConfig();
        
        byte[] barr = new byte[windowIcon.length];
        for(int i = 0; i < windowIcon.length; i++)
        {
            barr[i] = (byte) windowIcon[i];
        }
        
        this.setIconImage(new ImageIcon(barr).getImage());

        Listener listener = new Listener();
        addWindowListener(listener);
        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
        srcDataFileButton.addActionListener(listener);
        srcImgFileButton.addActionListener(listener);
        tgtImgFileButton.addActionListener(listener);
        imgForExtractFileButton.addActionListener(listener);
        outputDataFileButton.addActionListener(listener);

        // "Esc" key handling
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
        	public void actionPerformed(ActionEvent ev)
        	{
        		close();
        	}
        };
         
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    /**
     * This method embeds the selected data file into selected image file
     */
    private void embedData() throws IOException
    {
        OpenStego openStego = null;
        BufferedImage image = null;
        String dataFileName = null;
        String imgFileName = null;
        String outputFileName = null;
        File outputFile = null;

        loadConfig();
        openStego = new OpenStego(config);
        dataFileName = srcDataTextField.getText();
        imgFileName = srcImageTextField.getText();
        outputFileName = tgtImageTextField.getText();

        // Input Validations
        if(!checkMandatory(dataFileName, LabelUtil.getString("gui.label.sourceDataFile"))) return;
        if(!checkMandatory(imgFileName, LabelUtil.getString("gui.label.sourceImgFile"))) return;
        if(!checkMandatory(outputFileName, LabelUtil.getString("gui.label.outputImgFile"))) return;

        image = openStego.embedData(new File(dataFileName), new File(imgFileName));
        outputFile = new File(outputFileName);

        if(outputFile.exists())
        {
            if(JOptionPane.showConfirmDialog(this, LabelUtil.getString("gui.msg.warn.fileExists",
                    new Object[] { outputFileName }), LabelUtil.getString("gui.msg.title.warn"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
            {
                return;
            }
        }

        ImageIO.write(image, config.getDefaultImageOutputType(), outputFile);
        JOptionPane.showMessageDialog(this, LabelUtil.getString("gui.msg.success.embed"),
                LabelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

        this.srcDataTextField.setText("");
        this.srcImageTextField.setText("");
        this.tgtImageTextField.setText("");
        this.srcDataTextField.requestFocus();
    }

    /**
     * This method extracts data from the selected image file
     */
    private void extractData() throws IOException
    {
        OpenStego openStego = null;
        String imgFileName = null;
        String outputFileName = null;
        FileOutputStream fos = null;

        openStego = new OpenStego();
        imgFileName = imgForExtractTextField.getText();
        outputFileName = outputDataTextField.getText();

        // Input Validations
        if(!checkMandatory(imgFileName, LabelUtil.getString("gui.label.imgForExtractFile"))) return;
        if(!checkMandatory(outputFileName, LabelUtil.getString("gui.label.outputDataFile"))) return;

        fos = new FileOutputStream(outputFileName);
        fos.write(openStego.extractData(new File(imgFileName)));
        fos.close();

        JOptionPane.showMessageDialog(this, LabelUtil.getString("gui.msg.success.extract"),
                LabelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

        this.imgForExtractTextField.setText("");
        this.outputDataTextField.setText("");
        this.imgForExtractTextField.requestFocus();
    }

    /**
     * This method shows the file chooser and updates the text field based on the selection
     */
    private void selectFile(String action)
    {
        FileBrowser browser = new FileBrowser();
        String fileName = null;
        String title = null;
        String filterDesc = null;
        ArrayList allowedExts = new ArrayList();
        JTextField textField = null;

        if(action.equals("BROWSE_SRC_DATA"))
        {
            title = LabelUtil.getString("gui.filechooser.title.sourceDataFile");
            textField = this.srcDataTextField;
        }
        else if(action.equals("BROWSE_SRC_IMG"))
        {
            title = LabelUtil.getString("gui.filechooser.title.sourceImgFile");
            filterDesc = LabelUtil.getString("gui.filechooser.filter.imgFiles");
            allowedExts.add(".png");
            textField = this.srcImageTextField;
        }
        else if(action.equals("BROWSE_TGT_IMG"))
        {
            title = LabelUtil.getString("gui.filechooser.title.outputImgFile");
            filterDesc = LabelUtil.getString("gui.filechooser.filter.imgFiles");
            allowedExts.add(".png");
            textField = this.tgtImageTextField;
        }
        else if(action.equals("BROWSE_IMG_FOR_EXTRACT"))
        {
            title = LabelUtil.getString("gui.filechooser.title.imgForExtractFile");
            filterDesc = LabelUtil.getString("gui.filechooser.filter.imgFiles");
            allowedExts.add(".png");
            textField = this.imgForExtractTextField;
        }
        else if(action.equals("BROWSE_TGT_DATA"))
        {
            title = LabelUtil.getString("gui.filechooser.title.outputDataFile");
            textField = this.outputDataTextField;
        }
        
        fileName = browser.getFileName(title, filterDesc, allowedExts);
        if(fileName != null)
        {
            textField.setText(fileName);
        }
    }

    /**
     * This method exits the application.
     */
    private void close()
    {
        System.exit(0);
    }

    /**
     * Method to set the config items in GUI
     */
    private void setConfig()
    {
        useCompCheckBox.setSelected(config.isUseCompression());
        maxBitsComboBox.setSelectedItem(new Integer(config.getMaxBitsUsedPerChannel()));
    }

    /**
     * Method to load the config items from GUI
     */
    private void loadConfig()
    {
        config.setUseCompression(useCompCheckBox.isSelected());
        config.setMaxBitsUsedPerChannel(((Integer) maxBitsComboBox.getSelectedItem()).intValue());
    }

    /**
     * This method handles all the exceptions in the GUI
     * @param ex Exception to be handled
     */
    private void handleException(Exception ex)
    {
        String msg = ex.getMessage();

        if ((msg == null) || (msg.trim().equals("")))
        {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            msg = writer.toString();
        }

        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, msg,
                LabelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Method to check whether value is provided or not; and display message box in case it is not provided
     * @param value Value of the field
     * @param fieldName Name of the field
     * @return Flag whether value exists or not
     */
    private boolean checkMandatory(String value, String fieldName)
    {
        if(value == null || value.trim().equals(""))
        {
            JOptionPane.showMessageDialog(this, LabelUtil.getString("gui.msg.err.mandatoryCheck",
                    new Object[] { fieldName }), LabelUtil.getString("gui.msg.title.err"),
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }

    /**
     * Common listener class to handlw action and window events
     */
    class Listener implements ActionListener, WindowListener
    {
        public void actionPerformed(ActionEvent ev)
        {
            try
            {
                String action = ev.getActionCommand();

                if(action.startsWith("BROWSE_"))
                {
                    selectFile(action);
                }
                else if(action.equals("OK"))
                {
                    if(mainTabbedPane.getSelectedIndex() == 0) // Embed tab
                    {
                        embedData();
                    }
                    else // Extract tab
                    {
                        extractData();
                    }
                }
                else if(action.equals("CANCEL"))
                {
                    close();
                }
            }
            catch(Exception ex)
            {
                handleException(ex);
            }
        }

        public void windowClosing(WindowEvent ev)
        {
            close();
        }

        public void windowActivated(WindowEvent ev) {}
        public void windowClosed(WindowEvent ev) {}
        public void windowDeactivated(WindowEvent ev) {}
        public void windowDeiconified(WindowEvent ev) {}
        public void windowIconified(WindowEvent ev) {}
        public void windowOpened(WindowEvent ev) {}
    }

    /**
     * Class to implement File Chooser
     */
    class FileBrowser
    {
        /**
         * Method to get the display file chooser and return the selected file name
         * @param dialogTitle Title for the file chooser dialog box
         * @param filterDesc Description to be displayed for the filter in file chooser
         * @param allowedExts Allowed file extensions for the filter
         * @return Name of the selected file (null if no file was selected)
         */
        public String getFileName(String dialogTitle, String filterDesc, ArrayList allowedExts)
        {
            int retVal = 0;
            String fileName = null;

            JFileChooser chooser = new JFileChooser(".");
            if(filterDesc != null)
            {
                chooser.setFileFilter(new FileBrowserFilter(filterDesc, allowedExts));
            }
            chooser.setDialogTitle(dialogTitle);
            retVal = chooser.showOpenDialog(null);

            if(retVal == JFileChooser.APPROVE_OPTION)
            {
                fileName = chooser.getSelectedFile().getPath();
            }
            
            return fileName;
        }

        /**
         * Class to implement filter for file chooser
         */
        class FileBrowserFilter extends FileFilter
        {
            /**
             * Description of the filter
             */
            private String filterDesc = null;

            /**
             * List of allowed file extensions
             */
            private ArrayList allowedExts = null;

            /**
             * Default constructor
             * @param filterDesc Description of the filter
             * @param allowedExts List of allowed file extensions
             */
            public FileBrowserFilter(String filterDesc, ArrayList allowedExts)
            {
                this.filterDesc = filterDesc;
                this.allowedExts = allowedExts;
            }

            /**
             * Implementation of <code>accept</accept> method of <code>FileFilter</code> class
             * @param file File to check whether it is acceptable by this filter or not
             * @return Flag to indicate whether file is acceptable or not
             */
            public boolean accept(File file)
            {
                if(file != null)
                {
                    if(allowedExts == null || allowedExts.size() == 0 || file.isDirectory())
                    {
                        return true;
                    }

                    for(int i = 0; i < allowedExts.size(); i++)
                    {
                        if(file.getName().toLowerCase().endsWith(allowedExts.get(i).toString()))
                        {
                            return true;
                        }
                    }
                }

                return false;
            }

            /**
             * Implementation of <code>getDescription</accept> method of <code>FileFilter</code> class
             * @return Description of the filter
             */
            public String getDescription()
            {
                return filterDesc;
            }
        }
    }
}
