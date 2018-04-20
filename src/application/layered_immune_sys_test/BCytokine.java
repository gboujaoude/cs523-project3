package application.layered_immune_sys_test;

import engine.Circle2D;
import javafx.scene.paint.Color;

import java.util.Random;

// Cytokine for B-Cells
public class BCytokine extends Circle2D {
    private static final Color _color = new Color(255 / 255.0, 186 / 255.0, 55 / 255.0, 1.0);
    private CytokineData _data = new CytokineData();

    public BCytokine(double x, double y, double attackLocationX, double attackLocationY) {
        super(x, y, 5, 5, 1);
        Random rng = new Random();
        setColor(_color);
        final double speed = 100;
        setSpeedXY(rng.nextDouble() * speed, -rng.nextDouble() * speed);
        _data.locationX = attackLocationX;
        _data.locationY = attackLocationY;
    }

    public CytokineData getData() {
        return _data;
    }
}
