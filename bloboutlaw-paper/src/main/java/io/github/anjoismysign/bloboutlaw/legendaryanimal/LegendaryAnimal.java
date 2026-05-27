package io.github.anjoismysign.bloboutlaw.legendaryanimal;

import com.destroystokyo.paper.entity.ai.MobGoals;
import io.github.anjoismysign.bloblib.entities.AttributeModifierBean;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.goal.FollowerAnimalGoal;
import io.github.anjoismysign.bloboutlaw.goal.LegendaryAnimalGoal;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.bukkit.Bukkit;
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
                              boolean defaultIsFollower,
                              double awareDistance,
                              double attackDistance,
                              @NotNull RuntimeEntityBean defaultEntity,
                              @NotNull RuntimeEntityBean legendaryEntity) implements DataAsset {

    public void instantiate(@NotNull Mob mob, boolean isLegendary) {
        var logger = BlobOutlaw.getInstance().getLogger();
        if (mob.getType() != type) {
            logger.info(mob.getType() + " (" + mob.getUniqueId() + ") is not the same type of '" + identifier + "' LegendaryAnimal");
            return;
        }
        MobGoals mobGoals = Bukkit.getMobGoals();
        if (isLegendary) {
            mobGoals.addGoal(mob, 3, new LegendaryAnimalGoal(mob, awareDistance, attackDistance));
        } else {
            if (defaultIsFollower){
                mobGoals.addGoal(mob, 1, new FollowerAnimalGoal(mob, awareDistance));
            }
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
        private boolean defaultIsFollower;
        private double awareDistance;
        private double attackDistance;
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
            return new LegendaryAnimal(identifier, type, chance, defaultIsFollower, awareDistance, attackDistance, runtimeDefaultEntity, runtimeLegendaryEntity);
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

        public boolean isDefaultIsFollower() {
            return defaultIsFollower;
        }

        public void setDefaultIsFollower(boolean defaultIsFollower) {
            this.defaultIsFollower = defaultIsFollower;
        }

        public double getAwareDistance() {
            return awareDistance;
        }

        public void setAwareDistance(double awareDistance) {
            this.awareDistance = awareDistance;
        }

        public double getAttackDistance() {
            return attackDistance;
        }

        public void setAttackDistance(double attackDistance) {
            this.attackDistance = attackDistance;
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
