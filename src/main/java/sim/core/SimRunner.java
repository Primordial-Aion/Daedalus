package sim.core;

import sim.api.Simulation;
import engine.util.Config;

public class SimRunner {
    private final SimulationEngine engine;
    private final Class<? extends Simulation> simulationClass;

    private SimRunner(Class<? extends Simulation> simulationClass) {
        this.simulationClass = simulationClass;
        this.engine = new SimulationEngine();
    }

    public static SimRunnerBuilder builder(Class<? extends Simulation> simulationClass) {
        return new SimRunnerBuilder(simulationClass);
    }

    public static class SimRunnerBuilder {
        private final Class<? extends Simulation> simulationClass;
        
        public SimRunnerBuilder(Class<? extends Simulation> simulationClass) {
            this.simulationClass = simulationClass;
        }

        public SimRunnerBuilder withTitle(String title) {
            Config.get().windowTitle = title;
            return this;
        }

        public SimRunnerBuilder withResolution(int width, int height) {
            Config.get().windowWidth = width;
            Config.get().windowHeight = height;
            return this;
        }

        public SimRunnerBuilder withVsync(boolean vsync) {
            Config.get().enableVsync = vsync;
            return this;
        }

        public void start() {
            new SimRunner(simulationClass).start();
        }
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
        builder(simulationClass).start();
    }
}
