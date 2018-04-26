package application.liver_idea_model;

import application.quadrants_test.Quadrant;
import application.quadrants_test.QuadrantBuilder;
import engine.*;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.LinkedList;

// Kills anything it collides with
public class Barrier extends Rectangle2D implements PulseEntity {
    private int _numTCellsNeeded = 0;
    private final double _secondsPerTCell;
    private double _elapsedSeconds = 0.0;
    private final double _lymphocytesPerSecond;
    private LinkedList<Cytokine> _cytokines = new LinkedList<>();
    private Quadrant _quadrant = QuadrantBuilder.makeQuadrant(60, 70);

    public Barrier(double x, double y, double width, double height) {
        super(x, y, width, height, 1);
        setColor(new Color(0, 0, 0, 0));
        _lymphocytesPerSecond = Engine.getConsoleVariables().find(ModelGlobals.lymphocytePerSecond).getcvarAsInt();
        _secondsPerTCell = 1 / _lymphocytesPerSecond;
    }

    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) {
            actor.removeFromWorld();
            if (actor instanceof Cytokine) {
                ++_numTCellsNeeded;
                _cytokines.add((Cytokine) actor);
            }
        }
    }

    @Override
    public void pulse(double deltaSeconds) {
        _elapsedSeconds += deltaSeconds;
        if (_elapsedSeconds >= _secondsPerTCell && _numTCellsNeeded > 0) {
            --_numTCellsNeeded;
            _elapsedSeconds = 0.0;
            Lymphocyte cell = new Lymphocyte(_quadrant.getRandomPosition().getX(), _quadrant.getRandomPosition().getY());
            cell.addToWorld();
            // Add a cytokine right on top of the TCell so that it immediately gets activated
            Cytokine cytokine = _cytokines.poll();
            cytokine.setLocationXYDepth(cell.getLocationX(), cell.getLocationY(), cell.getDepth());
            cytokine.addToWorld();
        }
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
    }
}
