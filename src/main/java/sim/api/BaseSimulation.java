package sim.api;

import engine.scene.Scene;
import engine.camera.Camera;
import engine.asset.AssetLoader;
import sim.entity.SimEntity;
import sim.env.Environment;

public abstract class BaseSimulation implements Simulation {
    protected Scene scene;
    protected Camera camera;
    protected AssetLoader assetLoader;
    protected Environment environment;

    @Override
    public void init(Scene scene) {
        this.scene = scene;
        this.camera = scene.getCamera();
        this.assetLoader = AssetLoader.get();
        this.environment = new Environment();
        onInit();
    }

    protected abstract void onInit();

    @Override
    public void update(float deltaTime) {
        onUpdate(deltaTime);
    }

    protected abstract void onUpdate(float deltaTime);

    @Override
    public void cleanup() {
        onCleanup();
    }

    protected abstract void onCleanup();

    protected void addEntity(engine.entity.Entity entity) {
        scene.addEntity(entity);
    }

    protected void removeEntity(engine.entity.Entity entity) {
        scene.removeEntity(entity);
    }
}
