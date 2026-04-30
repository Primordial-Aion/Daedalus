package sim.core;

import engine.core.Engine;
import engine.scene.Scene;
import engine.camera.Camera;
import sim.api.Simulation;

public class SimulationEngine {
    private final Engine engine;
    private final Scene scene;
    private Simulation simulation;
    private boolean initialized = false;

    public SimulationEngine() {
        this.engine = Engine.get();
        this.scene = Scene.get();
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public void initialize() {
        if (simulation == null) {
            throw new IllegalStateException("Simulation not set");
        }
        engine.init();
        scene.init();
        simulation.init(scene);
        initialized = true;
    }

    public void run() {
        if (!initialized) {
            initialize();
        }
        simulationLoop();
    }

    private void simulationLoop() {
        while (!engine.getWindow().shouldClose()) {
            float deltaTime = engine.getDeltaTime();
            simulation.update(deltaTime);
            scene.update(deltaTime);
            engine.getWindow().update();
        }
        simulation.cleanup();
        scene.cleanup();
    }

    public Scene getScene() {
        return scene;
    }

    public Engine getEngine() {
        return engine;
    }

    public Camera getCamera() {
        return scene.getCamera();
    }
}
