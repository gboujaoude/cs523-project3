package engine;

/**
 * A pulse entity does not need to represent something within
 * the game environment. All a pulse entity means is that it
 * needs to update (pulse) as frequently as the simulation.engine can
 * allow it to update. So, if the simulation.engine is running at 60 frames
 * per second, each registered pulse entity should pulse 60 times
 * per second.
 *
 * @author Justin Hall
 */
public interface PulseEntity {
    /**
     * Called each time the simulation.engine updates.
     * @param deltaSeconds Change in seconds since the last update.
     *                     If the simulation.engine is running at 60 frames per second,
     *                     this value will be roughly equal to (1/60).
     */
    void pulse(double deltaSeconds);
}
