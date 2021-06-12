/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */
package com.openstego.desktop.ui;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.util.LabelUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Objects;

/**
 * Dialog to show Help - About information
 */
public class HelpAboutDialog extends JDialog {
    private static final long serialVersionUID = 2707372931999569066L;

    /**
     * LabelUtil instance to retrieve labels
     */
    private static final LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    private JEditorPane content;
    private JButton okButton;

    /**
     * Getter method for content
     *
     * @return content
     */
    public JEditorPane getContent() {
        if (this.content == null) {
            String buf = "<html>" +
                    "  <table width=100% cellspacing=0 cellpadding=0 style='font:serif;'>" +
                    "    <tr style='background-color:white'>" +
                    "      <td align=left style='padding-left:8px'>" +
                    "        <span style='font-size:24px; font-weight:bold'>" +
                    "          " + labelUtil.getString("appName") + "</span>" +
                    "        <p style='font-size:10px; margin-top:6px'>" +
                    "          " + labelUtil.getString("appVersion") + "</p>" +
                    "      </td>" +
                    "      <td align=right>" +
                    "        <img src='" + Objects.requireNonNull(getClass().getResource("/images/About.png")) + "'/>" +
                    "      </td>" +
                    "    </tr>" +
                    "    <tr>" +
                    "      <td colspan=2 style='padding:5px;font-size:10px'>" +
                    "        <p>" + labelUtil.getString("copyright") + "</p><br/>" +
                    "        <p>" + labelUtil.getString("gui.label.help.sitelink") + "</p><br/>" +
                    "        <p><b>" + labelUtil.getString("gui.label.help.ackHeader") + "</b></p>" +
                    "        <ol style='margin-left:10px; margin-top:2px;'>" +
                    "          <li>" + labelUtil.getString("gui.label.help.acknowledgement") + "</li>" +
                    "        </ol>" +
                    "      </td>" +
                    "    </tr>" +
                    "  </table>" +
                    "</html>";
            this.content = new JEditorPane("text/html", buf);
            this.content.setEditable(false);
            this.content.setBorder(BorderFactory.createEmptyBorder());
            this.content.setBackground(getBackground());
            this.content.setPreferredSize(new Dimension(450, 320));

            this.content.addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    JEditorPane pane = (JEditorPane) e.getSource();
                    if (e.getURL() == null) {
                        openURL(labelUtil.getString("homepage"), pane);
                    } else {
                        openURL(e.getURL().toString(), pane);
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
            this.okButton.addActionListener(e -> setVisible(false));
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

    private void openURL(String url, JComponent parent) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception ignore) {
            JOptionPane.showMessageDialog(parent,
                    labelUtil.getString("gui.msg.err.browserLaunch"),
                    labelUtil.getString("gui.msg.title.err"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
