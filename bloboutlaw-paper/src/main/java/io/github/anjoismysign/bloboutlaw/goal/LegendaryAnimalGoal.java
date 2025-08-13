package io.github.anjoismysign.bloboutlaw.goal;


import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.legendaryanimal.LegendaryAnimalManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;


public class LegendaryAnimalGoal implements Goal<@NotNull Mob> {
    private static final GoalKey<@NotNull Mob> key = GoalKey.of(Mob.class, new NamespacedKey(BlobOutlaw.getInstance(), "alpha_goal"));
    private static final Random random = new Random();
    private final Mob legendaryAnimal;
    private LivingEntity enemy;
    private int cooldown;

    public LegendaryAnimalGoal(Mob alpha) {
        this.legendaryAnimal = alpha;
    }

    @Override
    public boolean shouldActivate() {
        Location location = legendaryAnimal.getLocation();
        for (Entity entity : location.getNearbyEntities(15, 15, 15)) {
            if (entity.getType() != legendaryAnimal.getType())
                continue;
            UUID uuid = entity.getUniqueId();
            if (uuid.equals(legendaryAnimal.getUniqueId()))
                continue;
            @Nullable LivingEntity legendary = LegendaryAnimalManager.getSpawned(uuid);
            if (legendary == null)
                continue;
            enemy = legendary;
            return true;
        }
        if (enemy == null || !enemy.isValid())
            for (Entity entity : location.getNearbyEntities(15, 15, 15)) {
                if (entity.getType() != EntityType.PLAYER)
                    continue;
                Player player = Objects.requireNonNull(Bukkit.getPlayer(entity.getUniqueId()), "Entity#getUniqueId doesn't point to a valid player");
                if (player.getGameMode().isInvulnerable())
                    continue;
                if (player.isInvulnerable())
                    continue;
                enemy = player;
                return true;
            }
        return false;
    }

    @Override
    public boolean shouldStayActive() {
        return enemy.isValid() && legendaryAnimal.getLocation().distanceSquared(this.enemy.getLocation()) >= 400;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        this.enemy = null;
    }

    @Override
    public void tick() {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (legendaryAnimal.getLocation().distance(enemy.getLocation()) >= 7.84) {
            legendaryAnimal.getPathfinder().moveTo(enemy);
            return;
        }
        legendaryAnimal.teleport(enemy);
            legendaryAnimal.attack(enemy);
        this.cooldown = generateCooldown();
    }

    @Override
    public @NotNull
    GoalKey<@NotNull Mob> getKey() {
        return key;
    }

    @Override
    public @NotNull
    EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET);
    }

    private int generateCooldown() {
        return random.nextInt(20);
    }
}
