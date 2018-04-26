package application.liver_idea_model;

import application.CameraController;
import application.quadrants_test.Quadrant;
import application.quadrants_test.QuadrantBuilder;
import engine.*;
import javafx.scene.paint.Color;

import java.util.Random;

public class LiverIdeaModel implements ApplicationEntryPoint, MessageHandler {
    private static final Color _lymphocyteColor = new Color(255 / 255.0, 173 / 255.0, 31 / 255.0, 1);
    private static final Color _virusColor = Color.RED;
    private final Text2D _lymphocyteDisplay = new Text2D("Lymphocytes: 0", 25, 50, 300, 55, 0);
    private final Text2D _virusDisplay = new Text2D("Viruses: 0", 25, 100, 300, 55, 0);
    private int _numLymphocytes = 0;
    private int _numViruses = 0;

    @Override
    public void init() {
        Engine.getConsoleVariables().loadConfigFile("src/application/liver_idea_model/liver_idea_model.cfg");
        new CameraController().enableMouseInputComponent();
        _lymphocyteDisplay.addToWorld();
        _lymphocyteDisplay.setColor(_lymphocyteColor);
        _lymphocyteDisplay.setAsStaticActor(true);
        _virusDisplay.addToWorld();
        _virusDisplay.setColor(_virusColor);
        _virusDisplay.setAsStaticActor(true);
        _registerMessages();
        _signalInterestInMessages();
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
        int numMacrophages = Engine.getConsoleVariables().find(ModelGlobals.macrophageNum).getcvarAsInt();
        for (int i = 0; i < numMacrophages; ++i) {
            Macrophage mackyMack = new Macrophage(quad.getRandomPosition().getX(), quad.getRandomPosition().getY());
            mackyMack.addToWorld();
        }
        quad = QuadrantBuilder.makeQuadrant(60, 70);
        // Get the number of starting lymphocytes and add them to the world
        int numLymphocytes = Engine.getConsoleVariables().find(ModelGlobals.lymphocyteInitialNum).getcvarAsInt();
        final double lymphocyteSpeed = Engine.getConsoleVariables().find(ModelGlobals.lymphocyteSpeed).getcvarAsFloat();
        for (int i = 0; i < numLymphocytes; ++i) {
            Lymphocyte lymphocyte = new Lymphocyte(quad.getRandomPosition().getX(), quad.getRandomPosition().getY());
            lymphocyte.setSpeedXY(lymphocyteSpeed * random.nextDouble(), 0.0);
            lymphocyte.addToWorld();
        }
        // Add the barrier that intercepts all uncaught Cytokines and summons new Lymphocytes
        quad = QuadrantBuilder.makeQuadrant(71, 75);
        int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
        new Barrier(0, quad.getRandomPosition().getY(), worldWidth, 10).addToWorld();
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
        switch(message.getMessageName()) {
            case ModelGlobals.lymphocyteAddedToWorld:
                ++_numLymphocytes;
                _lymphocyteDisplay.setText("Lymphocytes: " + _numLymphocytes);
                break;
            case ModelGlobals.lymphocyteRemovedFromWorld:
                --_numLymphocytes;
                _lymphocyteDisplay.setText("Lymphocytes: " + _numLymphocytes);
                break;
            case ModelGlobals.virusAddedToWorld:
                ++_numViruses;
                _virusDisplay.setText("Viruses: " + _numViruses);
                break;
            case ModelGlobals.virusRemovedFromWorld:
                --_numViruses;
                _virusDisplay.setText("Viruses: " + _numViruses);
                break;
            default:
                break;
        }
    }

    private void _registerMessages() {
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.cellAddedToWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.cellRemovedFromWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.lymphocyteAddedToWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.lymphocyteRemovedFromWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.virusAddedToWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.virusRemovedFromWorld));
    }

    private void _signalInterestInMessages() {
        Engine.getMessagePump().signalInterest(ModelGlobals.lymphocyteAddedToWorld, this);
        Engine.getMessagePump().signalInterest(ModelGlobals.lymphocyteRemovedFromWorld, this);
        Engine.getMessagePump().signalInterest(ModelGlobals.virusAddedToWorld, this);
        Engine.getMessagePump().signalInterest(ModelGlobals.virusRemovedFromWorld, this);
    }

    public static void main(String ... args) {
        EngineLoop.start(new LiverIdeaModel(),args);
    }
}
