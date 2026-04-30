package sim.entity;

import engine.entity.Entity;
import engine.model.Model;
import org.joml.Vector3f;

public class SimEntity extends Entity {
    private float updatePriority = 0;

    public SimEntity(Model model) {
        super(model);
    }

    public SimEntity(Model model, float x, float y, float z) {
        super(model);
        super.setPosition(new Vector3f(x, y, z));
    }

    public SimEntity(Model model, Vector3f position) {
        super(model);
        super.setPosition(position);
    }

    public void update(float deltaTime) {
    }

    public float getUpdatePriority() {
        return updatePriority;
    }

    public void setUpdatePriority(float priority) {
        this.updatePriority = priority;
    }
}
