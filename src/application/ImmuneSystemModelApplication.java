package application;

import com.sun.tools.internal.jxc.ap.Const;
import engine.*;
import javafx.application.Platform;

public class ImmuneSystemModelApplication implements ApplicationEntryPoint{

    @Override
    public void init() {
        int streamMargin = 40;
        Rectangle2D bloodstream = new Rectangle2D(streamMargin,0,
                Engine.getConsoleVariables().find(Constants.SCR_WIDTH).getcvarAsFloat()-2*streamMargin,
                Engine.getConsoleVariables().find(Constants.SCR_HEIGHT).getcvarAsFloat(),1000);
        bloodstream.addToWorld();

        PulseEntity simulation = new CellManager();
        Engine.getMessagePump().sendMessage(new Message(Constants.ADD_PULSE_ENTITY, simulation));

        // Code that Justin was suggesting to use when managing all of the cells
//        SceneManager sceneManager = new SceneManager();
//        sceneManager.add(new Rectangle2D(0,0,0,0,0));
//        sceneManager.activateAll();
    }

    @Override
    public void shutdown() {

    }

    private void _algorithm() {

    }

    public static void main(String ... args) {
        ImmuneSystemModelApplication app = new ImmuneSystemModelApplication();
        EngineLoop.start(app,args);
    }
}
