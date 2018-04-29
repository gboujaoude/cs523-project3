package application;

import engine.*;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * Gives us the ability to scroll around using the mouse
 */
public class CameraController extends MouseInputComponent implements MessageHandler {
    private double _scrollAmount = 1.0;
    private double _deltaScroll = 1.1;
    private double _lastMouseX = 0.0;
    private double _lastMouseY = 0.0;
    private double _currentXOffset = 0.0;
    private double _currentYOffset = 0.0;
    private boolean _isPressed = false;

    public CameraController() {
        Engine.getMessagePump().signalInterest(Constants.CAMERA_OFFSET_CHANGED, this);
        Engine.getMessagePump().signalInterest(Constants.CAMERA_ZOOM_CHANGED, this);
    }

    @Override
    public void mousePressedDown(double mouseX, double mouseY, MouseButtonTypes button) {
        _isPressed = true;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, MouseButtonTypes button) {
        _isPressed = false;
    }

    @Override
    public void mouseMoved(double amountX, double amountY, double mouseX, double mouseY) {
        _lastMouseX = mouseX;
        _lastMouseY = mouseY;
        if (_isPressed) {
            _currentXOffset += amountX * 5.0;
            _currentYOffset += amountY * 5.0;
            Engine.getMessagePump().sendMessage(new Message(Constants.INCREMENT_CAMERA_X_OFFSET, amountX * 5.0));
            Engine.getMessagePump().sendMessage(new Message(Constants.INCREMENT_CAMERA_Y_OFFSET, amountY * 5.0));
        }
    }

    // @see https://stackoverflow.com/questions/13316481/zooming-into-a-window-based-on-the-mouse-position
    @Override
    public void scrolled(double direction) {
        boolean zoomIn = direction == 1.0;
        double scale = zoomIn ? _deltaScroll : 1 / _deltaScroll;
        double screenWidth = Engine.getConsoleVariables().find(Constants.SCR_WIDTH).getcvarAsFloat();
        double screenHeight = Engine.getConsoleVariables().find(Constants.SCR_HEIGHT).getcvarAsFloat();
        double originX = _currentXOffset / 2;
        double originY = _currentYOffset / 2;

        double oldScroll = _scrollAmount;
        _scrollAmount *= scale;
        _scrollAmount = Math.max(0.1, Math.min(5.0, _scrollAmount));
        if (oldScroll != _scrollAmount) {
            Engine.getMessagePump().sendMessage(new Message(Constants.SET_CAMERA_X_OFFSET, _currentXOffset + direction * (_lastMouseX - originX) * (1.0 - scale)));
            Engine.getMessagePump().sendMessage(new Message(Constants.SET_CAMERA_Y_OFFSET, _currentYOffset + direction * (_lastMouseY - originY) * (1.0 - scale)));
            Engine.getMessagePump().sendMessage(new Message(Constants.SET_CAMERA_ZOOM, _scrollAmount));
        }
    }
    /**
    public void scrolled(double direction) {
        double screenWidth = Engine.getConsoleVariables().find(Constants.SCR_WIDTH).getcvarAsFloat();
        double screenHeight = Engine.getConsoleVariables().find(Constants.SCR_HEIGHT).getcvarAsFloat();
        double originX = screenWidth / 2;
        double originY = screenHeight / 2;
        double oldScroll = _scrollAmount;
        _scrollAmount += _deltaScroll * direction;
        _scrollAmount = Math.max(_deltaScroll, Math.min(5.0, _scrollAmount));
        double lastMouseX = _lastMouseX;// / screenWidth;
        double lastMouseY = _lastMouseY;// / screenHeight;
        double newMouseX = lastMouseX * (_scrollAmount);
        double newMouseY = lastMouseY * (_scrollAmount);
        double deltaMouseX = (newMouseX - lastMouseX);
        double deltaMouseY = (newMouseY - lastMouseY);
        System.out.println(deltaMouseX + " " + deltaMouseY);
        //System.out.println(_currentXOffset + " " + _currentYOffset);
        //System.out.println(direction);
        Engine.getMessagePump().sendMessage(new Message(Constants.SET_CAMERA_ZOOM, _scrollAmount));
        if (oldScroll != _scrollAmount) {
            Engine.getMessagePump().sendMessage(new Message(Constants.SET_CAMERA_X_OFFSET, _currentXOffset + (lastMouseX - originX) * (1.0 - _scrollAmount)));
            Engine.getMessagePump().sendMessage(new Message(Constants.SET_CAMERA_Y_OFFSET, _currentYOffset + (lastMouseY - originY) * (1.0 - _scrollAmount)));
        }
        //Engine.getMessagePump().sendMessage(new Message(Constants.INCREMENT_CAMERA_Y_OFFSET, deltaMouseY));
    }
     */

    @Override
    public void processMouseCollisionResponse(ArrayList<Actor> actors) {

    }

    @Override
    public void handleMessage(Message message) {
        switch (message.getMessageName()) {
            case Constants.CAMERA_OFFSET_CHANGED:
            {
                Pair<Double, Double> offsets = (Pair<Double, Double>)message.getMessageData();
                //_currentXOffset = offsets.getKey();
                //_currentYOffset = offsets.getValue();
                break;
            }
        }
    }
}
