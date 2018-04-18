package engine;

/**
 * This represents the generic idea of a callback function. It
 * contains only one method that should be called whenever some
 * condition is met requiring it to be called.
 *
 * @author Justin Hall
 */
public interface Callback {
    void handleCallback();
}
