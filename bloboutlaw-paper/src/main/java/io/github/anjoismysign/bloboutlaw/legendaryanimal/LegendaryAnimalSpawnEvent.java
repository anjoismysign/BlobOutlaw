package io.github.anjoismysign.bloboutlaw.legendaryanimal;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LegendaryAnimalSpawnEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity entity;

    public LegendaryAnimalSpawnEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}