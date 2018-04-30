package engine;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Provides a way to start the engine loop.
 */
public class EngineLoop {
    private static volatile boolean _isRunning = false;
    private static volatile boolean _primedToRun = true;
    private static final ReentrantLock _lock = new ReentrantLock();

    /**
     * Starts the engine's main loop - this will not return until the engine
     * has shutdown.
     * @param application application to associate with the current instance of the engine
     * @param cmdArgs command line arguments
     */
    public static void start(ApplicationEntryPoint application, String[] cmdArgs) {
        try {
            // Acquire the lock
            _lock.lock();
            if (_isRunning || !_primedToRun) return; // Already running or already told to exit
            _isRunning = true;
        }
        finally {
            _lock.unlock();
        }
        Engine engine = new Engine();
        try {
            engine.start(application);
        }
        catch (Exception e) {
            e.printStackTrace();
            _isRunning = false;
            return;
        }
        // Now spin while the engine is running
        while (engine.isEngineRunning()) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                // Do nothing
            }
        }
        // Wait for the engine to shut down
        while (engine.isShuttingDown()) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                // Do nothing
            }
        }
        _isRunning = false;
    }

    /**
     * NOTE: Calling this while the engine loop is running will result in a no-op
     * WARNING: Forgetting to call this could lead the application to hang when main() exits, requiring a force-kill
     *
     * This will tag any remaining threads for close so that the application can
     * completely shut down. This is a final operation, meaning that the engine
     * will be unusable after calling this without restarting the whole application.
     *
     * @return true if the exit operation succeeded and false if it failed for any reason
     */
    public static boolean exit() {
        try {
            _lock.lock();
            if (_isRunning) return false; // Engine loop currently running
            else if (!_primedToRun) return true; // Already exited
            _primedToRun = false;
            Platform.exit(); // Kill JFX thread manually
        }
        finally {
            _lock.unlock();
        }
        return true;
    }

    // Make the constructor private since no one should be making an instance of this class
    private EngineLoop() { }
}
