package application.layered_immune_macrophage;

import engine.*;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Virus extends Circle2D {
    private static final Color _color = new Color(255 / 255.0, 73 / 255.0, 61 / 255.0, 1.0);
    private static Text2D _text = null;
    private static AtomicInteger _numViruses = new AtomicInteger(0);
    private volatile boolean _added = false;

    public Virus(double x, double y) {
        super(x, y, 5, 5, 1);
        if (_text == null) {
            _text = new Text2D("Viruses: " + _numViruses, 25, 50, 350, 50, 0);
            _text.setAsStaticActor(true);
            _text.addToWorld();
        }
        setColor(_color);
        Random rng = new Random();
        final double speed = 100;
        setSpeedXY(rng.nextDouble() * speed, rng.nextDouble() * speed);
    }

    public Virus(Virus other) {
        super(other.getLocationX(), other.getLocationY(), other.getWidth(), other.getHeight(), other.getDepth());
        setColor(_color);
        Random rng = new Random();
        final double speed = 100;
        setSpeedXY(rng.nextDouble() * speed, rng.nextDouble() * speed);
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        if (_added) return;
        _added = true;
        _numViruses.getAndIncrement();
        _text.setText("Viruses: " + _numViruses.get());
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        if (!_added) return;
        _added = false;
        _numViruses.getAndDecrement();
        _text.setText("Viruses: " + _numViruses.get());
    }

    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) {
            if (actor instanceof BystanderCell) {
                BystanderCell cell = (BystanderCell)actor;
                if (cell.infected()) continue;
                cell.infect(this);
                break;
            }
        }
    }
}
