package application.liver_idea_model;

import application.utils.Misc;
import engine.*;
import javafx.scene.paint.Color;

import java.util.HashSet;

/**
 * LiverCell == Hepatocyte
 */
public class LiverCell extends Circle2D implements PulseEntity {
    private static final Color _healthyColor = new Color(102 / 255.0, 189 / 255.0, 255 / 255.0, 1);
    private static final Color _unhealthyColor = new Color(255 / 255.0, 110 / 255.0, 88 / 255.0, 1);
    //private HashSet<Virus> _viruses = new HashSet<>(10);
    private final int _maxNumViruses;
    private final double _virusesPerSecond;
    private double _elapsedSec = 0.0;
    private boolean _infected = false;
    private Virus _infectedWith = null;
    private double _currentNumViruses = 0;

    public LiverCell(double x, double y) {
        super(x, y, 50, 50, 1);
        _maxNumViruses = Engine.getConsoleVariables().find(ModelGlobals.virusesBeforeExplosion).getcvarAsInt();
        _virusesPerSecond = Engine.getConsoleVariables().find(ModelGlobals.virusPerSecond).getcvarAsFloat();
        setColor(_healthyColor);
    }

    boolean infected() {
        return _infected;
    }

    void infect(Virus virus) {
        if (infected()) return; // Do not infect twice
        _infectedWith = virus;
        _infected = true;
        setColor(_unhealthyColor); // Visual marker of infection
    }

    @Override
    public void onActorOverlapped(Actor actor, HashSet<Actor> actors) {
        super.onActorOverlapped(actor, actors);
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
        if (infected()) {
            _elapsedSec += deltaSeconds;
            if (_elapsedSec >= 1.0) {
                _currentNumViruses += _virusesPerSecond;
                _elapsedSec = 0.0;
            }
            int viruses = (int)_currentNumViruses;
            if (viruses >= _maxNumViruses) {
                removeFromWorld(); // Remove self from world since we just exploded
                for (int i = 0; i < viruses; ++i) {
                    Virus virus = new Virus(Misc.offset(getLocationX(), 0, 10),
                            Misc.offset(getLocationY(), 0, 10));
                    virus.addToWorld();
                }
            }
        }
    }

    /**
     * This function ensures that the render entity is added to the world. After
     * calling this it will be regularly called by the Engine and its movement
     * will be calculated by the Renderer and it will be drawn on the screen.
     */
    @Override
    public void addToWorld() {
        super.addToWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
        Engine.getMessagePump().sendMessage(new Message(ModelGlobals.cellAddedToWorld));
    }

    /**
     * After calling this the entity will no longer be drawn and its update function will
     * not be called
     */
    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        Engine.getMessagePump().sendMessage(new Message(Constants.REMOVE_PULSE_ENTITY, this));
        Engine.getMessagePump().sendMessage(new Message(ModelGlobals.cellRemovedFromWorld));
    }
}
