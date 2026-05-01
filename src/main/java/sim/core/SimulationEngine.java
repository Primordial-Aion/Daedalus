package sim.core;

import engine.core.Window;
import engine.core.Engine;
import engine.scene.Scene;
import engine.render.Renderer;
import engine.shader.ShaderProgram;
import engine.camera.Camera;
import org.joml.Matrix4f;
import sim.api.Simulation;
import engine.input.Input;

public class SimulationEngine {
    private final Engine engine;
    private final Scene scene;
    private final Renderer renderer;
    private Simulation simulation;
    private boolean initialized = false;
    private long lastFrameTime;

    public SimulationEngine() {
        this.engine = Engine.get();
        this.scene = Scene.get();
        this.renderer = Renderer.get();
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public void initialize() {
        if (simulation == null) {
            throw new IllegalStateException("Simulation not set");
        }
        engine.init();
        renderer.init();
        ShaderProgram.init();
        scene.init();
        simulation.init(scene);
        lastFrameTime = System.currentTimeMillis();
        initialized = true;
    }

    public void run() {
        if (!initialized) {
            initialize();
        }
        simulationLoop();
    }

    private void simulationLoop() {
        Window window = engine.getWindow();

        while (!window.shouldClose()) {
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastFrameTime) / 1000.0f;
            lastFrameTime = currentTime;

            if (Input.isKeyPressed(Input.Keys.ESCAPE)) {
                window.close();
            }

            simulation.update(deltaTime);
            scene.update(deltaTime);

            render();

            Input.update();
            window.update();
        }

        simulation.cleanup();
        scene.cleanup();
    }

    private void render() {
        renderer.beginFrame();

        Window window = engine.getWindow();
        float aspectRatio = (float) window.getWidth() / window.getHeight();

        Camera camera = scene.getCamera();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix(aspectRatio);

        ShaderProgram defaultShader = ShaderProgram.getDefault();
        defaultShader.bind();

        scene.render(defaultShader, viewMatrix, projectionMatrix);

        renderer.endFrame();
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
