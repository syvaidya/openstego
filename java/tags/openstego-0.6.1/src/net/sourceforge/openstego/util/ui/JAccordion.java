/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Original source copied from http://greybeardedgeek.net/wordpress/wp-content/uploads/2008/04/jaccordion.java
 * Modifications - Copyright (c) 2011-2014 Samir Vaidya
 */

package net.sourceforge.openstego.util.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * This class is based on the code provided at: <a
 * href="http://greybeardedgeek.net/wordpress/wp-content/uploads/2008/04/jaccordion.java">
 * http://greybeardedgeek.net/wordpress/wp-content/uploads/2008/04/jaccordion.java</a>
 * <p>
 * This class provides accordion UI component. It is slightly modified from original source
 */
public class JAccordion extends JPanel implements ActionListener
{
    private static final Color BORDER_COLOR = Color.DARK_GRAY.brighter();

    /**
     * The top panel: contains the buttons displayed on the top of accordion
     */
    private JPanel topPanel = new JPanel(new GridLayout(1, 1));

    /**
     * The bottom panel: contains the buttons displayed on the bottom of the accordion
     */
    private JPanel bottomPanel = new JPanel(new GridLayout(1, 1));

    /**
     * A LinkedHashMap of tabs: we use a linked hash map to preserve the order of the tabs
     */
    private Map<String, TabInfo> tabs = new LinkedHashMap<String, TabInfo>();

    /**
     * The currently visible tab (zero-based index)
     */
    private int visibleTab = 0;

    /**
     * A place-holder for the currently visible component
     */
    private JPanel visibleComponent = null;

    /**
     * Creates a new JAccordion; after which you should make repeated calls to {@link #addTab(String, JComponent)} or
     * {@link #addTab(String, Icon, JComponent)} for each tab
     */
    public JAccordion()
    {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, BORDER_COLOR));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.weighty = 0.0;
        c.gridx = 0;

        c.gridy = 0;
        this.add(this.topPanel, c);

        c.gridy = 2;
        this.add(this.bottomPanel, c);

        this.visibleComponent = new JPanel();
        // this.visibleComponent.setLayout(new BorderLayout());
        this.visibleComponent.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
    }

    /**
     * Adds the specified component to the JAccordion and sets the tab's name
     * 
     * @param name The name of the tab in accordion
     * @param component The component to add to the tab
     */
    public void addTab(String name, JComponent component)
    {
        addTab(name, null, component);
    }

    /**
     * Adds the specified component to the JAccordion and sets the tab's name
     * 
     * @param name The name of the tab in accordion
     * @param icon An icon to display in the tab
     * @param component The component to add to the tab
     */
    public void addTab(String name, Icon icon, JComponent component)
    {
        component.setOpaque(false);
        TabInfo tabInfo = new TabInfo(name, icon, component);
        tabInfo.getHeader().getButton().addActionListener(this);
        this.tabs.put(name, tabInfo);
        render();
    }

    /**
     * Removes the specified tab from the JAccordion
     * 
     * @param name The name of the tab to remove
     */
    public void removeTab(String name)
    {
        this.tabs.remove(name);
        render();
    }

    /**
     * Returns the index of the currently visible tab (zero-based)
     * 
     * @return The index of the currently visible tab
     */
    public int getVisibleTab()
    {
        return this.visibleTab;
    }

    /**
     * Programmatically sets the currently visible tab; the visible tab index must be in the range of 0 to size() - 1
     * 
     * @param visibleTab The zero-based index of the component to make visible
     */
    public void setVisibleTab(int visibleTab)
    {
        if((visibleTab > 0) && (visibleTab < (this.tabs.size() - 1)))
        {
            this.visibleTab = visibleTab;
            render();
        }
    }

    /**
     * Causes the accordion component to rebuild itself; this means that it rebuilds the top and bottom panels of tabs
     * as well as making the currently selected tab's panel visible
     */
    public void render()
    {
        // Compute how many tabs we are going to have where
        int totalTabs = this.tabs.size();
        int topTabs = this.visibleTab + 1;
        int bottomTabs = totalTabs - topTabs;

        // Get an iterator to walk through out tabs with
        Iterator<String> itr = this.tabs.keySet().iterator();

        // Render the top tabs: remove all components, reset the GridLayout to hold to correct number of tabs, add the
        // tabs, and "validate" it to cause it to re-layout its components
        this.topPanel.removeAll();
        GridLayout topLayout = (GridLayout) this.topPanel.getLayout();
        topLayout.setRows(topTabs);
        TabInfo tabInfo = null;
        for(int i = 0; i < topTabs; i++)
        {
            String tabName = itr.next();
            tabInfo = this.tabs.get(tabName);
            this.topPanel.add(tabInfo.getHeader());
        }
        this.topPanel.validate();

        // Render the center component: remove the current component (if there is one) and then put the visible
        // component in the center of this panel
        this.visibleComponent.removeAll();
        this.visibleComponent.add(tabInfo.getComponent(), BorderLayout.NORTH);
        this.visibleComponent.validate();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        this.add(this.visibleComponent, c);

        // Render the bottom tabs: remove all components, reset the GridLayout to hold to correct number of tabs, add
        // the tabs, and "validate" it to cause it to re-layout its components
        this.bottomPanel.removeAll();
        GridLayout bottomLayout = (GridLayout) this.bottomPanel.getLayout();
        bottomLayout.setRows(bottomTabs);
        for(int i = 0; i < bottomTabs; i++)
        {
            String tabName = itr.next();
            tabInfo = this.tabs.get(tabName);
            this.bottomPanel.add(tabInfo.getHeader());
        }
        this.bottomPanel.validate();

        // Validate all of our components: cause this container to re-layout its subcomponents
        validate();
    }

    /**
     * Invoked when one of our tabs is selected
     */
    public void actionPerformed(ActionEvent e)
    {
        int currentTab = 0;
        for(Iterator<String> i = this.tabs.keySet().iterator(); i.hasNext();)
        {
            String tabName = i.next();
            TabInfo tabInfo = this.tabs.get(tabName);
            if(tabInfo.getHeader().getButton() == e.getSource())
            {
                // Found the selected button
                this.visibleTab = currentTab;
                render();
                return;
            }
            currentTab++;
        }
    }

    /**
     * Internal class that maintains information about individual accordion tabs; specifically it maintains the
     * following information: name The name of the tab button The associated JButton for the tab component The component
     * maintained in the tab
     */
    class TabInfo
    {
        /**
         * The name of this tab
         */
        private String name;

        /**
         * The header of the accordion tab
         */
        private TabHeader header;

        /**
         * The component that is the body of the tab
         */
        private JComponent component;

        /**
         * Creates a new TabInfo
         * 
         * @param name The name of the tab
         * @param component The component that is the body of the Tab
         */
        public TabInfo(String name, JComponent component)
        {
            this(name, null, component);
        }

        /**
         * Creates a new TabInfo
         * 
         * @param name The name of the tab
         * @param icon JButton icon
         * @param component The component that is the body of the Tab
         */
        public TabInfo(String name, Icon icon, JComponent component)
        {
            this.name = name;
            this.component = component;
            this.header = new TabHeader(name, icon);
        }

        /**
         * Returns the name of the tab
         * 
         * @return The name of the tab
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Sets the name of the tab
         * 
         * @param name The name of the tab
         */
        public void setName(String name)
        {
            this.name = name;
        }

        /**
         * Getter method for header
         * 
         * @return header
         */
        public TabHeader getHeader()
        {
            return this.header;
        }

        /**
         * Returns the component that implements the body of this tab
         * 
         * @return The component that implements the body of this tab
         */
        public JComponent getComponent()
        {
            return this.component;
        }
    }

    /**
     * Inner class for tab headers
     */
    class TabHeader extends GradientPanel
    {
        private JButton button;

        /**
         * Constructor with name
         * 
         * @param name Name of header
         */
        public TabHeader(String name)
        {
            this(name, null);
        }

        /**
         * Constructor with name and icon
         * 
         * @param name Name of the header
         * @param icon Icon for the header
         */
        public TabHeader(String name, Icon icon)
        {
            super((new JPanel()).getBackground(), (new JPanel()).getBackground().darker());

            createButton(name, icon);
            this.setLayout(new GridLayout(1, 1));

            this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
            add(this.button);
        }

        /**
         * Getter method for button
         * 
         * @return button
         */
        public JButton getButton()
        {
            return this.button;
        }

        private void createButton(String name, Icon icon)
        {
            this.button = new JButton(name, icon);

            this.button.setContentAreaFilled(false);
            this.button.setOpaque(false);
            this.button.setMargin(new Insets(3, 3, 3, 3));
            this.button.setFont(this.button.getFont().deriveFont(Font.BOLD));
            this.button.setFocusable(false);
        }
    }

    class GradientPanel extends JPanel
    {
        private Color startColor;
        private Color endColor;

        /**
         * Default constructor
         * 
         * @param startColor
         * @param endColor
         */
        public GradientPanel(Color startColor, Color endColor)
        {
            this.startColor = startColor;
            this.endColor = endColor;
        }

        /*
         * (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            int panelHeight = getHeight();
            int panelWidth = getWidth();

            GradientPaint gradientPaint = new GradientPaint(0, 0, this.startColor, 0, panelHeight, this.endColor);
            if(g instanceof Graphics2D)
            {
                Graphics2D graphics2D = (Graphics2D) g;
                graphics2D.setPaint(gradientPaint);
                graphics2D.fillRect(0, 0, panelWidth, panelHeight);
            }
        }
    }
}