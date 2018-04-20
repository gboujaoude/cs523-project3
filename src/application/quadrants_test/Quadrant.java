package application.quadrants_test;

import java.util.Random;

public class Quadrant {

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private Random rand;

    public Quadrant(double minX, double maxX, double minY, double maxY) {
        this(minX,maxX,minY,maxY,new Random());
    }

    public Quadrant(double minX, double maxX, double minY, double maxY, Random rand) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.rand = rand;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public Position getRandomPosition() {
        return new Position(minX + (maxX- minX) * rand.nextDouble(),minY + (maxY- minY) * rand.nextDouble());
    }
}
