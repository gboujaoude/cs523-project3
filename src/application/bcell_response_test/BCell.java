package application.bcell_response_test;


import engine.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class BCell extends Circle2D implements PulseEntity {
    private static final Color _color = new Color(98 / 255.0, 150 / 255.0, 255 / 255.0, 1);
    private static final double _speed = 15.0;
    private static final double[] _directionX = new double[] {
            0.0, // up
            -_speed, // left
            _speed, // right
            0.0 // down
    };
    private static final double[] _directionY = new double[] {
            -_speed, // up
            0.0, // left
            0.0, // right
            _speed, // down
    };
    private static final Random _rng = new Random();
    private double _elapsedSec = 0.0;
    private double _elapsedAntibodyTimer = 0.0;
    private double _maxSecBeforeMovementChange = 5.0; // Change direction every x seconds
    private double _keepGoingInSameDirectionProb = 0.5;
    private double _antibodyRate = 1.0; // 100% chance that will be making antibodies
    private double _probMatch = 2;
    private int _antibodiesLimit = 5;
    private int _memoryCells = 3;
    private boolean _isNaive = true; // true means it hasn't made any type of antibody yet
    private boolean _isActive = false;
    private ArrayList<Antibody> _antibodies = new ArrayList<>();

    public BCell(double locationX, double locationY) {
        this(locationX,locationY,true);
    }

    public BCell(double locationX, double locationY, boolean _isNaive) {
        super(locationX, locationY, 75, 75, 1);
        _changeDirection();
        setColor(_color);
        this._isNaive = _isNaive;
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
    }

    private void setActive() {
        _isNaive = false;
        _isActive = true;
    }

    private void releaseAntibodies() {
        for (Antibody ab : _antibodies) {
            ab.addToWorld();
        }
        produceMemoryCells();
        removeFromWorld();
    }

    /**
     * B cells produce only one kind of antibody. In this case we simulate this match
     * by rolling a dice.
     * @return
     */
    private boolean matchesPeptides() {
        if(_rng.nextDouble() < _probMatch) return true;
        return false;
    }

    // This will be called when we collide with any other entity at the same
    // depth in the world
    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) {
            if (actor instanceof Cytokine) {

                // Figure out if this should start producing antibodies
                if (_isNaive) {
                    setActive();
                } else if (matchesPeptides()) {
                    _isActive = true;
                }

                actor.removeFromWorld();
            }
        }
    }

    private void produceMemoryCells() {
        for (int i = 0; i < _memoryCells; i ++) {
            new BCell(this.getLocationX() + (i*40),this.getLocationY(),false).addToWorld();
        }
        System.out.println("B Cell reproducing");
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
    }

    @Override
    public void pulse(double deltaSeconds) {
        _elapsedSec += deltaSeconds;
        // See if it's time to change direction
        if (_elapsedSec >= _maxSecBeforeMovementChange) {
            double chanceToChangeDir = _rng.nextDouble();
            if (chanceToChangeDir > _keepGoingInSameDirectionProb) _changeDirection();
            _elapsedSec = 0.0; // Reset the timer
        }

        if (!_isNaive) {
            _elapsedAntibodyTimer += deltaSeconds;
            if (_antibodies.size() < _antibodiesLimit) {
                if (_elapsedAntibodyTimer >= _antibodyRate) {
                    _antibodies.add(new Antibody(this.getLocationX(), this.getLocationY(), 5, 5, 1));
                    _elapsedAntibodyTimer = 0.0;
                }
            } else if (_isActive) {
                releaseAntibodies();
            }
        }
    }

    private void _changeDirection() {
        int newDirection = _rng.nextInt(_directionX.length);
        setSpeedXY(_directionX[newDirection], _directionY[newDirection]);
    }
}

