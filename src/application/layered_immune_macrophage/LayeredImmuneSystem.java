package application.layered_immune_macrophage;

import application.CameraController;
import application.quadrants_test.Quadrant;
import application.quadrants_test.QuadrantBuilder;
import engine.ApplicationEntryPoint;
import engine.Constants;
import engine.Engine;
import engine.EngineLoop;

public class LayeredImmuneSystem implements ApplicationEntryPoint {
    @Override
    public void init() {
        Engine.getConsoleVariables().loadConfigFile("src/application/layered_immune_sys_test/layered_sys.cfg");
        new CameraController().enableMouseInputComponent();
        Quadrant quadrant = QuadrantBuilder.makeQuadrant(0, 30);
        for (int i = 0; i < 1000; ++i) {
            new BystanderCell(quadrant.getRandomPosition().getX(), quadrant.getRandomPosition().getY()).addToWorld();
        }

        quadrant = QuadrantBuilder.makeQuadrant(4,7);
        for (int i = 0; i < 1; ++i) {
            new Macrophage(quadrant.getRandomPosition().getX(), quadrant.getRandomPosition().getY()).addToWorld();
        }
        quadrant = QuadrantBuilder.makeQuadrant(30, 40);
        for (int i = 0; i < 400; ++i) {
            new TCell(quadrant.getRandomPosition().getX(), quadrant.getRandomPosition().getY()).addToWorld();
        }
        quadrant = QuadrantBuilder.makeQuadrant(0, 5);
        for (int i = 0; i < 100; ++i) {
            new Virus(quadrant.getRandomPosition().getX(), quadrant.getRandomPosition().getY()).addToWorld();
        }
        quadrant = QuadrantBuilder.makeQuadrant(95, 96);
        int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
        new Barrier(quadrant.getRandomPosition().getX(), quadrant.getRandomPosition().getY(), worldWidth, 10, 1).addToWorld();

    }

    @Override
    public void shutdown() {

    }

    public static void main(String ... args) {
        EngineLoop.start(new LayeredImmuneSystem(), args);
    }
}
