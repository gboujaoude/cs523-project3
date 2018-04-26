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
    private long _timestampOfLastCytokineEncounter = 0;
    private boolean _changeSpeedImmediately = true;
    private final double _freeRoamThresholdSec = 10.0;
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
            double elapsedSec = (_timestampOfLastCytokineEncounter) / 1000.0;
            // If it hasn't encountered a cytokine in threshold sec amount of time,
            // move to free roam stage
            if (elapsedSec >= _freeRoamThresholdSec) {
                if (_changeSpeedImmediately) {
                    _changeSpeedRandomly();
                    _changeSpeedImmediately = false;
                }
                else if (_rng.nextDouble() <= 0.01) {
                    _changeSpeedRandomly();
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
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
    }

    /**
     * After calling this the entity will no longer be drawn and its update function will
     * not be called
     */
    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
    }

    private void _changeSpeedRandomly() {
        double speedX = _speed * _rng.nextDouble();
        if (_rng.nextDouble() >= 0.5) speedX *= -1;
        double speedY = _speed * _rng.nextDouble();
        if (_rng.nextDouble() >= 0.5) speedY *= -1;
        setSpeedXY(speedX, speedY);
    }

    @Override
    public void onActorOverlapped(Actor actor, HashSet<Actor> actors) {
        super.onActorOverlapped(actor, actors);
        for (Actor collided : actors) {
            if (collided instanceof Cytokine) {
                Cytokine cytokine = (Cytokine)collided;
                double refX = cytokine.getReferencedLocation().getKey();
                double refY = cytokine.getReferencedLocation().getValue();
                _timestampOfLastCytokineEncounter = System.currentTimeMillis();
                _activated = true;
                Vector3 refVec = new Vector3(refX, refY, 0.0);
                Vector3 locationVec = new Vector3(getLocationX(), getLocationY(), 0.0);
                refVec.subtractThis(locationVec);
                refVec.normalizeThis();
                setSpeedXY(_speed * refVec.x(), _speed * refVec.y());
                _changeSpeedImmediately = true;
            }
        }
    }
}
