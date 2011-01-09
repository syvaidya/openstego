/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.FontUIResource;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.OpenStegoException;
import net.sourceforge.openstego.util.CommonUtil;
import net.sourceforge.openstego.util.LabelUtil;

/**
 * Frame class to build the Swing UI for OpenStego. This class includes only graphics rendering
 * code. Listeners are implemented in {@link net.sourceforge.openstego.ui.OpenStegoUI} class.
 */
public class OpenStegoFrame extends JFrame
{
    private static final long serialVersionUID = -880718904125121559L;

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    /**
     * Combobox for "Stego Algorithm" for "Embed"
     */
    protected JComboBox embedAlgoComboBox = new JComboBox();

    /**
     * Combobox for "Stego Algorithm" for "Extract"
     */
    protected JComboBox extractAlgoComboBox = new JComboBox();

    /**
     * "Message File" text field
     */
    protected JTextField msgFileTextField = new JTextField();

    /**
     * "Message File" browse file button
     */
    protected JButton msgFileButton = new JButton();

    /**
     * "Cover File" text field
     */
    protected JTextField coverFileTextField = new JTextField();

    /**
     * "Cover File" browse file button
     */
    protected JButton coverFileButton = new JButton();

    /**
     * "Stego File" text field
     */
    protected JTextField stegoFileTextField = new JTextField();

    /**
     * "Stego File" browse file button
     */
    protected JButton stegoFileButton = new JButton();

    /**
     * Checkbox for "Use Compression"
     */
    protected JCheckBox useCompCheckBox = new JCheckBox();

    /**
     * Checkbox for "Use Encryption"
     */
    protected JCheckBox useEncryptCheckBox = new JCheckBox();

    /**
     * "Password" text field
     */
    protected JPasswordField passwordTextField = new JPasswordField();

    /**
     * "Confirm Password" text field
     */
    protected JPasswordField confPasswordTextField = new JPasswordField();

    /**
     * "Input Stego File" text field
     */
    protected JTextField inputStegoFileTextField = new JTextField();

    /**
     * "Input Stego File" browse file button
     */
    protected JButton inputStegoFileButton = new JButton();

    /**
     * "Output Folder" text field
     */
    protected JTextField outputFolderTextField = new JTextField();

    /**
     * "Password for Extract" text field
     */
    protected JPasswordField extractPwdTextField = new JPasswordField();

    /**
     * "Output Folder" browse file button
     */
    protected JButton outputFolderButton = new JButton();

    /**
     * "OK" button
     */
    protected JButton okButton = new JButton();

    /**
     * "Cancel" button
     */
    protected JButton cancelButton = new JButton();

    /**
     * Tabbed pane for embed/extract tabs
     */
    protected JTabbedPane mainTabbedPane = new JTabbedPane();

    /**
     * Panel for plugin specific options for "Embed" action
     */
    protected JPanel pluginEmbedOptionsPanel = new JPanel();

    /**
     * Password panel handle (for enable/disable)
     */
    private JPanel passwordPanel = new JPanel();

    /**
     * Default constructor
     */
    public OpenStegoFrame()
    {
        setupUI();
        initComponents();
        setActionCommands();
    }

    /**
     * Get method for "Message File" text field
     * 
     * @return msgFileTextField
     */
    public JTextField getMsgFileTextField()
    {
        return this.msgFileTextField;
    }

    /**
     * Get method for "Message File" browse file button
     * 
     * @return msgFileButton
     */
    public JButton getMsgFileButton()
    {
        return this.msgFileButton;
    }

    /**
     * Get method for "Cover File" text field
     * 
     * @return coverFileTextField
     */
    public JTextField getCoverFileTextField()
    {
        return this.coverFileTextField;
    }

    /**
     * Get method for "Cover File" browse file button
     * 
     * @return coverFileButton
     */
    public JButton getCoverFileButton()
    {
        return this.coverFileButton;
    }

    /**
     * Get method for "Stego File" text field
     * 
     * @return stegoFileTextField
     */
    public JTextField getStegoFileTextField()
    {
        return this.stegoFileTextField;
    }

    /**
     * Get method for "Stego File" browse file button
     * 
     * @return stegoFileButton
     */
    public JButton getStegoFileButton()
    {
        return this.stegoFileButton;
    }

    /**
     * Get method for Checkbox for "Use Compression"
     * 
     * @return useCompCheckBox
     */
    public JCheckBox getUseCompCheckBox()
    {
        return this.useCompCheckBox;
    }

    /**
     * Get method for Checkbox for "Use Encryption"
     * 
     * @return useEncryptCheckBox
     */
    public JCheckBox getUseEncryptCheckBox()
    {
        return this.useEncryptCheckBox;
    }

    /**
     * Get method for "Password" text field
     * 
     * @return passwordTextField
     */
    public JPasswordField getPasswordTextField()
    {
        return this.passwordTextField;
    }

    /**
     * Get method for "Confirm Password" text field
     * 
     * @return confPasswordTextField
     */
    public JPasswordField getConfPasswordTextField()
    {
        return this.confPasswordTextField;
    }

    /**
     * Get method for "Input Stego File" text field
     * 
     * @return inputStegoFileTextField
     */
    public JTextField getInputStegoFileTextField()
    {
        return this.inputStegoFileTextField;
    }

    /**
     * Get method for "Input Stego File" browse file button
     * 
     * @return inputStegoFileButton
     */
    public JButton getInputStegoFileButton()
    {
        return this.inputStegoFileButton;
    }

    /**
     * Get method for "Output Folder" text field
     * 
     * @return outputFolderTextField
     */
    public JTextField getOutputFolderTextField()
    {
        return this.outputFolderTextField;
    }

    /**
     * Get method for "Output Folder" browse file button
     * 
     * @return outputFolderButton
     */
    public JButton getOutputFolderButton()
    {
        return this.outputFolderButton;
    }

    /**
     * Get method for "Password for Extract" text field
     * 
     * @return extractPwdTextField
     */
    public JPasswordField getExtractPwdTextField()
    {
        return this.extractPwdTextField;
    }

    /**
     * Get method for "OK" button
     * 
     * @return okButton
     */
    public JButton getOkButton()
    {
        return this.okButton;
    }

    /**
     * Get method for "Cancel" button
     * 
     * @return cancelButton
     */
    public JButton getCancelButton()
    {
        return this.cancelButton;
    }

    /**
     * This methos initializes the UI resources like fonts, size, etc.
     */
    private void setupUI()
    {
        // Special handling to ensure that Japanese fonts are readable
        if(Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage()))
        {
            Object key = null;
            Object value = null;
            Enumeration<?> keys = UIManager.getDefaults().keys();
            while(keys.hasMoreElements())
            {
                key = keys.nextElement();
                value = UIManager.get(key);
                if(value instanceof FontUIResource)
                {
                    UIManager.put(key, ((FontUIResource) value).deriveFont(12.0f));
                }
            }
            this.mainTabbedPane.setFont(new Font("Japanese", Font.PLAIN, 12));
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents()
    {
        GridBagConstraints gridBagConstraints = null;
        JPanel mainPanel = new JPanel();
        JPanel embedPanel = new JPanel();
        JPanel extractPanel = new JPanel();
        JPanel embedAlgoPanel = new JPanel();
        JPanel extractAlgoPanel = new JPanel();
        JPanel optionPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        JPanel extractPwdPanel = new JPanel();
        JLabel label = null;

        mainPanel.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
        embedPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        embedAlgoPanel.add(new JLabel(labelUtil.getString("gui.label.algorithmList")));
        embedAlgoPanel.add(this.embedAlgoComboBox);

        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        gridBagConstraints.gridy = 0;
        embedPanel.add(embedAlgoPanel, gridBagConstraints);

        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.gridy = 1;
        label = new JLabel(labelUtil.getString("gui.label.msgFile"));
        label.setLabelFor(this.msgFileTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 3;
        label = new JLabel(labelUtil.getString("gui.label.coverFile"));
        label.setLabelFor(this.coverFileTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 6;
        label = new JLabel(labelUtil.getString("gui.label.outputStegoFile"));
        label.setLabelFor(this.stegoFileTextField);
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        gridBagConstraints.gridy = 4;
        label = new JLabel(labelUtil.getString("gui.label.coverFileMsg"));
        label.setFont(label.getFont().deriveFont(Font.ITALIC));
        embedPanel.add(label, gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;

        this.msgFileTextField.setColumns(57);
        gridBagConstraints.gridy = 2;
        embedPanel.add(this.msgFileTextField, gridBagConstraints);

        this.coverFileTextField.setColumns(57);
        gridBagConstraints.gridy = 5;
        embedPanel.add(this.coverFileTextField, gridBagConstraints);

        this.stegoFileTextField.setColumns(57);
        gridBagConstraints.gridy = 7;
        embedPanel.add(this.stegoFileTextField, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);

        this.msgFileButton.setText("...");
        this.msgFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 2;
        embedPanel.add(this.msgFileButton, gridBagConstraints);

        this.coverFileButton.setText("...");
        this.coverFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 5;
        embedPanel.add(this.coverFileButton, gridBagConstraints);

        this.stegoFileButton.setText("...");
        this.stegoFileButton.setPreferredSize(new Dimension(22, 22));
        gridBagConstraints.gridy = 7;
        embedPanel.add(this.stegoFileButton, gridBagConstraints);

        optionPanel.setBorder(new TitledBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5, 5)),
                new EtchedBorder()), " " + labelUtil.getString("gui.label.option.title") + " "));
        optionPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        gridBagConstraints.gridy = 0;
        label = new JLabel(labelUtil.getString("gui.label.option.useCompression"));
        label.setLabelFor(this.useCompCheckBox);
        optionPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        label = new JLabel(labelUtil.getString("gui.label.option.useEncryption"));
        label.setLabelFor(this.useEncryptCheckBox);
        optionPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 10, 5, 5);
        optionPanel.add(this.useCompCheckBox, gridBagConstraints);

        gridBagConstraints.gridy = 1;
        optionPanel.add(this.useEncryptCheckBox, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        optionPanel.add(this.passwordPanel, gridBagConstraints);

        this.passwordPanel.setBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5, 5)),
                new EtchedBorder()));
        this.passwordPanel.setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        gridBagConstraints.gridx = 0;
        label = new JLabel(labelUtil.getString("gui.label.option.password"));
        label.setLabelFor(this.passwordTextField);
        this.passwordPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        this.passwordTextField.setColumns(15);
        this.passwordPanel.add(this.passwordTextField, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        label = new JLabel(labelUtil.getString("gui.label.option.confPassword"));
        label.setLabelFor(this.confPasswordTextField);
        this.passwordPanel.add(label, gridBagConstraints);

        gridBagConstraints.gridx = 3;
        this.confPasswordTextField.setColumns(15);
        this.passwordPanel.add(this.confPasswordTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        embedPanel.add(optionPanel, gridBagConstraints);

        this.pluginEmbedOptionsPanel.setBorder(new TitledBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(
                5, 5, 5, 5)), new EtchedBorder()), " " + labelUtil.getString("gui.label.pluginOption.title") + " "));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        embedPanel.add(this.pluginEmbedOptionsPanel, gridBagConstraints);

        extractPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;

        extractAlgoPanel.add(new JLabel(labelUtil.getString("gui.label.algorithmList")));
        extractAlgoPanel.add(this.extractAlgoComboBox);

        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        gridBagConstraints.gridy = 0;
        extractPanel.add(extractAlgoPanel, gridBagConstraints);

        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        gridBagConstraints.gridy = 1;
        extractPanel.add(new JLabel(labelUtil.getString("gui.label.inputStegoFile")), gridBagConstraints);

        gridBagConstraints.gridy = 3;
        extractPanel.add(new JLabel(labelUtil.getString("gui.label.outputDataFolder")), gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;

        gridBagConstraints.gridy = 2;
        this.inputStegoFileTextField.setColumns(57);
        extractPanel.add(this.inputStegoFileTextField, gridBagConstraints);

        gridBagConstraints.gridy = 4;
        this.outputFolderTextField.setColumns(57);
        extractPanel.add(this.outputFolderTextField, gridBagConstraints);

        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(20, 5, 0, 5);
        extractPanel.add(extractPwdPanel, gridBagConstraints);
        ((FlowLayout) extractPwdPanel.getLayout()).setAlignment(FlowLayout.LEFT);
        extractPwdPanel.add(new JLabel(labelUtil.getString("gui.label.option.password")));
        this.extractPwdTextField.setColumns(20);
        extractPwdPanel.add(this.extractPwdTextField);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.weightx = 0.01;

        gridBagConstraints.gridy = 2;
        this.inputStegoFileButton.setText("...");
        this.inputStegoFileButton.setPreferredSize(new Dimension(22, 22));
        extractPanel.add(this.inputStegoFileButton, gridBagConstraints);

        gridBagConstraints.gridy = 4;
        this.outputFolderButton.setText("...");
        this.outputFolderButton.setPreferredSize(new Dimension(22, 22));
        extractPanel.add(this.outputFolderButton, gridBagConstraints);

        // Dummy padding
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        extractPanel.add(new JLabel(" "), gridBagConstraints);

        this.mainTabbedPane.addTab(labelUtil.getString("gui.label.tab.embed"),
            new ImageIcon(getClass().getResource("/image/EmbedIcon.png")), embedPanel);
        this.mainTabbedPane.addTab(labelUtil.getString("gui.label.tab.extract"),
            new ImageIcon(getClass().getResource("/image/ExtractIcon.png")), extractPanel);

        mainPanel.add(this.mainTabbedPane);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(new Insets(0, 5, 5, 5)));

        this.okButton.setText(labelUtil.getString("gui.button.ok"));
        buttonPanel.add(this.okButton);

        this.cancelButton.setText(labelUtil.getString("gui.button.cancel"));
        buttonPanel.add(this.cancelButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setTitle(labelUtil.getString("gui.window.title"));
    }

    /**
     * Method to set the action commands for interactive UI items
     */
    private void setActionCommands()
    {
        this.msgFileButton.setActionCommand("BROWSE_SRC_DATA");
        this.coverFileButton.setActionCommand("BROWSE_SRC_IMG");
        this.stegoFileButton.setActionCommand("BROWSE_TGT_IMG");

        this.inputStegoFileButton.setActionCommand("BROWSE_IMG_FOR_EXTRACT");
        this.outputFolderButton.setActionCommand("BROWSE_TGT_DATA");

        this.okButton.setActionCommand("OK");
        this.cancelButton.setActionCommand("CANCEL");

        ChangeListener changeListener = new ChangeListener()
        {
            public void stateChanged(ChangeEvent changeEvent)
            {
                useEncryptionChanged();
            }
        };
        this.useEncryptCheckBox.addChangeListener(changeListener);
        useEncryptionChanged();

        ActionListener actionListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    embedAlgoChanged();
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        };
        this.embedAlgoComboBox.addActionListener(actionListener);
    }

    /**
     * Method to handle change event for 'useEncryption'
     */
    private void useEncryptionChanged()
    {
        if(this.useEncryptCheckBox.isSelected())
        {
            CommonUtil.setEnabled(this.passwordTextField, true);
            CommonUtil.setEnabled(this.confPasswordTextField, true);
            this.passwordTextField.requestFocus();
        }
        else
        {
            CommonUtil.setEnabled(this.passwordTextField, false);
            CommonUtil.setEnabled(this.confPasswordTextField, false);
        }
    }

    /**
     * Method to handle change event for 'embedAlgoComboBox'
     * 
     * @throws OpenStegoException
     */
    protected void embedAlgoChanged() throws OpenStegoException
    {
    }
}
