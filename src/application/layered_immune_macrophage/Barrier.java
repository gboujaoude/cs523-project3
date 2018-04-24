package application.layered_immune_macrophage;

import application.quadrants_test.Quadrant;
import application.quadrants_test.QuadrantBuilder;
import engine.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

// Kills anything it collides with
public class Barrier extends Rectangle2D implements PulseEntity {
    private int _numTCellsNeeded = 0;
    private final double _secondsPerTCell = 0.5;
    private double _elapsedSeconds = 0.0;
    private LinkedList<TCytokine> _cytokines = new LinkedList<>();
    private Quadrant _quadrant = QuadrantBuilder.makeQuadrant(60, 70);

    public Barrier(double x, double y, double width, double height, double depth) {
        super(x, y, width, height, depth);
        setColor(new Color(0, 0, 0, 0));
    }

    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) {
            actor.removeFromWorld();
            if (actor instanceof TCytokine) {
                ++_numTCellsNeeded;
                _cytokines.add((TCytokine)actor);
            }
        }
    }

    @Override
    public void pulse(double deltaSeconds) {
        _elapsedSeconds += deltaSeconds;
        if (_elapsedSeconds >= _secondsPerTCell && _numTCellsNeeded > 0) {
            --_numTCellsNeeded;
            _elapsedSeconds = 0.0;
            TCell cell = new TCell(_quadrant.getRandomPosition().getX(), _quadrant.getRandomPosition().getY());
            cell.addToWorld();
            // Add a cytokine right on top of the TCell so that it immediately gets activated
            TCytokine cytokine = _cytokines.poll();
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
