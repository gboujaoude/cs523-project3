package application.liver_idea_model;

import engine.EngineLoop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The Scientist conducts multiple experiments based on a list of config files.
 */
public class Scientist {

    public void run(String ... args) throws Exception {
        // Get list of all configs
        ArrayList<File> listOfConfigs = new ArrayList<>();
        File folder = new File("src/config_holder/");
        for (File file: folder.listFiles()) {
            listOfConfigs.add(file);
        }

        if (listOfConfigs.size() == 0) {
            System.out.println("No config files.");
            shutdown();
        }

        // Conduct tests per config file
        for (File file: listOfConfigs) {
            updateConfigFile(file);
            EngineLoop.start(new LiverIdeaModel(),args);
        }

        // Make sure the engine loop gets the exit signal
        EngineLoop.exit();
    }

    private void updateConfigFile(File file) throws IOException{
        Scanner scanner = new Scanner(file);
        FileWriter fw = new FileWriter("src/resources/liver_idea_model.cfg");
        while(scanner.hasNext()) {
            String curr = scanner.nextLine();
            fw.write(curr +"\n");
        }
        fw.close();
    }

    private void shutdown() {
        System.out.println("Shutting down.");
    }

    public static void main(String ... args) {
        try {
            new Scientist().run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
