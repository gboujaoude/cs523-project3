package application.bcell_response_test;

import engine.Actor;
import engine.Circle2D;
import engine.PulseEntity;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashSet;

public class Antibody extends Circle2D implements PulseEntity {
    public Antibody(double x, double y, double radiusX, double radiusY, double depth) {
        super(x, y, 3, 3, depth);
    }

    @Override
    public void onActorOverlapped(Actor actor, HashSet<Actor> actors) {
        super.onActorOverlapped(actor, actors);
    }

    /**
     * Called each time the simulation.engine updates.
     *
     * @param deltaSeconds Change in seconds since the last update.
     *                     If the simulation.engine is running at 60 frames per second,
     *                     this value will be roughly equal to (1/60).
     */
    @Override
    public void pulse(double deltaSeconds) {

    }
}
