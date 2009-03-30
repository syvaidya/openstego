/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class to store the list of command line options
 * @see CmdLineParser
 */
public class CmdLineOptions
{
    /**
     * Map to store the standard command-line options as map
     */
    private HashMap map = new HashMap();

    /**
     * Map to store the standard command-line options as list
     */
    private ArrayList list = new ArrayList();

    /**
     * Default constructor
     */
    public CmdLineOptions()
    {
    }

    /**
     * Method to add the command-line option
     * @param option Command-line option
     */
    public void add(CmdLineOption option)
    {
        map.put(option.getName(), option);
        list.add(option);

        // Put reference by alternate name too
        if(option.getAltName() != null)
        {
            map.put(option.getAltName(), option);
        }
    }

    /**
     * Overloaded method to add the command-line option
     * @param name Name of the option
     * @param altName Alternate name of the option
     * @param type Type of the option
     * @param takesArg Flag to indicate whether the option takes argument or not
     */
    public void add(String name, String altName, int type, boolean takesArg)
    {
        add(new CmdLineOption(name, altName, type, takesArg));
    }

    /**
     * Method to get the standard option data by name
     * @param name Name of the option
     * @return Command-line option
     */
    public CmdLineOption getOption(String name)
    {
        return (CmdLineOption) map.get(name);
    }

    /**
     * Method to get the standard option data by index
     * @param index Index of the option
     * @return Command-line option
     */
    public CmdLineOption getOption(int index)
    {
        return (CmdLineOption) list.get(index);
    }

    /**
     * Method to get the value of the given option
     * @param name Name of the option
     * @return Value of the command-line option
     */
    public String getOptionValue(String name)
    {
        CmdLineOption option = getOption(name);
        if(option != null)
        {
            return option.getValue();
        }
        else
        {
            return null;
        }
    }

    /**
     * Method to get the list of the given options
     * @return List of options
     */
    public List getList()
    {
        return list;
    }

    /**
     * Method to get the number of the given options
     * @return Number of options
     */
    public int size()
    {
        return list.size();
    }
}
