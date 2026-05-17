# ⚔️ Advance Wars (Java Remake)

A turn-based strategy game inspired by the **Advance Wars** series.  
Developed as a team project for the **IJA (Java Seminar Project)** course during the academic year **2025/2026**.

## 👨‍💻 Team Authors

### Andrej Bližnák (`xblizna00`)
- GUI implementation
- Bot logic (Dummy / Gemini)
- Pathfinding
- Asset management

### Roman Pružinský (`xpruzir00`)
- OOP architecture design
- JSON logging system
- Parser
- Game loop and game state management

---

# 🚀 Quick Start

The project uses **Maven** for dependency management and building.

Tested on:
- Debian 12 Linux

## Compile
```bash
mvn compile
```

## Run
```bash
mvn javafx:run -Djavafx.mainClass=gui.App
```

## Build JAR
```bash
mvn package
```

## Generate Javadoc
```bash
mvn javadoc:javadoc
```

## Clean Build
```bash
mvn clean
```

---

# 🗺️ Game Mechanics

## Map & Terrain

The game board is represented as an `M × N` grid.  
Each tile contains exactly one terrain type defined in `terrain.tsv`.

### Terrain Types

- **Plain**
  - Standard traversable terrain

- **Forest**
  - Provides defensive bonus

- **Mountain**
  - Impassable for vehicles
  - Traversable only by infantry

- **Water**
  - Completely impassable for all ground units

- **City**
  - Generates `1000 funds/turn`
  - Repairs damaged units
  - Owned by players

- **Factory**
  - Allows purchasing new units

- **HQ (Headquarters)**
  - Capturing enemy HQ wins the game

---

# ⚔️ Units & Combat

All units have HP in range `1–100`.

If HP reaches `0`, the unit is destroyed.

## Unit Types

### 🏃 Infantry
- High mobility
- Low damage
- Can capture buildings

### 🚜 Tank
- High damage
- Cannot traverse mountains

### 🚀 Artillery
- Long-range attack (`2–3` tiles)
- Cannot move and attack in the same turn
- Cannot counter-attack adjacent enemies

---

# 🧮 Deterministic Combat System

Combat is fully deterministic.

Damage formula:

```text
Damage = BaseDamage × (AttackerHP / 100) × (1 - TerrainBonus × 0.1)
```

Where:
- `BaseDamage` is loaded from `units-damage.tsv`
- `TerrainBonus` is loaded from `terrain.tsv`
- Damage is rounded down

## Counter-Attack

If the defender survives and the attacker is within attack range:
- the defender automatically retaliates
- retaliation uses the defender's NEW reduced HP

This makes attacking first highly advantageous.

---

# 🏙️ Capture Mechanics

Only Infantry can capture buildings.

Buildings require `20 capture points`.

Capture action reduces points by:

```text
floor(CurrentHP / 10)
```

Examples:
- `100 HP infantry` → captures in 2 turns
- `50 HP infantry` → captures in 4 turns

If infantry leaves the tile:
- capture progress resets

---

# 🔧 Repair Mechanics

Repairs happen at the START of the player's turn.

Requirements:
- Unit stands on friendly:
  - City
  - Factory
  - HQ
- Unit must be damaged

Effects:
- Repairs `+20 HP`
- HP cannot exceed `100`

Repair Cost:
- `10%` of original unit price per `10 HP` repaired

If the player lacks money:
- no repair occurs

---

# 🚶 Movement Rules & Pathfinding

Movement uses **4-directional adjacency only**:
- North
- South
- East
- West

Diagonal movement is NOT allowed.

## Pathfinding

Implemented using:
- BFS / Dijkstra-based movement calculation

Movement cost:
- Sum of terrain costs along the path
- Starting tile cost is ignored

## Unit Blocking Rules

### Friendly Units
- Traversable
- Cannot end movement on them

### Enemy Units
- Completely block movement

This allows defensive formations.

---

# 🏭 Factory Rules

- Only one unit may occupy a tile
- If a factory tile is occupied:
  - unit production is blocked

---

# 🎮 Turn Structure

## 1. Income & Repair Phase
- Player receives city income
- Units repair on friendly buildings

## 2. Action Phase
Players may:
- Move units
- Attack
- Capture buildings
- Purchase new units

---

# 🖱️ Unit Turn Lifecycle

## 1. Select
Player selects a unit.

The engine highlights valid movement range.

## 2. Move
Player selects destination tile.

Movement of `0 tiles` is allowed.

## 3. Action
After moving, available actions are evaluated:
- `Attack`
- `Capture`
- `Wait`

## 4. Deactivate
Unit becomes inactive until next turn.

---

# 🏗️ Architecture & Design Patterns

## MVC Architecture

Game engine is completely separated from JavaFX GUI.

### Model
- `Session`
- `Game`
- `GameBoard`

### View
- `GameView`
- `Renderer`

### Controller
- `GameController`

Handles:
- input
- state transitions
- interaction logic

---

## Command Pattern

Game actions are encapsulated as command/event objects:
- movement
- attacks
- purchases
- captures

Enables:
- replay system
- step-back functionality
- detailed logging

---

## Factory Pattern

Unit creation is centralized through a factory system.

Unit definitions are loaded from:
- `units.tsv`

---

# 🗄️ Logging & Replay System

The game supports full JSON-based logging and replay.

Implemented in:
- `LogFiler.java`

## Features

### Turn Logging
Logs:
- movement
- attacks
- purchases
- end-turn events

### Replay Navigation
Supports:
- Step Forward
- Step Backward

### Hot-Swap Gameplay
During replay:
- player can take control at any moment
- replay log is truncated
- game continues from current state

---

# 🤖 AI Bots

Bots operate fully through engine APIs without mouse simulation.

Actions are processed:
- sequentially
- one action per game-loop tick

This prevents JavaFX UI freezing.

---

# 🤖 Dummy Bot (Easy)

Simple AI opponent:
- Purchases units using fixed priorities
- Infantry targets buildings
- Vehicles target nearest enemies

---

# 🧠 Gemini Bot (Medium / Heuristic AI)

Advanced heuristic AI.

Features:
- Threat Map generation
- Position evaluation
- Dynamic target prioritization
- Counter-unit purchasing

Prioritizes:
- enemy HQ
- safe movement
- healing
- combat efficiency

---

# 🤖 Bot vs Bot

Supports fully autonomous:
- Bot vs Bot matches

---

# 📝 GenAI Usage

Generative AI tools were used according to course rules.

Documentation available in:

```text
ai_audit.md
```

Contains:
- prompts used
- generated code overview
- manual verification process

---

# 📚 Git History

Git contribution history exported from repository:

```text
git_history.txt
```
