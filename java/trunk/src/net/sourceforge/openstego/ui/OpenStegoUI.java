/*
 * Utility to embed data into images
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007 Samir Vaidya
 */

package net.sourceforge.openstego.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.util.LabelUtil;

/**
 * This is the main class for OpenStego GUI and it implements the action and window listeners.
 */
public class OpenStegoUI extends OpenStegoFrame
{
    /**
     * Static variable to holds path to last selected folder
     */
    private static String lastFolder = null;

    /**
     * Class variable to store OpenStego config data
     */
    private OpenStegoConfig config = new OpenStegoConfig();

    /**
     * Default constructor
     */
    public OpenStegoUI()
    {
        super();
        setConfig();

        URL iconURL = getClass().getResource("/image/OpenStegoIcon.png");
        if(iconURL != null)
        {
            this.setIconImage(new ImageIcon(iconURL).getImage());
        }

        Listener listener = new Listener();
        addWindowListener(listener);
        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
        srcDataFileButton.addActionListener(listener);
        srcImgFileButton.addActionListener(listener);
        tgtImgFileButton.addActionListener(listener);
        imgForExtractFileButton.addActionListener(listener);
        outputFolderButton.addActionListener(listener);

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

        srcDataTextField.requestFocus();
    }

    /**
     * This method embeds the selected data file into selected image file
     * @throws OpenStegoException
     */
    private void embedData() throws OpenStegoException
    {
        OpenStego openStego = null;
        BufferedImage image = null;
        String dataFileName = null;
        String imgFileName = null;
        String outputFileName = null;
        String password = null;
        String confPassword = null;
        File outputFile = null;

        dataFileName = srcDataTextField.getText();
        imgFileName = srcImageTextField.getText();
        outputFileName = tgtImageTextField.getText();
        password = new String(passwordTextField.getPassword());
        confPassword = new String(confPasswordTextField.getPassword());

        // START: Input Validations
        if(!checkMandatory(srcDataTextField, LabelUtil.getString("gui.label.sourceDataFile"))) return;
        if(!checkMandatory(srcImageTextField, LabelUtil.getString("gui.label.sourceImgFile"))) return;
        if(!checkMandatory(tgtImageTextField, LabelUtil.getString("gui.label.outputImgFile"))) return;

        if(useEncryptCheckBox.isSelected())
        {
            if(!checkMandatory(passwordTextField, LabelUtil.getString("gui.label.option.password"))) return;
            if(!checkMandatory(confPasswordTextField, LabelUtil.getString("gui.label.option.confPassword"))) return;
            if(!password.equals(confPassword))
            {
                JOptionPane.showMessageDialog(this, LabelUtil.getString("gui.msg.err.passwordMismatch"),
                    LabelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                confPasswordTextField.requestFocus();
                return;
            }
        }
        // END: Input Validations

        loadConfig();
        openStego = new OpenStego(config);
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

        openStego.writeImage(image, outputFileName);
        JOptionPane.showMessageDialog(this, LabelUtil.getString("gui.msg.success.embed"),
                LabelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

        srcDataTextField.setText("");
        srcImageTextField.setText("");
        tgtImageTextField.setText("");
        passwordTextField.setText("");
        confPasswordTextField.setText("");

        //Reset configuration
        config = new OpenStegoConfig();
        setConfig();
        srcDataTextField.requestFocus();
    }

    /**
     * This method extracts data from the selected image file
     * @throws OpenStegoException
     */
    private void extractData() throws OpenStegoException
    {
        OpenStego openStego = null;
        OpenStegoConfig config = null;
        String imgFileName = null;
        String outputFolder = null;
        String outputFileName = null;
        File file = null;
        FileOutputStream fos = null;
        List stegoOutput = null;

        config = new OpenStegoConfig();
        openStego = new OpenStego(config);
        imgFileName = imgForExtractTextField.getText();
        outputFolder = outputFolderTextField.getText();

        // Input Validations
        if(!checkMandatory(imgForExtractTextField, LabelUtil.getString("gui.label.imgForExtractFile"))) return;
        if(!checkMandatory(outputFolderTextField, LabelUtil.getString("gui.label.outputDataFolder"))) return;

        try
        {
            stegoOutput = openStego.extractData(new File(imgFileName));
        }
        catch(OpenStegoException osEx)
        {
            if(osEx.getErrorCode() == OpenStegoException.INVALID_PASSWORD)
            {
                JLabel label = new JLabel("Please enter your password:");
                JPasswordField pwdField = new JPasswordField();
                if(JOptionPane.showConfirmDialog(this, new Object[] {new JLabel(LabelUtil.getString(
                        "gui.msg.input.password")), pwdField }, LabelUtil.getString("gui.msg.title.input"),
                        JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
                {
                    return;
                }
                config.setPassword(new String(pwdField.getPassword()));
                stegoOutput = openStego.extractData(new File(imgFileName));
            }
            else
            {
                throw osEx;
            }
        }

        try
        {
            outputFileName = (String) stegoOutput.get(0);
            file = new File(outputFolder + File.separator + outputFileName);
            if(file.exists())
            {
                if(JOptionPane.showConfirmDialog(this, LabelUtil.getString("gui.msg.warn.fileExists",
                        new Object[] { outputFileName }), LabelUtil.getString("gui.msg.title.warn"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
                {
                    return;
                }
            }
            fos = new FileOutputStream(file);
            fos.write((byte[]) stegoOutput.get(1));
            fos.close();
        }
        catch(IOException ioEx)
        {
            throw new OpenStegoException(OpenStegoException.UNHANDLED_EXCEPTION, ioEx);
        }

        JOptionPane.showMessageDialog(this, LabelUtil.getString("gui.msg.success.extract", new Object[] {
                outputFileName }), LabelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

        this.imgForExtractTextField.setText("");
        this.outputFolderTextField.setText("");
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
        boolean dirOnly = false;
        List allowedExts = null;
        JTextField textField = null;

        if(action.equals("BROWSE_SRC_DATA"))
        {
            title = LabelUtil.getString("gui.filechooser.title.sourceDataFile");
            textField = this.srcDataTextField;
        }
        else if(action.equals("BROWSE_SRC_IMG"))
        {
            title = LabelUtil.getString("gui.filechooser.title.sourceImgFile");
            filterDesc = LabelUtil.getString("gui.filechooser.filter.readImgFiles", new Object[] {
                                                                                getExtensionsString("R") });
            allowedExts = getExtensionsList("R");
            textField = this.srcImageTextField;
        }
        else if(action.equals("BROWSE_TGT_IMG"))
        {
            title = LabelUtil.getString("gui.filechooser.title.outputImgFile");
            filterDesc = LabelUtil.getString("gui.filechooser.filter.writeImgFiles", new Object[] {
                                                                                getExtensionsString("W") });
            allowedExts = getExtensionsList("W");
            textField = this.tgtImageTextField;
        }
        else if(action.equals("BROWSE_IMG_FOR_EXTRACT"))
        {
            title = LabelUtil.getString("gui.filechooser.title.imgForExtractFile");
            filterDesc = LabelUtil.getString("gui.filechooser.filter.writeImgFiles", new Object[] {
                                                                                getExtensionsString("W") });
            allowedExts = getExtensionsList("W");
            textField = this.imgForExtractTextField;
        }
        else if(action.equals("BROWSE_TGT_DATA"))
        {
            title = LabelUtil.getString("gui.filechooser.title.outputDataFolder");
            dirOnly = true;
            textField = this.outputFolderTextField;
        }

        fileName = browser.getFileName(title, filterDesc, dirOnly, allowedExts);
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
        maxBitsComboBox.setSelectedItem(new Integer(config.getMaxBitsUsedPerChannel()));
        useCompCheckBox.setSelected(config.isUseCompression());
        useEncryptCheckBox.setSelected(config.isUseEncryption());
    }

    /**
     * Method to load the config items from GUI
     */
    private void loadConfig()
    {
        config.setMaxBitsUsedPerChannel(((Integer) maxBitsComboBox.getSelectedItem()).intValue());
        config.setUseCompression(useCompCheckBox.isSelected());
        config.setUseEncryption(useEncryptCheckBox.isSelected());
        config.setPassword(new String(passwordTextField.getPassword()));
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
     * @param textField Text field to be checked for value
     * @param fieldName Name of the field
     * @return Flag whether value exists or not
     */
    private boolean checkMandatory(JTextField textField, String fieldName)
    {
        String value = textField.getText();

        if(value == null || value.trim().equals(""))
        {
            JOptionPane.showMessageDialog(this, LabelUtil.getString("gui.msg.err.mandatoryCheck",
                    new Object[] { fieldName }), LabelUtil.getString("gui.msg.title.err"),
                    JOptionPane.ERROR_MESSAGE);

            textField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Method to get the list of image extensions as a single string
     * @param flag Flag to indicate whether readable ("R") or writeable ("W") extensions are required
     * @return List of image extensions (as string)
     */
    private String getExtensionsString(String flag)
    {
        List list = null;
        StringBuffer output = new StringBuffer();

        if(flag.equals("R"))
        {
            list = OpenStego.getSupportedReadFormats();
        }
        else if(flag.equals("W"))
        {
            list = OpenStego.getSupportedWriteFormats();
        }

        for(int i = 0; i < list.size(); i++)
        {
            if(i > 0)
            {
                output.append(", ");
            }
            output.append("*.").append(list.get(i));
        }
        return output.toString();
    }

    /**
     * Method to get the list of image extensions as a list
     * @param flag Flag to indicate whether readable ("R") or writeable ("W") extensions are required
     * @return List of image extensions (as list)
     */
    private List getExtensionsList(String flag)
    {
        List list = null;
        List output = new ArrayList();

        if(flag.equals("R"))
        {
            list = OpenStego.getSupportedReadFormats();
        }
        else if(flag.equals("W"))
        {
            list = OpenStego.getSupportedWriteFormats();
        }

        for(int i = 0; i < list.size(); i++)
        {
            output.add("." + list.get(i));
        }
        return output;
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
         * @param dirOnly Flag to indicate whether only directory selection should be allowed
         * @param allowedExts Allowed file extensions for the filter
         * @return Name of the selected file (null if no file was selected)
         */
        public String getFileName(String dialogTitle, String filterDesc, boolean dirOnly, List allowedExts)
        {
            int retVal = 0;
            String fileName = null;

            JFileChooser chooser = new JFileChooser(lastFolder);
            if(dirOnly)
            {
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }

            if(filterDesc != null)
            {
                chooser.setFileFilter(new FileBrowserFilter(filterDesc, allowedExts));
            }
            chooser.setDialogTitle(dialogTitle);
            retVal = chooser.showOpenDialog(null);

            if(retVal == JFileChooser.APPROVE_OPTION)
            {
                fileName = chooser.getSelectedFile().getPath();
                lastFolder = chooser.getSelectedFile().getParent();
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
            private List allowedExts = null;

            /**
             * Default constructor
             * @param filterDesc Description of the filter
             * @param allowedExts List of allowed file extensions
             */
            public FileBrowserFilter(String filterDesc, List allowedExts)
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
