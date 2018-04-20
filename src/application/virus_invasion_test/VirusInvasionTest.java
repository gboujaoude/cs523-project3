package application.virus_invasion_test;

import application.CameraController;
import application.quadrants_test.Quadrant;
import application.quadrants_test.QuadrantBuilder;
import engine.ApplicationEntryPoint;
import engine.Constants;
import engine.Engine;
import engine.EngineLoop;

import java.util.Random;

// Here we go
public class VirusInvasionTest implements ApplicationEntryPoint {
    @Override
    public void init() {
        new CameraController().enableMouseInputComponent();
        new Virus(0, 0).addToWorld();
        int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
        int worldHeight = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsInt();
        Random rng = new Random();
        Quadrant quadrant = QuadrantBuilder.makeQuadrant(0,10);
        for (int i = 0; i < 300; ++i) {
            new SittingDuckCell(quadrant.getRandomPosition().getX(), quadrant.getRandomPosition().getY());
        }
    }

    @Override
    public void shutdown() {

    }

    public static void main(String ... args) {
        EngineLoop.start(new VirusInvasionTest(), args);
    }
}
