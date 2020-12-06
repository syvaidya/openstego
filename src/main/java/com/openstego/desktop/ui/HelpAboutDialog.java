/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */
package com.openstego.desktop.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.util.LabelUtil;

/**
 * Dialog to show Help - About information
 */
public class HelpAboutDialog extends JDialog {
    private static final long serialVersionUID = 2707372931999569066L;

    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    private JEditorPane content;
    private JButton okButton;

    /**
     * Getter method for content
     *
     * @return content
     */
    public JEditorPane getContent() {
        if (this.content == null) {
            StringBuffer buf = new StringBuffer();

            buf.append("<html>");
            buf.append("  <table width=100% cellspacing=0 cellpadding=0 style='font:serif;'>");
            buf.append("    <tr style='background-color:white'>");
            buf.append("      <td align=left style='padding-left:8px'>");
            buf.append("        <span style='font-size:24px; font-weight:bold'>");
            buf.append("          ").append(labelUtil.getString("appName")).append("</span>");
            buf.append("        <p style='font-size:10px; margin-top:6px'>");
            buf.append("          ").append(labelUtil.getString("appVersion")).append("</p>");
            buf.append("      </td>");
            buf.append("      <td align=right>");
            buf.append("        <img src='").append(getClass().getResource("/images/About.png").toString()).append("'/></td>");
            buf.append("    </tr>");
            buf.append("    <tr>");
            buf.append("      <td colspan=2 style='padding:5px;font-size:10px'>");
            buf.append("        <p>").append(labelUtil.getString("copyright")).append("</p><br/>");
            buf.append("        <p>").append(labelUtil.getString("gui.label.help.sitelink")).append("</p><br/>");
            buf.append("        <p><u>").append(labelUtil.getString("gui.label.help.ackHeader")).append("</u></p>");
            buf.append("        <ol style='margin-left:10px; margin-top:2px;'>");
            buf.append("          <li>").append(labelUtil.getString("gui.label.help.acknowledgement")).append("</li>");
            buf.append("        </ol>");
            buf.append("      </td>");
            buf.append("    </tr>");
            buf.append("  </table>");
            buf.append("</html>");

            this.content = new JEditorPane("text/html", buf.toString());
            this.content.setEditable(false);
            this.content.setBorder(BorderFactory.createEmptyBorder());
            this.content.setBackground(getBackground());
            this.content.setPreferredSize(new Dimension(450, 320));

            this.content.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        JEditorPane pane = (JEditorPane) e.getSource();
                        if (e.getURL() == null) {
                            BareBonesBrowserLaunch.openURL(labelUtil.getString("homepage"), pane);
                        } else {
                            BareBonesBrowserLaunch.openURL(e.getURL().toString(), pane);
                        }
                    }
                }
            });
        }
        return this.content;
    }

    /**
     * Getter method for okButton
     *
     * @return okButton
     */
    public JButton getOkButton() {
        if (this.okButton == null) {
            this.okButton = new JButton("OK");
            this.okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });
        }
        return this.okButton;
    }

    /**
     * Default constructor
     *
     * @param parent Parent frame for the dialog box
     */
    public HelpAboutDialog(Frame parent) {
        super(parent, "About OpenStego", true);

        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.weighty = 1.0;
        g.fill = GridBagConstraints.BOTH;
        getContentPane().add(getContent(), g);

        g = new GridBagConstraints();
        g.gridy = 1;
        g.weighty = 0.0;
        g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(getOkButton(), g);

        // "Esc" key handling
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = -4890560722044735566L;

            @Override
            public void actionPerformed(ActionEvent ev) {
                setVisible(false);
            }
        };

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        pack();
        setResizable(false);
        Dimension parentSize = parent.getSize();
        setLocation(parent.getLocation().x + parentSize.width / 2 - (getWidth() / 2),
            parent.getLocation().y + parentSize.height / 2 - (getHeight() / 2));
    }

    /**
     * Helper class to launch browser. This code is copied from: http://www.centerkey.com/java/browser/
     */
    static class BareBonesBrowserLaunch {
        private static final String[] browsers = { "firefox", "google-chrome", "opera", "epiphany", "konqueror", "conkeror", "midori", "kazehakase",
                "mozilla" };

        public static void openURL(String url, JComponent parent) {
            try {
                // Attempt to use Desktop library from JDK 1.6+
                Class<?> d = Class.forName("java.awt.Desktop");
                d.getDeclaredMethod("browse", new Class[] { java.net.URI.class }).invoke(d.getDeclaredMethod("getDesktop").invoke(null),
                    new Object[] { java.net.URI.create(url) });
                // Above code mimics: java.awt.Desktop.getDesktop().browse()
            } catch (Exception ignore) {
                // Library not available or failed
                String osName = System.getProperty("os.name");
                try {
                    if (osName.startsWith("Mac OS")) {
                        Class.forName("com.apple.eio.FileManager").getDeclaredMethod("openURL", new Class[] { String.class }).invoke(null,
                            new Object[] { url });
                    } else if (osName.startsWith("Windows")) {
                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                    } else {
                        // Assume Unix or Linux
                        String browser = null;
                        for (String b : browsers) {
                            if (browser == null && Runtime.getRuntime().exec(new String[] { "which", b }).getInputStream().read() != -1) {
                                Runtime.getRuntime().exec(new String[] { browser = b, url });
                            }
                        }
                        if (browser == null) {
                            throw new Exception(Arrays.toString(browsers));
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(parent, labelUtil.getString("gui.msg.err.browserLaunch"), labelUtil.getString("gui.msg.title.err"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
