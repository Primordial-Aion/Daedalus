package sim.api;

import engine.scene.Scene;
import engine.camera.Camera;
import sim.env.Environment;

public interface Simulation {
    void init(Scene scene);
    void update(float deltaTime);
    void cleanup();

    default Camera getCamera() {
        return null;
    }

    default Environment getEnvironment() {
        return null;
    }
}
