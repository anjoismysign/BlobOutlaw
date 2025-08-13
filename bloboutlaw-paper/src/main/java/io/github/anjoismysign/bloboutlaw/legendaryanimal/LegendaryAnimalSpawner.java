package io.github.anjoismysign.bloboutlaw.legendaryanimal;

import io.github.anjoismysign.anjo.entities.Uber;
import io.github.anjoismysign.bloblib.utilities.SerializationLib;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.IdentityGeneration;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public record LegendaryAnimalSpawner(@NotNull String identifier,
                                     @NotNull List<String> blocksReferences,
                                     int minDelay,
                                     int maxDelay,
                                     int maxCount)
        implements DataAsset {

    @Nullable
    public LegendaryAnimal legendaryAnimal(){
        @Nullable LegendaryAnimal legendaryAnimal = LegendaryAnimalManager.getConfig(identifier);
        return legendaryAnimal;
    }

    @NotNull
    public List<Block> fetchBlocks(){
        return blocksReferences.stream()
                .map(SerializationLib::deserializeLocation)
                .map(Location::getBlock)
                .toList();
    }

    @NotNull
    public Mob spawn(@NotNull Block block){
        Location location = block.getLocation().toCenterLocation();
        Entity entity = location.getWorld().spawnEntity(location, legendaryAnimal().type());
        return (Mob) entity;
    }

    @NotNull
    public IdentityGeneration<LegendaryAnimalSpawner> generation(){
        Info info = new Info(minDelay, maxDelay, maxCount, blocksReferences);
        return new IdentityGeneration<>(identifier, info);
    }

    public record Info(int minDelay,
                       int maxDelay,
                       int maxCount,
                       @NotNull List<String> blocks)
            implements IdentityGenerator<LegendaryAnimalSpawner> {
        @Override
        public @NotNull LegendaryAnimalSpawner generate(@NotNull String identifier) {
            return new LegendaryAnimalSpawner(
                    identifier,
                    new ArrayList<>(blocks),
                    minDelay,
                    maxDelay,
                    maxCount);
        }
    }

    public record Task(@NotNull BukkitTask task,
                       @NotNull LegendaryAnimalSpawner spawner,
                       @NotNull Block block,
                       @NotNull Uber<Integer> delay,
                       @NotNull List<UUID> entities){

        private static final Random RANDOM = new Random();

        @NotNull
        private static Integer delay(@NotNull LegendaryAnimalSpawner spawner){
            return RANDOM.nextInt(spawner.minDelay, spawner.maxDelay+1);
        }

        @NotNull
        public static Task of(@NotNull LegendaryAnimalSpawner spawner,
                              @NotNull Block block) {
            Uber<Integer> delay = Uber.drive(delay(spawner));
            int maxCount = spawner.maxCount;
            List<UUID> entities = new ArrayList<>();
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    int currentDelay = delay.thanks();
                    int currentCount = entities.size();
                    if (currentDelay == 0 && currentCount < maxCount){
                        entities.add(spawner.spawn(block).getUniqueId());
                        delay.talk(delay(spawner));
                        return;
                    }
                    delay.talk(currentDelay-1);
                }
            }.runTaskTimer(BlobOutlaw.getInstance(),
                    0, 1);
            return new Task(task,spawner,block,delay,entities);
        }
    }

}
