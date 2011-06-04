/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2011 Samir Vaidya
 */
package net.sourceforge.openstego.util.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 * Helper class to execute tasks asynchronously
 */
public abstract class WorkerTask extends SwingWorker<Object, Void>
{
    /**
     * Parent component
     */
    protected JFrame parent;
    /**
     * Progress bar
     */
    protected JProgressBar progressBar;
    /**
     * Dialog box
     */
    protected JFrame dialog;
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
     */
    public WorkerTask(JFrame parent)
    {
        this.parent = parent;

        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setPreferredSize(new Dimension(300, 20));
        this.progressBar.setStringPainted(true);
        this.progressBar.setValue(0);

        this.dialog = new JFrame("Processing");
        Container rootPane = this.dialog.getContentPane();
        rootPane.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.gridx = 0;
        g.gridy = 0;
        g.insets = new Insets(10, 10, 10, 10);
        rootPane.add(this.progressBar, g);

        this.cancelButton = new JButton("Cancel");
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.EAST;
        g.gridx = 0;
        g.gridy = 1;
        g.insets = new Insets(0, 10, 10, 10);
        rootPane.add(this.cancelButton, g);

        this.dialog.pack();
        this.dialog.setLocationRelativeTo(parent);
        this.dialog.setLocation((parent.getWidth() - this.dialog.getWidth()) / 2,
            (parent.getHeight() - this.dialog.getHeight()) / 2);

        this.glass = new GlassPane();
        this.glass.setSize(parent.getSize());
        this.glass.setOpaque(false);
        this.parent.setGlassPane(this.glass);
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.SwingWorker#done()
     */
    protected void done()
    {
        this.dialog.setVisible(false);
        this.glass.setVisible(false);
    }

    /**
     * Static method to execute tasks
     * 
     * @param task
     */
    public static void exec(WorkerTask task)
    {
        Listener listener = new Listener(task);
        task.glass.setVisible(true);
        task.dialog.setVisible(true);
        task.dialog.addWindowListener(listener);
        task.cancelButton.addActionListener(listener);
        task.addPropertyChangeListener(listener);
        task.execute();
    }

    static class Listener extends WindowAdapter implements PropertyChangeListener, ActionListener
    {
        WorkerTask task;

        public Listener(WorkerTask task)
        {
            this.task = task;
        }

        /*
         * (non-Javadoc)
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if("progress".equals(evt.getPropertyName()))
            {
                int progress = (Integer) evt.getNewValue();
                this.task.progressBar.setValue(progress);
            }
        }

        /*
         * (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            this.task.cancel(true);
        }

        /*
         * (non-Javadoc)
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         */
        public void windowClosing(WindowEvent e)
        {
            this.task.cancel(true);
        }
    }

    class GlassPane extends JPanel implements MouseListener, FocusListener
    {
        public GlassPane()
        {
            addMouseListener(this);
            addFocusListener(this);
        }

        public void setVisible(boolean visible)
        {
            if(visible)
            {
                requestFocus();
            }
            super.setVisible(visible);
        }

        public void focusLost(FocusEvent fe)
        {
            if(isVisible())
            {
                requestFocus();
            }
        }

        public void focusGained(FocusEvent fe)
        {
        }

        public void mouseClicked(MouseEvent arg0)
        {
        }

        public void mouseEntered(MouseEvent arg0)
        {
        }

        public void mouseExited(MouseEvent arg0)
        {
        }

        public void mousePressed(MouseEvent arg0)
        {
        }

        public void mouseReleased(MouseEvent arg0)
        {
        }
    }
}
