package engine;

/**
 * This interface should be implemented by any class that wants
 * to participate in receiving and dealing with messages being
 * passed around the simulation.engine.
 *
 * @author Justin Hall
 */
public interface MessageHandler {
    /**
     * This will be called once for every message that this
     * object has signalled interest in. For example, if you
     * are interested in "ENTER_KEY_DOWN" and "LEFT_MOUSE_DOWN",
     * this could be registered with the MessagePump system. If
     * either (or both) of these fire during a given frame, the
     * generated messages will be relayed to this method.
     *
     * @param message message which was generated because of a specific event
     */
    void handleMessage(Message message);
}
