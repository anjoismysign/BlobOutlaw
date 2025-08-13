package io.github.anjoismysign.bloboutlaw.legendaryanimal;

import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.bloboutlaw.goal.LegendaryAnimalGoal;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record LegendaryAnimal(@NotNull String identifier,
                              @NotNull EntityType type,
                              double health,
                              double speed,
                              double scale,
                              double chance,
                              @NotNull List<String> legendaryDrops) implements DataAsset {

    public List<ItemStack> getLegendaryDrops() {
        return legendaryDrops
                .stream()
                .map(TranslatableItem::by)
                .filter(Objects::nonNull)
                .map(TranslatableItem::getClone)
                .toList();
    }

    public void instantiate(@NotNull Mob mob) {
        if (mob.getType() != type)
            return;
        Bukkit.getMobGoals().addGoal(mob, 3, new LegendaryAnimalGoal(mob));
        AttributeInstance healthInstance = mob.getAttribute(Attribute.MAX_HEALTH);
        if (healthInstance != null) {
            healthInstance.setBaseValue(healthInstance.getBaseValue() * health);
            mob.setHealth(healthInstance.getValue());
        }
        AttributeInstance speedInstance = mob.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedInstance != null)
            speedInstance.setBaseValue(speedInstance.getBaseValue() * speed);
        AttributeInstance scaleInstance = mob.getAttribute(Attribute.SCALE);
        if (scaleInstance != null)
            scaleInstance.setBaseValue(scaleInstance.getBaseValue() * scale);
    }

    public record Info(@NotNull EntityType type,
                       @NotNull String health,
                       @NotNull String speed,
                       @NotNull String scale,
                       @NotNull String chance,
                       @NotNull List<String> legendaryDrops) implements IdentityGenerator<LegendaryAnimal> {

        @NotNull
        @Override
        public LegendaryAnimal generate(@NotNull String identifier) {
            Class<? extends Entity> entityClass = type.getEntityClass();
            if (entityClass == null)
                throw new IllegalArgumentException("Entity type for '" + identifier + "' is null!");
            if (!Mob.class.isAssignableFrom(entityClass))
                throw new IllegalArgumentException("Entity type for '" + identifier + "' is not a Mob!");
            double health = Double.parseDouble(this.health);
            double speed = Double.parseDouble(this.speed);
            double scale = Double.parseDouble(this.scale);
            double chance = Double.parseDouble(this.chance);
            return new LegendaryAnimal(identifier, type, health, speed, scale, chance, legendaryDrops);
        }
    }
}
