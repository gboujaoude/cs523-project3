package application.bcell_response_test;

import engine.*;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;


public class BystanderCell extends Circle2D implements PulseEntity {
    private static final Color _healthyColor = new Color(102 / 255.0, 189 / 255.0, 255 / 255.0, 1);
    private static final Color _unhealthyColor = new Color(255 / 255.0, 110 / 255.0, 88 / 255.0, 1);
    private boolean _isInfected = true;
    private double _elapsedSec = 0.0;
    private final double _virusCreationRate = 2.0; // new virus every x seconds
    private final int _maxViruses = 10;
    private final Random _rng;
    private final double _probToCatch = 1.0;

    public BystanderCell(double x, double y) {
        super(x, y, 50, 50, 1);
        setColor(_unhealthyColor);
        _rng = new Random();
    }

    public boolean infected() {
        return _isInfected;
    }

    public void selfDestruct() {
        removeFromWorld();
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

    @Override
    public void pulse(double deltaSeconds) {

    }

    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor curr: collidedWith) {
            // Simulate chance that the antibody binds with the antigen
            if (curr instanceof  Antibody) {
                Antibody ab = (Antibody) curr;
                if (_rng.nextDouble() < _probToCatch) {
                    attachActor(ab);
                }
            }
        }
    }
}
