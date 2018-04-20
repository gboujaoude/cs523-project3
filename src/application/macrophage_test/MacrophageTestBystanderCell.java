package application.macrophage_test;

import engine.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;

// :(
public class MacrophageTestBystanderCell extends Circle2D implements PulseEntity {
    private static final Color _healthyColor = new Color(102 / 255.0, 189 / 255.0, 255 / 255.0, 1);
    private static final Color _unhealthyColor = new Color(255 / 255.0, 110 / 255.0, 88 / 255.0, 1);
    private boolean _isInfected = false;
    private double _elapsedSec = 0.0;
    private final double _virusProductionRate = 3.0; // Create 1 virus every 2 seconds
    private final int _maxViruses = 10;
    private Virus _virus = null;
    private ArrayList<Virus> _viruses = new ArrayList<>();

    public MacrophageTestBystanderCell(double locationX, double locationY) {
        super(locationX, locationY, 45, 45, 1);
        setColor(_healthyColor);
        addToWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
    }

    public boolean infected() {
        return _isInfected;
    }

    public void infect(Virus virus) {
        virus.removeFromWorld();
        _isInfected = true;
        _virus = virus;
        setColor(_unhealthyColor);
    }

    @Override
    public void pulse(double deltaSeconds) {
        if (infected()) {
            _elapsedSec += deltaSeconds;
            if (_elapsedSec >= _virusProductionRate) {
                Virus newVirus = new Virus(_virus); // Make a copy of the virus that infected us
                _viruses.add(newVirus);
                _elapsedSec = 0.0;
                if (_viruses.size() >= _maxViruses) {
                    for (Virus virus : _viruses) virus.addToWorld();
                    Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
                    removeFromWorld();
                }
            }
        }
    }
}
