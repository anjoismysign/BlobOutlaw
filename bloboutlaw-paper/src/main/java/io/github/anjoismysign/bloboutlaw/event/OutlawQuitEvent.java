package io.github.anjoismysign.bloboutlaw.event;

import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawProfile;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called on main thread
 */
public class OutlawQuitEvent extends OutlawEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public OutlawQuitEvent(@NotNull BukkitOutlawProfile bukkitOutlawProfile) {
        super(bukkitOutlawProfile, false);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
