package application.layered_immune_sys_test;

import engine.Actor;
import engine.Circle2D;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;

public class Virus extends Circle2D {
    private static final Color _color = new Color(255 / 255.0, 73 / 255.0, 61 / 255.0, 1.0);

    public Virus(double x, double y) {
        super(x, y, 5, 5, 1);
        setColor(_color);
        Random rng = new Random();
        final double speed = 100;
        setSpeedXY(rng.nextDouble() * speed, -rng.nextDouble() * speed);
    }

    public Virus(Virus other) {
        super(other.getLocationX(), other.getLocationY(), other.getWidth(), other.getHeight(), other.getDepth());
        setColor(_color);
        Random rng = new Random();
        final double speed = 100;
        setSpeedXY(rng.nextDouble() * speed, -rng.nextDouble() * speed);
    }

    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) {
            if (actor instanceof BystanderCell) {
                BystanderCell cell = (BystanderCell)actor;
                if (cell.infected()) continue;
                cell.infect(this);
                break;
            }
        }
    }
}
