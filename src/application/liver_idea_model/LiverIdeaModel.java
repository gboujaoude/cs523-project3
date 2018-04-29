package application.liver_idea_model;

import application.CameraController;
import application.library_of_congress.BookKeeper;
import application.library_of_congress.RecordBook;
import application.library_of_congress.StickyNotes;
import application.library_of_congress.TimeKeeper;
import application.quadrants_test.Quadrant;
import application.quadrants_test.QuadrantBuilder;
import engine.*;
import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;

public class LiverIdeaModel implements ApplicationEntryPoint, MessageHandler, PulseEntity {
    private static final Color _lymphocyteColor = new Color(255 / 255.0, 173 / 255.0, 31 / 255.0, 1);
    private static final Color _virusColor = Color.RED;
    private static final Color _cellColor = Color.BLUEVIOLET;
    private static final Color _timerColor = Color.FORESTGREEN;
    private final Text2D _lymphocyteDisplay = new Text2D("Lymphocytes: 0", 25, 50, 300, 55, 0);
    private final Text2D _virusDisplay = new Text2D("Viruses: 0", 25, 100, 300, 55, 0);
    private final Text2D _cellDisplay = new Text2D("Healthy/Infected Cells: 0/0", 25, 150, 500, 55, 0);
    private final Text2D _invasionTimer = new Text2D("Invasion Timer: 00.00.00", 25, 200, 500, 55, 0);
    private int _numLymphocytes = 0;
    private int _numViruses = 0;
    private int _numHealthyCells = 0;
    private int _numInfectedCells = 0;
    private int _numLeftLiver = 0;
    private double _elapsedSeconds;
    private double _elapsedRuntime;
    private double _elapsedRecordTime;
    private double _maxRuntime;
    private DecimalFormat _minuteFormat = new DecimalFormat("00");
    private DecimalFormat _secondFormat = new DecimalFormat("00");
    private DecimalFormat _msFormat = new DecimalFormat("00");
    private BookKeeper _keeper = new BookKeeper();
    private RecordBook _bookVirusCount;
    private RecordBook _bookInfectedCount;
    private RecordBook _bookHealthyCount;
    private RecordBook _bookLymphocyteCount;
    private TimeKeeper _timeKeeper;

    @Override
    public void init() {
        Engine.getConsoleVariables().loadConfigFile("src/resources/liver_idea_model.cfg");
        Label label = new Label("Time Scaling Slider");
        label.setLayoutX(25);
        label.setLayoutY(200);
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_UI_ELEMENT, label));
        Slider slider = new Slider(0,10,1);
        slider.setBlockIncrement(1);
        slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMinorTickCount(0);
        slider.setMajorTickUnit(1);
        slider.setLayoutX(25);
        slider.setLayoutY(220);
        slider.setValue(1);
        slider.setOnMouseClicked((e) -> {
            Engine.getConsoleVariables().find(Constants.TIME_SCALING_FACTOR).setValue(Double.toString(Math.floor(slider.getValue())));
        });
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_UI_ELEMENT, slider));
        new CameraController().enableMouseInputComponent();
        _lymphocyteDisplay.addToWorld();
        _lymphocyteDisplay.setColor(_lymphocyteColor);
        _lymphocyteDisplay.setAsStaticActor(true);
        _virusDisplay.addToWorld();
        _virusDisplay.setColor(_virusColor);
        _virusDisplay.setAsStaticActor(true);
        _cellDisplay.addToWorld();
        _cellDisplay.setColor(_cellColor);
        _cellDisplay.setAsStaticActor(true);
        _invasionTimer.addToWorld();
        _invasionTimer.setColor(_timerColor);
        _invasionTimer.setAsStaticActor(true);
        _maxRuntime = Engine.getConsoleVariables().find(ModelGlobals.maxRuntime).getcvarAsFloat();
        _timeKeeper = new TimeKeeper();
        new File("data/" + _timeKeeper.getTime()).mkdir();
        _keeper.setTime(_timeKeeper.getTime());
        RecordBook configHistory = new RecordBook("config-history",_timeKeeper.getTime());
        configHistory.recordConfig(Engine.getConsoleVariables().getAllConsoleVariables());
        _keeper.addBook(configHistory);
        _keeper.addNote(new StickyNotes("Memo: " + Engine.getConsoleVariables().find(ModelGlobals.memo).getcvarValue()));
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, this));
        _registerMessages();
        _createBookKeeper();
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
        quad = QuadrantBuilder.makeQuadrant(75, 80);
        int worldWidth = Engine.getConsoleVariables().find(Constants.WORLD_WIDTH).getcvarAsInt();
        new Barrier(0, quad.getRandomPosition().getY(), worldWidth, 10).addToWorld();
    }

    @Override
    public void shutdown() {
        _recordData(-1); // -1 forces bookKeeper to record all data
        _keeper.addNote(new StickyNotes("Num viruses that left liver: " + _numLeftLiver));
        _keeper.closeBooks();
        _keeper.closeNotes();
        Engine.getMessagePump().sendMessage(Constants.PERFORM_FULL_ENGINE_SHUTDOWN);
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
            case ModelGlobals.cellAddedToWorld:
                ++_numHealthyCells;
                _cellDisplay.setText("Healthy/Infected Cells: " + _numHealthyCells + "/" + _numInfectedCells);
                break;
            case ModelGlobals.cellRemovedFromWorld:
            {
                LiverCell cell = (LiverCell)message.getMessageData();
                if (cell.infected()) --_numInfectedCells;
                else --_numHealthyCells;
                _cellDisplay.setText("Healthy/Infected Cells: " + _numHealthyCells + "/" + _numInfectedCells);
                break;
            }
            case ModelGlobals.cellInfected:
                --_numHealthyCells;
                ++_numInfectedCells;
                _cellDisplay.setText("Healthy/Infected Cells: " + _numHealthyCells + "/" + _numInfectedCells);
                break;
            case ModelGlobals.virusLeftLiver:
                ++ _numLeftLiver;
                _keeper.addNote(new StickyNotes("Virus left liver @ " + _elapsedRuntime));
                break;
            default:
                break;
        }
    }

    @Override
    public void pulse(double deltaSeconds) {
        _elapsedRuntime += deltaSeconds;

        if (_elapsedRuntime > _maxRuntime) {
            _keeper.addNote(new StickyNotes("Reached max runtime. Shutting down."));
            shutdown();
        }

        if (_numViruses > 0 || _numInfectedCells > 0) {
            _elapsedSeconds += deltaSeconds;
            double ms = _elapsedSeconds * 1000;
            double sec = Math.floor(_elapsedSeconds);
            double min = Math.floor(_elapsedSeconds / 60);
            sec -= (min * 60);
            ms -= ((min * 60 * 1000) + (sec * 1000));
            _invasionTimer.setText("Invasion Timer: " + _minuteFormat.format(min) + "." +
                    _secondFormat.format(sec) + "." + _msFormat.format(ms));
            _recordData(deltaSeconds);
        } else {
            double ms = _elapsedRuntime * 1000;
            double sec = Math.floor(_elapsedRuntime);
            double min = Math.floor(_elapsedRuntime / 60);
            sec -= (min * 60);
            ms -= ((min * 60 * 1000) + (sec * 1000));
            _keeper.addNote(new StickyNotes("All Viruses killed at: " + _minuteFormat.format(min) + "." +
                    _secondFormat.format(sec) + "." + _msFormat.format(ms)));
            _recordData(deltaSeconds);
            shutdown();
        }
    }

    private void _registerMessages() {
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.cellAddedToWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.cellRemovedFromWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.cellInfected));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.lymphocyteAddedToWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.lymphocyteRemovedFromWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.virusAddedToWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.virusRemovedFromWorld));
        Engine.getMessagePump().registerMessage(new Message(ModelGlobals.virusLeftLiver));
    }

    private void _signalInterestInMessages() {
        Engine.getMessagePump().signalInterest(ModelGlobals.lymphocyteAddedToWorld, this);
        Engine.getMessagePump().signalInterest(ModelGlobals.lymphocyteRemovedFromWorld, this);
        Engine.getMessagePump().signalInterest(ModelGlobals.virusAddedToWorld, this);
        Engine.getMessagePump().signalInterest(ModelGlobals.virusRemovedFromWorld, this);
        Engine.getMessagePump().signalInterest(ModelGlobals.cellAddedToWorld, this);
        Engine.getMessagePump().signalInterest(ModelGlobals.cellRemovedFromWorld, this);
        Engine.getMessagePump().signalInterest(ModelGlobals.cellInfected, this);
        Engine.getMessagePump().signalInterest(ModelGlobals.virusLeftLiver, this);
    }

    private void _recordData(double deltaSeconds) {
        _elapsedRecordTime += deltaSeconds;

        // -1 signals shutdown
        if (_elapsedRecordTime >= 1 || deltaSeconds == -1) {
            _bookVirusCount.add(String.valueOf(_numViruses) + "\n");
            _bookInfectedCount.add(String.valueOf(_numInfectedCells)+ "\n");
            _bookHealthyCount.add(String.valueOf(_numHealthyCells)+ "\n");
            _bookLymphocyteCount.add(String.valueOf(_numLymphocytes)+ "\n");
            _elapsedRecordTime = 0;
        }
    }

    private void _createBookKeeper() {
        _bookVirusCount = new RecordBook("virus-over-time",_timeKeeper.getTime());
        _bookInfectedCount = new RecordBook("infected-over-time", _timeKeeper.getTime());
        _bookHealthyCount = new RecordBook("healthy-over-time",_timeKeeper.getTime()) ;
        _bookLymphocyteCount = new RecordBook("lymphocytes-over-time",_timeKeeper.getTime());
        _keeper.addBook(_bookVirusCount);
        _keeper.addBook(_bookInfectedCount);
        _keeper.addBook(_bookHealthyCount);
        _keeper.addBook(_bookLymphocyteCount);
    }

    public static void main(String ... args) {
        EngineLoop.start(new LiverIdeaModel(),args);
    }
}
