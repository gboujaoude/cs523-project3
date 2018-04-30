package application.liver_idea_model;

import application.utils.Misc;
import engine.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Macrophage extends Circle2D implements PulseEntity{
    private static Color _color = new Color(0 / 255.0, 167 / 255.0, 61 / 255.0, 1);
    private static Random _rng = new Random();
    private static final double _radius = 30;
    private final int _perimRatio = 3;
    private final double _speed;
    private double _elapsedSec = 0.0;
    private double _elapsedSecBeforeMovementChange = 0.0;
    private MacrophagePerimeter _mp;
    private int _cytokineCounter = 0;
    private int _maxPouchSize;

    public Macrophage(double x, double y) {
        super(x, y, _radius, _radius, 1);
//        _changeDirection();
        _speed = Engine.getConsoleVariables().find(ModelGlobals.macrophageSpeed).getcvarAsFloat();
        setColor(_color);
        _mp = new MacrophagePerimeter(x-(_radius),y-(_radius),
                _radius*_perimRatio, _radius*_perimRatio,1);
        _mp.addToWorld();
        this.attachActor(_mp);
        _changeDirection();
        _maxPouchSize = Engine.getConsoleVariables().find(ModelGlobals.cytokinePouchSize).getcvarAsInt();
        _cytokineCounter = _maxPouchSize; // Start with a full pouch
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
    }

    @Override
    public void setWidthHeight(double radiusX, double radiusY) {
        super.setWidthHeight(radiusX, radiusY);
    }

    @Override
    public void onActorOverlapped(Actor actor, HashSet<Actor> actors) {
        int numViruses = 0;
        int numInfected = 0;
        for(Actor curr: actors) {
            // Remove infected cells and viruses
            if (curr instanceof LiverCell) {
                LiverCell cell = (LiverCell) curr;
                if (cell.infected()) {
                    cell.removeFromWorld();
                    ++numInfected;
                    //System.out.println("Macrophage: Found infected cell -> destroying");
                }
            } else if (curr instanceof Virus) {
                curr.removeFromWorld();
                ++numViruses;
                Engine.getMessagePump().sendMessage(new Message(ModelGlobals.virusSquashedMacrophage));
                //System.out.println("Macrophage: Found virus -> destroying");
            }
        }
        //if (numInfected > 0) System.out.println("Macrophage: Found [" + numInfected + "] infected cells -> destroying");
        //if (numViruses > 0) System.out.println("Macrophage: Found [" + numViruses + "] viruses -> destroying");
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
        // Something to move direction
        _elapsedSec += deltaSeconds;
        _elapsedSecBeforeMovementChange += deltaSeconds;

        if (_elapsedSecBeforeMovementChange >= 5.0) {
            _changeDirection();
            _elapsedSecBeforeMovementChange = 0.0;
        }

        // Refill pouch
        if (_elapsedSec >= 2 && _cytokineCounter < _maxPouchSize) {
            _cytokineCounter ++;
            _elapsedSec = 0;
        }

        // Check if we are overwhelmed, if so empty pouch
        if (_mp.isOverwhelmed() && _cytokineCounter >= _maxPouchSize) {
            _emptyPouch();
            _elapsedSec = 0;
        }

        // Make sure the macrophage does not go out of bounds
        double worldHeight = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsFloat();
        double locationY = getLocationY();
        double speedX = getSpeedX();
        double speedY = getSpeedY();
        if ((locationY <= 10 && speedY < 0.0) || (locationY > worldHeight * 0.6 && speedY > 0.0)) setSpeedXY(speedX, -speedY);
    }

    private void _emptyPouch() {
        _mp.resetPerimeter();
        ArrayList<Double> spacing = Misc.linearSpacing(getLocationX(),getLocationY(),_maxPouchSize);
        for(int i = 0; i < _maxPouchSize; i ++) {
            new Cytokine(getLocationX(),
                    Misc.offset(getLocationY(), 10, 20), getLocationX(), getLocationY(),
                    -1, true).addToWorld();
        }
        _mp.resetPerimeter();
        _cytokineCounter = 0;
    }

    private void _changeDirection() {
        final double minSpeed = _speed;
        double speedX = _speed * _rng.nextDouble() + minSpeed;
        double speedY = _speed * _rng.nextDouble() + minSpeed;
        if (_rng.nextDouble() >= 0.5) speedX *= -1;
        if (_rng.nextDouble() >= 0.5) speedY *= -1;
        setSpeedXY(speedX, speedY);
    }
}
