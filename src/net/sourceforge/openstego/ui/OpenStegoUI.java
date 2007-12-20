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
        0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x30, 0x00, 0x30, 0x00, 0xc6, 0x55,
        0x00, 0xab, 0x28, 0x05, 0x48, 0x4d, 0x87, 0x48, 0x4d, 0x88, 0x4a, 0x4e,
        0x87, 0x4b, 0x4f, 0x86, 0x4c, 0x4f, 0x86, 0x4f, 0x51, 0x84, 0x4f, 0x52,
        0x84, 0x4f, 0x54, 0x8d, 0x53, 0x54, 0x82, 0x54, 0x55, 0x82, 0x57, 0x56,
        0x81, 0x59, 0x58, 0x80, 0x5a, 0x59, 0x7f, 0x5b, 0x59, 0x7f, 0x5c, 0x59,
        0x7e, 0x5e, 0x5b, 0x7d, 0x59, 0x5d, 0x93, 0x61, 0x5d, 0x7c, 0x63, 0x5e,
        0x7b, 0x63, 0x5f, 0x7b, 0x66, 0x60, 0x79, 0x69, 0x62, 0x78, 0x6b, 0x63,
        0x77, 0x6c, 0x64, 0x77, 0x6d, 0x65, 0x76, 0x73, 0x68, 0x74, 0x74, 0x69,
        0x73, 0x78, 0x6c, 0x71, 0x7a, 0x6d, 0x70, 0x7d, 0x6f, 0x6f, 0x7e, 0x70,
        0x6e, 0x81, 0x72, 0x6d, 0x83, 0x73, 0x6c, 0x8a, 0x77, 0x69, 0x8c, 0x78,
        0x68, 0x8f, 0x7a, 0x67, 0x91, 0x7c, 0x66, 0x93, 0x7d, 0x65, 0x7a, 0x7e,
        0xa9, 0x94, 0x7e, 0x64, 0xde, 0x73, 0x00, 0x9a, 0x82, 0x62, 0x80, 0x84,
        0xac, 0x9f, 0x85, 0x5f, 0xa5, 0x89, 0x5d, 0xab, 0x8c, 0x5a, 0xac, 0x8d,
        0x59, 0xad, 0x8e, 0x59, 0xb0, 0x90, 0x57, 0xb4, 0x92, 0x56, 0xbb, 0x97,
        0x52, 0xbe, 0x99, 0x51, 0xc2, 0x9c, 0x4f, 0xc6, 0x9e, 0x4e, 0x9d, 0xa0,
        0xbf, 0xcb, 0xa1, 0x4b, 0xcc, 0xa2, 0x4b, 0xce, 0xa3, 0x4a, 0xd0, 0xa4,
        0x49, 0xd3, 0xa7, 0x47, 0xd4, 0xa7, 0x47, 0xd5, 0xa8, 0x47, 0xda, 0xab,
        0x44, 0xdc, 0xac, 0x43, 0xab, 0xae, 0xc8, 0xdf, 0xae, 0x42, 0xe3, 0xb0,
        0x40, 0xe4, 0xb1, 0x40, 0xe8, 0xb4, 0x3e, 0xe9, 0xb5, 0x3d, 0xed, 0xb7,
        0x3b, 0xef, 0xb9, 0x3a, 0xba, 0xbc, 0xd2, 0xf3, 0xbb, 0x39, 0xf7, 0xbd,
        0x37, 0xfc, 0xc1, 0x35, 0xfe, 0xc2, 0x34, 0xff, 0xc3, 0x34, 0xce, 0xcf,
        0xdf, 0xcf, 0xd0, 0xe0, 0xe2, 0xe3, 0xec, 0xeb, 0xec, 0xff, 0xf5, 0xf5,
        0xf8, 0xfa, 0xfa, 0xfc, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
        0xff, 0x21, 0xf9, 0x04, 0x01, 0x0a, 0x00, 0x7f, 0x00, 0x2c, 0x00, 0x00,
        0x00, 0x00, 0x30, 0x00, 0x30, 0x00, 0x00, 0x07, 0xfe, 0x80, 0x7f, 0x82,
        0x83, 0x84, 0x85, 0x86, 0x82, 0x00, 0x87, 0x8a, 0x8b, 0x8c, 0x8b, 0x02,
        0x02, 0x7f, 0x00, 0x92, 0x8d, 0x94, 0x95, 0x86, 0x8f, 0x98, 0x92, 0x89,
        0x96, 0x9c, 0x8e, 0x98, 0x9f, 0x02, 0x9b, 0x9d, 0xa3, 0x83, 0x8f, 0x52,
        0xa7, 0xa0, 0x8f, 0xa4, 0x9d, 0x92, 0xa6, 0xa7, 0xaf, 0xa9, 0x90, 0xab,
        0x8c, 0xad, 0xae, 0xaf, 0xb7, 0xb1, 0xb3, 0x86, 0x92, 0x55, 0xb6, 0xb7,
        0xbf, 0x52, 0xb9, 0xba, 0xbc, 0xbd, 0x02, 0xc0, 0xc7, 0xb8, 0xaa, 0xab,
        0x9a, 0x55, 0xc5, 0xc6, 0xb0, 0x98, 0xc8, 0xc1, 0xca, 0xa3, 0xcc, 0x55,
        0x29, 0xbe, 0xbf, 0x9f, 0xda, 0xd3, 0xd5, 0x00, 0xcd, 0xd7, 0xd8, 0xcf,
        0xd2, 0xa8, 0xcf, 0xae, 0xd4, 0x94, 0xc4, 0x29, 0xeb, 0xd1, 0xe4, 0xc0,
        0xe3, 0xd3, 0xb2, 0x8b, 0xd6, 0xeb, 0xec, 0xf0, 0xed, 0xd2, 0xf0, 0xdd,
        0xf3, 0xdf, 0xcd, 0xf5, 0xf6, 0xc7, 0xb6, 0x69, 0x13, 0x88, 0xae, 0x90,
        0xba, 0x7f, 0xe2, 0xdc, 0xc5, 0x0b, 0x98, 0x49, 0xd4, 0x20, 0x7a, 0x08,
        0x13, 0x2a, 0x2c, 0xc7, 0xb0, 0xca, 0xa4, 0x87, 0xfd, 0xc2, 0x45, 0xcc,
        0x36, 0xf1, 0x9d, 0x80, 0x66, 0x9a, 0x22, 0x65, 0x8c, 0x08, 0x10, 0x1a,
        0x47, 0x77, 0x8f, 0x52, 0x80, 0xd3, 0x34, 0x92, 0xe4, 0xc9, 0x78, 0xfa,
        0x18, 0xae, 0x5b, 0xa9, 0xc9, 0x89, 0x13, 0x70, 0x1b, 0x63, 0x7a, 0x7c,
        0xf9, 0x08, 0x9c, 0x4d, 0x96, 0x36, 0x6d, 0xe2, 0x2c, 0xd9, 0x71, 0x21,
        0xb4, 0x66, 0x41, 0x9d, 0x00, 0x4d, 0x7a, 0xd3, 0xdf, 0xcb, 0x8e, 0x3d,
        0xab, 0x30, 0x55, 0x5a, 0x73, 0x6a, 0xd3, 0x62, 0xe5, 0x9e, 0x26, 0x43,
        0x6a, 0x75, 0xa9, 0xd5, 0xab, 0xbe, 0x04, 0x0e, 0xe4, 0xfa, 0xd5, 0xeb,
        0x57, 0xb0, 0x31, 0xdb, 0x45, 0x3d, 0x1b, 0xd4, 0x2c, 0x5b, 0x70, 0xfe,
        0x27, 0xd7, 0xb2, 0x6d, 0x5b, 0x75, 0x6e, 0x50, 0xb8, 0xe3, 0xe4, 0xda,
        0xfd, 0x59, 0x77, 0xaf, 0xd0, 0x66, 0x98, 0xc8, 0xfa, 0xa5, 0x2a, 0x69,
        0x30, 0x53, 0x70, 0x52, 0x0d, 0xd3, 0x2d, 0xac, 0xb8, 0xe9, 0xd5, 0xc6,
        0x84, 0x01, 0x28, 0xf6, 0x79, 0x37, 0xb1, 0x61, 0xb7, 0x73, 0x05, 0x57,
        0x56, 0x8c, 0xf9, 0xac, 0xe6, 0xa4, 0x9f, 0xd9, 0x76, 0x9e, 0x4a, 0xf9,
        0xad, 0xe5, 0xb9, 0xa3, 0x41, 0x4b, 0xfd, 0xd1, 0xa1, 0x75, 0x87, 0x12,
        0x36, 0x45, 0x74, 0x10, 0x11, 0x34, 0xc9, 0x89, 0x0e, 0x40, 0x6c, 0x12,
        0x31, 0x61, 0x01, 0x82, 0x05, 0x12, 0x3e, 0xf8, 0x32, 0xf6, 0x9c, 0xb8,
        0x47, 0x85, 0x02, 0x02, 0x1a, 0x80, 0xb0, 0xe9, 0xe0, 0x51, 0x0e, 0x27,
        0x4c, 0x1e, 0x3c, 0x7a, 0xe2, 0x64, 0x87, 0x01, 0x01, 0x09, 0x36, 0x4c,
        0x10, 0x80, 0x41, 0xb8, 0xe4, 0xaf, 0xa5, 0x6d, 0x52, 0x10, 0x20, 0x23,
        0x68, 0x73, 0x01, 0x1d, 0x9c, 0xd0, 0xc0, 0xf4, 0x84, 0x4a, 0x73, 0x0f,
        0x4b, 0x6c, 0xf6, 0x18, 0xe1, 0x1d, 0xfc, 0xe9, 0xa0, 0xe3, 0xcb, 0x33,
        0x17, 0xb0, 0x5d, 0x48, 0x06, 0x09, 0x03, 0x08, 0xa0, 0x03, 0x14, 0x8f,
        0x14, 0xd1, 0x55, 0x5f, 0x9b, 0x59, 0x95, 0x9f, 0x79, 0x02, 0xc4, 0x20,
        0xc0, 0x05, 0x02, 0xc0, 0x10, 0xa0, 0x0e, 0x33, 0x08, 0x80, 0x40, 0x15,
        0x43, 0x58, 0xa0, 0x61, 0x08, 0xf5, 0xa9, 0x76, 0x5f, 0x52, 0x0b, 0xee,
        0xc7, 0x83, 0x06, 0x02, 0x1c, 0xa0, 0xc4, 0x84, 0x36, 0x08, 0x10, 0xc0,
        0x14, 0x51, 0x50, 0xb0, 0x80, 0x00, 0x15, 0x74, 0xf8, 0xd7, 0x5c, 0x21,
        0x3a, 0xd1, 0x1c, 0x0f, 0x35, 0x1c, 0x80, 0x82, 0x13, 0x13, 0x22, 0x41,
        0x80, 0x00, 0x2d, 0x34, 0x13, 0x04, 0x8c, 0x32, 0x86, 0x16, 0xd4, 0x10,
        0x2a, 0x28, 0x47, 0x20, 0x00, 0x07, 0x2f, 0x88, 0x98, 0xd4, 0x84, 0x4e,
        0xb0, 0xf0, 0xc8, 0x07, 0x2e, 0xac, 0x40, 0x64, 0x64, 0x8f, 0xb1, 0x85,
        0xc3, 0x27, 0x31, 0xda, 0x28, 0x00, 0x0f, 0x4f, 0x0a, 0xe8, 0x44, 0x13,
        0x30, 0x30, 0x80, 0x49, 0x04, 0x37, 0x48, 0xc5, 0x92, 0x91, 0x90, 0x7d,
        0x65, 0x84, 0x10, 0x47, 0x5c, 0xb5, 0x66, 0x9b, 0x74, 0xde, 0xc4, 0xd2,
        0x9d, 0x78, 0xe6, 0xa9, 0xe7, 0x9e, 0x7c, 0xf6, 0xe9, 0xe7, 0x9f, 0x7f,
        0x06, 0x02, 0x00, 0x3b
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
