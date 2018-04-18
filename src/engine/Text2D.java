package engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

import java.util.HashSet;

/**
 * Allows you to draw text on the screen and manipulate it like you would
 * a shape or textured object.
 */
public class Text2D extends GraphicsEntity {
    private String _text = "";
    private Font _font;

    public Text2D(String text, double x, double y, double width, double fontSize, double depth) {
        setText(text);
        setLocationXYDepth(x, y, depth);
        setWidthHeight(width, fontSize);
        _font = new Font(fontSize);
    }

    public void setText(String text) {
        _text = text;
    }

    public String getText() {
        return _text;
    }

    public void setFont(Font font) {
        _font = font;
    }

    public Font getFont() {
        return _font;
    }

    @Override
    public void render(GraphicsContext gc, double x, double y) {
        gc.setFont(_font);
        gc.fillText(_text, x, y, getWidth());
    }

    @Override
    public void setTexture(String texture)
    {
        // Do nothing
    }

    @Override
    public void onActorOverlapped(Actor actor, HashSet<Actor> actors) {
    }
}
