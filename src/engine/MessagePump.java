package engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The message pump is responsible for collecting messages
 * passed around the application during a given frame. At some point
 * it can be asked to dispatch the messages (events) to any system
 * or other object which has expressed interest.
 *
 * This system effectively removes the need to pass around references
 * to objects which manage a certain piece of functionality. Instead the
 * message pump collects all relevant references and dispatches the messages
 * to whoever has signaled interest.
 *
 * As an added benefit, since you are not passing around references to
 * everyone that needs the functionality the underlying objects provide,
 * you can move functionality around behind the scenes without having
 * to change any of the code which sends the messages and signals interest
 * in those messages.
 *
 * @author Justin Hall
 */
public class MessagePump {
    private final ConcurrentHashMap<String, Message> _registeredMessages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Message, LinkedList<MessageHandler>> _registeredHandlers = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Message> _messageBuffer = new ConcurrentLinkedQueue<>();

    /**
     * Gets rid of all registered message handlers, meaning no references will
     * be kept to them
     */
    public void clearAllMessageHandlers()
    {
        _registeredHandlers.clear();
    }

    /**
     * Tells the message pump that you are interested in receiving event
     * notifications for the given message
     * @param message message to receive event notifications for
     * @param handler callback
     */
    public void signalInterest(String message, MessageHandler handler)
    {
        LinkedList<MessageHandler> handlers = _registeredHandlers.get(getRegisteredMessage(message));
        if (handlers == null) {
            throw new IllegalArgumentException("Non-registered message passed into MessagePump.signalInterest");
        }
        handlers.add(handler);
    }

    /**
     * Tells the message pump that the given message should be cached and it
     * should expect messages of its type fo be written in the future.
     *
     * @param message message to register
     */
    public void registerMessage(Message message)
    {
        // Only add it if it has not been added yet
        System.out.println("Registering message type (" + message.getMessageName() + ")");
        _registeredMessages.putIfAbsent(message.getMessageName(), message);
        _registeredHandlers.putIfAbsent(message, new LinkedList<>());
    }

    /**
     * Removes a message from the message pump
     */
    public void unregisterMessage(Message message)
    {
        _registeredMessages.remove(message.getMessageName());
        _registeredHandlers.remove(message);
    }

    /**
     * Converts a String to a registered Message object
     */
    public Message getRegisteredMessage(String message)
    {
        return _registeredMessages.get(message);
    }

    /**
     * Retrieves a list of all messages currently registered by the MessagePump.
     * @return list of messages
     */
    public LinkedList<Message> getAllRegisteredMessages()
    {
        LinkedList<Message> result = new LinkedList<>();
        for (Map.Entry<String, Message> entry : _registeredMessages.entrySet())
        {
            result.add(entry.getValue());
        }
        return result;
    }

    /**
     * Checks to see if the given message (as string) has been registered
     * @return true if registered and false if not
     */
    public boolean contains(String message)
    {
        return _registeredMessages.containsKey(message);
    }

    /**
     * Equivalent to contains(String), but it allows you to pass in
     * the actual Message object.
     * @return true if registered and false if not
     */
    public boolean contains(Message message)
    {
        return contains(message.getMessageName());
    }

    /**
     * @return integer corresponding to the total number of registered messages
     */
    public int size()
    {
        return _registeredMessages.size();
    }

    /**
     * Notifies the system that you want to send a message to anyone interested
     * in receiving and processing it.
     * @param message message to send
     */
    public void sendMessage(Message message)
    {
        //System.out.println("Sending message: " + message.getMessageName());
        if (!_registeredMessages.containsKey(message.getMessageName()))
        {
            throw new IllegalArgumentException("Non-registered message passed into MessagePump");
        }
        _messageBuffer.add(message);
    }

    /**
     * Allows you to send a message without having a hard reference to the Message object
     * you want to send. Instead the message pump will look it up for you.
     * @param message message to send
     */
    public void sendMessage(String message)
    {
        sendMessage(getRegisteredMessage(message));
    }

    /**
     * If you are not the simulation.engine then it is best not to call this
     */
    void dispatchMessages()
    {
        LinkedList<Message> buffer = new LinkedList<>();
        synchronized(this) {
            int numMessages = _messageBuffer.size(); // Take a snapshot of the size
            for (int i = 0; i < numMessages; ++i) {
                Message msg = _messageBuffer.poll();
                buffer.add(msg);
            }
        }
        for (Message msg : buffer)
        {
            LinkedList<MessageHandler> interested = _registeredHandlers.get(msg);
            for (MessageHandler handler : interested)
            {
                handler.handleMessage(msg);
            }
        }
    }
}
