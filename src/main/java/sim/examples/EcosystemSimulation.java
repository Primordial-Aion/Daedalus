package sim.examples;

import sim.api.BaseSimulation;
import sim.entity.SimEntity;
import sim.core.SimRunner;
import engine.model.Model;
import engine.model.ModelLoader;
import org.joml.Vector3f;

public class EcosystemSimulation extends BaseSimulation {

    @Override
    protected void onInit() {
        camera.setPosition(new Vector3f(0, 10, 20));
        camera.setRotation(new Vector3f(20, 0, 0));

        environment.setAmbientLight(0.3f, 0.3f, 0.4f);
        environment.setTimeOfDay(10.0f);

        try {
            Model creatureModel = ModelLoader.loadObjModel("/assets/models/creature.obj");

            for (int i = 0; i < 10; i++) {
                SimEntity creature = new SimEntity(creatureModel,
                    (float)(Math.random() * 20 - 10),
                    0,
                    (float)(Math.random() * 20 - 10));
                addEntity(creature);
            }
        } catch (Exception e) {
            System.err.println("Failed to load model: " + e.getMessage());
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        scene.getEntities().forEach(entity -> {
            if (entity instanceof SimEntity) {
                ((SimEntity)entity).update(deltaTime);
            }
        });
    }

    @Override
    protected void onCleanup() {
    }

    public static void main(String[] args) {
        SimRunner.run(EcosystemSimulation.class);
    }
}
