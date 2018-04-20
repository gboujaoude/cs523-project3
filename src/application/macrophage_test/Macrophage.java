package application.macrophage_test;

import application.macrophage_test.MacrophageTestBystanderCell;
import engine.*;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * The Macrophage eats things that are not on its whitelist. If it is overpowered, it will send out cytokines
 * out to recruit help from other cells.
 */
public class Macrophage extends Circle2D  implements PulseEntity {
    private static final double _speed = 75;
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
    private int _numVirusesEaten = 0;

    public Macrophage(double locationX, double locationY) {
        super(locationX, locationY, 25, 25, 1);
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
            if (actor instanceof MacrophageTestBystanderCell) {
                if (!onWhitelist(actor)) {
                    System.out.println("Found infected cell -> destroying");
                    actor.removeFromWorld();
                }
            }
            else if (actor instanceof Virus) {
                ++_numVirusesEaten;
                System.out.println("Found virus -> eating (eaten [" + _numVirusesEaten + "] viruses total)");
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

    private boolean onWhitelist(Actor actor) {
        MacrophageTestBystanderCell cell = (MacrophageTestBystanderCell) actor;
        return !cell.infected();
    }

    private void _changeDirection() {
        int newDirectionX = _rng.nextInt(_directionX.length);
        int newDirectionY = _rng.nextInt(_directionY.length);
        double speedX = _directionX[newDirectionX];
        double speedY = _directionY[newDirectionY];
        while (speedX == 0 && speedY == 0) {
            newDirectionX = _rng.nextInt(_directionX.length);
            newDirectionY = _rng.nextInt(_directionY.length);
            speedX = _directionX[newDirectionX];
            speedY = _directionY[newDirectionY];
        }
        setSpeedXY(_directionX[newDirectionX], _directionY[newDirectionY]);
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
