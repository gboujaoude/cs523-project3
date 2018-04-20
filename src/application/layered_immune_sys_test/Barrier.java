package application.layered_immune_sys_test;

import engine.Actor;
import engine.Circle2D;
import javafx.scene.paint.Color;

import java.util.HashSet;

// Kills anything it collides with
public class Barrier extends Circle2D {
    public Barrier(double x, double y, double width, double height, double depth) {
        super(x, y, width, height, depth);
        setColor(new Color(0, 0, 0, 0));
    }

    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) actor.removeFromWorld();
    }
}
