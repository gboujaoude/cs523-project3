package application.layered_immune_sys_test;

import application.CameraController;
import engine.ApplicationEntryPoint;
import engine.Engine;
import engine.EngineLoop;

public class LayeredImmuneSystem implements ApplicationEntryPoint {
    @Override
    public void init() {
        Engine.getConsoleVariables().loadConfigFile("src/application/layered_immune_sys_test/layered_sys.cfg");
        new CameraController().enableMouseInputComponent();
    }

    @Override
    public void shutdown() {

    }

    public static void main(String ... args) {
        EngineLoop.start(new LayeredImmuneSystem(), args);
    }
}
