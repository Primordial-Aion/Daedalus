# Daedalus

> *"The Labyrinth's architect — crafting worlds, breathing life into bronze."*

**Daedalus** is a 3D simulation framework built atop the [Talos Game Engine](https://github.com/Primordial-Aion/Talos). Just as Daedalus constructed the Labyrinth to house complex, living systems, this framework provides the scaffolding to build rich simulations with minimal boilerplate.

It handles the engine wiring — rendering, scene management, and the update loop — so you can focus on what matters: **simulation logic**.

---

## Why Daedalus?

| Problem | Daedalus Solution |
|---------|-------------------|
| Engine setup is verbose | One class with `onInit()` and `onUpdate()` |
| Managing entities/scene | `addEntity()` / `removeEntity()` helpers |
| Camera & lighting setup | `Environment` and `Camera` ready to use |
| OpenGL internals | Fully abstracted away |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Your Simulation Logic                     │
│            (extends BaseSimulation, custom entities)       │
└──────────────────────┬────────────────────────────────────┘
                       │ uses
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  Daedalus (sim.*)                          │
│                                                             │
│  sim.api      → Simulation, BaseSimulation                 │
│  sim.core     → SimulationEngine, SimRunner                 │
│  sim.entity   → SimEntity (your actors)                    │
│  sim.env      → Environment (terrain, time, lighting)       │
└──────────────────────┬────────────────────────────────────┘
                       │ depends on
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Talos Game Engine (engine-core)                │
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

---

## Quick Start

### 1. Prerequisites

- Java 17+
- Maven
- [Talos Game Engine](https://github.com/Primordial-Aion/Talos) built and installed to `../JAVA GAME ENGINE/dist/libs/`

### 2. Build Talos First

```bash
cd "JAVA GAME ENGINE"
mvn clean package
mvn install:install-file -Dfile=dist/libs/engine-core-1.0.0.jar \
  -DgroupId=com.gameengine -DartifactId=engine-core -Dversion=1.0.0 \
  -Dpackaging=jar -DlocalRepositoryPath=dist/libs
```

### 3. Create Your First Simulation

```java
package my.sim;

import sim.api.BaseSimulation;
import sim.core.SimRunner;
import engine.model.Model;
import engine.model.ModelLoader;
import org.joml.Vector3f;

public class MySimulation extends BaseSimulation {

    @Override
    protected void onInit() {
        // Set up camera
        camera.setPosition(new Vector3f(0, 15, 30));

        // Set up environment
        environment.setTimeOfDay(10.0f); // 10:00 AM
        environment.setAmbientLight(0.3f, 0.3f, 0.4f);

        // Load models and spawn entities
        Model creature = ModelLoader.loadObjModel("/assets/models/creature.obj");
        for (int i = 0; i < 10; i++) {
            SimEntity entity = new SimEntity(creature,
                (float)(Math.random() * 20 - 10), 0,
                (float)(Math.random() * 20 - 10));
            addEntity(entity);
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Your simulation logic runs here every frame
        scene.getEntities().forEach(e -> {
            if (e instanceof SimEntity) ((SimEntity) e).update(deltaTime);
        });
    }

    @Override
    protected void onCleanup() { }

    public static void main(String[] args) {
        SimRunner.run(MySimulation.class);
    }
}
```

### 4. Run It

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="my.sim.MySimulation"
```

---

## Example: Rabbit Ecosystem Simulation

A complete example bundled with the framework — rabbits with hunger/thirst, food/water seeking AI, state machines, and 3D models with animations.

```java
// Entity with simulation logic
public class RabbitEntity extends SimEntity {
    private float hunger = 100;
    private float thirst = 100;
    enum State { IDLE, SEEKING_FOOD, EATING, SEEKING_WATER, DRINKING }
    private State state = State.IDLE;

    @Override
    public void update(float deltaTime) {
        hunger -= 0.8f * deltaTime;
        thirst -= 1.0f * deltaTime;

        if (hunger < 30) state = State.SEEKING_FOOD;
        else if (thirst < 30) state = State.SEEKING_WATER;

        // ... seeking, eating, drinking logic
    }
}
```

See `src/main/java/sim/examples/EcosystemSimulation.java` for the full implementation.

---

## Core API

### BaseSimulation (What You Extend)

| Method | Purpose |
|--------|---------|
| `onInit()` | Set up camera, terrain, entities, lighting |
| `onUpdate(float deltaTime)` | Per-frame simulation logic |
| `onCleanup()` | Release resources at shutdown |
| `addEntity(SimEntity)` | Add entity to the scene |
| `removeEntity(SimEntity)` | Remove entity from the scene |

### Available Fields in BaseSimulation

| Field | Type | Description |
|-------|------|-------------|
| `scene` | `Scene` | Entity container — add/remove entities here |
| `camera` | `Camera` | Viewport control — position, rotation, FOV |
| `environment` | `Environment` | World settings — terrain, lighting, time of day |
| `assetLoader` | `AssetLoader` | Load models, textures, and prefabs |

### SimEntity (Your Simulation Actors)

```java
public class SimEntity extends Entity {
    public SimEntity(Model model, float x, float y, float z);
    public void update(float deltaTime); // Override for behavior
    public void setUpdatePriority(float); // Lower = earlier update
}
```

### Environment

```java
environment.setTerrain(terrain);           // Set the ground
environment.setTimeOfDay(10.0f);           // 0-24, controls sun position
environment.setAmbientLight(0.3f, 0.3f, 0.4f); // RGB, 0.0-1.0
```

---

## Project Structure

```
JAVA SIMULATION FRAMEWORK/
├── src/main/java/sim/
│   ├── api/           # Simulation interface, BaseSimulation
│   ├── core/          # SimulationEngine, SimRunner
│   ├── entity/        # SimEntity wrapper
│   ├── env/           # Environment (terrain, lighting)
│   └── examples/      # Example simulations
├── USER_MANUAL.md     # Full documentation with examples
├── README.md          # This file
└── pom.xml
```

---

## See Also

- **Talos Game Engine** — [github.com/Primordial-Aion/Talos](https://github.com/Primordial-Aion/Talos)
- **Full User Manual** — `USER_MANUAL.md` in this repository
- **Example Simulation** — `src/main/java/sim/examples/EcosystemSimulation.java`

---

*Daedalus — named for the master craftsman of Greek myth who built the Labyrinth, a twisting world of complexity and life.*
