package application.cytokine_test;

import application.CameraController;
import engine.ApplicationEntryPoint;
import engine.Constants;
import engine.Engine;
import engine.EngineLoop;

import java.util.Random;

// Here we go
public class CytokineTest implements ApplicationEntryPoint {
    @Override
    public void init() {
        // Add the camera first
        new CameraController().enableMouseInputComponent();
        // Get the world width/height which are separate from the screen width/height
        int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
        int worldHeight = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsInt();
        // Put the B-cell in the center of the world
        BCell cell = new BCell(worldWidth / 2, worldHeight / 2);
        cell.addToWorld();
        Random rng = new Random();
        // Create 100 cytokines
        for (int i = 0; i < 250; ++i) {
            new Cytokine(rng.nextDouble() * worldWidth, rng.nextDouble() * worldHeight).addToWorld();
        }
    }

    @Override
    public void shutdown() {

    }

    public static void main(String ... args) {
        EngineLoop.start(new CytokineTest(), args);
    }
}
