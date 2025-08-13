package io.github.anjoismysign.bloboutlaw.legendaryanimal;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LegendaryAnimalDeathEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity entity;
    private final List<ItemStack> drops;

    public LegendaryAnimalDeathEvent(LivingEntity entity,
                                     List<ItemStack> drops) {
        this.entity = entity;
        this.drops = drops;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public List<ItemStack> getDrops() {
        return drops;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}