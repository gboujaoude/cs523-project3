package engine;

import javafx.scene.canvas.GraphicsContext;

import java.util.HashSet;

/**
 * Simple class for representing a 2D oval, optionally textured.
 */
public class Circle2D extends GraphicsEntity {

    public Circle2D(double x, double y, double radiusX, double radiusY, double depth) {
        setLocationXYDepth(x, y, depth);
        setWidthHeight(radiusX, radiusY);
    }

    public void setWidthHeight(double radiusX, double radiusY) {
        super.setWidthHeight(radiusX, radiusY);
    }

    @Override
    public void render(GraphicsContext gc, double x, double y, double width, double height) {
        gc.fillOval(x, y, width, height);
    }

    @Override
    public void onActorOverlapped(Actor actor, HashSet<Actor> actors) {
    }
}
