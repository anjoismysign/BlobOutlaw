package io.github.anjoismysign.bloboutlaw.event;

import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlaw;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BountyClaimEvent extends BountyEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public BountyClaimEvent(@NotNull BukkitOutlaw serializable,
                            double amount) {
        super(serializable, amount);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
