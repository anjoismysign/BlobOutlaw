package io.github.anjoismysign.bloboutlaw.goal;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.legendaryanimal.LegendaryAnimalManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class FollowerAnimalGoal implements Goal<@NotNull Mob> {

    private static final GoalKey<@NotNull Mob> key = GoalKey.of(Mob.class, new NamespacedKey(BlobOutlaw.getInstance(), "follower_goal"));

    private Mob legendaryEntity;
    private final Mob entity;
    private final double awareDistance;

    public FollowerAnimalGoal(Mob entity, double awareDistance) {
        this.entity = entity;
        this.awareDistance = awareDistance;
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
            @Nullable LivingEntity livingEntity = LegendaryAnimalManager.getSpawned(uuid);
            if (!(livingEntity instanceof Mob mob)) {
                continue;
            }
            if (location.distanceSquared(mob.getLocation()) < awareDistance * awareDistance) {
                this.legendaryEntity = mob;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldStayActive() {
        return legendaryEntity.getLocation().distanceSquared(entity.getLocation()) <= awareDistance * awareDistance;
    }

    @Override
    public void stop() {
        entity.getPathfinder().stopPathfinding();
        legendaryEntity = null;
    }

    @Override
    public void tick() {
        entity.getPathfinder().moveTo(legendaryEntity);
    }

    @Override
    public @NotNull
    GoalKey<@NotNull Mob> getKey() {
        return key;
    }

    @Override
    public @NotNull
    EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }

}
