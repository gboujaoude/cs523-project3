package engine;

import javafx.scene.canvas.GraphicsContext;

/**
 * Implement this class for each object you want to be able to add to
 * the world, render it and have it move around.
 *
 * @author Justin Hall
 */
public abstract class RenderEntity extends GraphicsEntity implements PulseEntity {
    /**
     * This function ensures that the render entity is added to the world. After
     * calling this it will be regularly called by the Engine and its movement
     * will be calculated by the Renderer and it will be drawn on the screen.
     */
    public void addToWorld()
    {
        super.addToWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
    }

    /**
     * After calling this the entity will no longer be drawn and its update function will
     * not be called
     */
    public void removeFromWorld()
    {
        super.addToWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
    }

    @Override
    public void render(GraphicsContext gc, double x, double y, double width, double height) {
        gc.fillRect(x, y, width, height);
    }
}
