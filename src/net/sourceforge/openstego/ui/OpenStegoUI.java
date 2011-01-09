/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.OpenStegoConfig;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.OpenStegoPlugin;
import net.sourceforge.openstego.util.CommonUtil;
import net.sourceforge.openstego.util.LabelUtil;
import net.sourceforge.openstego.util.PluginManager;

/**
 * This is the main class for OpenStego GUI and it implements the action and window listeners.
 */
public class OpenStegoUI extends OpenStegoFrame
{
    private static final long serialVersionUID = -7485426167074985636L;

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    /**
     * Static variable to holds path to last selected folder
     */
    private static String lastFolder = null;

    /**
     * Class variable to store OpenStego config data
     */
    private OpenStegoConfig config = null;

    /**
     * Class variable to store stego plugin object
     */
    private OpenStegoPlugin plugin = null;

    /**
     * Reference to the UI Panel specific to the plugin
     */
    protected PluginEmbedOptionsUI pluginEmbedOptionsUI = null;

    /**
     * Default constructor
     * 
     * @throws OpenStegoException
     */
    public OpenStegoUI() throws OpenStegoException
    {
        super();

        // Populate the combo box with list of algorithm plugins available

        // Functionality of Auto-select algorithm is removed
        // extractAlgoComboBox.addItem(labelUtil.getString("gui.label.plugin.auto"));

        List<String> algoList = PluginManager.getPluginNames();
        for(int i = 0; i < algoList.size(); i++)
        {
            this.embedAlgoComboBox.addItem(algoList.get(i));
            this.extractAlgoComboBox.addItem(algoList.get(i));
        }

        resetGUI();

        URL iconURL = getClass().getResource("/image/OpenStegoIcon.png");
        if(iconURL != null)
        {
            this.setIconImage(new ImageIcon(iconURL).getImage());
        }

        Listener listener = new Listener();
        addWindowListener(listener);
        this.okButton.addActionListener(listener);
        this.cancelButton.addActionListener(listener);
        this.msgFileButton.addActionListener(listener);
        this.coverFileButton.addActionListener(listener);
        this.stegoFileButton.addActionListener(listener);
        this.inputStegoFileButton.addActionListener(listener);
        this.outputFolderButton.addActionListener(listener);

        // "Esc" key handling
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            private static final long serialVersionUID = -4890560722044735566L;

            public void actionPerformed(ActionEvent ev)
            {
                close();
            }
        };

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    /**
     * Method to reset the GUI components from scratch
     * 
     * @throws OpenStegoException
     */
    private void resetGUI() throws OpenStegoException
    {
        this.plugin = PluginManager.getPluginByName((String) this.embedAlgoComboBox.getSelectedItem());
        this.config = this.plugin.createConfig();

        // Remove the existing UI object
        this.pluginEmbedOptionsPanel.removeAll();

        // Get UI object for plugin
        this.pluginEmbedOptionsUI = this.plugin.getEmbedOptionsUI(this);
        if(this.pluginEmbedOptionsUI != null)
        {
            this.pluginEmbedOptionsPanel.setLayout(new GridLayout(1, 1));
            this.pluginEmbedOptionsPanel.add(this.pluginEmbedOptionsUI);
            this.pluginEmbedOptionsPanel.setVisible(true);
        }
        else
        {
            this.pluginEmbedOptionsPanel.setVisible(false);
        }

        setGUIFromConfig();
        pack();
        setResizable(false);

        this.msgFileTextField.setText("");
        this.coverFileTextField.setText("");
        this.stegoFileTextField.setText("");
        this.passwordTextField.setText("");
        this.confPasswordTextField.setText("");
        this.msgFileTextField.requestFocus();
    }

    /**
     * Method to handle change event for 'embedAlgoComboBox'
     * 
     * @throws OpenStegoException
     */
    protected void embedAlgoChanged() throws OpenStegoException
    {
        resetGUI();
    }

    /**
     * This method embeds the selected data file into selected image file
     * 
     * @throws OpenStegoException
     */
    private void embedData() throws OpenStegoException
    {
        OpenStego openStego = null;
        byte[] stegoData = null;
        String dataFileName = null;
        String outputFileName = null;
        String password = null;
        String confPassword = null;
        List<File> coverFileList = null;
        File imgFile = null;
        File outputFile = null;
        int processCount = 0;
        int skipCount = 0;

        dataFileName = this.msgFileTextField.getText();
        coverFileList = CommonUtil.parseFileList(this.coverFileTextField.getText(), ";");
        outputFileName = this.stegoFileTextField.getText();
        outputFile = new File(outputFileName);
        password = new String(this.passwordTextField.getPassword());
        confPassword = new String(this.confPasswordTextField.getPassword());

        // START: Input Validations
        if(!checkMandatory(this.msgFileTextField, labelUtil.getString("gui.label.msgFile")))
        {
            return;
        }
        if(!checkMandatory(this.coverFileTextField, labelUtil.getString("gui.label.coverFile")))
        {
            return;
        }
        if(!checkMandatory(this.stegoFileTextField, labelUtil.getString("gui.label.outputStegoFile")))
        {
            return;
        }

        // Check if single or multiple cover files are selected
        if(coverFileList.size() <= 1)
        {
            // If user has provided a wildcard for cover file name, and parser returns zero length, then it means that
            // there are no matching files with that wildcard
            if(coverFileList.size() == 0 && !this.coverFileTextField.getText().trim().equals(""))
            {
                JOptionPane.showMessageDialog(
                    this,
                    labelUtil.getString("gui.msg.err.coverFileNotFound",
                        new Object[] { this.coverFileTextField.getText() }), labelUtil.getString("gui.msg.title.err"),
                    JOptionPane.ERROR_MESSAGE);
                this.stegoFileTextField.requestFocus();
                return;
            }
            // If single cover file is given, then output stego file must not be a directory
            if(outputFile.isDirectory())
            {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.outputIsDir"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                this.stegoFileTextField.requestFocus();
                return;
            }
        }
        else
        {
            // If multiple cover files are given, then output stego file must be a directory
            if(!outputFile.isDirectory())
            {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.outputShouldBeDir"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                this.stegoFileTextField.requestFocus();
                return;
            }
        }

        if(this.useEncryptCheckBox.isSelected())
        {
            if(!checkMandatory(this.passwordTextField, labelUtil.getString("gui.label.option.password")))
            {
                return;
            }
            if(!checkMandatory(this.confPasswordTextField, labelUtil.getString("gui.label.option.confPassword")))
            {
                return;
            }
            if(!password.equals(confPassword))
            {
                JOptionPane.showMessageDialog(this, labelUtil.getString("gui.msg.err.passwordMismatch"),
                    labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
                this.confPasswordTextField.requestFocus();
                return;
            }
        }

        if(this.pluginEmbedOptionsUI != null && !this.pluginEmbedOptionsUI.validateEmbedAction())
        {
            return;
        }
        // END: Input Validations

        setConfigFromGUI();
        openStego = new OpenStego(this.plugin, this.config);
        if(coverFileList.size() <= 1)
        {
            if(coverFileList.size() == 1)
            {
                imgFile = coverFileList.get(0);
            }

            if(outputFile.exists())
            {
                if(JOptionPane.showConfirmDialog(this,
                    labelUtil.getString("gui.msg.warn.fileExists", new Object[] { outputFileName }),
                    labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
                {
                    return;
                }
            }

            processCount++;
            stegoData = openStego.embedData(dataFileName == null || dataFileName.equals("") ? null : new File(
                    dataFileName), imgFile, outputFileName);
            CommonUtil.writeFile(stegoData, outputFile);
        }
        else
        {
            for(int i = 0; i < coverFileList.size(); i++)
            {
                imgFile = coverFileList.get(i);

                // Use cover file name as the output file name. Change the folder to given output folder
                outputFileName = outputFile.getPath() + File.separator + imgFile.getName();

                // If the output filename extension is not supported for writing, then change the same
                if(!this.plugin.getWritableFileExtensions().contains(
                    outputFileName.substring(outputFileName.lastIndexOf('.') + 1).toLowerCase()))
                {
                    outputFileName = outputFileName + "." + this.plugin.getWritableFileExtensions().get(0);
                }

                if((new File(outputFileName)).exists())
                {
                    if(JOptionPane.showConfirmDialog(this,
                        labelUtil.getString("gui.msg.warn.fileExists", new Object[] { outputFileName }),
                        labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
                    {
                        skipCount++;
                        continue;
                    }
                }

                processCount++;
                stegoData = openStego.embedData(dataFileName == null || dataFileName.equals("") ? null : new File(
                        dataFileName), imgFile, outputFileName);
                CommonUtil.writeFile(stegoData, outputFileName);
            }
        }

        JOptionPane.showMessageDialog(
            this,
            labelUtil.getString("gui.msg.success.embed", new Object[] { new Integer(processCount),
                    new Integer(skipCount) }), labelUtil.getString("gui.msg.title.success"),
            JOptionPane.INFORMATION_MESSAGE);

        // Reset configuration
        resetGUI();
    }

    /**
     * This method extracts data from the selected image file
     * 
     * @throws OpenStegoException
     */
    private void extractData() throws OpenStegoException
    {
        OpenStego openStego = null;
        OpenStegoConfig config = null;
        OpenStegoPlugin extractPlugin = null;
        String imgFileName = null;
        String outputFolder = null;
        String outputFileName = null;
        File file = null;
        List<?> stegoOutput = null;

        extractPlugin = PluginManager.getPluginByName((String) this.extractAlgoComboBox.getSelectedItem());
        config = extractPlugin.createConfig();

        openStego = new OpenStego(extractPlugin, config);
        config = openStego.getConfig();
        config.setPassword(new String(this.extractPwdTextField.getPassword()));
        imgFileName = this.inputStegoFileTextField.getText();
        outputFolder = this.outputFolderTextField.getText();

        // START: Input Validations
        if(!checkMandatory(this.inputStegoFileTextField, labelUtil.getString("gui.label.inputStegoFile")))
        {
            return;
        }
        if(!checkMandatory(this.outputFolderTextField, labelUtil.getString("gui.label.outputDataFolder")))
        {
            return;
        }
        // END: Input Validations

        stegoOutput = openStego.extractData(new File(imgFileName));
        outputFileName = (String) stegoOutput.get(0);
        file = new File(outputFolder + File.separator + outputFileName);
        if(file.exists())
        {
            if(JOptionPane.showConfirmDialog(this,
                labelUtil.getString("gui.msg.warn.fileExists", new Object[] { outputFileName }),
                labelUtil.getString("gui.msg.title.warn"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
            {
                return;
            }
        }

        CommonUtil.writeFile((byte[]) stegoOutput.get(1), outputFolder + File.separator + outputFileName);
        JOptionPane.showMessageDialog(this,
            labelUtil.getString("gui.msg.success.extract", new Object[] { outputFileName }),
            labelUtil.getString("gui.msg.title.success"), JOptionPane.INFORMATION_MESSAGE);

        this.inputStegoFileTextField.setText("");
        this.outputFolderTextField.setText("");
        this.extractPwdTextField.setText("");
        this.inputStegoFileTextField.requestFocus();
    }

    /**
     * This method shows the file chooser and updates the text field based on the selection
     * 
     * @throws OpenStegoException
     */
    private void selectFile(String action) throws OpenStegoException
    {
        FileBrowser browser = new FileBrowser();
        String fileName = null;
        String title = null;
        String filterDesc = null;
        List<String> allowedExts = null;
        String allowFileDir = "F";
        boolean multiSelect = false;
        int coverFileListSize = 0;
        JTextField textField = null;

        coverFileListSize = CommonUtil.parseFileList(this.coverFileTextField.getText(), ";").size();
        if(action.equals("BROWSE_SRC_DATA"))
        {
            title = labelUtil.getString("gui.filechooser.title.msgFile");
            textField = this.msgFileTextField;
        }
        else if(action.equals("BROWSE_SRC_IMG"))
        {
            title = labelUtil.getString("gui.filechooser.title.coverFile");
            filterDesc = labelUtil.getString("gui.filechooser.filter.coverFiles",
                new Object[] { getExtensionsString("R") });
            allowedExts = getExtensionsList("R");
            textField = this.coverFileTextField;
            multiSelect = true;
        }
        else if(action.equals("BROWSE_TGT_IMG"))
        {
            title = labelUtil.getString("gui.filechooser.title.outputStegoFile");
            if(coverFileListSize > 1)
            {
                allowFileDir = "D";
            }
            else
            {
                filterDesc = labelUtil.getString("gui.filechooser.filter.stegoFiles",
                    new Object[] { getExtensionsString("W") });
                allowedExts = getExtensionsList("W");
            }
            textField = this.stegoFileTextField;
        }
        else if(action.equals("BROWSE_IMG_FOR_EXTRACT"))
        {
            title = labelUtil.getString("gui.filechooser.title.inputStegoFile");
            filterDesc = labelUtil.getString("gui.filechooser.filter.stegoFiles",
                new Object[] { getExtensionsString("W") });
            allowedExts = getExtensionsList("W");
            textField = this.inputStegoFileTextField;
        }
        else if(action.equals("BROWSE_TGT_DATA"))
        {
            title = labelUtil.getString("gui.filechooser.title.outputDataFolder");
            allowFileDir = "D";
            textField = this.outputFolderTextField;
        }

        fileName = browser.getFileName(title, filterDesc, allowedExts, allowFileDir, multiSelect);
        if(fileName != null)
        {
            // Check for valid extension for output file (in case of BROWSE_TGT_IMG)
            if(action.equals("BROWSE_TGT_IMG") && (coverFileListSize <= 1))
            {
                if(!this.plugin.getWritableFileExtensions().contains(
                    fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()))
                {
                    fileName = fileName + "." + this.plugin.getWritableFileExtensions().get(0);
                }
            }
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
     * 
     * @throws OpenStegoException
     */
    private void setGUIFromConfig() throws OpenStegoException
    {
        this.useCompCheckBox.setSelected(this.config.isUseCompression());
        this.useEncryptCheckBox.setSelected(this.config.isUseEncryption());

        if(this.pluginEmbedOptionsUI != null)
        {
            this.pluginEmbedOptionsUI.setGUIFromConfig(this.config);
        }
    }

    /**
     * Method to load the config items from GUI
     * 
     * @throws OpenStegoException
     */
    private void setConfigFromGUI() throws OpenStegoException
    {
        this.config.setUseCompression(this.useCompCheckBox.isSelected());
        this.config.setUseEncryption(this.useEncryptCheckBox.isSelected());
        this.config.setPassword(new String(this.passwordTextField.getPassword()));

        if(this.pluginEmbedOptionsUI != null)
        {
            this.pluginEmbedOptionsUI.setConfigFromGUI(this.config);
        }
    }

    /**
     * This method handles all the exceptions in the GUI
     * 
     * @param ex Exception to be handled
     */
    private void handleException(Throwable ex)
    {
        String msg = null;

        if(ex instanceof OutOfMemoryError)
        {
            msg = labelUtil.getString("err.memory.full");
        }
        else
        {
            msg = ex.getMessage();
        }

        if((msg == null) || (msg.trim().equals("")))
        {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            msg = writer.toString();
        }

        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, msg, labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Method to check whether value is provided or not; and display message box in case it is not provided
     * 
     * @param textField Text field to be checked for value
     * @param fieldName Name of the field
     * @return Flag whether value exists or not
     */
    private boolean checkMandatory(JTextField textField, String fieldName)
    {
        if(!textField.isEnabled())
        {
            return true;
        }

        String value = textField.getText();
        if(value == null || value.trim().equals(""))
        {
            JOptionPane.showMessageDialog(this,
                labelUtil.getString("gui.msg.err.mandatoryCheck", new Object[] { fieldName }),
                labelUtil.getString("gui.msg.title.err"), JOptionPane.ERROR_MESSAGE);

            textField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Method to get the list of image extensions as a single string
     * 
     * @param flag Flag to indicate whether readable ("R") or writeable ("W") extensions are required
     * @return List of image extensions (as string)
     * @throws OpenStegoException
     */
    private String getExtensionsString(String flag) throws OpenStegoException
    {
        List<String> list = null;
        StringBuffer output = new StringBuffer();

        if(flag.equals("R"))
        {
            list = this.plugin.getReadableFileExtensions();
        }
        else if(flag.equals("W"))
        {
            list = this.plugin.getWritableFileExtensions();
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
     * 
     * @param flag Flag to indicate whether readable ("R") or writeable ("W") extensions are required
     * @return List of image extensions (as list)
     * @throws OpenStegoException
     */
    private List<String> getExtensionsList(String flag) throws OpenStegoException
    {
        List<String> list = null;
        List<String> output = new ArrayList<String>();

        if(flag.equals("R"))
        {
            list = this.plugin.getReadableFileExtensions();
        }
        else if(flag.equals("W"))
        {
            list = this.plugin.getWritableFileExtensions();
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
                    if(OpenStegoUI.this.mainTabbedPane.getSelectedIndex() == 0) // Embed tab
                    {
                        embedData();
                    }
                    else
                    // Extract tab
                    {
                        extractData();
                    }
                }
                else if(action.equals("CANCEL"))
                {
                    close();
                }
            }
            catch(Throwable ex)
            {
                handleException(ex);
            }
        }

        public void windowClosing(WindowEvent ev)
        {
            close();
        }

        public void windowActivated(WindowEvent ev)
        {
        }

        public void windowClosed(WindowEvent ev)
        {
        }

        public void windowDeactivated(WindowEvent ev)
        {
        }

        public void windowDeiconified(WindowEvent ev)
        {
        }

        public void windowIconified(WindowEvent ev)
        {
        }

        public void windowOpened(WindowEvent ev)
        {
        }
    }

    /**
     * Class to implement File Chooser
     */
    class FileBrowser
    {
        /**
         * Method to get the display file chooser and return the selected file name
         * 
         * @param dialogTitle Title for the file chooser dialog box
         * @param filterDesc Description to be displayed for the filter in file chooser
         * @param allowedExts Allowed file extensions for the filter
         * @param allowFileDir Type of objects allowed to be selected (F = Files only, D = Directories only, B = Both)
         * @param multiSelect Flag to indicate whether multiple file selection is allowed or not
         * @return Name of the selected file (null if no file was selected)
         */
        public String getFileName(String dialogTitle, String filterDesc, List<String> allowedExts, String allowFileDir,
                boolean multiSelect)
        {
            int retVal = 0;
            String fileName = null;
            File[] files = null;

            JFileChooser chooser = new JFileChooser(lastFolder);
            chooser.setMultiSelectionEnabled(multiSelect);
            if(allowFileDir.equals("F"))
            {
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            }
            else if(allowFileDir.equals("D"))
            {
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            else if(allowFileDir.equals("B"))
            {
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            }

            if(filterDesc != null)
            {
                chooser.setFileFilter(new FileBrowserFilter(filterDesc, allowedExts));
            }
            chooser.setDialogTitle(dialogTitle);
            retVal = chooser.showOpenDialog(null);

            if(retVal == JFileChooser.APPROVE_OPTION)
            {
                if(multiSelect)
                {
                    StringBuffer fileList = new StringBuffer();
                    files = chooser.getSelectedFiles();
                    for(int i = 0; i < files.length; i++)
                    {
                        if(i != 0)
                        {
                            fileList.append(";");
                        }
                        fileList.append(files[i].getPath());
                    }
                    fileName = fileList.toString();
                }
                else
                {
                    fileName = chooser.getSelectedFile().getPath();
                }
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
            private List<String> allowedExts = null;

            /**
             * Default constructor
             * 
             * @param filterDesc Description of the filter
             * @param allowedExts List of allowed file extensions
             */
            public FileBrowserFilter(String filterDesc, List<String> allowedExts)
            {
                this.filterDesc = filterDesc;
                this.allowedExts = allowedExts;
            }

            /**
             * Implementation of <code>accept</accept> method of <code>FileFilter</code> class
             * 
             * @param file File to check whether it is acceptable by this filter or not
             * @return Flag to indicate whether file is acceptable or not
             */
            public boolean accept(File file)
            {
                if(file != null)
                {
                    if(this.allowedExts == null || this.allowedExts.size() == 0 || file.isDirectory())
                    {
                        return true;
                    }

                    for(int i = 0; i < this.allowedExts.size(); i++)
                    {
                        if(file.getName().toLowerCase().endsWith(this.allowedExts.get(i).toString()))
                        {
                            return true;
                        }
                    }
                }

                return false;
            }

            /**
             * Implementation of <code>getDescription</accept> method of <code>FileFilter</code> class
             * 
             * @return Description of the filter
             */
            public String getDescription()
            {
                return this.filterDesc;
            }
        }
    }
}
