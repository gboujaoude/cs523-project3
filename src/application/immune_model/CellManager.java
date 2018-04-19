package application.immune_model;

import engine.Engine;
import engine.PulseEntity;
import engine.Rectangle2D;
import javafx.scene.paint.Color;

public class CellManager implements PulseEntity {

    Rectangle2D bystanderCells = new Rectangle2D(3,3,20,20,1);

    public void CellManager() {
        bystanderCells.setColor(Color.BLUE);
        bystanderCells.setSpeedXY(40,40);
        bystanderCells.addToWorld();
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
        bystanderCells.setColor(Color.BLUE);
        bystanderCells.setSpeedXY(100,100);
        bystanderCells.addToWorld();
    }
}
