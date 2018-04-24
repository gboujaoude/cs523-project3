package application.layered_immune_macrophage;

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
    private static final int _radius = 30;
    private static final int _perimRatio = 3;
    private double _maxSecBeforeMovementChange = 5.0; // Change direction every x seconds
    private double _keepGoingInSameDirectionProb = 0.5;
    private List<String> _whitelist;
    private static Color _color = new Color(0 / 255.0, 167 / 255.0, 61 / 255.0, 1);
    private int _numVirusesEaten = 0;
    private CytokinePouch _pouch = new CytokinePouch();
    private MacrophagePerimeter mp;

    public Macrophage(double locationX, double locationY) {
        super(locationX, locationY, _radius, _radius, 1);
        _initializeWhitelist();
        _changeDirection();
        setColor(_color);
        mp = new MacrophagePerimeter(locationX-(_radius),locationY-(_radius),
                _radius*_perimRatio, _radius*_perimRatio,1);
        this.attachActor(mp);
        mp.addToWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
    }

    // This will be called when we collide with any other entity at the same
    // depth in the world
    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) {
            if (actor instanceof BystanderCell) {
                if (!onWhitelist(actor)) {
                    System.out.println("Found infected cell -> destroying");
                    ((BystanderCell) actor).selfDestruct();
                    ++_numVirusesEaten;
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
        _refillPouch(deltaSeconds);

        if (isOverwhelmed() && _pouch._pouchFull()) {
            _pouch._emptyPouch(getLocationX(),getLocationY(),getLocationX(),getLocationY());
            _numVirusesEaten = 0;
        }
        // See if it's time to change direction
        if (_elapsedSec >= _maxSecBeforeMovementChange) {
            double chanceToChangeDir = _rng.nextDouble();
            //if (chanceToChangeDir > _keepGoingInSameDirectionProb)
            _changeDirection();
            _elapsedSec = 0.0; // Reset the timer
        }
    }

    private void _refillPouch(double deltaSeconds) {
        _pouch._refillPouch(deltaSeconds);
    }

    private boolean isOverwhelmed() {
        return mp.isOverwhelmed();
    }

    private boolean onWhitelist(Actor actor) {
        BystanderCell cell = (BystanderCell) actor;
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

    private class CytokinePouch {
        private double _elapsedTime = 0;
        private int _maxPouchSize = 10;
        private double _offsetMin = 10;
        private double _offsetMax = 20;
        private final Random _rng = new Random();
        private ArrayList<TCytokine> cytokinePouch = new ArrayList<>(_maxPouchSize);

        public CytokinePouch() {
            for(int i = 0; i<_maxPouchSize; i++) {
                cytokinePouch.add(new TCytokine(0,0,0,0));
            }
        }

        public void _refillPouch(double deltaTime) {
            _elapsedTime += deltaTime;
            if (_elapsedTime > 2) {
                System.out.println("new tcytokine");
                cytokinePouch.add(new TCytokine(0,0,0,0));
                _elapsedTime = 0.0;
            }
        }

        public void _emptyPouch(double x, double y, double attackLocX, double attackLocY) {
            for (TCytokine tCytokine : cytokinePouch) {
                tCytokine.setStartLocation(offset(x),offset(y));
                tCytokine.setAttackLocation(attackLocX,attackLocY);
                tCytokine.addToWorld();
                tCytokine.setSpeedXY(-2,50);
            }
            cytokinePouch.clear();
            mp.resetPerimeter();
        }

        public boolean _pouchFull() {
            if (cytokinePouch.size() >= _maxPouchSize) {
                return true;
            } else {
                return false;
            }
        }

        private double offset(double a) {
            int sign;
            if (_rng.nextDouble() > 0.5) {
                sign = 1;
            } else {
                sign = -1;
            }
            return a + sign * (_offsetMin + (_offsetMax- _offsetMin) * _rng.nextDouble());
        }
    }
}
