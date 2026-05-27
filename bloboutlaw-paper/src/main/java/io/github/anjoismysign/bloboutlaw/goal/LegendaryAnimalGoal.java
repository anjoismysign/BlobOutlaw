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
    private static final GoalKey<@NotNull Mob> key = GoalKey.of(Mob.class, new NamespacedKey(BlobOutlaw.getInstance(), "legendary_goal"));
    private static final Random random = new Random();
    private final Mob entity;
    private final double awareDistance;
    private final double attackDistance;
    private LivingEntity enemy;
    private int cooldown;

    public LegendaryAnimalGoal(Mob entity, double awareDistance, double attackDistance) {
        this.entity = entity;
        this.awareDistance = awareDistance;
        this.attackDistance = attackDistance;
    }

    @Override
    public boolean shouldActivate() {
        Location location = entity.getLocation();
        for (Entity entity : location.getNearbyEntities(awareDistance, awareDistance, awareDistance)) {
            if (entity.getType() != this.entity.getType()) {
                continue;
            }
            UUID uuid = entity.getUniqueId();
            if (uuid.equals(this.entity.getUniqueId())) {
                continue;
            }
            @Nullable LivingEntity legendary = LegendaryAnimalManager.getSpawned(uuid);
            if (legendary == null) {
                continue;
            }
            enemy = legendary;
            return true;
        }
        if (enemy == null || !enemy.isValid())
            for (Entity entity : location.getNearbyEntities(awareDistance, awareDistance, awareDistance)) {
                if (entity.getType() != EntityType.PLAYER) {
                    continue;
                }
                Player player = Objects.requireNonNull(Bukkit.getPlayer(entity.getUniqueId()), "Entity#getUniqueId doesn't point to a valid player");
                if (player.getGameMode().isInvulnerable()) {
                    continue;
                }
                if (player.isInvulnerable()) {
                    continue;
                }
                enemy = player;
                return true;
            }
        return false;
    }

    @Override
    public boolean shouldStayActive() {
        return enemy.isValid() && entity.getLocation().distanceSquared(this.enemy.getLocation()) >= awareDistance * awareDistance;
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
        if (entity.getLocation().distanceSquared(enemy.getLocation()) >= attackDistance * attackDistance) {
            entity.getPathfinder().moveTo(enemy, 2.0);
            return;
        }
        entity.teleport(enemy);
        entity.attack(enemy);
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
