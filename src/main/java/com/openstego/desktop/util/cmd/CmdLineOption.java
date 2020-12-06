/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util.cmd;

/**
 * Stores the master information about a command line option
 *
 * @see CmdLineParser
 */
public class CmdLineOption {
    /**
     * Command-line option type: COMMAND
     */
    public static final int TYPE_COMMAND = 0;

    /**
     * Command-line option type: OPTION
     */
    public static final int TYPE_OPTION = 1;

    /**
     * Name of the option
     */
    private String name = null;

    /**
     * Alternate name of the option
     */
    private String altName = null;

    /**
     * Type of the option
     */
    private int type = -1;

    /**
     * Flag to indicate whether the option takes argument or not
     */
    private boolean takesArgVal = false;

    /**
     * Value of the option
     */
    private String value = null;

    /**
     * Default constructor
     *
     * @param name Name of the option
     * @param altName Altername name of the option
     * @param type Type of the option
     * @param takesArgVal Flag to indicate whether the option takes argument or not
     * @throws IllegalArgumentException If option type is TYPE_COMMAND and takesArgVal is specified as true
     */
    public CmdLineOption(String name, String altName, int type, boolean takesArgVal) throws IllegalArgumentException {
        this.name = name;
        this.altName = altName;
        this.type = type;
        this.takesArgVal = takesArgVal;

        if (type == TYPE_COMMAND && takesArgVal) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set method for value
     *
     * @param value Value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get method for name
     *
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get method for altName
     *
     * @return altN
     */
    public String getAltName() {
        return this.altName;
    }

    /**
     * Get method for type
     *
     * @return type
     */
    public int getType() {
        return this.type;
    }

    /**
     * Get method for takesArgVal
     *
     * @return takesArgVal
     */
    public boolean takesArg() {
        return this.takesArgVal;
    }

    /**
     * Get method for value
     *
     * @return value
     */
    public String getValue() {
        return this.value;
    }
}
