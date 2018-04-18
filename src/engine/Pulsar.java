package engine;

/**
 * A Pulsar represents a timer which is capable of notifying
 * you once a given time period has elapsed. For example, if
 * you have said that you are interested in 30 second intervals,
 * the callback you register with the Pulsar will be called
 * every 30 seconds.
 *
 * If the engine is paused then the Pulsar will not update.
 *
 * @author Justin Hall
 */
public class Pulsar implements PulseEntity {
    private double _intervalSeconds;
    private double _elapsedSeconds = 0.0;
    private Callback _callback;
    private boolean _started = false;

    /**
     * NOTE :: By default the pulsar will not start itself - see "start()"
     *
     * Creates a new Pulsar with an immutable interval
     * @param intervalSeconds how much time should pass before the Pulsar goes off
     * @param callback Object whose handle will be called every time the time
     *                 interval has elapsed
     */
    public Pulsar(double intervalSeconds, Callback callback)
    {
        _intervalSeconds = intervalSeconds;
        _callback = callback;
        // If they give us a null callback then stop the world
        if (callback == null)
        {
            throw new IllegalArgumentException("Null callback passed to Pulsar");
        }
    }

    /**
     * Starts the Pulsar - calling this twice will not cause any issues
     */
    public void start()
    {
        if (_started) return; // Don't start twice in a row
        _started = true;
        Engine.getMessagePump().sendMessage(new Message(Singleton.ADD_PULSE_ENTITY, this));
    }

    /**
     * Stops the Pulsar, meaning the callback will no longer be invoked
     */
    public void stop()
    {
        if (!_started) return; // Don't stop if we haven't started
        _started = false;
        Engine.getMessagePump().sendMessage(new Message(Singleton.REMOVE_PULSE_ENTITY, this));
    }

    // Updates the pulsar
    @Override
    public void pulse(double deltaSeconds) {
        _elapsedSeconds += deltaSeconds;
        if (_elapsedSeconds >= _intervalSeconds)
        {
            _elapsedSeconds = 0.0; // Reset the elapsed time
            _callback.handleCallback();
        }
    }
}
