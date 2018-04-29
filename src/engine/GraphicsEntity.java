package engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A graphics entity is the second to last layer in the rendering system
 * before the actual, concrete classes that can be instantiated (such
 * as class RenderEntity). In terms of inheritance, it goes:
 *      -Actor
 *          -ActorGraph
 *              -GraphicsEntity
 *                  -RenderEntity
 *
 * This class contains the state needed to draw either a raw object
 * using a graphics context (this includes shapes), or textured data.
 * In the case of a textured element, it will need help from higher-up
 * since the textures are cached in the rendering system itself.
 *
 * @author Justin Hall
 */
public abstract class GraphicsEntity extends ActorGraph {
    private String _texture = "";
    private Color _color = Color.RED;

    /**
     * This function ensures that the render entity is added to the world. After
     * calling this it will be regularly called by the Engine and its movement
     * will be calculated by the Renderer and it will be drawn on the screen.
     */
    public void addToWorld()
    {
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_GRAPHICS_ENTITY, this));
    }

    /**
     * After calling this the entity will no longer be drawn and its update function will
     * not be called
     */
    public void removeFromWorld()
    {
        Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_GRAPHICS_ENTITY, this));
    }

    /**
     * This will only be called if the texture is not set, in which case
     * the rendering system will assume that it needs to render a primitive
     * object such as a square or a circle.
     *
     * @param gc active graphics context from the rendering system
     * @param x screen x-location for this entity (renderer likely recalculated your location, so use this)
     * @param y screen y-location for this entity (renderer likely recalculated your location, so use this)
     * @param width what the renderer has calculated your screen width to be
     * @param height what the renderer has calculated your screen height to be
     */
    // package-private
    public abstract void render(GraphicsContext gc, double x, double y, double width, double height);

    public void setTexture(String texture)
    {
        _texture = texture;
        Engine.getMessagePump().sendMessage(new Message(Constants.REGISTER_TEXTURE, texture));
    }

    public void setColor(Color color)
    {
        _color = color;
    }

    public String getTexture()
    {
        return _texture;
    }

    public Color getColor()
    {
        return _color;
    }
}
