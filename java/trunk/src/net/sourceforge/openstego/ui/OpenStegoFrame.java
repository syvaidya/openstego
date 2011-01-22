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
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.plaf.FontUIResource;

import net.sourceforge.openstego.OpenStego;
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

    private JPanel mainContentPane;
    /**
     * "Message File" text field
     */
    private JTextField msgFileTextField;

    /**
     * "Message File" browse file button
     */
    private JButton msgFileButton;

    /**
     * "Cover File" text field
     */
    private JTextField coverFileTextField;

    /**
     * "Cover File" browse file button
     */
    private JButton coverFileButton;

    /**
     * "Stego File" text field
     */
    private JTextField stegoFileTextField;

    /**
     * "Stego File" browse file button
     */
    private JButton stegoFileButton;

    /**
     * "Password" text field
     */
    private JPasswordField passwordTextField;

    /**
     * "Confirm Password" text field
     */
    private JPasswordField confPasswordTextField;

    /**
     * "Input Stego File" text field
     */
    private JTextField inputStegoFileTextField;

    /**
     * "Input Stego File" browse file button
     */
    private JButton inputStegoFileButton;

    /**
     * "Output Folder" text field
     */
    private JTextField outputFolderTextField;

    /**
     * "Password for Extract" text field
     */
    private JPasswordField extractPwdTextField;

    /**
     * "Output Folder" browse file button
     */
    private JButton outputFolderButton;

    /**
     * "OK" button
     */
    private JButton okButton;

    /**
     * "Cancel" button
     */
    private JButton cancelButton;

    /**
     * Tabbed pane for embed/extract tabs
     */
    private JTabbedPane mainTabbedPane;

    /**
     * Password panel handle (for enable/disable)
     */
    private JPanel passwordPanel;

    private JPanel mainPanel;
    private JPanel embedPanel;
    private JPanel extractPanel;
    private JPanel optionPanel;
    private JPanel buttonPanel;
    private JPanel extractPwdPanel;

    /**
     * Default constructor
     */
    public OpenStegoFrame()
    {
        super();
        initialize();
        setActionCommands();
        setupUI();
    }

    /**
     * Getter method for mainContentPane
     * 
     * @return mainContentPane
     */
    public JPanel getMainContentPane()
    {
        if(this.mainContentPane == null)
        {
            this.mainContentPane = new JPanel();
            this.mainContentPane.setLayout(new BorderLayout());
            this.mainContentPane.add(getMainPanel(), BorderLayout.CENTER);
            this.mainContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
        }
        return this.mainContentPane;
    }

    /**
     * Get method for "Message File" text field
     * 
     * @return msgFileTextField
     */
    public JTextField getMsgFileTextField()
    {
        if(this.msgFileTextField == null)
        {
            this.msgFileTextField = new JTextField();
            this.msgFileTextField.setColumns(57);
        }
        return this.msgFileTextField;
    }

    /**
     * Get method for "Message File" browse file button
     * 
     * @return msgFileButton
     */
    public JButton getMsgFileButton()
    {
        if(this.msgFileButton == null)
        {
            this.msgFileButton = new JButton();
            this.msgFileButton.setText("...");
            this.msgFileButton.setPreferredSize(new Dimension(22, 22));
        }
        return this.msgFileButton;
    }

    /**
     * Get method for "Cover File" text field
     * 
     * @return coverFileTextField
     */
    public JTextField getCoverFileTextField()
    {
        if(this.coverFileTextField == null)
        {
            this.coverFileTextField = new JTextField();
            this.coverFileTextField.setColumns(57);
        }
        return this.coverFileTextField;
    }

    /**
     * Get method for "Cover File" browse file button
     * 
     * @return coverFileButton
     */
    public JButton getCoverFileButton()
    {
        if(this.coverFileButton == null)
        {
            this.coverFileButton = new JButton();
            this.coverFileButton.setText("...");
            this.coverFileButton.setPreferredSize(new Dimension(22, 22));
        }
        return this.coverFileButton;
    }

    /**
     * Get method for "Stego File" text field
     * 
     * @return stegoFileTextField
     */
    public JTextField getStegoFileTextField()
    {
        if(this.stegoFileTextField == null)
        {
            this.stegoFileTextField = new JTextField();
            this.stegoFileTextField.setColumns(57);
        }
        return this.stegoFileTextField;
    }

    /**
     * Get method for "Stego File" browse file button
     * 
     * @return stegoFileButton
     */
    public JButton getStegoFileButton()
    {
        if(this.stegoFileButton == null)
        {
            this.stegoFileButton = new JButton();
            this.stegoFileButton.setText("...");
            this.stegoFileButton.setPreferredSize(new Dimension(22, 22));
        }
        return this.stegoFileButton;
    }

    /**
     * Get method for "Password" text field
     * 
     * @return passwordTextField
     */
    public JPasswordField getPasswordTextField()
    {
        if(this.passwordTextField == null)
        {
            this.passwordTextField = new JPasswordField();
            this.passwordTextField.setColumns(15);
        }
        return this.passwordTextField;
    }

    /**
     * Get method for "Confirm Password" text field
     * 
     * @return confPasswordTextField
     */
    public JPasswordField getConfPasswordTextField()
    {
        if(this.confPasswordTextField == null)
        {
            this.confPasswordTextField = new JPasswordField();
            this.confPasswordTextField.setColumns(15);
        }
        return this.confPasswordTextField;
    }

    /**
     * Get method for "Input Stego File" text field
     * 
     * @return inputStegoFileTextField
     */
    public JTextField getInputStegoFileTextField()
    {
        if(this.inputStegoFileTextField == null)
        {
            this.inputStegoFileTextField = new JTextField();
            this.inputStegoFileTextField.setColumns(57);
        }
        return this.inputStegoFileTextField;
    }

    /**
     * Get method for "Input Stego File" browse file button
     * 
     * @return inputStegoFileButton
     */
    public JButton getInputStegoFileButton()
    {
        if(this.inputStegoFileButton == null)
        {
            this.inputStegoFileButton = new JButton();
            this.inputStegoFileButton.setText("...");
            this.inputStegoFileButton.setPreferredSize(new Dimension(22, 22));
        }
        return this.inputStegoFileButton;
    }

    /**
     * Get method for "Output Folder" text field
     * 
     * @return outputFolderTextField
     */
    public JTextField getOutputFolderTextField()
    {
        if(this.outputFolderTextField == null)
        {
            this.outputFolderTextField = new JTextField();
            this.outputFolderTextField.setColumns(57);
        }
        return this.outputFolderTextField;
    }

    /**
     * Get method for "Output Folder" browse file button
     * 
     * @return outputFolderButton
     */
    public JButton getOutputFolderButton()
    {
        if(this.outputFolderButton == null)
        {
            this.outputFolderButton = new JButton();
            this.outputFolderButton.setText("...");
            this.outputFolderButton.setPreferredSize(new Dimension(22, 22));
        }
        return this.outputFolderButton;
    }

    /**
     * Get method for "Password for Extract" text field
     * 
     * @return extractPwdTextField
     */
    public JPasswordField getExtractPwdTextField()
    {
        if(this.extractPwdTextField == null)
        {
            this.extractPwdTextField = new JPasswordField();
            this.extractPwdTextField.setColumns(20);
        }
        return this.extractPwdTextField;
    }

    /**
     * Get method for "OK" button
     * 
     * @return okButton
     */
    public JButton getOkButton()
    {
        if(this.okButton == null)
        {
            this.okButton = new JButton();
            this.okButton.setText(labelUtil.getString("gui.button.ok"));
        }
        return this.okButton;
    }

    /**
     * Get method for "Cancel" button
     * 
     * @return cancelButton
     */
    public JButton getCancelButton()
    {
        if(this.cancelButton == null)
        {
            this.cancelButton = new JButton();
            this.cancelButton.setText(labelUtil.getString("gui.button.cancel"));
        }
        return this.cancelButton;
    }

    /**
     * Getter method for mainTabbedPane
     * 
     * @return mainTabbedPane
     */
    public JTabbedPane getMainTabbedPane()
    {
        if(this.mainTabbedPane == null)
        {
            this.mainTabbedPane = new JTabbedPane();
            this.mainTabbedPane.addTab(labelUtil.getString("gui.label.tab.embed"), new ImageIcon(getClass()
                    .getResource("/image/EmbedIcon.png")), getEmbedPanel());
            this.mainTabbedPane.addTab(labelUtil.getString("gui.label.tab.extract"), new ImageIcon(getClass()
                    .getResource("/image/ExtractIcon.png")), getExtractPanel());
        }
        return this.mainTabbedPane;
    }

    /**
     * Getter method for passwordPanel
     * 
     * @return passwordPanel
     */
    public JPanel getPasswordPanel()
    {
        if(this.passwordPanel == null)
        {
            JLabel label;
            this.passwordPanel = new JPanel();
            this.passwordPanel.setBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5, 5)),
                    new EtchedBorder()));
            this.passwordPanel.setLayout(new GridBagLayout());

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.gridx = 0;
            label = new JLabel(labelUtil.getString("gui.label.option.password"));
            label.setLabelFor(getPasswordTextField());
            this.passwordPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.gridx = 1;
            this.passwordPanel.add(getPasswordTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.gridx = 2;
            label = new JLabel(labelUtil.getString("gui.label.option.confPassword"));
            label.setLabelFor(getConfPasswordTextField());
            this.passwordPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.gridx = 3;
            this.passwordPanel.add(getConfPasswordTextField(), gridBagConstraints);
        }
        return this.passwordPanel;
    }

    /**
     * Getter method for mainPanel
     * 
     * @return mainPanel
     */
    public JPanel getMainPanel()
    {
        if(this.mainPanel == null)
        {
            this.mainPanel = new JPanel();
            this.mainPanel.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
            this.mainPanel.add(getMainTabbedPane());
        }
        return this.mainPanel;
    }

    /**
     * Getter method for embedPanel
     * 
     * @return embedPanel
     */
    public JPanel getEmbedPanel()
    {
        if(this.embedPanel == null)
        {
            JLabel label;
            this.embedPanel = new JPanel();
            this.embedPanel.setLayout(new GridBagLayout());

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            gridBagConstraints.gridy = 1;
            label = new JLabel(labelUtil.getString("gui.label.msgFile"));
            label.setLabelFor(getMsgFileTextField());
            this.embedPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            gridBagConstraints.gridy = 3;
            label = new JLabel(labelUtil.getString("gui.label.coverFile"));
            label.setLabelFor(getCoverFileTextField());
            this.embedPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            gridBagConstraints.gridy = 6;
            label = new JLabel(labelUtil.getString("gui.label.outputStegoFile"));
            label.setLabelFor(getStegoFileTextField());
            this.embedPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 5, 0, 5);
            gridBagConstraints.gridy = 4;
            label = new JLabel(labelUtil.getString("gui.label.coverFileMsg"));
            label.setFont(label.getFont().deriveFont(Font.ITALIC));
            this.embedPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridy = 2;
            this.embedPanel.add(getMsgFileTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridy = 5;
            this.embedPanel.add(getCoverFileTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridy = 7;
            this.embedPanel.add(getStegoFileTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridy = 7;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.insets = new Insets(0, 0, 5, 5);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            this.embedPanel.add(getMsgFileButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 5;
            this.embedPanel.add(getCoverFileButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 7;
            this.embedPanel.add(getStegoFileButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 8;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            this.embedPanel.add(getOptionPanel(), gridBagConstraints);
        }
        return this.embedPanel;
    }

    /**
     * Getter method for extractPanel
     * 
     * @return extractPanel
     */
    public JPanel getExtractPanel()
    {
        if(this.extractPanel == null)
        {
            this.extractPanel = new JPanel();
            this.extractPanel.setLayout(new GridBagLayout());

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            gridBagConstraints.gridy = 1;
            this.extractPanel.add(new JLabel(labelUtil.getString("gui.label.inputStegoFile")), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            gridBagConstraints.gridy = 3;
            this.extractPanel.add(new JLabel(labelUtil.getString("gui.label.outputDataFolder")), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridy = 2;
            this.extractPanel.add(getInputStegoFileTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridy = 4;
            this.extractPanel.add(getOutputFolderTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridy = 5;
            gridBagConstraints.insets = new Insets(20, 5, 0, 5);
            this.extractPanel.add(getExtractPwdPanel(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.insets = new Insets(0, 0, 5, 5);
            gridBagConstraints.weightx = 0.01;
            gridBagConstraints.gridy = 2;
            this.extractPanel.add(getInputStegoFileButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.insets = new Insets(0, 0, 5, 5);
            gridBagConstraints.weightx = 0.01;
            gridBagConstraints.gridy = 4;
            this.extractPanel.add(getOutputFolderButton(), gridBagConstraints);

            // Dummy padding
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 0.01;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 6;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(0, 0, 0, 0);
            this.extractPanel.add(new JLabel(" "), gridBagConstraints);
        }
        return this.extractPanel;
    }

    /**
     * Getter method for optionPanel
     * 
     * @return optionPanel
     */
    public JPanel getOptionPanel()
    {
        if(this.optionPanel == null)
        {
            this.optionPanel = new JPanel();
            this.optionPanel.setBorder(new TitledBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5,
                    5)), new EtchedBorder()), " " + labelUtil.getString("gui.label.option.title") + " "));
            this.optionPanel.setLayout(new GridBagLayout());

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.insets = new Insets(0, 0, 0, 0);
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            this.optionPanel.add(getPasswordPanel(), gridBagConstraints);
        }
        return this.optionPanel;
    }

    /**
     * Getter method for buttonPanel
     * 
     * @return buttonPanel
     */
    public JPanel getButtonPanel()
    {
        if(this.buttonPanel == null)
        {
            this.buttonPanel = new JPanel();
            this.buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            this.buttonPanel.setBorder(new EmptyBorder(new Insets(0, 5, 5, 5)));
            this.buttonPanel.add(getOkButton());
            this.buttonPanel.add(getCancelButton());
        }
        return this.buttonPanel;
    }

    /**
     * Getter method for extractPwdPanel
     * 
     * @return extractPwdPanel
     */
    public JPanel getExtractPwdPanel()
    {
        if(this.extractPwdPanel == null)
        {
            this.extractPwdPanel = new JPanel();
            ((FlowLayout) this.extractPwdPanel.getLayout()).setAlignment(FlowLayout.LEFT);
            this.extractPwdPanel.add(new JLabel(labelUtil.getString("gui.label.option.password")));
            this.extractPwdPanel.add(getExtractPwdTextField());
        }
        return this.extractPwdPanel;
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
    private void initialize()
    {
        this.setContentPane(getMainContentPane());
        this.setTitle(labelUtil.getString("gui.window.title"));
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
    }
}
