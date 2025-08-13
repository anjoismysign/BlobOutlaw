package io.github.anjoismysign.bloboutlaw.legendaryanimal;

import io.github.anjoismysign.bloblib.utilities.SerializationLib;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.director.OutlawManager;
import io.github.anjoismysign.bloboutlaw.director.OutlawManagerDirector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LegendaryAnimalManager extends OutlawManager implements Listener {
    private static LegendaryAnimalManager instance;
    private final Map<UUID, Mob> legendaryAnimals = new HashMap<>();
    private final Map<Block, LegendaryAnimalSpawner.Task> tasks = new HashMap<>();
    private final Map<EntityType, LegendaryAnimalSpawner> spawners = new HashMap<>();
    private final Map<EntityType, LegendaryAnimal> types = new HashMap<>();

    public LegendaryAnimalManager(OutlawManagerDirector managerDirector) {
        super(managerDirector);
        instance = this;
        reload();
        Bukkit.getPluginManager().registerEvents(this, managerDirector.getPlugin());
    }

    @Override
    public void reload() {
        types.keySet()
                .forEach(entityType -> Bukkit.getWorlds()
                        .forEach(world -> world.getEntities()
                                .forEach(entity -> {
                                    if (entity.getType() == entityType)
                                        entity.remove();
                                })));
        types.clear();
        tasks.values().forEach(task -> {
            task.task().cancel();
        });
        spawners.clear();
        tasks.clear();
        Bukkit.getScheduler().runTask(getPlugin(),()->{
            BlobOutlaw blobOutlaw = BlobOutlaw.getInstance();
            blobOutlaw.getLegendaryAnimalManager().forEach(animal -> {
                types.put(animal.type(), animal);
            });
            blobOutlaw.getLegendaryAnimalSpawnerManager().forEach(spawner->{
                @Nullable LegendaryAnimal legendaryAnimal = spawner.legendaryAnimal();
                if (legendaryAnimal == null)
                    return;
                spawners.put(legendaryAnimal.type(), spawner);
                List<Block> blocks = spawner.fetchBlocks();
                blocks.forEach(block -> {
                    tasks.put(block, LegendaryAnimalSpawner.Task.of(spawner,block));
                });
            });
        });
    }

    @EventHandler
    public void remove(EntityRemoveEvent event){
        Entity entity = event.getEntity();
        UUID uuid = entity.getUniqueId();
        @Nullable LegendaryAnimalSpawner.Task belonging = tasks.values()
                .stream()
                .filter(task -> {
                    return task.entities().contains(uuid);
                })
                .findFirst()
                .orElse(null);
        if (belonging == null)
            return;
        belonging.entities().remove(uuid);
    }

    @EventHandler
    public void spawnerSet(PlayerInteractEvent event){
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        @Nullable Block block = event.getClickedBlock();
        if (block == null)
            return;
        if (block.getType() != Material.SPAWNER)
            return;

        @Nullable ItemStack hand = event.getItem();
        if (hand == null)
            return;
        String materialName = hand.getType().name();
        if (!materialName.endsWith("_SPAWN_EGG")) {
            return;
        }
        EntityType entityType = EntityType.valueOf(materialName.replace("_SPAWN_EGG", ""));
        @Nullable LegendaryAnimalSpawner spawner = spawners.get(entityType);
        if (spawner == null)
            return;
        event.setCancelled(true);
        spawner.blocksReferences().add(SerializationLib.serialize(block.getLocation()));
        getPlugin().getLegendaryAnimalSpawnerManager().add(spawner.generation());
        block.setType(Material.AIR);
    }

    @EventHandler
    public void cancelSpawn(CreatureSpawnEvent event){
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.CUSTOM || reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;
        event.setCancelled(true);
    }

    @EventHandler (ignoreCancelled = true)
    public void spawn(CreatureSpawnEvent event) {
        EntityType entityType = event.getEntityType();
        @Nullable LegendaryAnimal legendaryAnimal = types.get(entityType);
        if (legendaryAnimal == null)
            return;
        double random = Math.random();
        if (random > legendaryAnimal.chance()) {
            return;
        }
        Mob entity = (Mob) event.getEntity();
        entity.setPersistent(false);
        legendaryAnimal.instantiate(entity);
        legendaryAnimals.put(entity.getUniqueId(), entity);
        LegendaryAnimalSpawnEvent legendaryAnimalSpawnEvent = new LegendaryAnimalSpawnEvent(entity);
        Bukkit.getPluginManager().callEvent(legendaryAnimalSpawnEvent);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity normal = event.getEntity();
        @Nullable LegendaryAnimal legendaryAnimal = types.get(normal.getType());
        if (legendaryAnimal == null)
            return;
        @Nullable LivingEntity legendary = legendaryAnimals.get(normal.getUniqueId());
        if (legendary == null) {
            return;
        }
        List<ItemStack> drops = event.getDrops();
        drops.clear();
        drops.addAll(legendaryAnimal.getLegendaryDrops());
        LegendaryAnimalDeathEvent legendaryAnimalDeathEvent = new LegendaryAnimalDeathEvent(normal, drops);
        Bukkit.getPluginManager().callEvent(legendaryAnimalDeathEvent);
    }

    /**
     * Retrieves the spawned legendary animal entity associated with the given UUID.
     *
     * @param uuid the unique identifier of the entity to check
     * @return the spawned legendary animal entity if present; otherwise, null
     */
    @Nullable
    public static LivingEntity getSpawned(@NotNull UUID uuid) {
        if (instance == null)
            return null;
        return instance.legendaryAnimals.get(uuid);
    }

    /**
     * Retrieves the legendary animal configuration for the given entity type.
     *
     * @param entityType the entity type to look up
     * @return the legendary animal configuration if available; otherwise, null
     */
    @Nullable
    public static LegendaryAnimal getConfig(@NotNull EntityType entityType){
        if (instance == null)
            return null;
        return instance.types.get(entityType);
    }

    /**
     * Retrieves the legendary animal configuration based on a unique identifier.
     * This method searches through all legendary animals and returns the one
     * that matches the given identifier. If no match is found, it returns null.
     *
     * @param identifier the unique identifier of the legendary animal
     * @return the legendary animal configuration if found; otherwise, null
     */
    @Nullable
    public static LegendaryAnimal getConfig(@NotNull String identifier){
        if (instance == null)
            return null;
        return instance.types.values()
                .stream().
                filter(legendaryAnimal -> {
                    return legendaryAnimal.identifier().equals(identifier);
                })
                .findFirst()
                .orElse(null);
    }

}