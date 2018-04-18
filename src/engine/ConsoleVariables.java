package engine;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * These are essentially global variables accessible to the
 * entire engine/application. They can be in part default-initialized
 * via code, part overwritten by command line arguments, and part overwritten
 * by values read in from a file.
 *
 * This class also provides a function called loadConfigFile() which can be
 * used to load a config file which is separate from the one that the engine
 * loads. This is useful in the case where you wish to create your own
 * default console variables to be used by your own code.
 *
 * @author Justin Hall
 */
public class ConsoleVariables {
    private ConcurrentHashMap<String, ConsoleVariable> _cvars = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ConsoleVariable, Integer> _cvarEditCounts = new ConcurrentHashMap<>(); // Keeps track of how many times the cvars were edited

    /**
     * @return a list of references to all console variables currently managed by this object
     */
    public LinkedList<ConsoleVariable> getAllConsoleVariables()
    {
        LinkedList<ConsoleVariable> cvars = new LinkedList<>();
        for (Map.Entry<String, ConsoleVariable> entry : _cvars.entrySet())
        {
            cvars.add(entry.getValue());
        }
        return cvars;
    }

    /**
     * Prints all console variables
     */
    public void printAllConsoleVariables()
    {
        System.out.println("---Console Variable Listing---");
        for (Map.Entry<String, ConsoleVariable> entry : _cvars.entrySet())
        {
            System.out.println(entry.getValue());
        }
    }

    /**
     * Registers a console variable with the cvar system
     */
    public void registerVariable(ConsoleVariable cvar)
    {
        // If it already exists then do nothing except potentially
        // override the default value if it is different
        if (contains(cvar.getcvarName()))
        {
            find(cvar.getcvarName()).setDefault(cvar.getcvarDefault());
        }
        else
        {
            System.out.println("Registering console variable (" + cvar + ")");
            _cvars.put(cvar.getcvarName(), cvar);
            _cvarEditCounts.put(cvar, cvar.getEditCount());
        }
    }

    /**
     * Determines which variables have been changed since the last time this
     * method was called
     * @return list containing all variables that were changed
     */
    public ArrayList<ConsoleVariable> getVariableChangesSinceLastCall()
    {
        ArrayList<ConsoleVariable> editedCvars = new ArrayList<>();
        editedCvars.clear();
        for (Map.Entry<String, ConsoleVariable> entry : _cvars.entrySet())
        {
            ConsoleVariable cvar = entry.getValue();
            if (_cvarEditCounts.get(cvar) != cvar.getEditCount())
            {
                editedCvars.add(cvar);
                _cvarEditCounts.put(cvar, cvar.getEditCount());
            }
        }
        return editedCvars;
    }

    /**
     * Unregisters a variable from the console variable table
     * @param cvar cvar to remove
     */
    public void unregisterVariable(String cvar)
    {
        _cvars.remove(cvar);
    }

    /**
     * Checks if the given variable has been registered
     * @return true if registered and false if not
     */
    public boolean contains(String cvar)
    {
        return _cvars.containsKey(cvar);
    }

    /**
     * Warning! This can return null!
     */
    public ConsoleVariable find(String cvar)
    {
        if (contains(cvar)) return _cvars.get(cvar);
        return null;
    }

    /**
     * Removes all console variables
     */
    public void clear()
    {
        _cvars.clear();
        _cvarEditCounts.clear();
    }

    /**
     * Loads a key-value config file and registers each one as
     * a console variable that can be retrieved later.
     * @param configFile path to the config file to load
     */
    public void loadConfigFile(String configFile)
    {
        System.out.println("Reading " + configFile);
        try
        {
            FileReader fileReader = new FileReader(configFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.replaceAll(" ", "");
                String variable = "";
                String value = "";
                boolean isReadingValue = false;
                for (int i = 0; i < line.length(); ++i)
                {
                    char c = line.charAt(i);
                    if (c == '+') continue;
                    if (c == '/' && (i + 1) < line.length() && line.charAt(i + 1) == '/') break; // Found a comment
                    if (c == '=')
                    {
                        isReadingValue = true;
                        continue;
                    }
                    if (isReadingValue) value += c;
                    else variable += c;
                }
                if (variable.equals("")) continue;
                if (contains(variable)) find(variable).setValue(value);
                else registerVariable(new ConsoleVariable(variable, value));
            }
        }
        catch (Exception e)
        {
            System.err.println("WARNING: Unable to load " + configFile);
            //System.exit(-1);
        }
    }
}
