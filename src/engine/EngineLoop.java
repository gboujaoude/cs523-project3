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
    private static volatile boolean _waitingForEngineInit;
    private static volatile boolean _isRunning = false;
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
            if (_isRunning) return; // Already called
            _isRunning = true;
            _waitingForEngineInit = true;
        }
        finally {
            _lock.unlock();
        }
        Engine engine = new Engine();
        JFXPanel panel = new JFXPanel(); // Forces JavaFX to initialize itself
        Platform.runLater(() ->
            {
            engine.start(application);
            _waitingForEngineInit = false;
            });
        // Spin while we're waiting for engine init
        while (_waitingForEngineInit) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                // Do nothing
            }
        }
        // Now spin while the engine is running
        while (engine._isEngineRunning()) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                // Do nothing
            }
        }
        _isRunning = false;
    }

    // Make the constructor private since no one should be making an instance of this class
    private EngineLoop() { }
}
