/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2014 Samir Vaidya
 */

package net.sourceforge.openstego.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
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

    private JMenuBar topMenuBar;
    private JMenu fileMenu;
    private JMenuItem fileExitMenuItem;
    private JMenu helpMenu;
    private JMenuItem helpAboutMenuItem;

    private JPanel mainContentPane;

    private JScrollPane accordionPane;
    private JAccordion accordion;
    private JPanel dhPanel;
    private JPanel wmPanel;
    private ButtonGroup actionButtonGroup = new ButtonGroup();
    private JToggleButton embedButton;
    private JToggleButton extractButton;
    private JToggleButton genSigButton;
    private JToggleButton signWmButton;
    private JToggleButton verifyWmButton;

    private JPanel headerPanel;
    private JLabel header;

    private JPanel mainPanel;
    private EmbedPanel embedPanel;
    private ExtractPanel extractPanel;
    private GenerateSignaturePanel genSigPanel;
    private EmbedWatermarkPanel embedWmPanel;
    private VerifyWatermarkPanel verifyWmPanel;

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
     * Getter method for topMenuBar
     * 
     * @return topMenuBar
     */
    public JMenuBar getTopMenuBar()
    {
        if(this.topMenuBar == null)
        {
            this.topMenuBar = new JMenuBar();
            this.topMenuBar.add(getFileMenu());
            this.topMenuBar.add(getHelpMenu());
        }
        return this.topMenuBar;
    }

    /**
     * Getter method for fileMenu
     * 
     * @return fileMenu
     */
    public JMenu getFileMenu()
    {
        if(this.fileMenu == null)
        {
            this.fileMenu = new JMenu(labelUtil.getString("gui.menu.file"));
            this.fileMenu.setMnemonic(KeyEvent.VK_F);
            this.fileMenu.add(getFileExitMenuItem());
        }
        return this.fileMenu;
    }

    /**
     * Getter method for fileExitMenuItem
     * 
     * @return fileExitMenuItem
     */
    public JMenuItem getFileExitMenuItem()
    {
        if(this.fileExitMenuItem == null)
        {
            this.fileExitMenuItem = new JMenuItem(labelUtil.getString("gui.menu.file.exit"));
            this.fileExitMenuItem.setMnemonic(KeyEvent.VK_X);
        }
        return this.fileExitMenuItem;
    }

    /**
     * Getter method for helpMenu
     * 
     * @return helpMenu
     */
    public JMenu getHelpMenu()
    {
        if(this.helpMenu == null)
        {
            this.helpMenu = new JMenu(labelUtil.getString("gui.menu.help"));
            this.helpMenu.setMnemonic(KeyEvent.VK_H);
            this.helpMenu.add(getHelpAboutMenuItem());
        }
        return this.helpMenu;
    }

    /**
     * Getter method for helpAboutMenuItem
     * 
     * @return helpAboutMenuItem
     */
    public JMenuItem getHelpAboutMenuItem()
    {
        if(this.helpAboutMenuItem == null)
        {
            this.helpAboutMenuItem = new JMenuItem(labelUtil.getString("gui.menu.help.about"));
            this.helpAboutMenuItem.setMnemonic(KeyEvent.VK_A);
        }
        return this.helpAboutMenuItem;
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
            // this.mainContentPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            this.mainContentPane.setLayout(new GridBagLayout());

            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.gridy = 0;
            g.gridheight = 2;
            g.weightx = 0.0;
            g.fill = GridBagConstraints.VERTICAL;
            this.mainContentPane.add(getAccordionPane(), g);

            g = new GridBagConstraints();
            g.gridx = 1;
            g.gridy = 0;
            g.weighty = 0.0;
            g.fill = GridBagConstraints.HORIZONTAL;
            this.mainContentPane.add(getHeaderPanel(), g);

            g = new GridBagConstraints();
            g.gridx = 1;
            g.gridy = 1;
            g.weightx = 1.0;
            g.weighty = 1.0;
            g.fill = GridBagConstraints.BOTH;
            this.mainContentPane.add(getMainPanel(), g);
        }
        return this.mainContentPane;
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
            this.accordionPane.setPreferredSize(new Dimension(200, 0));
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
            this.accordion.addTab(labelUtil.getString("gui.label.tabHeader.dataHiding"), getDhPanel());
            this.accordion.addTab(labelUtil.getString("gui.label.tabHeader.watermarking"), getWmPanel());
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
            this.wmPanel.add(getGenSigButton());
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
            this.embedButton = new JToggleButton(labelUtil.getString("gui.label.tab.dhEmbed"), new ImageIcon(getClass()
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
            this.extractButton = new JToggleButton(labelUtil.getString("gui.label.tab.dhExtract"), new ImageIcon(
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
     * Getter method for genSigButton
     * 
     * @return genSigButton
     */
    public JToggleButton getGenSigButton()
    {
        if(this.genSigButton == null)
        {
            this.genSigButton = new JToggleButton(labelUtil.getString("gui.label.tab.wmGenSig"), new ImageIcon(
                    getClass().getResource("/image/EmbedIcon.png"))); // TODO
            if(toggleUiHack)
            {
                this.genSigButton.setUI(new MetalToggleButtonUI());
            }
            this.genSigButton.setVerticalTextPosition(SwingConstants.BOTTOM);
            this.genSigButton.setHorizontalTextPosition(SwingConstants.CENTER);
            this.genSigButton.setFocusable(false);
            this.actionButtonGroup.add(this.genSigButton);
        }
        return this.genSigButton;
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
            this.signWmButton = new JToggleButton(labelUtil.getString("gui.label.tab.wmEmbed"), new ImageIcon(
                    getClass().getResource("/image/EmbedIcon.png")));
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
            this.verifyWmButton = new JToggleButton(labelUtil.getString("gui.label.tab.wmVerify"), new ImageIcon(
                    getClass().getResource("/image/ExtractIcon.png")));
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
     * Getter method for headerPanel
     * 
     * @return headerPanel
     */
    public JPanel getHeaderPanel()
    {
        if(this.headerPanel == null)
        {
            this.headerPanel = new JPanel();
            this.headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 1, Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            this.headerPanel.setLayout(new GridLayout());
            this.headerPanel.add(getHeader());
        }
        return this.headerPanel;
    }

    /**
     * Getter method for header
     * 
     * @return header
     */
    public JLabel getHeader()
    {
        if(this.header == null)
        {
            this.header = new JLabel();
            this.header.setFont(this.header.getFont().deriveFont(Font.BOLD, this.header.getFont().getSize2D() + 3f));
        }
        return this.header;
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
    public EmbedPanel getEmbedPanel()
    {
        if(this.embedPanel == null)
        {
            this.embedPanel = new EmbedPanel();
        }
        return this.embedPanel;
    }

    /**
     * Getter method for extractPanel
     * 
     * @return extractPanel
     */
    public ExtractPanel getExtractPanel()
    {
        if(this.extractPanel == null)
        {
            this.extractPanel = new ExtractPanel();
        }
        return this.extractPanel;
    }

    /**
     * Getter method for genSigPanel
     * 
     * @return genSigPanel
     */
    public GenerateSignaturePanel getGenSigPanel()
    {
        if(this.genSigPanel == null)
        {
            this.genSigPanel = new GenerateSignaturePanel();
        }
        return this.genSigPanel;
    }

    /**
     * Getter method for embedWmPanel
     * 
     * @return embedWmPanel
     */
    public EmbedWatermarkPanel getEmbedWmPanel()
    {
        if(this.embedWmPanel == null)
        {
            this.embedWmPanel = new EmbedWatermarkPanel();
        }
        return this.embedWmPanel;
    }

    /**
     * Getter method for verifyWmPanel
     * 
     * @return verifyWmPanel
     */
    public VerifyWatermarkPanel getVerifyWmPanel()
    {
        if(this.verifyWmPanel == null)
        {
            this.verifyWmPanel = new VerifyWatermarkPanel();
        }
        return this.verifyWmPanel;
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
        this.setJMenuBar(getTopMenuBar());

        getMainPanel().add(getEmbedPanel());
        getHeader().setText(labelUtil.getString("gui.label.panelHeader.dhEmbed"));
    }

    /**
     * Method to set the action commands for interactive UI items
     */
    private void setActionCommands()
    {
        getFileExitMenuItem().setActionCommand(ActionCommands.MENU_FILE_EXIT);
        getHelpAboutMenuItem().setActionCommand(ActionCommands.MENU_HELP_ABOUT);

        getEmbedButton().setActionCommand(ActionCommands.SWITCH_DH_EMBED);
        getExtractButton().setActionCommand(ActionCommands.SWITCH_DH_EXTRACT);
        getGenSigButton().setActionCommand(ActionCommands.SWITCH_WM_GENSIG);
        getSignWmButton().setActionCommand(ActionCommands.SWITCH_WM_EMBED);
        getVerifyWmButton().setActionCommand(ActionCommands.SWITCH_WM_VERIFY);

        getEmbedPanel().getMsgFileButton().setActionCommand(ActionCommands.BROWSE_DH_EMB_MSGFILE);
        getEmbedPanel().getCoverFileButton().setActionCommand(ActionCommands.BROWSE_DH_EMB_CVRFILE);
        getEmbedPanel().getStegoFileButton().setActionCommand(ActionCommands.BROWSE_DH_EMB_STGFILE);
        getEmbedPanel().getRunEmbedButton().setActionCommand(ActionCommands.RUN_DH_EMBED);

        getExtractPanel().getInputStegoFileButton().setActionCommand(ActionCommands.BROWSE_DH_EXT_STGFILE);
        getExtractPanel().getOutputFolderButton().setActionCommand(ActionCommands.BROWSE_DH_EXT_OUTDIR);
        getExtractPanel().getRunExtractButton().setActionCommand(ActionCommands.RUN_DH_EXTRACT);

        getGenSigPanel().getSignatureFileButton().setActionCommand(ActionCommands.BROWSE_WM_GSG_SIGFILE);
        getGenSigPanel().getRunGenSigButton().setActionCommand(ActionCommands.RUN_WM_GENSIG);

        getEmbedWmPanel().getFileForWmButton().setActionCommand(ActionCommands.BROWSE_WM_EMB_INPFILE);
        getEmbedWmPanel().getSignatureFileButton().setActionCommand(ActionCommands.BROWSE_WM_EMB_SIGFILE);
        getEmbedWmPanel().getOutputWmFileButton().setActionCommand(ActionCommands.BROWSE_WM_EMB_OUTFILE);
        getEmbedWmPanel().getRunEmbedWmButton().setActionCommand(ActionCommands.RUN_WM_EMBED);

        getVerifyWmPanel().getInputFileButton().setActionCommand(ActionCommands.BROWSE_WM_VER_INPFILE);
        getVerifyWmPanel().getSignatureFileButton().setActionCommand(ActionCommands.BROWSE_WM_VER_SIGFILE);
        getVerifyWmPanel().getRunVerifyWmButton().setActionCommand(ActionCommands.RUN_WM_VERIFY);
    }

    /**
     * Enumeration for button actions
     */
    public interface ActionCommands
    {
        /**
         * Menu - File - Exit
         */
        public static String MENU_FILE_EXIT = "MENU_FILE_EXIT";
        /**
         * Menu - Help - About
         */
        public static String MENU_HELP_ABOUT = "MENU_HELP_ABOUT";

        /**
         * Switch to Data Hiding - Embed panel
         */
        public static String SWITCH_DH_EMBED = "SWITCH_DH_EMBED";
        /**
         * Switch to Data Hiding - Extract panel
         */
        public static String SWITCH_DH_EXTRACT = "SWITCH_DH_EXTRACT";
        /**
         * Switch to Watermarking - GenSig panel
         */
        public static String SWITCH_WM_GENSIG = "SWITCH_WM_GENSIG";
        /**
         * Switch to Watermarking - Embed panel
         */
        public static String SWITCH_WM_EMBED = "SWITCH_WM_EMBED";
        /**
         * Switch to Watermarking - Verify panel
         */
        public static String SWITCH_WM_VERIFY = "SWITCH_WM_VERIFY";

        /**
         * Browse action for DH-Embed-MessageFile
         */
        public static String BROWSE_DH_EMB_MSGFILE = "BROWSE_DH_EMB_MSGFILE";
        /**
         * Browse action for DH-Embed-CoverFile
         */
        public static String BROWSE_DH_EMB_CVRFILE = "BROWSE_DH_EMB_CVRFILE";
        /**
         * Browse action for DH-Embed-StegoFile
         */
        public static String BROWSE_DH_EMB_STGFILE = "BROWSE_DH_EMB_STGFILE";
        /**
         * Execute DH-Embed
         */
        public static String RUN_DH_EMBED = "RUN_DH_EMBED";

        /**
         * Browse action for DH-Extract-StegoFile
         */
        public static String BROWSE_DH_EXT_STGFILE = "BROWSE_DH_EXT_STGFILE";
        /**
         * Browse action for DH-Extract-OutputFolder
         */
        public static String BROWSE_DH_EXT_OUTDIR = "BROWSE_DH_EXT_OUTDIR";
        /**
         * Execute DH-Extract
         */
        public static String RUN_DH_EXTRACT = "RUN_DH_EXTRACT";

        /**
         * Browse action for WM-GenSig-SigFile
         */
        public static String BROWSE_WM_GSG_SIGFILE = "BROWSE_WM_GSG_SIGFILE";
        /**
         * Execute WM-GenSig
         */
        public static String RUN_WM_GENSIG = "RUN_WM_GENSIG";

        /**
         * Browse action for WM-Embed-InputFile
         */
        public static String BROWSE_WM_EMB_INPFILE = "BROWSE_WM_EMB_INPFILE";
        /**
         * Browse action for WM-Embed-SignatureFile
         */
        public static String BROWSE_WM_EMB_SIGFILE = "BROWSE_WM_EMB_SIGFILE";
        /**
         * Browse action for WM-Embed-OutputFile
         */
        public static String BROWSE_WM_EMB_OUTFILE = "BROWSE_WM_EMB_OUTFILE";
        /**
         * Execute WM-Embed
         */
        public static String RUN_WM_EMBED = "RUN_WM_EMBED";

        /**
         * Browse action for WM-Verify-InputFile
         */
        public static String BROWSE_WM_VER_INPFILE = "BROWSE_WM_VER_INPFILE";
        /**
         * Browse action for WM-Verify-SignatureFile
         */
        public static String BROWSE_WM_VER_SIGFILE = "BROWSE_WM_VER_SIGFILE";
        /**
         * Execute WM-Verify
         */
        public static String RUN_WM_VERIFY = "RUN_WM_VERIFY";
    }
}
