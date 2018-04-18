package engine;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents an individual key-value console variable and
 * corresponding helper functions. A console variable
 * is a very simple means of storing some piece of data
 * which is meant to be modified from a variety of different
 * input streams. Such input streams include:
 *
 *      Command Line
 *      External Config Files
 *      Other Objects
 *
 * On top of storing the data as a raw string, a console variable
 * will also attempt to cast the string to an integer, double, and
 * boolean. You can then retrieve easily retrieve the pre-casted
 * value via a couple of helper functions.
 *
 * If it fails to cast the data, this is not an issue. It will simply
 * maintain the data as a raw string for later use/manual casting.
 *
 * @author Justin Hall
 */
public class ConsoleVariable {
    private AtomicReference<String> _cvarName;
    private AtomicReference<String> _defaultValue;
    private AtomicReference<String> _cvarValue; // Raw String value
    private AtomicInteger _cvarIntVal; // Defaults to -1 if _cvarValue cannot be casted
    private AtomicReference<Double> _cvarFloatVal; // Defaults to -1.0 if _cvarValue cannot be casted
    private AtomicBoolean _cvarBoolVal; // Defaults to false
    private AtomicInteger _numEdits; // Number of times this variable was edited

    {
        _cvarName = new AtomicReference<>("");
        _defaultValue = new AtomicReference<>("");
        _cvarValue = new AtomicReference<>("");
        _cvarIntVal = new AtomicInteger();
        _cvarBoolVal = new AtomicBoolean(false);
        _cvarFloatVal = new AtomicReference<>();
        _numEdits = new AtomicInteger(0);
    }

    /**
     * Creates a new console variable. Keep in mind that the actual value of the
     * console variable will be determined by the default value given here.
     * @param name name of the variable
     * @param defaultValue default value in the form of a string (used during console variable reset)
     */
    public ConsoleVariable(String name, String defaultValue)
    {
        _cvarName.set(name);
        //Singleton.simulation.engine.getMessagePump().registerMessage(new Message(_cvarName + "_WAS_CHANGED"));
        _defaultValue.set(defaultValue);
        _setValueNoMessageDispatch(defaultValue);
    }

    /**
     * Creates a new, fully-specified console variable
     * @param name name of the console variable
     * @param defaultValue default value in the form of a string (used during console variable reset)
     * @param value starting value of the console variable which is separate from the default value
     */
    public ConsoleVariable(String name, String defaultValue, String value)
    {
        _cvarName.set(name);
        _defaultValue.set(defaultValue);
        _setValueNoMessageDispatch(value);
    }

    /**
     * Resets the console variable to its default value
     */
    public void reset()
    {
        setValue(_defaultValue.get());
    }

    /**
     * @return total number of times this console variable has been edited
     */
    public int getEditCount()
    {
        return _numEdits.get();
    }

    /**
     * Gets the cvar name
     */
    public String getcvarName()
    {
        return _cvarName.get();
    }

    /**
     * Gets the raw cvar value as a string (Ex: "127.26")
     */
    public String getcvarValue()
    {
        return _cvarValue.get();
    }

    public String getcvarDefault()
    {
        return _defaultValue.get();
    }

    /**
     * Gets cvar value as int (Ex: 127)
     */
    public int getcvarAsInt()
    {
        return _cvarIntVal.get();
    }

    /**
     * Gets cvar value as double (Ex: 127.26)
     * @return
     */
    public double getcvarAsFloat()
    {
        return _cvarFloatVal.get();
    }

    public boolean getcvarAsBool()
    {
        return _cvarBoolVal.get();
    }

    /**
     * Sets the value of the console variable (Ex: "127")
     */
    public void setValue(String value)
    {
        _setValueNoMessageDispatch(value);
        // Notify anyone who is interested that this variable was changed
        //Singleton.simulation.engine.getMessagePump().sendMessage(_cvarName + "_WAS_CHANGED");
    }

    /**
     * This determines what the cvar resets to if reset() is called
     */
    public void setDefault(String defaultValue)
    {
        _defaultValue.set(defaultValue);
    }

    private void _setValueNoMessageDispatch(String value)
    {
        _numEdits.getAndIncrement();
        _cvarValue.set(value);
        try
        {
            _cvarIntVal.set(Integer.parseInt(value));
            _cvarFloatVal.set(Double.parseDouble(value));
        }
        catch (Exception e)
        {
            _cvarIntVal.set(-1);
            _cvarFloatVal.set(-1.0);
        }
        // Try to cast it to a boolean
        try
        {
            _cvarBoolVal.set(Boolean.parseBoolean(value));
        }
        catch (Exception e)
        {
            _cvarBoolVal.set(false);
        }
    }

    @Override
    public int hashCode() {
        return _cvarName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConsoleVariable && ((ConsoleVariable)obj)._cvarName.equals(_cvarName);
    }

    @Override
    public String toString() {
        return "name: " + _cvarName + "; value: " + _cvarValue;
    }
}
