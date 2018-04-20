package application.quadrants_test;

import engine.Constants;
import engine.Engine;

public class QuadrantBuilder {

    public static Quadrant makeQuadrant(double minPercentile, double maxPercentile) {
        double width = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsFloat();
        double height = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsFloat();

        double xLeft = 0;
        double xRight = width;

        double yTop = height/100 * minPercentile;
        double yBottom = height/100 * maxPercentile;

        return new Quadrant(xLeft,xRight,yTop,yBottom);
    }
}
