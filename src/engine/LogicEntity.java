package engine;

/**
 * A logic entity is meant to run on threads dedicated to performing
 * computations/handling logic. They are specifically not allowed to do
 * anything related to rendering images/creating or dealing with GUI elements.
 *
 * The main guarantee that a LogicEntity gives you over a Task is that a LogicEntity's
 * process() method will be called continuously until you tell the engine to stop.
 *
 * @author Justin Hall
 */
public interface LogicEntity {
    /**
     * Performs the logic/computation associated with the logic entity. Just like a task,
     * there should never be an infinite loop inside of this method.
     *
     * @param deltaSeconds time elapsed since the last time this method was called
     */
    void process(double deltaSeconds);
}
