/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */
package com.openstego.desktop.util.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;

import com.openstego.desktop.OpenStego;
import com.openstego.desktop.util.LabelUtil;

/**
 * Helper class to execute tasks asynchronously
 */
public abstract class WorkerTask extends SwingWorker<Object, Void> {
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(OpenStego.NAMESPACE);

    /**
     * Parent component
     */
    protected JFrame parent;
    /**
     * Data for task
     */
    protected Object data;
    /**
     * Progress bar
     */
    protected JProgressBar progressBar;
    /**
     * Cancel button
     */
    protected JButton cancelButton;
    /**
     * Glass pane
     */
    protected GlassPane glass;

    /**
     * Default constructor
     *
     * @param parent Parent component
     * @param data Any data to be passed to task
     * @param determinate Flag to indicate whether task progress is determinable or not
     */
    public WorkerTask(JFrame parent, Object data, boolean determinate) {
        this.parent = parent;
        this.data = data;

        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setPreferredSize(new Dimension(300, 20));
        this.progressBar.setValue(0);
        if (determinate) {
            this.progressBar.setStringPainted(true);
        } else {
            this.progressBar.setIndeterminate(true);
            this.progressBar.setStringPainted(false);
        }

        this.glass = new GlassPane();
        this.glass.setSize(parent.getSize());
        this.glass.setOpaque(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.gridy = 0;
        g.insets = new Insets(10, 10, 10, 10);
        panel.add(new JLabel(labelUtil.getString("gui.label.progress.processing")), g);

        g.fill = GridBagConstraints.BOTH;
        g.gridx = 0;
        g.gridy = 1;
        g.insets = new Insets(0, 10, 10, 10);
        panel.add(this.progressBar, g);

        this.cancelButton = new JButton(labelUtil.getString("gui.label.progress.cancel"));
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.EAST;
        g.gridx = 0;
        g.gridy = 2;
        g.insets = new Insets(0, 10, 10, 10);
        panel.add(this.cancelButton, g);

        this.glass.setLayout(new GridBagLayout());
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.CENTER;
        g.gridx = 0;
        g.gridy = 0;
        this.glass.add(panel, g);
        this.parent.setGlassPane(this.glass);
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done() {
        this.glass.setVisible(false);
    }

    /**
     * Method to execute task
     */
    public void start() {
        Listener listener = new Listener(this);
        this.glass.setVisible(true);
        this.cancelButton.addActionListener(listener);
        addPropertyChangeListener(listener);
        execute();
    }

    class Listener implements PropertyChangeListener, ActionListener {
        WorkerTask task;

        public Listener(WorkerTask task) {
            this.task = task;
        }

        /*
         * (non-Javadoc)
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("progress".equals(evt.getPropertyName())) {
                int progress = (Integer) evt.getNewValue();
                this.task.progressBar.setValue(progress);
            }
        }

        /*
         * (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            this.task.cancel(true);
        }
    }

    class GlassPane extends JPanel implements MouseListener, FocusListener {
        /**
         *
         */
        private static final long serialVersionUID = -9083908347142749524L;

        public GlassPane() {
            addMouseListener(this);
            addFocusListener(this);
        }

        @Override
        public void paintComponent(Graphics g) {
            g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.5f));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }

        @Override
        public void setVisible(boolean visible) {
            if (visible) {
                requestFocus();
            }
            super.setVisible(visible);
        }

        @Override
        public void focusLost(FocusEvent fe) {
            if (isVisible()) {
                requestFocus();
            }
        }

        @Override
        public void focusGained(FocusEvent fe) {
        }

        @Override
        public void mouseClicked(MouseEvent arg0) {
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
        }

        @Override
        public void mousePressed(MouseEvent arg0) {
        }

        @Override
        public void mouseReleased(MouseEvent arg0) {
        }
    }
}
