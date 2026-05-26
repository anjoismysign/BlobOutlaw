package io.github.anjoismysign.bloboutlaw.legendaryanimal;

import io.github.anjoismysign.bloblib.entities.AttributeModifierBean;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.goal.LegendaryAnimalGoal;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record LegendaryAnimal(@NotNull String identifier,
                              @NotNull EntityType type,
                              double chance,
                              @NotNull RuntimeEntityBean defaultEntity,
                              @NotNull RuntimeEntityBean legendaryEntity) implements DataAsset {

    public void instantiate(@NotNull Mob mob, boolean isLegendary) {
        var logger = BlobOutlaw.getInstance().getLogger();
        if (mob.getType() != type) {
            logger.info(mob.getType() + " (" + mob.getUniqueId() + ") is not the same type of '" + identifier + "' LegendaryAnimal");
            return;
        }
        if (isLegendary) {
            Bukkit.getMobGoals().addGoal(mob, 3, new LegendaryAnimalGoal(mob));
        }
        RuntimeEntityBean entityBean = isLegendary ? legendaryEntity : defaultEntity;
        Map<Attribute, AttributeModifier> attributes = entityBean.attributes;
        attributes.forEach((attribute, modifier) -> {
            @Nullable AttributeInstance instance = mob.getAttribute(attribute);
            if (instance == null) {
                return;
            }
            instance.addModifier(modifier);
        });
    }

    public static final class Info implements IdentityGenerator<LegendaryAnimal> {
        private EntityType type;
        private double chance;
        private @NotNull EntityBean defaultEntity;
        private @NotNull EntityBean legendaryEntity;

        @NotNull
        @Override
        public LegendaryAnimal generate(@NotNull String identifier) {
            Class<? extends Entity> entityClass = type.getEntityClass();
            if (entityClass == null)
                throw new IllegalArgumentException("Entity type for '" + identifier + "' is null!");
            if (!Mob.class.isAssignableFrom(entityClass))
                throw new IllegalArgumentException("Entity type for '" + identifier + "' is not a Mob!");
            RuntimeEntityBean runtimeDefaultEntity = defaultEntity.toRuntimeEntityBean();
            RuntimeEntityBean runtimeLegendaryEntity = legendaryEntity.toRuntimeEntityBean();
            return new LegendaryAnimal(identifier, type, chance, runtimeDefaultEntity, runtimeLegendaryEntity);
        }


        public EntityType getType() {
            return type;
        }

        public void setType(EntityType type) {
            this.type = type;
        }

        public double getChance() {
            return chance;
        }

        public void setChance(double chance) {
            this.chance = chance;
        }

        public @NotNull EntityBean getDefaultEntity() {
            return defaultEntity;
        }

        public void setDefaultEntity(@NotNull EntityBean defaultEntity) {
            this.defaultEntity = defaultEntity;
        }

        public @NotNull EntityBean getLegendaryEntity() {
            return legendaryEntity;
        }

        public void setLegendaryEntity(@NotNull EntityBean legendaryEntity) {
            this.legendaryEntity = legendaryEntity;
        }
    }

    public record RuntimeEntityBean(Map<Attribute, AttributeModifier> attributes,
                                    String lootTable,
                                    String model) {

        public EntityBean toEntityBean() {
            EntityBean entityBean = new EntityBean();
            entityBean.setAttributes(AttributeModifierBean.serializeAttributes(attributes));
            entityBean.setLootTable(lootTable);
            entityBean.setModel(model);
            return entityBean;
        }

    }

    public static final class EntityBean {
        private Map<String, AttributeModifierBean> attributes;
        private String lootTable;
        private String model;

        public RuntimeEntityBean toRuntimeEntityBean() {
            return new RuntimeEntityBean(AttributeModifierBean.deserializeAttributes(attributes), lootTable, model);
        }

        public Map<String, AttributeModifierBean> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, AttributeModifierBean> attributes) {
            this.attributes = attributes;
        }

        public String getLootTable() {
            return lootTable;
        }

        public void setLootTable(String lootTable) {
            this.lootTable = lootTable;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
