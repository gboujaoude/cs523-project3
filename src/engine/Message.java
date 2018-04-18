package engine;

/**
 * A message represents anything within the simulation.engine that
 * is of interest to any of its objects/systems. For example,
 * if we are particularly interested in when the enter key
 * is pressed down, we might register a "K_DOWN" message with
 * the MessagePump system.
 *
 * @author Justin Hall
 */
public class Message {
    private String _msgName;
    private Object _msgData = null;

    public Message(String msgName)
    {
        _msgName = msgName;
    }

    public Message(String msgName, Object msgData)
    {
        _msgName = msgName;
        _msgData = msgData;
    }

    /**
     * Basic copy constructor - copies the values of message
     * @param message existing Message to copy from
     */
    public Message(Message message)
    {
        _msgName = message._msgName;
        _msgData = message._msgData;
    }

    /**
     * Advanced copy constructor - pulls the name name from message and uses
     * the given "msgData" as the message's data
     * @param message existing Message to pull the name from
     * @param msgData object to set as the Message data
     */
    public Message (Message message, Object msgData)
    {
        _msgName = message.getMessageName();
        _msgData = msgData;
    }

    public final String getMessageName()
    {
        return _msgName;
    }

    /**
     * Determines whether the message contains associated data
     * or not. If false, it is expected that getMessageData() would
     * return null.
     *
     * @return true if there is valid data associated with this message and false if not
     */
    public boolean containsData()
    {
        return _msgData != null;
    }

    /**
     * Warning! This might be null if the message does not
     * need to include data!
     *
     * The idea with this function is that some messages will
     * want to include specialized data along with them. For example,
     * if one message is "TIMER_EXPIRED", the data may be a link to
     * the timer which has recently completed.
     *
     * To retrieve the actual object, simply cast it.
     *
     * @return data associated with the message (potentially null)
     */
    public Object getMessageData()
    {
        return _msgData;
    }

    @Override
    public int hashCode() {
        return _msgName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Message)) return false;
        return ((Message)obj)._msgName.equals(_msgName);
    }

    @Override
    public String toString() {
        return _msgName;
    }
}
