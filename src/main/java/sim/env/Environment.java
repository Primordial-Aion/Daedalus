package sim.env;

import engine.lighting.LightManager;
import engine.lighting.Light;
import engine.terrain.Terrain;
import engine.scene.Scene;
import org.joml.Vector3f;

public class Environment {
    private Terrain terrain;
    private final LightManager lightManager;
    private float timeOfDay = 12.0f;

    public Environment() {
        this.lightManager = LightManager.get();
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
        Scene.get().setTerrain(terrain);
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTimeOfDay(float hour) {
        this.timeOfDay = hour;
        updateLighting();
    }

    private void updateLighting() {
        float sunAngle = (timeOfDay / 24.0f) * 360.0f - 90.0f;
        float rad = (float) Math.toRadians(sunAngle);
        Light sun = lightManager.getSunLight();
        if (sun != null) {
            sun.setPosition(new Vector3f((float) Math.cos(rad), (float) Math.sin(rad), -0.5f));
        }
    }

    public void setAmbientLight(float r, float g, float b) {
        Light sun = lightManager.getSunLight();
        if (sun != null) {
            sun.setAmbientColor(new Vector3f(r, g, b));
        }
    }
}
