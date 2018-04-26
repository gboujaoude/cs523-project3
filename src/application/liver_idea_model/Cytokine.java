package application.liver_idea_model;

import application.utils.Misc;
import engine.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.HashSet;
import java.util.Random;

public class Cytokine extends Circle2D implements PulseEntity {
    private static final Color _color = new Color(255 / 255.0, 186 / 255.0, 55 / 255.0, 1.0);
    private static final Random _rng = new Random();
    private double _elapsedSeconds = 0.0;
    private final double _secondsUntilDuplication;
    private final boolean _isMainCytokine;
    private final Pair<Double, Double> _startLocationXY;
    private final Pair<Double, Double> _referencedLocation;

    /**
     * @param x starting x location
     * @param y starting y location
     * @param isMainCytokine if true, this cytokine will replicate itself
     */
    public Cytokine(double x, double y, double referenceX, double referenceY, boolean isMainCytokine) {
        super(x, y, 5, 5, 1);
        setColor(_color);
        _isMainCytokine = isMainCytokine;
        _secondsUntilDuplication = Engine.getConsoleVariables().find(ModelGlobals.cytokineSecondsUntilDuplication).getcvarAsFloat();
        // If we are not the main cytokine, use referenceX and referenceY for the reference locations,
        // but otherwise use our starting x and y locations
        if (!_isMainCytokine) _referencedLocation = new Pair<>(referenceX, referenceY);
        else {
            _referencedLocation = new Pair<>(x, y);
            // Only set the speed of this cytokine if we are the main cytokine
            final double speed = Engine.getConsoleVariables().find(ModelGlobals.cytokineSpeed).getcvarAsFloat();
            setSpeedXY(speed * _rng.nextDouble(), speed * _rng.nextDouble());
        }
        _startLocationXY = new Pair<>(x, y);
    }

    public Pair<Double, Double> getReferencedLocation() {
        return _referencedLocation;
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
        _elapsedSeconds += deltaSeconds;
        if (_elapsedSeconds >= _secondsUntilDuplication) {
            _elapsedSeconds = 0.0;
            Cytokine cytokine = new Cytokine(getLocationX(), getLocationY(),
                    _startLocationXY.getKey(), _startLocationXY.getValue(), false);
            cytokine.addToWorld();
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
        // Only add this as a pulse entity if it is a primary cytokine
        if (_isMainCytokine) {
            Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
        }
    }

    /**
     * After calling this the entity will no longer be drawn and its update function will
     * not be called
     */
    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        if (_isMainCytokine) {
            Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
        }
    }
}
