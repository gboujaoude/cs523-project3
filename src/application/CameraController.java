package application;

import engine.*;

import java.util.ArrayList;

/**
 * Gives us the ability to scroll around using the mouse
 */
public class CameraController extends MouseInputComponent {
    private boolean _isPressed = false;

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
        if (_isPressed) {
            Engine.getMessagePump().sendMessage(new Message(Constants.INCREMENT_CAMERA_X_OFFSET, amountX * 5.0));
            Engine.getMessagePump().sendMessage(new Message(Constants.INCREMENT_CAMERA_Y_OFFSET, amountY * 5.0));
        }
    }

    @Override
    public void processMouseCollisionResponse(ArrayList<Actor> actors) {

    }
}
