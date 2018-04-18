package engine;

/**
 * Executes a single task. This is not meant to run until it
 * is called by one of the task manager's internal threads.
 */
public interface Task {
    /**
     * Runs the code associated with the task. Such code should
     * never contain an infinite loop.
     */
    void execute();
}
