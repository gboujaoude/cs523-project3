package application.bcell_response_test;

import engine.*;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;

public class Antibody extends Circle2D implements PulseEntity {
    private static final Color _color = new Color(255 / 255.0, 186 / 255.0, 55 / 255.0, 1.0);

    public Antibody(double x, double y, double radiusX, double radiusY, double depth) {
        super(x, y, 3, 3, depth);
        setColor(_color);
        Random rng = new Random();
        double speed = 50;
        setSpeedXY(rng.nextDouble() * 75, -speed);
    }

    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor curr: collidedWith) {
            if (curr instanceof  BystanderCell) {
                BystanderCell cell = (BystanderCell) curr;
                cell.removeFromWorld();
                this.removeFromWorld();
                System.out.println("removing self");
            }
        }

    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
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
