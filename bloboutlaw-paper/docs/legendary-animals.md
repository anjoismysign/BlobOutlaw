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
health: "3.0"
speed: "1.5"
scale: "1.8"
chance: "0.05"
lootTable: "zombie"
```

| Field | Type | Description |
|---|---|---|
| `type` | EntityType | Any Bukkit mob entity type (e.g. `ZOMBIE`, `SKELETON`, `CREEPER`). Must be a `Mob`. |
| `health` | String (double) | Health multiplier applied to the mob's base max health. |
| `speed` | String (double) | Movement speed multiplier. |
| `scale` | String (double) | Scale multiplier applied to the mob's scale attribute. |
| `chance` | String (double) | Spawn probability, 0.0 to 1.0. E.g. `"0.05"` = 5% chance. |
| `lootTable` | String | Identifies the loot table (JSON file in `plugins/BlobLib/LootTable/`) that replaces normal drops on death. |

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
- Legendary mobs automatically have `health`, `speed`, and `scale` multipliers applied.
- On death, the `lootTable` is used to generate drops via BlobLib's loot table system.

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
health: "3.0"
speed: "1.5"
scale: "1.8"
chance: "0.05"
lootTable: "scorpion"
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
