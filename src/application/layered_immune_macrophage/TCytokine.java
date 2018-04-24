package application.layered_immune_macrophage;

import engine.Circle2D;
import javafx.scene.paint.Color;

import java.util.Random;

// Cytokine for T-Cells
public class TCytokine extends Circle2D {
    private static final Color _color = new Color(255 / 255.0, 186 / 255.0, 55 / 255.0, 1.0);
    private CytokineData _data = new CytokineData();

    public TCytokine(double x, double y, double attackLocationX, double attackLocationY) {
        super(x, y, 5, 5, 1);
        Random rng = new Random();
        setColor(_color);
        final double speed = 350;
        setSpeedXY(rng.nextDouble() * 75, speed);
        _data.locationX = attackLocationX;
        _data.locationY = attackLocationY;
    }

    public CytokineData getData() {
        return _data;
    }

    public void setAttackLocation(double x, double y) {
        _data.locationX = x;
        _data.locationY = y;
    }

    public void setStartLocation(double x, double y) {
        setLocationXYDepth(x,y,1);
    }
}
