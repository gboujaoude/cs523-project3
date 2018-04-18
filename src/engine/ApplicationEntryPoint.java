package engine;

/**
 * The application entry point is the only means of writing code
 * to run with the engine. You only need to implement init() and shutdown(),
 * and any additional functionality (such as creating a PulseEntity for continuous
 * update) is optional.
 */
public interface ApplicationEntryPoint {
    void init();

    void shutdown();
}
