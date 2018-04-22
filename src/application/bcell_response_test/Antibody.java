package application.bcell_response_test;

import engine.*;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;

public class Antibody extends Circle2D implements PulseEntity {
    private static final Color _color = new Color(255 / 255.0, 186 / 255.0, 55 / 255.0, 1.0);
    private final Random _rng;

    public Antibody(double x, double y, double radiusX, double radiusY, double depth) {
        super(x, y, 3, 3, depth);
        setColor(_color);
        _rng = new Random();
        double speed = 50;
        double goRight = 1;
        if (_rng.nextDouble() < 0.5) goRight = -goRight;
        setSpeedXY(_rng.nextDouble() * 75 * goRight, -speed);
    }

    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor curr: collidedWith) {
//            if (curr instanceof  BystanderCell) {
//                BystanderCell cell = (BystanderCell) curr;
//                cell.removeFromWorld();
//                this.removeFromWorld();
//            }
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
