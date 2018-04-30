package engine;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The engine is a singleton class as there should never be more than
 * one instance during an application. It is responsible for the startup
 * and shutdown of all subsystems which comprise the application, and from
 * there it drives the system in real time (30-60+ updates per second).
 *
 * Notable functions include:
 *      getMessagePump()
 *      getConsoleVariables()
 *
 * A message pump is used to connect the various parts of the application without
 * having to pass hard references to everyone that needs them. Instead, messages
 * are registered and sent and those who are interested will signal interest in
 * them.
 *
 * On the other hand, console variables provide a way to store global state. This
 * state is made up of a variety of input sources which can include the command
 * line, a config file, and input from various objects during initialization. A
 * combination of all of these is well-supported and even expected.
 *
 * Be aware that this class is meant to be the central point of startup for
 * the process, and as such it has implemented a main method.
 *
 * @author Justin Hall
 */
public class Engine implements PulseEntity, MessageHandler {
    private static volatile Engine _engine; // Self-reference
    private static volatile boolean _isInitialized = false;
    // Package private
    static final String R_RENDER_SCENE = "r_render_screen";
    static final String R_UPDATE_ENTITIES = "r_update_entities";

    private Stage _initialStage;
    private HashSet<PulseEntity> _pulseEntities;
    private ApplicationEntryPoint _application;
    private AtomicReference<MessagePump> _messageSystem = new AtomicReference<>();
    private AtomicReference<ConsoleVariables> _cvarSystem = new AtomicReference<>();
    private AtomicReference<TaskManager> _taskManager = new AtomicReference<>();
    private Window _window;
    private Renderer _renderer;
    private ConcurrentHashMap<TaskManager.Counter, Callback> _taskCallbackMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<LogicEntity, LogicEntityTask> _registeredLogicEntities = new ConcurrentHashMap<>();
    private Filesystem _fileSys;
    private volatile int _maxFrameRate;
    private final int _maxMessageQueueProcessingRate = 240; // Measures in Hertz, i.e. times per second
    private double _timeScalingFactor = 1.0;
    private volatile long _lastMessageQueueFrameTimeMS;
    private volatile long _lastFrameTimeMS;
    private volatile boolean _isRunning = false;
    private volatile boolean _updateEntities = true; // If false, nothing is allowed to move
    private volatile boolean _requiresRestart = false;
    private volatile boolean _headless = false;
    private volatile boolean _initializing = false;
    private volatile boolean _pendingShutdown = false;
    private Runnable _gameLoop;

    // Wrapper around each logic entity
    private class LogicEntityTask implements Task {
        private LogicEntity _entity;
        private Engine _engine;
        private long _startTimeNSec;

        LogicEntityTask(LogicEntity entity, Engine engine) {
            _entity = entity;
            _engine = engine;
            _startTimeNSec = System.nanoTime();
        }

        LogicEntity getLogicEntity() {
            return _entity;
        }

        @Override
        public void execute() {
            long currTimeNSec = System.nanoTime();
            long elapsedNSec = currTimeNSec - _startTimeNSec;
            double deltaSeconds = elapsedNSec / 1000000000.0;
            _startTimeNSec = currTimeNSec;
            try {
                _entity.process(deltaSeconds * _timeScalingFactor);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            _engine._notifyOfLogicTaskCompletion(this);
        }
    }

    // Make this package private so that only certain classes can create/initialize the
    // engine
    Engine()
    {
    }

    /**
     * Warning! Do not call the MessagePump's dispatch method!
     *
     * This allows other systems to pass messages/register messages/
     * signal interest in message.
     * @return MessagePump for modification
     */
    public static MessagePump getMessagePump()
    {
        return _engine._messageSystem.get();
    }

    /**
     * Returns the console variable listing for viewing/modification
     */
    public static ConsoleVariables getConsoleVariables()
    {
        return _engine._cvarSystem.get();
    }

    /**
     * Returns the engine's file system for opening and manipulating files.
     */
    public static Filesystem getFileSystem() {
        return _engine._fileSys;
    }

    /**
     * WARNING: Do not interface with JavaFX from a task on a logic thread. This is almost
     * guaranteed to cause JavaFX to throw an exception.
     *
     * Specifies a list of tasks to run on the engine's logic threads (strictly logic - no graphics!)
     * @param tasks list of functions to run later
     * @param callback this can be null - function to call when the given task has successfully completed
     *                 (this will always be called on the main application thread to avoid synchronization issues)
     */
    public static void scheduleLogicTasks(Callback callback, Task ... tasks) {
        TaskManager.Counter counter = _engine._taskManager.get().submitTasks(tasks);
        if (callback != null) {
            _engine._taskCallbackMap.put(counter, callback);
        }
    }

    public void start(ApplicationEntryPoint application) {
        synchronized(this) {
            if (_isRunning) return; // Already running
            // Create the game loop
            _gameLoop = new Runnable() {
                @Override
                public void run() {
                    if (!_isRunning) return;
                    else if (_pendingShutdown) { // Need to shut the system down
                        shutdown();
                        return;
                    }
                    try {
                        if (_initializing) return; // Engine is not ready to run
                        long currentTimeMS = System.currentTimeMillis();
                        double deltaSeconds = (currentTimeMS - _lastFrameTimeMS) / 1000.0;
                        _timeScalingFactor = Engine.getConsoleVariables().find(Constants.TIME_SCALING_FACTOR).getcvarAsFloat();
                        // Don't pulse faster than the maximum refresh rate
                        if (deltaSeconds >= (1.0 / _maxFrameRate)) {
                            pulse(deltaSeconds * _timeScalingFactor);
                            _lastFrameTimeMS = currentTimeMS;
                        }
                        // Message processing happens at a very fast rate, i.e. 240 times per second
                        // to ensure high degree of responsiveness
                        deltaSeconds = (currentTimeMS - _lastMessageQueueFrameTimeMS) / 1000.0;
                        if (deltaSeconds >= (1.0 / _maxMessageQueueProcessingRate)) {
                            _processMessages();
                            _processCompletedTasks();
                            _lastMessageQueueFrameTimeMS = currentTimeMS;
                        }
                    }
                    finally {
                        if (_requiresRestart) {
                            _softRestart();
                            _requiresRestart = false;
                        }
                        // Kick off the next frame if the engine is not restarting
                        else _dispatchEngineLogic(_gameLoop);
                    }
                }
            };
            // Initialize the engine
            _application = application;
            _preInit();
            _init();
        }
    }

    private void _dispatchEngineLogic(Runnable runnable) {
        if (_headless) scheduleLogicTasks(null, runnable::run);
        else Platform.runLater(runnable);
    }

    private void _processMessages() {
        // Check if any console variables changed and send messages for any that have
        ArrayList<ConsoleVariable> changedVars = getConsoleVariables().getVariableChangesSinceLastCall();
        for (ConsoleVariable cvar : changedVars)
        {
            _messageSystem.get().sendMessage(new Message(Constants.CONSOLE_VARIABLE_CHANGED, cvar));
        }
        // Make sure we keep the messages flowing
        getMessagePump().dispatchMessages();
    }

    private void _processCompletedTasks() {
        // See if any tasks have finished on the logic threads and notify the caller if so
        LinkedList<TaskManager.Counter> _completedCounters = new LinkedList<>();
        // numCounters takes a snapshot of the task callback map so that we are guaranteed to
        // only process a finite number of them during a given frame
        int numCounters = _taskCallbackMap.size();
        for (Map.Entry<TaskManager.Counter, Callback> entry : _taskCallbackMap.entrySet()) {
            if (numCounters == 0) break;
            if (entry.getKey().isComplete()) {
                // Notify of task completion
                entry.getValue().handleCallback();
                _completedCounters.add(entry.getKey());
            }
            --numCounters;
        }
        // Remove any completed counters
        for (TaskManager.Counter counter : _completedCounters) {
            _taskCallbackMap.remove(counter);
        }
    }

    /**
     * Represents the main game/simulation loop
     */
    @Override
    public void pulse(double deltaSeconds) {
        if (_updateEntities) getMessagePump().sendMessage(new Message(Engine.R_UPDATE_ENTITIES, deltaSeconds));
        getMessagePump().sendMessage(new Message(Engine.R_RENDER_SCENE, deltaSeconds));
        for (PulseEntity entity : _pulseEntities)
        {
            entity.pulse(deltaSeconds);
        }
    }

    @Override
    public void handleMessage(Message message) {
        switch(message.getMessageName())
        {
            case Constants.ADD_PULSE_ENTITY:
                _registerPulseEntity((PulseEntity)message.getMessageData());
                break;
            case Constants.REMOVE_PULSE_ENTITY:
                _deregisterPulseEntity((PulseEntity)message.getMessageData());
                break;
            case Constants.REMOVE_ALL_PULSE_ENTITIES:
                _pulseEntities.clear();
                break;
            case Constants.CONSOLE_VARIABLE_CHANGED:
            {
                ConsoleVariable cvar = (ConsoleVariable)message.getMessageData();
                if (cvar.getcvarName().equals(Constants.CALCULATE_MOVEMENT))
                {
                    _updateEntities = Boolean.parseBoolean(cvar.getcvarValue());
                }
                break;
            }
            case Constants.PERFORM_SOFT_RESET:
                System.err.println("Engine: performing an in-place soft reset");
                //_softRestart();
                _requiresRestart = true;
                break;
            case Constants.ADD_LOGIC_ENTITY:
            {
                LogicEntity entity = (LogicEntity)message.getMessageData();
                LogicEntityTask task = new LogicEntityTask(entity, this);
                _registeredLogicEntities.putIfAbsent(entity, task);
                _notifyOfLogicTaskCompletion(task); // This will schedule it on the logic threads
                break;
            }
            case Constants.REMOVE_LOGIC_ENTITY:
            {
                LogicEntity entity = (LogicEntity)message.getMessageData();
                _registeredLogicEntities.remove(entity);
                break;
            }
            case Constants.PERFORM_FULL_ENGINE_SHUTDOWN:
            {
                _pendingShutdown = true; // Signals to the game loop that it should call shutdown and stop
                break;
            }
        }
    }

    public void shutdown()
    {
        synchronized(this) {
            if (!_isRunning) return; // Not currently running
            _pendingShutdown = true; // Make sure this is set
            System.err.println("Performing full engine shutdown");
            _isRunning = false;
            _taskCallbackMap.clear();
            _registeredLogicEntities.clear();
            _application.shutdown();
            _window.shutdown();
            _taskManager.get().stop();
            _fileSys.shutdown();
            _initialStage = null;
            _isInitialized = false;
            _pulseEntities = null;
            _application = null;
            _messageSystem.set(null);
            _cvarSystem.set(null);
            _taskManager.set(null);
            _window = null;
            _renderer = null;
            _fileSys = null;
            _updateEntities = true; // If false, nothing is allowed to move
            _requiresRestart = false;
            _headless = false;
            _initializing = false;
            _pendingShutdown = false; // Now unset
        }
    }

    public boolean isEngineRunning() {
        return _isRunning;
    }

    public boolean isShuttingDown() {
        return _pendingShutdown;
    }

    // Performs memory allocation of core submodules so that
    // the _init function can safely initialize everything
    private void _preInit()
    {
        synchronized(this) {
            if (_isInitialized) return; // Already initialized
            System.out.println("Engine -> Pre-Initialize Stage");
            _isInitialized = true;
            _engine = this; // This is a static variable
            _cvarSystem.set(new ConsoleVariables());
            _messageSystem.set(new MessagePump());
            _pulseEntities = new HashSet<>();
            //_taskManager = new TaskManager();
            _window = new Window();
            _renderer = new Renderer();
            _fileSys = new Filesystem();
            _isRunning = true;
        }
    }

    private void _notifyOfLogicTaskCompletion(LogicEntityTask task) {
        if (!_isRunning) return; // Engine is no longer active
        LogicEntity entity = task.getLogicEntity();
        if (_registeredLogicEntities.containsKey(entity)) {
            Engine.scheduleLogicTasks(null, task);
        }
    }

    // Performs minimal allocations but initializes all submodules in the
    // correct order
    private void _init()
    {
        synchronized(this) {
            System.out.println("Engine -> Initialize Stage");
            _fileSys.init(); // Make sure this gets initialized first
            getConsoleVariables().loadConfigFile("src/resources/engine.cfg");
            _registerDefaultCVars();
            _maxFrameRate = Math.abs(Engine.getConsoleVariables().find(Constants.ENG_LIMIT_FPS).getcvarAsInt());
            _registeredLogicEntities.clear();
            _headless = Engine.getConsoleVariables().find(Constants.HEADLESS).getcvarAsBool();
            _updateEntities = Boolean.parseBoolean(getConsoleVariables().find(Constants.CALCULATE_MOVEMENT).getcvarValue());
            // Make sure we register all of the message types
            _registerMessageTypes();
            // Signal interest in the things the simulation.engine needs to know about
            getMessagePump().signalInterest(Constants.ADD_PULSE_ENTITY, this);
            getMessagePump().signalInterest(Constants.REMOVE_PULSE_ENTITY, this);
            getMessagePump().signalInterest(Constants.CONSOLE_VARIABLE_CHANGED, this);
            getMessagePump().signalInterest(Constants.REMOVE_ALL_PULSE_ENTITIES, this);
            getMessagePump().signalInterest(Constants.PERFORM_SOFT_RESET, this);
            getMessagePump().signalInterest(Constants.ADD_LOGIC_ENTITY, this);
            getMessagePump().signalInterest(Constants.REMOVE_LOGIC_ENTITY, this);
            getMessagePump().signalInterest(Constants.PERFORM_FULL_ENGINE_SHUTDOWN, this);
            if (_taskManager.get() != null) {
                _taskManager.get().stop();
            }
            _taskManager.set(new TaskManager(getConsoleVariables().find(Constants.NUM_LOGIC_THREADS).getcvarAsInt()));
            _taskManager.get().start();
            _pulseEntities = new HashSet<>();
            _lastFrameTimeMS = System.currentTimeMillis();
            _lastMessageQueueFrameTimeMS = System.currentTimeMillis();
            _maxFrameRate = getConsoleVariables().find(Constants.ENG_MAX_FPS).getcvarAsInt();
            if (!_headless) {
                _initializing = true; // Let's the main game loop know to spin while delayed initialization takes place
                new JFXPanel(); // This forces JavaFX to initialize itself
                Platform.setImplicitExit(false); // Prevents JFX from killing itself after all threads are closed
                _dispatchEngineLogic(() -> {
                    if (_initialStage != null) _initialStage.close();
                    _initialStage = new Stage();
                    _initialStage.show();
                    _initialStage.setOnCloseRequest((value) -> _pendingShutdown = true);
                    GraphicsContext gc = _window.init(_initialStage);
                    _renderer.init(gc);
                    _application.init();
                    _initializing = false;
                });
            }
            else {
                _window.init(null);
                _renderer.init(null);
                _application.init();
            }
            // Schedule the first frame and then it will schedule itself from then on
            _dispatchEngineLogic(_gameLoop);
        }
    }

    /**
     * This allows a partial restart to take place. The
     * minimum number of memory allocations to perform this will take place
     * and all submodules (including the ApplicationEntryPoint) will be re-initialized.
     *
     * This will ensure the removal of all render entities, all pulse entities,
     * and all GUI elements. Along with this it will reset the console variables
     * and the entire message pump.
     */
    private void _softRestart()
    {
        synchronized(this) {
            getMessagePump().sendMessage(new Message(Constants.REMOVE_ALL_RENDER_ENTITIES));
            getMessagePump().sendMessage(new Message(Constants.REMOVE_ALL_PULSE_ENTITIES));
            getMessagePump().sendMessage(new Message(Constants.REMOVE_ALL_UI_ELEMENTS));
            // Dispatch the messages immediately
            getMessagePump().dispatchMessages();
            // Reallocate these only
            //_cvarSystem.set(new ConsoleVariables());
            //_messageSystem.set(new MessagePump());
            getMessagePump().clearAllMessageHandlers();
            _application.shutdown();
            _fileSys.shutdown();
            //_init();
        }
    }

    private void _registerDefaultCVars()
    {
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.ENG_MAX_FPS, "60", "60"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.ENG_LIMIT_FPS, "true", "true"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.WORLD_START_X, "0", "0"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.WORLD_START_Y, "0", "0"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.WORLD_WIDTH, "1000", "0"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.WORLD_HEIGHT, "1000", "0"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.CALCULATE_MOVEMENT, "true", "true"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.NUM_LOGIC_THREADS, "2", "2"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.HEADLESS, "false", "false"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.ALLOW_MOUSE_MOVE, "true", "true"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.ALLOW_MOUSE_SCROLL, "true", "true"));
        getConsoleVariables().registerVariable(new ConsoleVariable(Constants.TIME_SCALING_FACTOR, "1.0", "1.0"));
    }

    private void _registerMessageTypes()
    {
        getMessagePump().registerMessage(new Message(Constants.ADD_PULSE_ENTITY));
        getMessagePump().registerMessage(new Message(Constants.REMOVE_PULSE_ENTITY));
        getMessagePump().registerMessage(new Message(Constants.ADD_UI_ELEMENT));
        getMessagePump().registerMessage(new Message(Constants.REMOVE_UI_ELEMENT));
        getMessagePump().registerMessage(new Message(Constants.SET_FULLSCREEN));
        getMessagePump().registerMessage(new Message(Constants.SET_SCR_HEIGHT));
        getMessagePump().registerMessage(new Message(Constants.SET_SCR_WIDTH));
        getMessagePump().registerMessage(new Message(Constants.ADD_GRAPHICS_ENTITY));
        getMessagePump().registerMessage(new Message(Constants.REMOVE_GRAPHICS_ENTITY));
        getMessagePump().registerMessage(new Message(Constants.REGISTER_TEXTURE));
        getMessagePump().registerMessage(new Message(Constants.SET_MAIN_CAMERA));
        getMessagePump().registerMessage(new Message(Constants.CONSOLE_VARIABLE_CHANGED));
        getMessagePump().registerMessage(new Message(R_RENDER_SCENE));
        getMessagePump().registerMessage(new Message(R_UPDATE_ENTITIES));
        getMessagePump().registerMessage(new Message(Constants.REMOVE_ALL_UI_ELEMENTS));
        getMessagePump().registerMessage(new Message(Constants.REMOVE_ALL_PULSE_ENTITIES));
        getMessagePump().registerMessage(new Message(Constants.REMOVE_ALL_RENDER_ENTITIES));
        getMessagePump().registerMessage(new Message(Constants.PERFORM_SOFT_RESET));
        getMessagePump().registerMessage(new Message(Constants.ADD_LOGIC_ENTITY));
        getMessagePump().registerMessage(new Message(Constants.REMOVE_LOGIC_ENTITY));
        getMessagePump().registerMessage(new Message(Constants.INCREMENT_CAMERA_X_OFFSET));
        getMessagePump().registerMessage(new Message(Constants.INCREMENT_CAMERA_Y_OFFSET));
        getMessagePump().registerMessage(new Message(Constants.RESET_CAMERA_XY_OFFSET));
        getMessagePump().registerMessage(new Message(Constants.PERFORM_FULL_ENGINE_SHUTDOWN));
        getMessagePump().registerMessage(new Message(Constants.SET_CAMERA_ZOOM));
        getMessagePump().registerMessage(new Message(Constants.CAMERA_OFFSET_CHANGED));
        getMessagePump().registerMessage(new Message(Constants.CAMERA_ZOOM_CHANGED));
        getMessagePump().registerMessage(new Message(Constants.SET_CAMERA_X_OFFSET));
        getMessagePump().registerMessage(new Message(Constants.SET_CAMERA_Y_OFFSET));
    }

    /**
     * Registers a pulse entity, which is an entity which must be updated once
     * per simulation.engine/simulation frame.
     * @param entity entity to update every frame
     */
    private void _registerPulseEntity(PulseEntity entity)
    {
        _pulseEntities.add(entity);
    }

    private void _deregisterPulseEntity(PulseEntity entity)
    {
        _pulseEntities.remove(entity);
    }
}
