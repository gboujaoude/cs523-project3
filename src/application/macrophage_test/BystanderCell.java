package application.macrophage_test;

import application.macrophage_test.Virus;
import engine.Actor;
import engine.Circle2D;
import engine.PulseEntity;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;

public class BystanderCell extends Circle2D implements PulseEntity {
    private static final Color color = new Color(225 / 255.0, 179 / 255.0, 131 / 255.0, 1);
    private boolean _isInfected;
    private String _infectionString = "abc";
    private Virus _virus;

    public BystanderCell(double locationX, double locationY) {
        super(locationX, locationY, 5, 5, 1);
        Random rng = new Random();
        setSpeedXY(rng.nextDouble() * 100, rng.nextDouble() * 100);
        setColor(color);
        _isInfected = false;
    }

    public boolean isInfected() {
        return _isInfected;
    }

    public void setInfected(Virus virus) {
        _isInfected = true;
        _infectionString = "abc";
        _virus = virus;
    }

    public Optional<Virus> peekAtVirus() {
        return Optional.ofNullable(_virus);
    }

    public Optional<String> getInfectionString() {
        return Optional.ofNullable(_infectionString);
    }

    // This will be called when we collide with any other entity at the same
    // depth in the world
    @Override
    public void onActorOverlapped(Actor self, HashSet<Actor> collidedWith) {
        for (Actor actor : collidedWith) {
            if (actor instanceof Virus) {
            }
        }
    }

    /**
     * Called each time the simulation.engine updates.
     *
     * @param deltaSeconds Change in seconds since the last update.
     *                     If the simulation.engine is running at 60 frames per second,
     *                     this value will be roughly equal to (1/60).
     */
    @Override
    public void pulse(double deltaSeconds) {

    }
}
