package application.bcell_response_test;

import application.CameraController;
import application.quadrants_test.Quadrant;
import application.quadrants_test.QuadrantBuilder;
import engine.ApplicationEntryPoint;
import engine.Constants;
import engine.Engine;
import engine.EngineLoop;

import java.util.Random;

public class BcellResponseTest implements ApplicationEntryPoint {
    @Override
    public void init() {
        Engine.getConsoleVariables().loadConfigFile("src/application/layered_immune_sys_test/layered_sys.cfg");
        new CameraController().enableMouseInputComponent();
        Quadrant quadrant = QuadrantBuilder.makeQuadrant(0, 30);
//        for (int i = 0; i < 400; ++i) {
//            new TCell(quadrant.getRandomPosition().getX(), quadrant.getRandomPosition().getY()).addToWorld();
//        }

        quadrant = QuadrantBuilder.makeQuadrant(0,10);
        for (int i =0; i < 30; i++) {
            new BCell(quadrant.getRandomPosition().getX(), quadrant.getRandomPosition().getY()).addToWorld();
        }
        quadrant = QuadrantBuilder.makeQuadrant(0, 5);
        quadrant = QuadrantBuilder.makeQuadrant(95, 96);
        int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
        int worldHeight = Engine.getConsoleVariables().find(Constants.WORLD_HEIGHT).getcvarAsInt();


        Random rng = new Random();
        for (int i = 0; i < 250; ++i) {
            new Cytokine(rng.nextDouble() * worldWidth, rng.nextDouble() * worldHeight).addToWorld();
        }
//        new Barrier(quadrant.getRandomPosition().getX(), quadrant.getRandomPosition().getY(), worldWidth, 10, 1).addToWorld();

    }

    @Override
    public void shutdown() {

    }

    public static void main(String ... args) {
        EngineLoop.start(new BcellResponseTest(), args);
    }
}
