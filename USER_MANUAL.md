# Simulation Framework - User Manual

## Table of Contents
1. [Introduction](#1-introduction)
2. [Architecture Overview](#2-architecture-overview)
3. [Core Concepts](#3-core-concepts)
4. [Installation & Setup](#4-installation--setup)
5. [Step-by-Step Usage Guide](#5-step-by-step-usage-guide)
6. [Concrete Example: Rabbit Ecosystem Simulation](#6-concrete-example-rabbit-ecosystem-simulation)
7. [API Reference](#7-api-reference)
8. [Best Practices](#8-best-practices)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Introduction

The Simulation Framework is a layer built on top of the **Java Game Engine (Talos Engine)** that simplifies creating 3D simulations. It handles the rendering loop, OpenGL initialization, and scene management, allowing you to focus purely on simulation logic.

### What It Provides
- Automatic engine initialization and cleanup
- Scene management (entities, terrain, lighting)
- Camera and environment control
- A simple hook-based API (`onInit`, `onUpdate`, `onCleanup`)

### What You Provide
- Simulation logic (AI, behavior, stats)
- 3D models and animations
- Terrain data
- Spawn logic for entities

---

## 2. Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                   Your Simulation Code                      │
│         (extends BaseSimulation, custom SimEntities)       │
└──────────────────────┬────────────────────────────────────┘
                       │ uses
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Simulation Framework (sim.*)                   │
│                                                             │
│  sim.api      → Simulation, BaseSimulation                 │
│  sim.core     → SimulationEngine, SimRunner                 │
│  sim.entity   → SimEntity                                  │
│  sim.env      → Environment (terrain, lighting, time)       │
└──────────────────────┬────────────────────────────────────┘
                       │ depends on
                       ▼
┌─────────────────────────────────────────────────────────────┐
│          Java Game Engine (engine-core JAR)                 │
│                                                             │
│  engine.core      → Engine, Window                          │
│  engine.scene     → Scene (entity container)                │
│  engine.camera    → Camera                                  │
│  engine.model     → Model, Mesh, ModelLoader               │
│  engine.render    → Renderer, ShaderProgram                 │
│  engine.lighting  → LightManager, Light                     │
│  engine.terrain   → Terrain                                 │
│  engine.animation → Animation, Animator                     │
└─────────────────────────────────────────────────────────────┘
```

### Execution Flow

```
SimRunner.run(MySimulation.class)
    │
    ├─ Creates SimulationEngine
    ├─ Instantiates MySimulation via reflection
    ├─ Calls engine.init()       → OpenGL, Window, Input
    ├─ Calls scene.init()        → Camera, LightManager, Terrain
    ├─ Calls simulation.init()   → Your onInit() runs here
    │
    └─ Main Loop:
        ├─ simulation.update(deltaTime)  → Your onUpdate() runs here
        ├─ scene.update(deltaTime)       → Engine updates all entities
        └─ window.update()              → Swap buffers, poll events
            │
            └─ (ESC or window close) → cleanup()
```

---

## 3. Core Concepts

### 3.1 Simulation Interface

Every simulation must implement `sim.api.Simulation`:

```java
public interface Simulation {
    void init(Scene scene);        // Called once at startup
    void update(float deltaTime);  // Called every frame (deltaTime in seconds)
    void cleanup();                // Called at shutdown

    default Camera getCamera() { return null; }        // Optional
    default Environment getEnvironment() { return null; } // Optional
}
```

### 3.2 BaseSimulation (Recommended)

Instead of implementing `Simulation` directly, extend `BaseSimulation`. It provides:

- `scene` - The current Scene (entity container)
- `camera` - The Camera (position, rotation, FOV)
- `assetLoader` - The AssetLoader for loading models/textures
- `environment` - Environment controller (terrain, lighting, time of day)
- `addEntity(SimEntity)` / `removeEntity(SimEntity)` - Manage entities

```java
public abstract class BaseSimulation implements Simulation {
    protected Scene scene;
    protected Camera camera;
    protected AssetLoader assetLoader;
    protected Environment environment;

    // Override these three methods:
    protected abstract void onInit();
    protected abstract void onUpdate(float deltaTime);
    protected abstract void onCleanup();
}
```

### 3.3 SimEntity

`SimEntity` extends `engine.entity.Entity` and adds simulation-specific behavior:

```java
public class SimEntity extends Entity {
    private float updatePriority = 0;

    // Constructors
    public SimEntity(Model model);
    public SimEntity(Model model, float x, float y, float z);
    public SimEntity(Model model, Vector3f position);

    // Override this for per-frame behavior
    public void update(float deltaTime) { }

    // Control update order (lower = earlier)
    public float getUpdatePriority();
    public void setUpdatePriority(float priority);
}
```

**Key:** Your custom entity logic goes inside `update(float deltaTime)`.

### 3.4 Environment

Controls the simulation world's physical properties:

```java
public class Environment {
    public void setTerrain(Terrain terrain);        // Set the ground
    public void setTimeOfDay(float hour);           // 0-24 (controls sun position)
    public void setAmbientLight(float r, g, b);    // Ambient light color (0-1)
}
```

### 3.5 Running Simulations

**Option 1: Using SimRunner (Recommended)**
```java
SimRunner.run(MySimulation.class);
```

**Option 2: Manual Control**
```java
SimulationEngine engine = new SimulationEngine();
MySimulation sim = new MySimulation();
engine.setSimulation(sim);
engine.run();
```

---

## 4. Installation & Setup

### Prerequisites
- Java 17+
- Maven
- Java Game Engine built and installed to `../JAVA GAME ENGINE/dist/libs/`

### Build the Java Game Engine First
```bash
cd "../JAVA GAME ENGINE"
mvn clean package
mvn install:install-file -Dfile=dist/libs/engine-core-1.0.0.jar \
  -DgroupId=com.gameengine -DartifactId=engine-core -Dversion=1.0.0 \
  -Dpackaging=jar -DlocalRepositoryPath=dist/libs
```

### Build the Simulation Framework
```bash
cd "Simulation Framework"
mvn clean package
```

---

## 5. Step-by-Step Usage Guide

### Step 1: Create Your Custom Entity Class

This is where your simulation logic lives (hunger, thirst, AI, etc.):

```java
package my.sim;

import sim.entity.SimEntity;
import engine.model.Model;
import org.joml.Vector3f;

public class MyCreature extends SimEntity {
    // Your simulation state
    private float hunger = 100.0f;
    private float thirst = 100.0f;
    private Vector3f velocity = new Vector3f(0, 0, 0);

    public MyCreature(Model model, float x, float y, float z) {
        super(model, x, y, z);
    }

    @Override
    public void update(float deltaTime) {
        // Your logic here - runs every frame
        hunger -= 0.5f * deltaTime;
        thirst -= 0.7f * deltaTime;

        if (hunger < 30) {
            // Seek food logic
        }

        // Apply velocity to position
        Vector3f pos = getPosition();
        pos.add(velocity.x * deltaTime, 0, velocity.z * deltaTime);
        setPosition(pos);
    }
}
```

### Step 2: Create Your Simulation Class

```java
package my.sim;

import sim.api.BaseSimulation;
import sim.core.SimRunner;
import sim.entity.SimEntity;
import engine.model.Model;
import engine.model.ModelLoader;
import engine.terrain.Terrain;
import org.joml.Vector3f;

public class MySimulation extends BaseSimulation {

    @Override
    protected void onInit() {
        // --- CAMERA SETUP ---
        camera.setPosition(new Vector3f(0, 15, 30));
        camera.setRotation(new Vector3f(30, 0, 0));

        // --- ENVIRONMENT SETUP ---
        environment.setTimeOfDay(10.0f);  // 10:00 AM
        environment.setAmbientLight(0.3f, 0.3f, 0.4f);

        // --- LOAD TERRAIN ---
        Terrain terrain = Terrain.get();
        terrain.generateFlatTerrain();  // or generateNoiseTerrain(seed)
        environment.setTerrain(terrain);

        // --- LOAD MODELS ---
        Model creatureModel = ModelLoader.loadObjModel("/assets/models/creature.obj");
        Model foodModel = ModelLoader.loadObjModel("/assets/models/food.obj");

        // --- SPAWN INITIAL ENTITIES ---
        for (int i = 0; i < 20; i++) {
            float x = (float)(Math.random() * 40 - 20);
            float z = (float)(Math.random() * 40 - 20);
            float y = terrain.getHeightAt(x, z);  // Snap to terrain

            MyCreature creature = new MyCreature(creatureModel, x, y, z);
            addEntity(creature);  // Adds to scene automatically
        }

        // Spawn initial food sources
        spawnFood(foodModel, terrain);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Called every frame
        // You can add global logic here:
        // - Check entity interactions
        // - Spawn new resources
        // - Adjust environment

        if (shouldSpawnMoreFood()) {
            // spawn logic
        }
    }

    @Override
    protected void onCleanup() {
        // Cleanup resources if needed
        System.out.println("Simulation ended.");
    }

    private void spawnFood(Model foodModel, Terrain terrain) {
        for (int i = 0; i < 10; i++) {
            float x = (float)(Math.random() * 40 - 20);
            float z = (float)(Math.random() * 40 - 20);
            float y = terrain.getHeightAt(x, z);
            SimEntity food = new SimEntity(foodModel, x, y, z);
            addEntity(food);
        }
    }

    // --- ENTRY POINT ---
    public static void main(String[] args) {
        SimRunner.run(MySimulation.class);
    }
}
```

### Step 3: Run It

```bash
mvn exec:java -Dexec.mainClass="my.sim.MySimulation"
```

Or simply run the `main` method in your IDE.

---

## 6. Concrete Example: Rabbit Ecosystem Simulation

This is the exact scenario you described - rabbits with hunger/thirst, food/water seeking, terrain, and 3D models with animations.

### 6.1 Rabbit Entity with Full Logic

```java
package rabbit.sim;

import sim.entity.SimEntity;
import engine.model.Model;
import engine.animation.Animator;
import engine.animation.Animation;
import org.joml.Vector3f;

public class RabbitEntity extends SimEntity {

    // --- State ---
    public enum State { IDLE, SEEKING_FOOD, SEEKING_WATER, EATING, DRINKING, MOVING }

    private float hunger = 100.0f;
    private float thirst = 100.0f;
    private float energy = 100.0f;
    private State currentState = State.IDLE;

    private Vector3f targetPosition = null;
    private float moveSpeed = 3.0f;

    // Animation controller (from engine)
    private Animator animator;

    public RabbitEntity(Model model, float x, float y, float z) {
        super(model, x, y, z);
        this.animator = Animator.get();  // Engine's animation system
    }

    @Override
    public void update(float deltaTime) {
        // --- Decay stats ---
        hunger -= 0.8f * deltaTime;
        thirst -= 1.0f * deltaTime;
        energy -= 0.3f * deltaTime;

        // Clamp
        hunger = Math.max(0, hunger);
        thirst = Math.max(0, thirst);
        energy = Math.max(0, energy);

        // --- State machine ---
        switch (currentState) {
            case IDLE:
                if (hunger < 30) {
                    currentState = State.SEEKING_FOOD;
                    playAnimation("walk");
                } else if (thirst < 30) {
                    currentState = State.SEEKING_WATER;
                    playAnimation("walk");
                } else {
                    // Wander
                    if (targetPosition == null) {
                        targetPosition = new Vector3f(
                            getPosition().x + (float)(Math.random() * 6 - 3),
                            0,
                            getPosition().z + (float)(Math.random() * 6 - 3)
                        );
                    }
                    currentState = State.MOVING;
                    playAnimation("walk");
                }
                break;

            case SEEKING_FOOD:
                // Find nearest food (simplified - in real code, query scene)
                if (targetPosition != null) {
                    moveTowards(targetPosition, deltaTime);
                }
                // If reached food: currentState = EATING
                break;

            case SEEKING_WATER:
                if (targetPosition != null) {
                    moveTowards(targetPosition, deltaTime);
                }
                break;

            case EATING:
                hunger += 20.0f * deltaTime;
                energy += 5.0f * deltaTime;
                playAnimation("eat");
                if (hunger >= 80) {
                    currentState = State.IDLE;
                    targetPosition = null;
                }
                break;

            case DRINKING:
                thirst += 25.0f * deltaTime;
                energy += 5.0f * deltaTime;
                playAnimation("drink");
                if (thirst >= 80) {
                    currentState = State.IDLE;
                    targetPosition = null;
                }
                break;

            case MOVING:
                if (targetPosition != null) {
                    moveTowards(targetPosition, deltaTime);
                    float dist = getPosition().distance(targetPosition);
                    if (dist < 0.5f) {
                        currentState = State.IDLE;
                        targetPosition = null;
                        playAnimation("idle");
                    }
                }
                break;
        }
    }

    private void moveTowards(Vector3f target, float deltaTime) {
        Vector3f pos = getPosition();
        Vector3f direction = new Vector3f(target).sub(pos).normalize();
        direction.y = 0; // Keep on ground
        pos.add(direction.mul(moveSpeed * deltaTime));
        setPosition(pos);
    }

    private void playAnimation(String name) {
        animator.playAnimation(name);
    }

    // Getters for simulation-wide logic
    public float getHunger() { return hunger; }
    public float getThirst() { return thirst; }
    public State getCurrentState() { return currentState; }
}
```

### 6.2 Rabbit Simulation Class

```java
package rabbit.sim;

import sim.api.BaseSimulation;
import sim.core.SimRunner;
import sim.entity.SimEntity;
import engine.model.Model;
import engine.model.ModelLoader;
import engine.terrain.Terrain;
import org.joml.Vector3f;

public class RabbitSimulation extends BaseSimulation {

    private Model rabbitModel;
    private Model foodModel;
    private Model waterModel;
    private Terrain terrain;

    @Override
    protected void onInit() {
        System.out.println("Initializing Rabbit Ecosystem Simulation...");

        // --- Camera ---
        camera.setPosition(new Vector3f(0, 20, 40));
        camera.setRotation(new Vector3f(25, 0, 0));
        camera.setFov(70.0f);

        // --- Environment ---
        environment.setTimeOfDay(10.0f);  // Morning
        environment.setAmbientLight(0.4f, 0.4f, 0.5f);

        // --- Terrain ---
        terrain = Terrain.get();
        terrain.generateNoiseTerrain(12345L);  // Seeded terrain
        environment.setTerrain(terrain);

        // --- Load 3D Models ---
        try {
            rabbitModel = ModelLoader.loadObjModel("/assets/models/rabbit.obj");
            foodModel = ModelLoader.loadObjModel("/assets/models/carrot.obj");
            waterModel = ModelLoader.loadObjModel("/assets/models/water_source.obj");
        } catch (Exception e) {
            System.err.println("Failed to load models: " + e.getMessage());
        }

        // --- Spawn Rabbits ---
        for (int i = 0; i < 15; i++) {
            float x = (float)(Math.random() * 50 - 25);
            float z = (float)(Math.random() * 50 - 25);
            float y = terrain.getHeightAt(x, z);

            RabbitEntity rabbit = new RabbitEntity(rabbitModel, x, y, z);
            addEntity(rabbit);
        }

        // --- Spawn Food and Water Sources ---
        spawnResources(foodModel, waterModel, terrain, 10, 5);

        System.out.println("Simulation initialized with " + scene.getEntities().size() + " entities.");
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // --- Global simulation logic ---

        // Example: Spawn new food if running low
        long foodCount = scene.getEntities().stream()
            .filter(e -> e.getName() != null && e.getName().contains("food"))
            .count();

        if (foodCount < 5) {
            spawnFoodCluster(foodModel, terrain);
        }

        // Example: Log stats every 5 seconds
        if ((int)(System.currentTimeMillis() / 1000) % 5 == 0) {
            logRabbitStats();
        }
    }

    @Override
    protected void onCleanup() {
        System.out.println("Shutting down Rabbit Ecosystem Simulation.");
    }

    private void spawnResources(Model foodModel, Model waterModel, Terrain terrain, int foodCount, int waterCount) {
        for (int i = 0; i < foodCount; i++) {
            float x = (float)(Math.random() * 50 - 25);
            float z = (float)(Math.random() * 50 - 25);
            float y = terrain.getHeightAt(x, z);

            SimEntity food = new SimEntity(foodModel, x, y, z);
            food.setName("food_" + i);
            addEntity(food);
        }

        for (int i = 0; i < waterCount; i++) {
            float x = (float)(Math.random() * 50 - 25);
            float z = (float)(Math.random() * 50 - 25);
            float y = terrain.getHeightAt(x, z);

            SimEntity water = new SimEntity(waterModel, x, y, z);
            water.setName("water_" + i);
            addEntity(water);
        }
    }

    private void spawnFoodCluster(Model foodModel, Terrain terrain) {
        float centerX = (float)(Math.random() * 40 - 20);
        float centerZ = (float)(Math.random() * 40 - 20);

        for (int i = 0; i < 3; i++) {
            float x = centerX + (float)(Math.random() * 4 - 2);
            float z = centerZ + (float)(Math.random() * 4 - 2);
            float y = terrain.getHeightAt(x, z);

            SimEntity food = new SimEntity(foodModel, x, y, z);
            food.setName("food_auto_" + System.currentTimeMillis());
            addEntity(food);
        }
    }

    private void logRabbitStats() {
        int idle = 0, seeking = 0, eating = 0;
        for (var entity : scene.getEntities()) {
            if (entity instanceof RabbitEntity) {
                RabbitEntity.State state = ((RabbitEntity) entity).getCurrentState();
                switch (state) {
                    case IDLE: idle++; break;
                    case SEEKING_FOOD: case SEEKING_WATER: seeking++; break;
                    case EATING: case DRINKING: eating++; break;
                }
            }
        }
        System.out.printf("[Stats] Idle: %d | Seeking: %d | Eating/Drinking: %d%n", idle, seeking, eating);
    }

    public static void main(String[] args) {
        SimRunner.run(RabbitSimulation.class);
    }
}
```

### 6.3 How It All Connects - Summary

| Your Code | Where It Goes | What Framework Does |
|-----------|---------------|---------------------|
| Rabbit hunger/thirst logic | `RabbitEntity.update(deltaTime)` | Calls `scene.update()` which calls each entity's `update()` |
| Food/water seeking AI | `RabbitEntity.update()` switch statement | Runs every frame automatically |
| 3D rabbit model + animations | Loaded in `onInit()`, passed to `RabbitEntity` constructor | Renders via engine's `Renderer` |
| Terrain generation | `terrain.generateNoiseTerrain()` in `onInit()` | Used for height queries and rendering |
| Food spawn logic | `onUpdate()` or helper methods in `RabbitSimulation` | Added to scene via `addEntity()` |
| Camera setup | `camera.setPosition()` in `onInit()` | Engine uses it for view matrix |

---

## 7. API Reference

### 7.1 sim.api.Simulation (Interface)

| Method | Description |
|--------|-------------|
| `void init(Scene scene)` | Called once when simulation starts. Set up your world here. |
| `void update(float deltaTime)` | Called every frame. Delta time is in seconds. |
| `void cleanup()` | Called when simulation ends. Release resources here. |
| `Camera getCamera()` | Optional. Return custom camera if needed. |
| `Environment getEnvironment()` | Optional. Return custom environment if needed. |

### 7.2 sim.api.BaseSimulation (Abstract Class)

| Field | Type | Description |
|-------|------|-------------|
| `scene` | `Scene` | The world container. Add/remove entities here. |
| `camera` | `Camera` | Control viewport position, rotation, FOV. |
| `assetLoader` | `AssetLoader` | Load models, textures, prefabs. |
| `environment` | `Environment` | Control terrain, lighting, time of day. |

| Method | Description |
|--------|-------------|
| `abstract void onInit()` | Override to set up your simulation. |
| `abstract void onUpdate(float deltaTime)` | Override for per-frame logic. |
| `abstract void onCleanup()` | Override for cleanup. |
| `void addEntity(SimEntity)` | Adds entity to the scene. |
| `void removeEntity(SimEntity)` | Removes entity from the scene. |

### 7.3 sim.entity.SimEntity

| Constructor | Description |
|-------------|-------------|
| `SimEntity(Model model)` | Create with model only. |
| `SimEntity(Model model, float x, y, z)` | Create at world position. |
| `SimEntity(Model model, Vector3f pos)` | Create at world position (Vector3f). |

| Method | Description |
|--------|-------------|
| `void update(float deltaTime)` | Override for entity behavior. Called every frame. |
| `float getUpdatePriority()` | Get update order (lower = earlier). |
| `void setUpdatePriority(float)` | Set update order. |
| `Vector3f getPosition()` | Get world position. |
| `void setPosition(Vector3f)` | Set world position. |
| `void setRotation(Vector3f)` | Set rotation in degrees. |
| `void setScale(float)` | Set uniform scale. |

*Inherited from `engine.entity.Entity`:* `render()`, `setVisible()`, `isVisible()`, `setName()`, `getName()`, `getModel()`

### 7.4 sim.env.Environment

| Method | Description |
|--------|-------------|
| `void setTerrain(Terrain terrain)` | Set the ground terrain for the scene. |
| `void setTimeOfDay(float hour)` | Set time (0-24). Controls sun position. |
| `void setAmbientLight(float r, g, b)` | Set ambient light color (0.0 - 1.0). |
| `Terrain getTerrain()` | Get the current terrain. |

### 7.5 sim.core.SimRunner

| Method | Description |
|--------|-------------|
| `static void run(Class<? extends Simulation>)` | Run a simulation class. Instantiates and starts it. |

### 7.6 sim.core.SimulationEngine

| Method | Description |
|--------|-------------|
| `void setSimulation(Simulation)` | Set the simulation to run. |
| `void run()` | Start the simulation loop. |
| `void initialize()` | Manually initialize (optional). |
| `Scene getScene()` | Get the scene. |
| `Engine getEngine()` | Get the engine instance. |
| `Camera getCamera()` | Get the camera. |

---

## 8. Best Practices

### 8.1 Entity Update Logic
- Put all per-entity behavior in `SimEntity.update(deltaTime)`.
- Use `deltaTime` for frame-rate independent behavior.
- Don't do heavy computation in `update()` - it runs every frame.

### 8.2 Scene Management
- Use `addEntity()` / `removeEntity()` from `BaseSimulation` - they handle thread-safe additions.
- For mass entity operations, batch them in `onInit()` or spread across frames.

### 8.3 Model Loading
- Load models once in `onInit()` and reuse them.
- Models are shared - creating 100 rabbits only needs one `rabbitModel`.

### 8.4 Terrain
- Generate terrain in `onInit()` before spawning entities.
- Use `terrain.getHeightAt(x, z)` to place entities on the ground.

### 8.5 State Machines
- Use enums for entity states (like `IDLE`, `SEEKING_FOOD`).
- Keep state transition logic clear and contained.

### 8.6 Performance
- Scene automatically updates all entities.
- If you have 1000+ entities, consider spatial partitioning.
- Remove entities that are far from camera or no longer needed.

---

## 9. Troubleshooting

### Engine-core JAR Not Found
```
Error: Could not find artifact com.gameengine:engine-core:jar:1.0.0
```
**Fix:** Build the Java Game Engine first:
```bash
cd "../JAVA GAME ENGINE"
mvn clean package
# The JAR should be at dist/libs/engine-core-1.0.0.jar
```

### Model Not Loading
```
Failed to load model: /assets/models/rabbit.obj
```
**Fix:** Ensure the model file is in `src/main/resources/assets/models/` or the correct path relative to the working directory.

### Entities Not Visible
- Check `entity.isVisible()` - should be `true`.
- Check camera position - are you looking at the entities?
- Check terrain height - entities might be underground.

### Update Logic Not Running
- Ensure your entity's `update()` method is overridden (not the empty default).
- `scene.update(deltaTime)` is called automatically - your entities will update.

### Animation Not Playing
- Ensure animations are loaded and named correctly.
- Call `animator.playAnimation("animation_name")` from your entity.

---

## Quick Reference: Typical File Structure

```
MySimulationProject/
├── src/main/java/my/sim/
│   ├── MySimulation.java       # Main simulation class (extends BaseSimulation)
│   ├── MyCreature.java         # Custom entity (extends SimEntity)
│   └── package-info.java       # Optional
├── src/main/resources/
│   └── assets/
│       ├── models/
│       │   ├── creature.obj
│       │   └── food.obj
│       └── textures/
│           └── creature.png
└── pom.xml                     # Depends on simulation-framework
```

---

For more information about the underlying engine, see the **Java Game Engine USER_MANUAL.md**.
