/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util.cmd;

import com.openstego.desktop.OpenStegoException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to store the list of command line options
 *
 * @see CmdLineParser
 */
public class CmdLineOptions {
    /**
     * Map to store the standard command-line options
     */
    private final Map<String, CmdLineOption> map = new HashMap<>();

    /**
     * List to store the standard command-line options
     */
    private final List<CmdLineOption> list = new ArrayList<>();

    /**
     * Default constructor
     */
    public CmdLineOptions() {
    }

    /**
     * Method to add the command-line option
     *
     * @param option Command-line option
     */
    public void add(CmdLineOption option) {
        this.map.put(option.getName(), option);
        this.list.add(option);

        // Put reference by alternate name too
        if (option.getAltName() != null) {
            this.map.put(option.getAltName(), option);
        }
    }

    /**
     * Overloaded method to add the command-line option
     *
     * @param name     Name of the option
     * @param altName  Alternate name of the option
     * @param type     Type of the option
     * @param takesArg Flag to indicate whether the option takes argument or not
     */
    public void add(String name, String altName, int type, boolean takesArg) {
        add(new CmdLineOption(name, altName, type, takesArg));
    }

    /**
     * Method to get the standard option data by name
     *
     * @param name Name of the option
     * @return Command-line option
     */
    public CmdLineOption getOption(String name) {
        return this.map.get(name);
    }

    /**
     * Method to get the standard option data by index
     *
     * @param index Index of the option
     * @return Command-line option
     */
    public CmdLineOption getOption(int index) {
        return this.list.get(index);
    }

    /**
     * Method to get the value of the given option as String
     *
     * @param name Name of the option
     * @return Value of the command-line option
     */
    public String getStringValue(String name) {
        CmdLineOption option = getOption(name);
        return (option == null) ? null : option.getValue().trim();
    }

    /**
     * Method to get the value of the given option as integer
     *
     * @param name         Name of the option
     * @param errNamespace Namespace to be used for exception in case of parsing error
     * @param errCode      Error code to be used for exception in case of parsing error
     * @return null if options is not present on command line, else integer value
     * @throws OpenStegoException If value is provided but not an integer
     */
    public Integer getIntegerValue(String name, String errNamespace, int errCode) throws OpenStegoException {
        String val = getStringValue(name);
        if (val == null) {
            return null;
        }

        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException nfEx) {
            throw new OpenStegoException(nfEx, errNamespace, errCode, val);
        }
    }

    /**
     * Method to get the value of the given option as boolean. All boolean-y values like true, false, t, f, yes, no,
     * y, n, 1, 0 are allowed (case insensitive)
     *
     * @param name         Name of the option
     * @param errNamespace Namespace to be used for exception in case of parsing error
     * @param errCode      Error code to be used for exception in case of parsing error
     * @return null if options is not present on command line, else boolean value
     * @throws OpenStegoException If value is provided but not boolean-y
     */
    @SuppressWarnings("unused")
    public Boolean getBooleanValue(String name, String errNamespace, int errCode) throws OpenStegoException {
        String val = getStringValue(name);
        if (val == null) {
            return null;
        }

        val = val.toLowerCase();
        if ("t".equals(val) || "true".equals(val) || "y".equals(val) || "yes".equals(val) || "1".equals(val)) {
            return true;
        } else if ("f".equals(val) || "false".equals(val) || "n".equals(val) || "no".equals(val) || "0".equals(val)) {
            return false;
        } else {
            throw new OpenStegoException(null, errNamespace, errCode, val);
        }
    }

    /**
     * Method to get the list of the given options
     *
     * @return List of options
     */
    public List<CmdLineOption> getList() {
        return this.list;
    }

    /**
     * Method to get the number of the given options
     *
     * @return Number of options
     */
    public int size() {
        return this.list.size();
    }
}
