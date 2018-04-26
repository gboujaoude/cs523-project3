package application.liver_idea_model;

import application.CameraController;
import application.quadrants_test.Quadrant;
import application.quadrants_test.QuadrantBuilder;
import engine.*;

import java.util.Random;

public class LiverIdeaModel implements ApplicationEntryPoint, MessageHandler {
    @Override
    public void init() {
        Engine.getConsoleVariables().loadConfigFile("src/application/liver_idea_model/liver_idea_model.cfg");
        new CameraController().enableMouseInputComponent();
        _registerMessages();
        Random random = new Random();
        Quadrant quad = QuadrantBuilder.makeQuadrant(0, 10);
        // Get the number of starting viruses and add them to the world
        int numViruses = Engine.getConsoleVariables().find(ModelGlobals.virusInitialNum).getcvarAsInt();
        for (int i = 0; i < numViruses; ++i) {
            Virus virus = new Virus(quad.getRandomPosition().getX(), quad.getRandomPosition().getY());
            virus.addToWorld();
        }
        quad = QuadrantBuilder.makeQuadrant(0, 60);
        // Get the number of starting cells and add them to the world
        int numLiverCells = Engine.getConsoleVariables().find(ModelGlobals.liverCellInitialNum).getcvarAsInt();
        for (int i = 0; i < numLiverCells; ++i) {
            LiverCell cell = new LiverCell(quad.getRandomPosition().getX(), quad.getRandomPosition().getY());
            cell.addToWorld();
        }
        quad = QuadrantBuilder.makeQuadrant(60, 70);
        int numLymphocytes = Engine.getConsoleVariables().find(ModelGlobals.lymphocyteInitialNum).getcvarAsInt();
        final double lymphocyteSpeed = Engine.getConsoleVariables().find(ModelGlobals.lymphocyteSpeed).getcvarAsFloat();
        for (int i = 0; i < numLymphocytes; ++i) {
            Lymphocyte lymphocyte = new Lymphocyte(quad.getRandomPosition().getX(), quad.getRandomPosition().getY());
            lymphocyte.setSpeedXY(lymphocyteSpeed * random.nextDouble(), 0.0);
            lymphocyte.addToWorld();
        }
    }

    @Override
    public void shutdown() {

    }

    /**
     * This will be called once for every message that this
     * object has signalled interest in. For example, if you
     * are interested in "ENTER_KEY_DOWN" and "LEFT_MOUSE_DOWN",
     * this could be registered with the MessagePump system. If
     * either (or both) of these fire during a given frame, the
     * generated messages will be relayed to this method.
     *
     * @param message message which was generated because of a specific event
     */
    @Override
    public void handleMessage(Message message) {

    }

    private void _registerMessages() {
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.cellAddedToWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.cellRemovedFromWorld));
    }

    public static void main(String ... args) {
        EngineLoop.start(new LiverIdeaModel(),args);
    }
}
