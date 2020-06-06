/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util.dwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class to read the Filters XML and generate corresponding Java Objects
 */
public class FilterXMLReader {
    /**
     * Constructor is private so that this class is not instantiated
     */
    private FilterXMLReader() {
    }

    /**
     * This method parses the given XML file into the list of objects
     *
     * @param fileURI URI for the XML file
     * @return Map of filters with key being Integer object for filter ID
     */
    public static Map<Integer, FilterGH> parse(String fileURI) {
        Map<Integer, FilterGH> filterGHMap = new HashMap<Integer, FilterGH>();
        DocumentBuilder db = null;
        Document dom = null;
        Element el = null;
        NodeList nl = null;
        FilterGH filterGH = null;

        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            // Parse to get DOM representation of the XML file
            dom = db.parse(FilterXMLReader.class.getResourceAsStream(fileURI));

            // Get a node list of filterGH elements
            nl = dom.getDocumentElement().getElementsByTagName("filterGH");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    // Get the 'filterGH' element
                    el = (Element) nl.item(i);

                    // Create the object
                    filterGH = getFilterGH(el);

                    // Add it to map
                    filterGHMap.put(filterGH.getId(), filterGH);
                }
            }

            return filterGHMap;
        } catch (ParserConfigurationException pcEx) {
            pcEx.printStackTrace();
            throw new IllegalArgumentException("Invalid Filter XML file");
        } catch (SAXException saxEx) {
            saxEx.printStackTrace();
            throw new IllegalArgumentException("Invalid Filter XML file");
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            throw new IllegalArgumentException("Invalid Filter XML file");
        }
    }

    /**
     * This method reads XML node and creates corresponding FilterGH object
     *
     * @param el XML node element
     * @return FilterGH object
     */
    private static FilterGH getFilterGH(Element el) {
        FilterGH filterGH = new FilterGH();
        Element innerEl = null;
        NodeList nl = null;
        Filter filter = null;

        filterGH.setId(Integer.parseInt(el.getAttribute("id")));
        filterGH.setName(el.getAttribute("name"));
        filterGH.setType(el.getAttribute("type"));

        // Get a nodelist of filter elements
        nl = el.getElementsByTagName("filter");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                // Get the 'filter' element
                innerEl = (Element) nl.item(i);

                // Create the object
                filter = getFilter(innerEl);
                filter.setHiPass(!((i & 1) == 1));

                // Set the object in filterGH
                if (i == 0) {
                    filterGH.setG(filter);
                } else if (i == 1) {
                    filterGH.setH(filter);
                } else if (i == 2) {
                    filterGH.setGi(filter);
                } else if (i == 3) {
                    filterGH.setHi(filter);
                }
            }
        }

        return filterGH;
    }

    /**
     * This method reads XML node and creates corresponding Filter object
     *
     * @param el XML node element
     * @return Filter object
     */
    private static Filter getFilter(Element el) {
        Filter filter = new Filter();
        Element innerEl = null;
        NodeList nl = null;
        double[] data = null;

        filter.setType(el.getAttribute("type"));
        filter.setStart(Integer.parseInt(el.getAttribute("start")));
        filter.setEnd(Integer.parseInt(el.getAttribute("end")));

        // Get a nodelist of data elements
        nl = el.getElementsByTagName("data");
        if (nl != null && nl.getLength() > 0) {
            data = new double[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
                // Get the 'data' element
                innerEl = (Element) nl.item(i);

                // Add data to array
                data[i] = Double.parseDouble(innerEl.getFirstChild().getNodeValue());
            }
        }

        filter.setData(data);

        return filter;
    }
}
