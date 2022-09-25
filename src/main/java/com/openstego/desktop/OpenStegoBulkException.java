/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom exception class to store multiple errors
 */
public class OpenStegoBulkException extends Exception {

    /**
     * List of keys for the exception
     */
    private final List<String> keys = new ArrayList<>();
    /**
     * List of exceptions
     */
    private final List<OpenStegoException> exceptions = new ArrayList<>();

    /**
     * Add an exception to this bulk list
     *
     * @param key Key for the exception (e.g. filename)
     * @param e   Exception to be added
     */
    public void add(String key, OpenStegoException e) {
        keys.add(key);
        exceptions.add(e);
    }

    /**
     * Return the current list of keys
     */
    public List<String> getKeys() {
        return keys;
    }

    /**
     * Return the current list of exceptions
     */
    public List<OpenStegoException> getExceptions() {
        return exceptions;
    }

    /**
     * Throw this exception if list is not empty
     */
    public void throwIfRequired() throws OpenStegoBulkException {
        if (!exceptions.isEmpty()) {
            throw this;
        }
    }

}
