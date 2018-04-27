package application.liver_idea_model;

import engine.Actor;
import engine.Circle2D;
import engine.Engine;
import engine.Message;
import engine.math.Vector3;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;

public class Virus extends Circle2D {
    private static final Color _color = new Color(255 / 255.0, 73 / 255.0, 61 / 255.0, 1.0);
    private static final Random _rng = new Random();
    private final double _speed;
    private boolean _added = false;

    public Virus(double x, double y) {
        super(x, y, 5, 5, 1);
        _speed = Engine.getConsoleVariables().find(ModelGlobals.virusSpeed).getcvarAsFloat();
        setColor(_color);
        Vector3 vec = new Vector3(_rng.nextDouble(), _rng.nextDouble(), 0);
        vec.normalizeThis();
        setSpeedXY(_speed * vec.x(), _speed * vec.y());
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

    /**
     * This function ensures that the render entity is added to the world. After
     * calling this it will be regularly called by the Engine and its movement
     * will be calculated by the Renderer and it will be drawn on the screen.
     */
    @Override
    public void addToWorld() {
        super.addToWorld();
        if (!_added) {
            Engine.getMessagePump().sendMessage(new Message(ModelGlobals.virusAddedToWorld));
        }
        _added = true;
    }

    /**
     * After calling this the entity will no longer be drawn and its update function will
     * not be called
     */
    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        if (_added) {
            Engine.getMessagePump().sendMessage(new Message(ModelGlobals.virusRemovedFromWorld));
        }
        _added = false;
    }
}
