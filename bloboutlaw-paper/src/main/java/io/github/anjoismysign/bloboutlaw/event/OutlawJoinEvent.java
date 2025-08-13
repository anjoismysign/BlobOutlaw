package io.github.anjoismysign.bloboutlaw.event;

import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlaw;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OutlawJoinEvent extends OutlawEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public OutlawJoinEvent(@NotNull BukkitOutlaw serializable) {
        super(serializable, false);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
