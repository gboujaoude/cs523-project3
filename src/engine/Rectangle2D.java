package engine;

import javafx.scene.canvas.GraphicsContext;

import java.util.HashSet;

/**
 * Simple class for representing a rectangle, optionally textured.
 */
public class Rectangle2D extends GraphicsEntity {
    public Rectangle2D(double x, double y, double width, double height, double depth) {
        setLocationXYDepth(x, y, depth);
        setWidthHeight(width, height);
    }

    @Override
    public void render(GraphicsContext gc, double x, double y, double width, double height) {
        gc.fillRect(x, y, width, height);
    }

    @Override
    public void onActorOverlapped(Actor actor, HashSet<Actor> actors) {

    }
}
