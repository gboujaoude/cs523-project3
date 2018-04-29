package application.liver_idea_model;

import engine.*;
import engine.math.Vector3;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;

public class Lymphocyte extends Circle2D implements PulseEntity {
    private static final Color _color = new Color(255 / 255.0, 173 / 255.0, 31 / 255.0, 1);
    private static final Random _rng = new Random();
    private boolean _activated = false;
    private double _elapsedLifeSeconds = 0.0;
    private boolean _changeSpeedImmediately = true;
    private boolean _reachedEndOfTrail = false;
    private double _elapsedChangeDirSec = 0.0;
    private final double _secondsBeforeChangeDir = 5.0;
    private final double _lifeSpanSec;
    private final double _speed;

    public Lymphocyte(double x, double y) {
        super(x, y, 25, 25, 1);
        setColor(_color);
        _lifeSpanSec = Engine.getConsoleVariables().find(ModelGlobals.lymphocyteLifespan).getcvarAsFloat();
        _speed = Engine.getConsoleVariables().find(ModelGlobals.lymphocyteSpeed).getcvarAsFloat();
    }

    public boolean activated() {
        return _activated;
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
        if (activated()) {
            _elapsedLifeSeconds += deltaSeconds;
            if (_elapsedLifeSeconds >= _lifeSpanSec) removeFromWorld(); // Lymphocyte died
            if (_reachedEndOfTrail) {
                _elapsedChangeDirSec += deltaSeconds;
                if (_changeSpeedImmediately) {
                    _changeSpeedRandomly();
                    _changeSpeedImmediately = false;
                }
                if (_elapsedChangeDirSec >= _secondsBeforeChangeDir) {
                    if (_rng.nextDouble() <= 0.5) _changeSpeedRandomly();
                    _elapsedChangeDirSec = 0.0;
                }
            }
        }
        double worldHeight = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsFloat();
        double locationY = getLocationY();
        double speedX = getSpeedX();
        double speedY = getSpeedY();
        if ((locationY <= 10 && speedY < 0.0) || (locationY > worldHeight * 0.7 && speedY > 0.0)) setSpeedXY(speedX, -speedY);
    }

    /**
     * This function ensures that the render entity is added to the world. After
     * calling this it will be regularly called by the Engine and its movement
     * will be calculated by the Renderer and it will be drawn on the screen.
     */
    @Override
    public void addToWorld() {
        super.addToWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
        Engine.getMessagePump().sendMessage(new Message(ModelGlobals.lymphocyteAddedToWorld, this));
    }

    /**
     * After calling this the entity will no longer be drawn and its update function will
     * not be called
     */
    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
        Engine.getMessagePump().sendMessage(new Message(ModelGlobals.lymphocyteRemovedFromWorld, this));
    }

    private void _changeSpeedRandomly() {
        final double speed = _speed;
        final double minSpeed = _speed;
        double speedX = speed * _rng.nextDouble() + minSpeed;
        if (_rng.nextDouble() >= 0.5) speedX *= -1;
        double speedY = speed * _rng.nextDouble() + minSpeed;
        if (_rng.nextDouble() >= 0.5) speedY *= -1;
        setSpeedXY(speedX, speedY);
    }

    @Override
    public void onActorOverlapped(Actor actor, HashSet<Actor> actors) {
        super.onActorOverlapped(actor, actors);
        int numViruses = 0;
        int numInfected = 0;
        for (Actor collided : actors) {
            if (collided instanceof Virus) {
                collided.removeFromWorld();
                ++numViruses;
                Engine.getMessagePump().sendMessage(new Message(ModelGlobals.virusSquashedLymphocyte));
                //System.out.println("Lymphocyte: Found virus -> destroying");
            }
            else if (collided instanceof LiverCell) {
                LiverCell cell = (LiverCell)collided;
                if (cell.infected()) {
                    cell.removeFromWorld();
                    ++numInfected;
                    //System.out.println("Lymphocyte: Found infected cell -> destroying");
                }
            }
            if (collided instanceof Cytokine) {
                Cytokine cytokine = (Cytokine)collided;
                cytokine.removeFromWorld();
                if (_reachedEndOfTrail) return;
                double refX = cytokine.getReferencedLocation().getKey();
                double refY = cytokine.getReferencedLocation().getValue();
                _activated = true;
                if (refX != -1 && refY != -1) {
                    Vector3 refVec = new Vector3(refX, refY, 0.0);
                    Vector3 locationVec = new Vector3(getLocationX(), getLocationY(), 0.0);
                    refVec.subtractThis(locationVec);
                    refVec.normalizeThis();
                    setSpeedXY(_speed * refVec.x(), _speed * refVec.y());
                }
                else {
                    _reachedEndOfTrail = true;
                    _changeSpeedImmediately = true;
                }
            }
        }
        if (numViruses > 0) System.out.println("Lymphocyte: Found [" + numViruses + "] viruses -> destroying");
        if (numInfected > 0) System.out.println("Lymphocyte: Found [" + numInfected + "] infected cells -> destroying");
    }
}
