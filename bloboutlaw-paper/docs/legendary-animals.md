# Legendary Animals & Spawners

Configuration files live in `plugins/BlobOutlaw/` under two subdirectories:

| Directory | Contents |
|---|---|
| `legendary_animal/` | Legendary animal type definitions |
| `legendary_animal_spawner/` | Spawner configurations per animal |

Each file is a `.yml` named after the animal identifier (e.g. `zombie.yml`, `skeleton.yml`). Files are auto-created on first run — populate them manually or via another plugin.

---

## Legendary Animal (`legendary_animal/<identifier>.yml`)

```yaml
type: ZOMBIE
chance: "0.005"
defaultIsFollower: true
awareDistance: 32.0
attackDistance: 3.0
defaultEntity:
  attributes:
    movement_speed:
      key: "minecraft:movement_speed"
      operation: ADD_NUMBER
      amount: 0.1
      equipmentSlotGroup: any
  lootTable: ""
  model: blackant
legendaryEntity:
  attributes:
    max_health:
      key: "minecraft:max_health"
      operation: ADD_NUMBER
      amount: 30.0
      equipmentSlotGroup: any
    movement_speed:
      key: "minecraft:movement_speed"
      operation: ADD_NUMBER
      amount: 0.15
      equipmentSlotGroup: any
    scale:
      key: "minecraft:scale"
      operation: ADD_NUMBER
      amount: 0.25
      equipmentSlotGroup: any
  lootTable: ""
  model: blackant
```

| Field | Type | Description |
|---|---|---|
| `type` | EntityType | Any Bukkit mob entity type (e.g. `ZOMBIE`, `SKELETON`, `CREEPER`). Must be a `Mob`. |
| `chance` | String (double) | Spawn probability, 0.0 to 1.0. E.g. `"0.005"` = 0.5% chance. |
| `defaultIsFollower` | boolean | If `true` (default `false`), non-legendary mobs of this type follow nearby legendary mobs of the same type using `FollowerAnimalGoal`. |
| `awareDistance` | double | Detection radius in blocks. For legendaries: used to detect other legendary mobs and players. For followers: used to detect the legendary mob to follow. |
| `attackDistance` | double | Attack range in blocks for legendary mobs. Within this distance the legendary teleports and attacks; beyond it, pathfinds toward the target. |
| `defaultEntity` | EntityBean | Configuration applied to normal (non-legendary) spawns of this type. |
| `legendaryEntity` | EntityBean | Configuration applied when the chance roll succeeds. |

### EntityBean

Applied to the mob when it spawns.

| Field | Type | Description                                                                                                                                                            |
|---|---|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `attributes` | Map of `attributeKey : AttributeModifierBean` | Attribute modifiers to apply to the mob. Each key is the attribute name (e.g. `max_health`, `movement_speed`, `scale`). See `AttributeModifierBean` below.             |
| `lootTable` | String | Identifies the loot table (JSON file in `plugins/BlobLib/LootTable/`) that replaces normal drops on death. Leave empty (`""`) to skip (no drops and no vanilla drops). |
| `model` | String | BetterModel model name to apply to the entity on spawn. Leave empty (`""`) for no custom model.                                                                        |

### AttributeModifierBean

Each attribute entry is an object with the following fields. Inline YAML flow style (`{key: "", operation: ADD_NUMBER, amount: 0, equipmentSlotGroup: any}`) is also accepted.

| Field | Type | Description |
|---|---|---|
| `key` | String | Namespaced key identifying this modifier (e.g. `"minecraft:max_health"`). Used as the modifier's unique identifier. |
| `operation` | AttributeModifier.Operation | How the `amount` is applied. One of `ADD_NUMBER`, `ADD_SCALAR`, or `MULTIPLY_SCALAR_1`. |
| `amount` | double | The modifier value. Positive for increases, negative for decreases. |
| `equipmentSlotGroup` | String | Slot group this modifier applies to. Typically `any` for universal modifiers. See Minecraft's `EquipmentSlotGroup` values. |

---

## Legendary Animal Spawner (`legendary_animal_spawner/<identifier>.yml`)

The identifier must match a legendary animal defined in `legendary_animal/`.

```yaml
minDelay: 1200
maxDelay: 3600
maxCount: 3
blocks: []
```

| Field | Type | Description |
|---|---|---|
| `minDelay` | int | Minimum ticks between spawns (20 ticks = 1 second). |
| `maxDelay` | int | Maximum ticks between spawns. |
| `maxCount` | int | Maximum number of legendary mobs from this spawner alive concurrently. |
| `blocks` | List of Strings | Serialized block locations. Populated **in-game** by using a spawn egg on a vanilla `SPAWNER` block. Leave as `[]` in config files. |

---

## In-Game Setup

1. Place a vanilla `SPAWNER` block.
2. Hold the corresponding spawn egg (e.g. `ZOMBIE_SPAWN_EGG`) and right-click the spawner.
3. The block location is added to `blocks` and the config is saved automatically.
4. The spawner begins spawning legendary mobs of that type between `minDelay` and `maxDelay` ticks.

---

## How Natural Spawning Works

- Natural mob spawns are **cancelled** for configured types.
- Only `SPAWNER_EGG` and `CUSTOM` spawn reasons are allowed to pass through.
- Each time a mob of a configured type would spawn, the `chance` roll determines whether it becomes legendary.
- If legendary, the `legendaryEntity` bean is applied (attributes, model, loot table); otherwise `defaultEntity` is applied instead.
- Attribute modifiers from the chosen `EntityBean` are attached to the mob on spawn.
- On death, the mob's `lootTable` is used to generate drops via BlobLib's loot table system.
- **AI Goals:**
  - Legendary mobs receive a `LegendaryAnimalGoal` (priority 3): they hunt other legendary mobs of the same type within `awareDistance`, and if none are found, attack players within the same radius. Within `attackDistance`, they teleport to the target and strike; outside it, they pathfind at speed 2.0.
  - If `defaultIsFollower` is `true`, non-legendary mobs receive a `FollowerAnimalGoal` (priority 1): they follow nearby legendary mobs of the same type within `awareDistance`, forming a pack that trails the leader.

---

## Loot Tables

Loot tables define a weighted, randomised pool of items that can be dropped when a legendary animal dies. BlobLib reads loot tables from JSON files placed in the `LootTable` directory and exposes them through `BlobLibLootAPI`.

### Directory & Discovery

- **Runtime directory:** `plugins/BlobLib/LootTable/`
- **File format:** `.json`
- **Identifier:** the filename without extension (e.g. `zombie.json` → `zombie`)
- **Subdirectories:** supported — all `.json` files in subfolders are loaded recursively
- **Duplicate identifiers:** logged and skipped (first loaded wins)
- **Bundled example:** `BlobLib.Example.json` is written automatically on first run if absent

### JSON Schema

```json
{
  "pools": [
    {
      "rolls": <int>,
      "entries": [
        {
          "type": "<entry-type>",
          "name": "<namespaced-id>",
          "weight": <int>,
          "functions": [
            {
              "function": "<function-type>",
              "count": {
                "min": <int>,
                "max": <int>
              }
            }
          ]
        }
      ]
    }
  ]
}
```

#### Top level

| Field   | Type  | Description                           |
|---------|-------|---------------------------------------|
| `pools` | array | One or more loot pools (see below).   |

#### Pool object

| Field     | Type  | Description                                              |
|-----------|-------|----------------------------------------------------------|
| `rolls`   | int   | Number of times this pool is rolled per generation.      |
| `entries` | array | Weighted entries that can be selected on each roll.      |

#### Entry object

| Field       | Type    | Description                                                    |
|-------------|---------|----------------------------------------------------------------|
| `type`      | string  | Entry type key (see below).                                    |
| `name`      | string  | Namespaced item ID (for `minecraft:item`) or translatable key. |
| `weight`    | int     | Relative weight (defaults to `1` if omitted).                  |
| `functions` | array   | Optional list of functions to apply after selection.           |

#### Function object (currently only `minecraft:set_count`)

| Field      | Type   | Description                                          |
|------------|--------|------------------------------------------------------|
| `function` | string | Function type key.                                   |
| `count`    | object | Range object with `min` and `max` (both int).        |

### Entry Types

| Type key                      | `name` expected value                | Behaviour                                     |
|-------------------------------|--------------------------------------|-----------------------------------------------|
| `minecraft:item`              | Item namespaced ID (e.g. `minecraft:diamond`) | Creates an `ItemStack` from the vanilla item registry. |
| `minecraft:empty`             | Ignored                              | Entry is skipped (produces nothing).          |
| `bloblib:translatableitem`    | Translatable item key                 | Looks up a `TranslatableItem` and returns a clone.    |

### Functions

#### `minecraft:set_count`

Randomises the stack amount within a range.

| Parameter | Type | Description |
|-----------|------|-------------|
| `count.min` | int | Minimum stack size (inclusive). |
| `count.max` | int | Maximum stack size (inclusive). |

If omitted from an entry, the item's default stack size (typically `1`) is used.

### BlobLibLootAPI

Access the API via the singleton:

```java
BlobLibLootAPI api = BlobLibLootAPI.getInstance();
```

#### `spawn(Location location, String identifier)`

Drops the loot table's items naturally at the given world location.

| Parameter    | Type     | Description                       |
|--------------|----------|-----------------------------------|
| `location`   | Location | Where items are dropped.          |
| `identifier` | String   | Loot table identifier.            |

Returns `true` if the loot table exists and items were dropped.

#### `generateLoot(String identifier, String locale)`

Generates the loot and returns the `Collection<ItemStack>` without spawning or inventory insertion.

| Parameter    | Type   | Description                                    |
|--------------|--------|------------------------------------------------|
| `identifier` | String | Loot table identifier.                         |
| `locale`     | String | Locale string or `null` to skip localization.  |

Returns an empty collection if the loot table identifier is not registered.

### LootTable.by(String key)

Static convenience method on the record itself:

```java
LootTable table = LootTable.by("zombie");
```

---

## Example: Scorpion with Weighted Drops

To replicate the behaviour described in the original request — a legendary scorpion that drops `BlobOutlaw.Baton` at 5% and 1–4 `Animals.Legendary-Chitin` always:

**`plugins/BlobOutlaw/legendary_animal/scorpion.yml`**
```yaml
type: SCORPION
chance: "0.05"
defaultEntity:
  attributes:
    movement_speed:
      key: "minecraft:movement_speed"
      operation: ADD_NUMBER
      amount: 0.0
      equipmentSlotGroup: any
  lootTable: ""
  model: ""
legendaryEntity:
  attributes:
    max_health:
      key: "minecraft:max_health"
      operation: ADD_SCALAR
      amount: 2.0
      equipmentSlotGroup: any
    movement_speed:
      key: "minecraft:movement_speed"
      operation: ADD_SCALAR
      amount: 0.5
      equipmentSlotGroup: any
    scale:
      key: "minecraft:scale"
      operation: ADD_SCALAR
      amount: 0.8
      equipmentSlotGroup: any
  lootTable: "scorpion"
  model: ""
```

**`plugins/BlobLib/LootTable/scorpion.json`**
```json
{
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "bloblib:translatableitem",
          "name": "BlobOutlaw.Baton",
          "weight": 5,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": {
                "min": 1,
                "max": 1
              }
            }
          ]
        },
        {
          "type": "minecraft:empty",
          "name": "",
          "weight": 95
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "bloblib:translatableitem",
          "name": "Animals.Legendary-Chitin",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": {
                "min": 1,
                "max": 4
              }
            }
          ]
        }
      ]
    }
  ]
}
```

This uses two pools — the first rolls the baton at 5% (5 weight vs 95 empty), the second always drops 1–4 chitin.

---

## Commands

The plugin registers commands under the `bloboutlaw` admin command for live reloading (see plugin's main command documentation).
