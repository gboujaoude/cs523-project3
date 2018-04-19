package application.macrophage_test;

import application.macrophage_test.BystanderCell;
import engine.*;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * The Macrophage eats things that are not on its whitelist. If it is overpowered, it will send out cytokines
 * out to recruit help from other cells.
 */
public class Macrophage extends Circle2D  implements PulseEntity {
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
    private List<String> _whitelist;
    private static Color _color = new Color(0 / 255.0, 167 / 255.0, 61 / 255.0, 1);

    public Macrophage(double locationX, double locationY) {
        super(locationX, locationY, 75, 75, 1);
        _initializeWhitelist();
        _changeDirection();
        setColor(_color);
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
    }

    // This will be called when we collide with any other entity at the same
    // depth in the world
    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) {
            if (actor instanceof BystanderCell) {
                if (!onWhitelist(actor)) {
                    actor.removeFromWorld();
                }
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

    private boolean onWhitelist(Actor actor) {
        BystanderCell cell = (BystanderCell) actor;
//        if (_whitelist.contains(cell.getInfectionString().orElse("xyz"))) {
        if (_whitelist.contains(cell.peekAtVirus())) {
            System.out.println("here.");
            return true;
        }
        System.out.println("not on whitelist.");
        return false;
    }

    private void _changeDirection() {
        int newDirection = _rng.nextInt(_directionX.length);
        setSpeedXY(_directionX[newDirection], _directionY[newDirection]);
    }

    private void _initializeWhitelist() {
        _whitelist = new ArrayList<>();
        _whitelist.add("abc");
    }

    private void _bounceDirection(Actor self, Actor other) {
        other.setSpeedXY(-other.getSpeedX(),-other.getSpeedY());
        self.setSpeedXY(-self.getSpeedX(),-self.getSpeedY());
    }
}
