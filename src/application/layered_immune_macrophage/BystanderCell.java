package application.layered_immune_macrophage;

import engine.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BystanderCell extends Circle2D implements PulseEntity {
    private static final Color _healthyColor = new Color(102 / 255.0, 189 / 255.0, 255 / 255.0, 1);
    private static final Color _unhealthyColor = new Color(255 / 255.0, 110 / 255.0, 88 / 255.0, 1);
    private static Text2D _text = null;
    private static AtomicInteger _numHealthyCells = new AtomicInteger(0);
    private static AtomicInteger _numInfectedCells = new AtomicInteger(0);
    private boolean _isInfected = false;
    private volatile boolean _added = false;
    private double _elapsedSec = 0.0;
    private final double _virusCreationRate = 2.0; // new virus every x seconds
    private final int _maxViruses = 10;
    private Virus _virus = null;
    private ArrayList<Virus> _viruses = new ArrayList<>(_maxViruses);

    public BystanderCell(double x, double y) {
        super(x, y, 50, 50, 1);
        if (_text == null) {
            _text = new Text2D("Healthy/Infected Cells: " + _numHealthyCells + "/" + _numInfectedCells, 25, 150, 500, 50, 0);
            _text.setColor(Color.BLUEVIOLET);
            _text.setAsStaticActor(true);
            _text.addToWorld();
        }
        setColor(_healthyColor);
    }

    public boolean infected() {
        return _isInfected;
    }

    public void infect(Virus virus) {
        _isInfected = true;
        _virus = virus;
        virus.removeFromWorld();
        setColor(_unhealthyColor);
        _numHealthyCells.getAndDecrement();
        _numInfectedCells.getAndIncrement();
        _text.setText("Healthy/Infected Cells: " + _numHealthyCells + "/" + _numInfectedCells);
    }

    public void selfDestruct() {
        removeFromWorld();
    }

    @Override
    public void addToWorld() {
        if (_added) return;
        super.addToWorld();
        _added = true;
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
        if (infected()) _numInfectedCells.getAndIncrement();
        else _numHealthyCells.getAndIncrement();
        _text.setText("Healthy/Infected Cells: " + _numHealthyCells + "/" + _numInfectedCells);
    }

    @Override
    public void removeFromWorld() {
        if (!_added) return;
        super.removeFromWorld();
        _added = false;
        Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
        if (infected()) _numInfectedCells.getAndDecrement();
        else _numHealthyCells.getAndDecrement();
        _text.setText("Healthy/Infected Cells: " + _numHealthyCells + "/" + _numInfectedCells);
    }

    @Override
    public void pulse(double deltaSeconds) {
        if (infected()) {
            _elapsedSec += deltaSeconds;
            if (_elapsedSec >= _virusCreationRate) {
                _viruses.add(new Virus(_virus));
                if (_viruses.size() >= _maxViruses) {
                    for (Virus virus : _viruses) virus.addToWorld();
                    selfDestruct();
                }
                _elapsedSec = 0.0;
            }
        }
    }
}
