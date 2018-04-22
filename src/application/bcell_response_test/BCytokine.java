package application.bcell_response_test;

import application.layered_immune_sys_test.CytokineData;
import engine.Circle2D;
import engine.Constants;
import engine.Engine;
import engine.PulseEntity;
import javafx.scene.paint.Color;

import java.util.Random;

// Cytokine for T-Cells
public class BCytokine extends Circle2D implements PulseEntity {
    private static final Color _color = new Color(255 / 255.0, 186 / 255.0, 55 / 255.0, 1.0);
    private CytokineData _data = new CytokineData();

    public BCytokine(double x, double y, double attackLocationX, double attackLocationY) {
        super(x, y, 5, 5, 1);
        Random rng = new Random();
        setColor(_color);
        final double speed = 175;
        setSpeedXY(rng.nextDouble() * 75, speed);
        _data.locationX = attackLocationX;
        _data.locationY = attackLocationY;
    }

    public CytokineData getData() {
        return _data;
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
        if (this.getLocationY() > Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsFloat()) {
            this.removeFromWorld();
        }
    }
}
