/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util.cmd;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to parse the command line parameters
 */
public class CmdLineParser {
    /**
     * Stores the list of parsed options
     */
    private final CmdLineOptions parsedOptions = new CmdLineOptions();

    /**
     * Stores the list of non-standard arguments
     */
    private final List<String> nonStdArgList = new ArrayList<>();

    /**
     * Flag to indicate whether non-standard options are mixed within standard options or not
     */
    private boolean nonStdMixedWithStdOptions = false;

    /**
     * Default constructor
     *
     * @param stdOptions List of standard options
     * @param args       Command line arguments
     */
    public CmdLineParser(CmdLineOptions stdOptions, String[] args) {
        int i = 0;
        int index;
        CmdLineOption option;
        String arg;
        String value;

        while (i < args.length) {
            arg = args[i];
            value = null;
            index = arg.indexOf("=");

            // If arg is of the form "name=value", split it
            if (index >= 0) {
                value = arg.substring(index + 1);
                arg = arg.substring(0, index);
            }

            // Check that arg exists in standard list of options
            option = stdOptions.getOption(arg);

            if (option == null) // Non-standard option
            {
                this.nonStdArgList.add(arg);
            } else
            // Standard option
            {
                // If non-standard option is already parsed then standard option should not be provided now
                if (!this.nonStdMixedWithStdOptions && this.nonStdArgList.size() > 0) {
                    this.nonStdMixedWithStdOptions = true;
                }

                if (option.takesArg()) {
                    // If value was already provided (in the form of "name=value" pair), use it
                    if (value != null) {
                        option.setValue(value);
                    } else {
                        // Get the next argument from command line (for value of the option)
                        i++;
                        if (i < args.length) {
                            arg = args[i];
                        }

                        // Check that next arg is not an option itself
                        if (stdOptions.getOption(arg) == null) {
                            option.setValue(arg);
                        }
                    }
                }
                this.parsedOptions.add(option);
            }
            i++;
        }
    }

    /**
     * Method to check whether the command-line options are valid or not. This should be called immediately
     * after the constructor is called.
     *
     * @return Flag to indicate whether options are valid or not
     */
    public boolean isValid() {
        List<CmdLineOption> list;
        CmdLineOption option;

        if (this.nonStdMixedWithStdOptions) {
            return false;
        }

        list = this.parsedOptions.getList();
        for (CmdLineOption cmdLineOption : list) {
            option = cmdLineOption;
            if (option.takesArg() && option.getValue() == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Method to get the value of the given option (by name)
     *
     * @param name Name of the option
     * @return Value of the command-line option
     */
    @SuppressWarnings("unused")
    public String getOptionValue(String name) {
        return this.parsedOptions.getStringValue(name);
    }

    /**
     * Method to get the name of the option by index
     *
     * @param index Index of the option
     * @return Name of the command-line option
     */
    @SuppressWarnings("unused")
    public String getOptionName(int index) {
        return this.parsedOptions.getOption(index).getName();
    }

    /**
     * Method to get the list of standard options
     *
     * @return List of standard options
     */
    public List<CmdLineOption> getParsedOptionsAsList() {
        return this.parsedOptions.getList();
    }

    /**
     * Method to get the list of non-standard options
     *
     * @return List of non-standard options
     */
    public List<String> getNonStdOptions() {
        return this.nonStdArgList;
    }

    /**
     * Get method for parsedOptions
     *
     * @return parsedOptions
     */
    public CmdLineOptions getParsedOptions() {
        return this.parsedOptions;
    }

    /**
     * Method to get the total number of options (standard plus non-standard) provided in the command-line
     *
     * @return Total number of options provided in the command-line
     */
    @SuppressWarnings("unused")
    public int getNumOfOptions() {
        return this.parsedOptions.size() + this.nonStdArgList.size();
    }
}
