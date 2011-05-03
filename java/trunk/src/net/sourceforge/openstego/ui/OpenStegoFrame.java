/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import net.sourceforge.openstego.OpenStego;
import net.sourceforge.openstego.util.LabelUtil;
import net.sourceforge.openstego.util.ui.JAccordion;

/**
 * Frame class to build the Swing UI for OpenStego. This class includes only graphics rendering
 * code. Listeners are implemented in {@link net.sourceforge.openstego.ui.OpenStegoUI} class.
 */
public class OpenStegoFrame extends JFrame
{
    private static final long serialVersionUID = -880718904125121559L;

    private static final boolean toggleUiHack = false;

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
     * Embed OK button
     */
    private JButton runEmbedButton;

    /**
     * Extract OK button
     */
    private JButton runExtractButton;

    private JToggleButton embedButton;
    private JToggleButton extractButton;

    private JToggleButton signWmButton;
    private JToggleButton verifyWmButton;

    private JPanel mainPanel;
    private JPanel dhPanel;
    private JPanel wmPanel;
    private JPanel embedPanel;
    private JPanel optionPanel;
    private JPanel extractPanel;
    private JPanel extractPwdPanel;

    private JScrollPane accordionPane = null;
    private JAccordion accordion = null;

    private ButtonGroup actionButtonGroup = new ButtonGroup();

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
            this.mainContentPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            this.mainContentPane.setLayout(new BorderLayout());
            this.mainContentPane.add(getMainPanel(), BorderLayout.CENTER);
            this.mainContentPane.add(getAccordionPane(), BorderLayout.WEST);
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
     * Get method for Embed "OK" button
     * 
     * @return runEmbedButton
     */
    public JButton getRunEmbedButton()
    {
        if(this.runEmbedButton == null)
        {
            this.runEmbedButton = new JButton();
            this.runEmbedButton.setText(labelUtil.getString("gui.button.ok"));
        }
        return this.runEmbedButton;
    }

    /**
     * Get method for Extract "OK" button
     * 
     * @return runExtractButton
     */
    public JButton getRunExtractButton()
    {
        if(this.runExtractButton == null)
        {
            this.runExtractButton = new JButton();
            this.runExtractButton.setText(labelUtil.getString("gui.button.ok"));
        }
        return this.runExtractButton;
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
            this.mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 1, Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            this.mainPanel.setLayout(new GridLayout());
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
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            label = new JLabel(labelUtil.getString("gui.label.msgFile"));
            label.setLabelFor(getMsgFileTextField());
            this.embedPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            label = new JLabel(labelUtil.getString("gui.label.coverFile"));
            label.setLabelFor(getCoverFileTextField());
            this.embedPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 6;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            label = new JLabel(labelUtil.getString("gui.label.outputStegoFile"));
            label.setLabelFor(getStegoFileTextField());
            this.embedPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.insets = new Insets(0, 5, 0, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            label = new JLabel(labelUtil.getString("gui.label.coverFileMsg"));
            label.setFont(label.getFont().deriveFont(Font.ITALIC));
            this.embedPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            this.embedPanel.add(getMsgFileTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 5;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            this.embedPanel.add(getCoverFileTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 7;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            this.embedPanel.add(getStegoFileTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            this.embedPanel.add(getMsgFileButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 5;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            this.embedPanel.add(getCoverFileButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 7;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            this.embedPanel.add(getStegoFileButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 8;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            this.embedPanel.add(getOptionPanel(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.EAST;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 9;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            this.embedPanel.add(getRunEmbedButton(), gridBagConstraints);
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
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            this.extractPanel.add(new JLabel(labelUtil.getString("gui.label.inputStegoFile")), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            this.extractPanel.add(new JLabel(labelUtil.getString("gui.label.outputDataFolder")), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            this.extractPanel.add(getInputStegoFileTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.insets = new Insets(0, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            this.extractPanel.add(getOutputFolderTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 5;
            gridBagConstraints.insets = new Insets(20, 5, 0, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            this.extractPanel.add(getExtractPwdPanel(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.insets = new Insets(0, 0, 5, 5);
            gridBagConstraints.weightx = 0.01;
            gridBagConstraints.weighty = 0.0;
            this.extractPanel.add(getInputStegoFileButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.insets = new Insets(0, 0, 5, 5);
            gridBagConstraints.weightx = 0.01;
            gridBagConstraints.weighty = 0.0;
            this.extractPanel.add(getOutputFolderButton(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.EAST;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 6;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 0.01;
            gridBagConstraints.weighty = 0.0;
            this.extractPanel.add(getRunExtractButton(), gridBagConstraints);

            // Dummy padding
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 7;
            gridBagConstraints.insets = new Insets(0, 0, 0, 0);
            gridBagConstraints.weightx = 0.01;
            gridBagConstraints.weighty = 1.0;
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
            JLabel label;
            this.optionPanel = new JPanel();
            this.optionPanel.setBorder(new TitledBorder(new CompoundBorder(new EmptyBorder(new java.awt.Insets(5, 5, 5,
                    5)), new EtchedBorder()), " " + labelUtil.getString("gui.label.option.title") + " "));
            this.optionPanel.setLayout(new GridBagLayout());

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            label = new JLabel(labelUtil.getString("gui.label.option.password"));
            label.setLabelFor(getPasswordTextField());
            this.optionPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            this.optionPanel.add(getPasswordTextField(), gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            label = new JLabel(labelUtil.getString("gui.label.option.confPassword"));
            label.setLabelFor(getConfPasswordTextField());
            this.optionPanel.add(label, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            this.optionPanel.add(getConfPasswordTextField(), gridBagConstraints);
        }
        return this.optionPanel;
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
     * Getter method for accordionPane
     * 
     * @return accordionPane
     */
    public JScrollPane getAccordionPane()
    {
        if(this.accordionPane == null)
        {
            this.accordionPane = new JScrollPane();
            this.accordionPane.setBorder(null);
            this.accordionPane.setViewportView(getAccordion());
            this.accordionPane.setPreferredSize(new Dimension(150, 0));
        }
        return this.accordionPane;
    }

    /**
     * Getter method for accordion
     * 
     * @return accordion
     */
    public JAccordion getAccordion()
    {
        if(this.accordion == null)
        {
            this.accordion = new JAccordion();
            this.accordion.addTab("Data Hiding", getDhPanel());
            this.accordion.addTab("Digital Watermarking", getWmPanel());
        }
        return this.accordion;
    }

    /**
     * Getter method for dhPanel
     * 
     * @return dhPanel
     */
    public JPanel getDhPanel()
    {
        if(this.dhPanel == null)
        {
            this.dhPanel = new JPanel();
            this.dhPanel.setLayout(new GridLayout(0, 1));
            this.dhPanel.add(getEmbedButton());
            this.dhPanel.add(getExtractButton());
        }
        return this.dhPanel;
    }

    /**
     * Getter method for wmPanel
     * 
     * @return wmPanel
     */
    public JPanel getWmPanel()
    {
        if(this.wmPanel == null)
        {
            this.wmPanel = new JPanel();
            this.wmPanel.setLayout(new GridLayout(0, 1));
            this.wmPanel.add(getSignWmButton());
            this.wmPanel.add(getVerifyWmButton());
        }
        return this.wmPanel;
    }

    /**
     * Getter method for embedButton
     * 
     * @return embedButton
     */
    public JToggleButton getEmbedButton()
    {
        if(this.embedButton == null)
        {
            this.embedButton = new JToggleButton(labelUtil.getString("gui.label.tab.embed"), new ImageIcon(getClass()
                    .getResource("/image/EmbedIcon.png")), true);
            if(toggleUiHack)
            {
                this.embedButton.setUI(new MetalToggleButtonUI());
            }
            this.embedButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            this.embedButton.setHorizontalTextPosition(SwingConstants.CENTER);
            this.embedButton.setFocusable(false);
            this.actionButtonGroup.add(this.embedButton);
        }
        return this.embedButton;
    }

    /**
     * Getter method for extractButton
     * 
     * @return extractButton
     */
    public JToggleButton getExtractButton()
    {
        if(this.extractButton == null)
        {
            this.extractButton = new JToggleButton(labelUtil.getString("gui.label.tab.extract"), new ImageIcon(
                    getClass().getResource("/image/ExtractIcon.png")));
            if(toggleUiHack)
            {
                this.extractButton.setUI(new MetalToggleButtonUI());
            }
            this.extractButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            this.extractButton.setHorizontalTextPosition(SwingConstants.CENTER);
            this.extractButton.setFocusable(false);
            this.actionButtonGroup.add(this.extractButton);
        }
        return this.extractButton;
    }

    /**
     * Getter method for signWmButton
     * 
     * @return signWmButton
     */
    public JToggleButton getSignWmButton()
    {
        if(this.signWmButton == null)
        {
            this.signWmButton = new JToggleButton("Embed Watermark", new ImageIcon(getClass().getResource(
                "/image/EmbedIcon.png")));
            if(toggleUiHack)
            {
                this.signWmButton.setUI(new MetalToggleButtonUI());
            }
            this.signWmButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            this.signWmButton.setHorizontalTextPosition(SwingConstants.CENTER);
            this.signWmButton.setFocusable(false);
            this.actionButtonGroup.add(this.signWmButton);
        }
        return this.signWmButton;
    }

    /**
     * Getter method for verifyWmButton
     * 
     * @return verifyWmButton
     */
    public JToggleButton getVerifyWmButton()
    {
        if(this.verifyWmButton == null)
        {
            this.verifyWmButton = new JToggleButton("Verify Watermark", new ImageIcon(getClass().getResource(
                "/image/ExtractIcon.png")));
            if(toggleUiHack)
            {
                this.verifyWmButton.setUI(new MetalToggleButtonUI());
            }
            this.verifyWmButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            this.verifyWmButton.setHorizontalTextPosition(SwingConstants.CENTER);
            this.verifyWmButton.setFocusable(false);
            this.actionButtonGroup.add(this.verifyWmButton);
        }
        return this.verifyWmButton;
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
            getMainContentPane().setFont(new Font("Japanese", Font.PLAIN, 12));
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initialize()
    {
        if(toggleUiHack)
        {
            UIManager.put("ToggleButton.select", new MetalLookAndFeel().getDefaults().getColor("ToggleButton.select")
                    .darker());
        }
        this.setContentPane(getMainContentPane());
        this.setTitle(labelUtil.getString("gui.window.title"));

        getMainPanel().add(getEmbedPanel());
    }

    /**
     * Method to set the action commands for interactive UI items
     */
    private void setActionCommands()
    {
        getMsgFileButton().setActionCommand("BROWSE_SRC_DATA");
        getCoverFileButton().setActionCommand("BROWSE_SRC_IMG");
        getStegoFileButton().setActionCommand("BROWSE_TGT_IMG");

        getInputStegoFileButton().setActionCommand("BROWSE_IMG_FOR_EXTRACT");
        getOutputFolderButton().setActionCommand("BROWSE_TGT_DATA");

        getEmbedButton().setActionCommand("SWITCH_EMBED");
        getExtractButton().setActionCommand("SWITCH_EXTRACT");
        getSignWmButton().setActionCommand("SWITCH_EMBEDWM");
        getVerifyWmButton().setActionCommand("SWITCH_VERIFYWM");

        getRunEmbedButton().setActionCommand("RUN_EMBED");
        getRunExtractButton().setActionCommand("RUN_EXTRACT");
    }
}
