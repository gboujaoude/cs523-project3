package engine;

import java.util.ArrayList;

/**
 * A mouse input component can be added to the engine's window manager
 * so that mouse events can be handled and processed. These events include
 * the standard pressed/released/moved, and also the mouse collision event
 * for any actors that were underneath the mouse when it was clicked.
 *
 * Multiple mouse input components can be added to the window. What will happen
 * in this event is that they will receive identical inputs each frame from the engine.
 */
public abstract class MouseInputComponent {
    /**
     * When the mouse is clicked down, this method will be called
     * to notify you where the mouse was when this happened.
     *
     * @param mouseX mouse location x on the screen in pixels
     * @param mouseY mouse location y on the screen in pixels
     * @param button which mouse button resulted in the pressed down event
     */
    public abstract void mousePressedDown(double mouseX, double mouseY, MouseButtonTypes button);

    /**
     * Triggered after a user both presses and releases the mouse. This
     * will notify you of where the mouse was when this happened.
     *
     * @param mouseX mouse location x on the screen in pixels
     * @param mouseY mouse location y on the screen in pixels
     * @param button which mouse button resulted in the pressed down event
     */
    public abstract void mouseReleased(double mouseX, double mouseY, MouseButtonTypes button);

    /**
     * This can occur whether the mouse is pressed down or not. This
     * method can be combined with mousePressedDown to create a "mouseDragged"
     * type of method.
     *
     * @param amountX How much the mouse has moved in the x-direction in pixels. A negative value means
     *                that the mouse has moved to the left by amountX number of pixels.
     * @param amountY How much the mouse has moved in the y-direction in pixels. A negative value means
     *                that the mouse has moved up the screen by amountY number of pixels.
     * @param mouseX Current mouse x-location in pixels
     * @param mouseY Current mouse y-location in pixels
     */
    public abstract void mouseMoved(double amountX, double amountY, double mouseX, double mouseY);

    /**
     * When the mouse is fully clicked (down then up), it will generate a collision response
     * with world actors underneath the mouse cursor. The type of the actors will need to
     * be checked since they could be render entities or they could be something else.
     *
     * @param actors list of actors that were underneath the mouse when it was clicked
     */
    public abstract void processMouseCollisionResponse(ArrayList<Actor> actors);

    /**
     * Actives the mouse input component so that it will receive updates from
     * the Engine's window manager.
     */
    public final void enableMouseInputComponent() {
        Engine.getMessagePump().sendMessage(new Message(Window.W_REGISTER_MOUSE_INPUT_COMPONENT, this));
    }

    /**
     * Disables the mouse input component so that it will no longer receive updates.
     */
    public final void disableMouseInputComponent() {
        Engine.getMessagePump().sendMessage(new Message(Window.W_DEREGISTER_MOUSE_INPUT_COMPONENT, this));
    }
}
