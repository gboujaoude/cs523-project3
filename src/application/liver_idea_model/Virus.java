package application.liver_idea_model;

import engine.Actor;
import engine.Circle2D;
import engine.Engine;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;

public class Virus extends Circle2D {
    private static final Color _color = new Color(255 / 255.0, 73 / 255.0, 61 / 255.0, 1.0);
    private static final Random _rng = new Random();
    private final double _speed;

    public Virus(double x, double y) {
        super(x, y, 5, 5, 1);
        _speed = Engine.getConsoleVariables().find(ModelGlobals.virusSpeed).getcvarAsFloat();
        setColor(_color);
        setSpeedXY(_speed * _rng.nextDouble(), _speed * _rng.nextDouble());
    }

    @Override
    public void onActorOverlapped(Actor actor, HashSet<Actor> actors) {
        super.onActorOverlapped(actor, actors);
        for (Actor collided : actors) {
            if (collided instanceof LiverCell) {
                LiverCell cell = (LiverCell)collided;
                if (!cell.infected()) {
                    cell.infect(this);
                    removeFromWorld(); // Remove self from world
                    return; // End early since we successfully infected a virus
                }
            }
        }
    }
}
