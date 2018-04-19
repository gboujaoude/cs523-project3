package application.cytokine_test;


import engine.*;
import javafx.scene.paint.Color;

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
    private double _maxSecBeforeMovementChange = 5.0; // Change direction every x seconds
    private double _keepGoingInSameDirectionProb = 0.5;

    public BCell(double locationX, double locationY) {
        super(locationX, locationY, 75, 75, 1);
        _changeDirection();
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
    }

    // This will be called when we collide with any other entity at the same
    // depth in the world
    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) {
            if (actor instanceof Cytokine) {
                System.out.println("BCell intercepted Cytokine - analyzing");
                actor.removeFromWorld();
            }
        }
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
    }

    private void _changeDirection() {
        int newDirection = _rng.nextInt(_directionX.length);
        setSpeedXY(_directionX[newDirection], _directionY[newDirection]);
    }
}
