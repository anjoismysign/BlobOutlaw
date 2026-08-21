package io.github.anjoismysign.bloboutlaw.event;

import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawProfile;
import io.github.anjoismysign.bloboutlaw.law.Law;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BehaviourFlagEvent extends OutlawEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final @Nullable Entity victim;
    private final @Nullable Law.Crime facingCharge;

    public BehaviourFlagEvent(@NotNull BukkitOutlawProfile serializable,
                              @Nullable Law.Crime facingCharge,
                              @Nullable Entity victim) {
        super(serializable, false);
        this.victim = victim;
        this.facingCharge = facingCharge;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    /**
     * @return The victim of this behavior, null if nobody is affected
     */
    public @Nullable Entity getVictim() {
        return victim;
    }

    /**
     * @return The facing charge, or null if the outlaw no longer has charges (were cleared)
     */
    public @Nullable Law.Crime getFacingCharge() {
        return facingCharge;
    }
}
