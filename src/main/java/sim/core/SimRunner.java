package sim.core;

import sim.api.Simulation;

public class SimRunner {
    private final SimulationEngine engine;
    private final Class<? extends Simulation> simulationClass;

    public SimRunner(Class<? extends Simulation> simulationClass) {
        this.simulationClass = simulationClass;
        this.engine = new SimulationEngine();
    }

    public void start() {
        try {
            Simulation simulation = simulationClass.getDeclaredConstructor().newInstance();
            engine.setSimulation(simulation);
            engine.run();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start simulation", e);
        }
    }

    public static void run(Class<? extends Simulation> simulationClass) {
        new SimRunner(simulationClass).start();
    }
}
