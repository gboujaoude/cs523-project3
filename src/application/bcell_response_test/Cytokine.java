package application.bcell_response_test;

import engine.Circle2D;
import javafx.scene.paint.Color;

import java.util.Random;

public class Cytokine extends Circle2D {
    private static final Color color = new Color(93 / 255.0, 255 / 255.0, 131 / 255.0, 1);

    public Cytokine(double locationX, double locationY) {
        super(locationX, locationY, 5, 5, 1);
        Random rng = new Random();
        setSpeedXY(rng.nextDouble() * 100, rng.nextDouble() * 100);
        setColor(color);
    }
}
