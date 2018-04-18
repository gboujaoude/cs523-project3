package engine;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.HashSet;

/**
 * Represents the window for the game/simulation
 *
 * @author Justin Hall
 */
public class Window implements MessageHandler, PulseEntity {
    // Package-private
    static final String W_REGISTER_MOUSE_INPUT_COMPONENT = "w_register_mouse_input_component";
    static final String W_DEREGISTER_MOUSE_INPUT_COMPONENT = "w_deregister_mouse_input_component";

    private Stage _stage;
    private Pane _stack;
    private Canvas _canvas;
    private Scene _jfxScene;
    private GraphicsContext _gc;
    private boolean _isFullscreen = false;
    private int _width = 1024;
    private int _height = 768;
    private boolean _resizeable = true;
    private String _title = "Application";
    private HashSet<MouseInputComponent> _mouseInputComponents;
    private InternalMouseInputManager _mouseInputManager;

    private class MouseSnapshot {
        boolean moved = false;
        boolean pressedDown = false;
        boolean released = false;
        double x = 0.0;
        double y = 0.0;
        double movedX = 0.0;
        double movedY = 0.0;
        MouseButtonTypes button;
    }

    private class InternalMouseInputManager
    {
        private double _prevMouseX = 0.0;
        private double _prevMouseY = 0.0;
        private double _mouseX = 0.0;
        private double _mouseY = 0.0;
        private MouseSnapshot _snapshot = new MouseSnapshot();

        void mousePressedDown(MouseEvent event) {
            swap(event.getX(), event.getY());
            _snapshot.pressedDown = true;
            setMouseButtonType(event.getButton());
        }

        void mouseReleased(MouseEvent event) {
            swap(event.getX(), event.getY());
            _snapshot.released = true;
            setMouseButtonType(event.getButton());
        }

        void mouseMoved(MouseEvent event) {
            swap(event.getX(), event.getY());
            _snapshot.moved = true;
        }

        MouseSnapshot snapshot() {
            _snapshot.x = _mouseX;
            _snapshot.y = _mouseY;
            if (_snapshot.moved) {
                _snapshot.movedX = _mouseX - _prevMouseX;
                _snapshot.movedY = _mouseY - _prevMouseY;
            }
            // Now reset the internal snapshot before returning the old snapshot
            MouseSnapshot snapshot = _snapshot;
            _snapshot = new MouseSnapshot();
            return snapshot;
        }

        void swap(double x, double y) {
            _prevMouseX = _mouseX;
            _prevMouseY = _mouseY;
            _mouseX = x;
            _mouseY = y;
        }

        void setMouseButtonType(MouseButton button) {
            if (button.equals(MouseButton.PRIMARY)) _snapshot.button = MouseButtonTypes.LEFT_MOUSE_BUTTON;
            else if (button.equals(MouseButton.SECONDARY)) _snapshot.button = MouseButtonTypes.RIGHT_MOUSE_BUTTON;
            else _snapshot.button = MouseButtonTypes.MIDDLE_MOUSE_BUTTON;
        }
    }

    /**
     * @return current pixel width of the window
     */
    public int getWidth()
    {
        return _width;
    }

    /**
     * @return current pixel height of the window
     */
    public int getHeight()
    {
        return _height;
    }

    /**
     * Initializes the window for use
     * @param stage valid stage representing the window
     * @return GraphicsContext which is created after initializing the nodes of the stage
     */
    public GraphicsContext init(Stage stage)
    {
        // We want to update frequently to check for resizes, so tell the system to add us as a pulse entity
        Engine.getMessagePump().sendMessage(new Message(Singleton.ADD_PULSE_ENTITY, this));
        Engine.getConsoleVariables().registerVariable(new ConsoleVariable(Singleton.SCR_FULLSCREEN, Boolean.toString(_isFullscreen)));
        Engine.getConsoleVariables().registerVariable(new ConsoleVariable(Singleton.SCR_WIDTH, Integer.toString(_width)));
        Engine.getConsoleVariables().registerVariable(new ConsoleVariable(Singleton.SCR_HEIGHT, Integer.toString(_height)));
        Engine.getConsoleVariables().registerVariable(new ConsoleVariable(Singleton.SCR_RESIZEABLE, Boolean.toString(_resizeable)));
        Engine.getConsoleVariables().registerVariable(new ConsoleVariable(Singleton.SCR_TITLE, _title));
        ConsoleVariables cvars = Engine.getConsoleVariables();
        _isFullscreen = Boolean.parseBoolean(cvars.find(Singleton.SCR_FULLSCREEN).getcvarValue());
        _width = Integer.parseInt(cvars.find(Singleton.SCR_WIDTH).getcvarValue());
        _height = Integer.parseInt(cvars.find(Singleton.SCR_HEIGHT).getcvarValue());
        _resizeable = Boolean.parseBoolean(cvars.find(Singleton.SCR_RESIZEABLE).getcvarValue());
        _title = cvars.find(Singleton.SCR_TITLE).getcvarValue();
        _mouseInputComponents = new HashSet<>();
        _mouseInputManager = new InternalMouseInputManager();

        // Register window-specific messages
        Engine.getMessagePump().registerMessage(new Message(W_REGISTER_MOUSE_INPUT_COMPONENT));
        Engine.getMessagePump().registerMessage(new Message(W_DEREGISTER_MOUSE_INPUT_COMPONENT));

        // Signal interest
        Engine.getMessagePump().signalInterest(W_REGISTER_MOUSE_INPUT_COMPONENT, this);
        Engine.getMessagePump().signalInterest(W_DEREGISTER_MOUSE_INPUT_COMPONENT, this);
        Engine.getMessagePump().signalInterest(Singleton.SET_SCR_WIDTH, this);
        Engine.getMessagePump().signalInterest(Singleton.SET_SCR_HEIGHT, this);
        Engine.getMessagePump().signalInterest(Singleton.SET_FULLSCREEN, this);
        Engine.getMessagePump().signalInterest(Singleton.ADD_UI_ELEMENT, this);
        Engine.getMessagePump().signalInterest(Singleton.REMOVE_UI_ELEMENT, this);
        Engine.getMessagePump().signalInterest(Singleton.CONSOLE_VARIABLE_CHANGED, this);
        Engine.getMessagePump().signalInterest(Singleton.REMOVE_ALL_UI_ELEMENTS, this);
        stage.setFullScreen(_isFullscreen);
        stage.setResizable(_resizeable);
        if (_isFullscreen)
        {
            Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
            _width = (int)screenSize.getWidth();
            _height = (int)screenSize.getHeight();
        }
        //stage.setResizable(false);
        stage.setTitle(_title);
        _stage = stage;
        Group root = new Group();
        _canvas = new Canvas(_width, _height);
        _stack = new Pane();
        _stack.getChildren().addAll(_canvas);
        root.getChildren().add(_stack);
        //root.getChildren().add(_canvas);
        _jfxScene = new Scene(root, _width, _height);
        _jfxScene.setOnMousePressed(_mouseInputManager::mousePressedDown);
        _jfxScene.setOnMouseReleased(_mouseInputManager::mouseReleased);
        _jfxScene.setOnMouseMoved(_mouseInputManager::mouseMoved);
        stage.setScene(_jfxScene);
        stage.show();
        _gc = _canvas.getGraphicsContext2D();
        return _gc;
    }

    @Override
    public void handleMessage(Message message) {
        switch(message.getMessageName())
        {
            case Singleton.CONSOLE_VARIABLE_CHANGED:
            {
                ConsoleVariable cvar = (ConsoleVariable)message.getMessageData();
                if (cvar.getcvarName().equals(Singleton.SCR_WIDTH) || cvar.getcvarName().equals(Singleton.SCR_HEIGHT))
                {
                    _width = (int) _jfxScene.getWidth();
                    _height = (int) _jfxScene.getHeight();
                    _canvas.setWidth(_width);
                    _canvas.setHeight(_height);
                }
                else if (cvar.getcvarName().equals(Singleton.SCR_FULLSCREEN))
                {
                    _stage.setFullScreen(_isFullscreen);
                }
                else if (cvar.getcvarName().equals(Singleton.SCR_RESIZEABLE))
                {
                    _stage.setResizable(true);
                }
                break;
            }
            case Singleton.ADD_UI_ELEMENT:
            {
                _stack.getChildren().add((Node)message.getMessageData());
                break;
            }
            case Singleton.REMOVE_UI_ELEMENT:
            {
                _stack.getChildren().remove((Node)message.getMessageData());
                break;
            }
            case Singleton.REMOVE_ALL_UI_ELEMENTS:
            {
                _stack.getChildren().clear();
                break;
            }
            case W_REGISTER_MOUSE_INPUT_COMPONENT:
            {
                System.out.println("registered");
                _mouseInputComponents.add((MouseInputComponent)message.getMessageData());
                break;
            }
            case W_DEREGISTER_MOUSE_INPUT_COMPONENT:
            {
                _mouseInputComponents.remove((MouseInputComponent)message.getMessageData());
                break;
            }
        }
    }

    @Override
    public void pulse(double deltaSeconds) {
        if (_width != (int)_jfxScene.getWidth() || _height != (int)_jfxScene.getHeight())
        {
            Engine.getConsoleVariables().find(Singleton.SCR_WIDTH).setValue(Integer.toString((int)_jfxScene.getWidth()));
            Engine.getConsoleVariables().find(Singleton.SCR_HEIGHT).setValue(Integer.toString((int)_jfxScene.getHeight()));
        }
        // Process mouse input
        MouseSnapshot snapshot = _mouseInputManager.snapshot();
        // Dispatch mouse input events
        for (MouseInputComponent component : _mouseInputComponents) {
            if (snapshot.pressedDown) component.mousePressedDown(snapshot.x, snapshot.y, snapshot.button);
            if (snapshot.released) component.mouseReleased(snapshot.x, snapshot.y, snapshot.button);
            if (snapshot.moved) component.mouseMoved(snapshot.movedX, snapshot.movedY, snapshot.x, snapshot.y);
        }
    }
}
