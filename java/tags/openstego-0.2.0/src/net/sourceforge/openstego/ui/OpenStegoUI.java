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
