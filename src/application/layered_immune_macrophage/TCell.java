package application.layered_immune_macrophage;

import application.quadrants_test.Quadrant;
import application.quadrants_test.QuadrantBuilder;
import engine.*;
import engine.math.Vector3;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TCell extends Circle2D implements PulseEntity {
    private static final Color _color = new Color(255 / 255.0, 173 / 255.0, 31 / 255.0, 1);
    private static Text2D _text = null;
    private static AtomicInteger _numTCells = new AtomicInteger(0);
    private double _targetX = 0.0;
    private double _targetY = 0.0;
    private double _elapsedSec = 0.0;
    private double _elapsedSecMovement = 0.0;
    private final double _secBeforeMovementChangeWhileActive = 5.0;
    private double _timeBeforeDeactivation = 60.0;
    private boolean _movingToWarzone = false;
    private boolean _movingToHome = false;
    private boolean _active = false;
    private boolean _killedPathogen = false;
    private boolean _lifespanCounterActive = false;
    private double _elapsedLifespanSec = 0.0;
    private final double _lifespanSec = 120;
    private Random _rng = new Random();
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

    public TCell(double x, double y) {
        super(x, y, 30, 30, 1);
        if (_text == null) {
            _text = new Text2D("TCells: " + _numTCells.get(), 25, 100, 350, 50, 0);
            _text.setColor(_color);
            _text.setAsStaticActor(true);
            _text.addToWorld();
        }
        setColor(_color);
        setSpeedXY(_rng.nextDouble() * 75, 0);
    }

    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) {
            if (actor instanceof TCytokine && !_movingToWarzone) {
                TCytokine cytokine = (TCytokine)actor;
                CytokineData data = cytokine.getData();
                cytokine.removeFromWorld();
                _targetX = data.locationX;
                _targetY = data.locationY;
                int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsInt();
                _targetY /= (double)worldWidth; // Normalize targetY so it works with QuadrantBuilder
                _targetY *= 100; // Convert to a percentage
                final double speed = 350.0;
                Quadrant quadrant = QuadrantBuilder.makeQuadrant(_targetY - 1, _targetY + 1);
                _targetX = quadrant.getRandomPosition().getX();
                _targetY = quadrant.getRandomPosition().getY();
                Vector3 speedVec = new Vector3(_targetX - getLocationX(), _targetY - getLocationY(), 0.0);
                speedVec.normalizeThis();
                setSpeedXY(speedVec.x() * speed, speedVec.y() * speed);
                _movingToWarzone = true;
                _active = true;
                _lifespanCounterActive = true; // TCell will eventually die
                _elapsedSec = 0.0;
                _killedPathogen = false;
                System.out.println("TCell activated -> moving to war zone");
            }
            else if (actor instanceof Virus && _active) {
                actor.removeFromWorld();
                System.out.println("TCell discovered virus -> destroying");
                _killedPathogen = true;
                _elapsedLifespanSec = 0.0;
            }
            else if (actor instanceof BystanderCell && _active) {
                BystanderCell cell = (BystanderCell)actor;
                if (cell.infected()) {
                    System.out.println("TCell discovered infected cell -> destroying");
                    cell.selfDestruct();
                    _killedPathogen = true;
                    _elapsedLifespanSec = 0.0;
                }
            }
        }
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
        _numTCells.getAndIncrement();
        _text.setText("TCells: " + _numTCells);
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
        _numTCells.getAndDecrement();
        _text.setText("TCells: " + _numTCells);
    }

    @Override
    public void pulse(double deltaSeconds) {
        if (_lifespanCounterActive) {
            _elapsedLifespanSec += deltaSeconds;
            if (_elapsedLifespanSec >= _lifespanSec) removeFromWorld(); // TCell's time ran out and it died
        }
        if (Math.abs(getLocationX() - _targetX) < 10 && Math.abs(getLocationY() - _targetY) < 10 && _movingToWarzone) {
            _movingToWarzone = false;
            setSpeedXY(0.0, 0.0);
        }
        else if (Math.abs(getLocationX() - _targetX) < 10 && Math.abs(getLocationY() - _targetY) < 10 && _movingToHome) {
            _movingToHome = false;
            setSpeedXY(0.0, 0.0);
        }
        else if (_active && !_movingToWarzone && !_movingToHome) {
            _elapsedSec += deltaSeconds;
            _elapsedSecMovement += deltaSeconds;
            // Change direction
            if (_elapsedSecMovement >= _secBeforeMovementChangeWhileActive) {
                _elapsedSecMovement = 0.0;
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
            if (_killedPathogen) {
                _killedPathogen = false;
                _elapsedSec = 0.0;
            }
            else if (_elapsedSec >= _timeBeforeDeactivation) {
                System.out.println("TCell deactivating -> moving back");
                _active = false;
                _movingToWarzone = false;
                _movingToHome = true;
                Quadrant quadrant = QuadrantBuilder.makeQuadrant(60, 70);
                _targetX = quadrant.getRandomPosition().getX();
                _targetY = quadrant.getRandomPosition().getY();
                final double speed = 350.0;
                Vector3 speedVec = new Vector3(_targetX - getLocationX(), _targetY - getLocationY(), 0.0);
                speedVec.normalizeThis();
                setSpeedXY(speedVec.x() * speed, speedVec.y() * speed);
            }
        }
    }
}
