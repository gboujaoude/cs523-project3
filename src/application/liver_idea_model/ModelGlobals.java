package application.liver_idea_model;

public class ModelGlobals {
    /**
     * List of configurable parameters for the model
     */
    public static final String macrophageNum = "macrophage_num";
    public static final String macrophageSpeed = "macrophage_speed";

    public static final String virusInitialNum = "virus_initial_num";
    public static final String virusSpeed = "virus_speed";
    public static final String virusPerSecond = "virus_per_second";
    public static final String virusesBeforeExplosion = "viruses_before_explosion";

    public static final String lymphocyteInitialNum = "lymphocyte_initial_num";
    public static final String lymphocyteLifespan = "lymphocyte_lifespan";
    public static final String lymphocytePerSecond = "lymphocyte_per_second";
    public static final String lymphocyteSpeed = "lymphocyte_speed";

    public static final String cytokineSpeed = "cytokine_speed";
    public static final String cytokinePouchSize = "cytokine_pouch_size";
    public static final String cytokineSecondsUntilDuplication = "cytokine_seconds_until_duplication";

    public static final String liverCellInitialNum = "livercell_initial_num";

    /**
     * Message subjects that will be passed around while the simulation is running
     */
    // For cellAddedToWorld, the data should be a reference to the cell that has just been added
    public static final String cellAddedToWorld = "cell_added";
    // For cellRemovedFromWorld, the data should be a reference to the cell that has been removed
    public static final String cellRemovedFromWorld = "cell_removed";
    public static final String cellInfected = "cell_infected";

    public static final String lymphocyteAddedToWorld = "lymphocyte_added";
    public static final String lymphocyteRemovedFromWorld = "lymphocyte_removed";

    public static final String virusAddedToWorld = "virus_added";
    public static final String virusRemovedFromWorld =  "virus_removed";
}
