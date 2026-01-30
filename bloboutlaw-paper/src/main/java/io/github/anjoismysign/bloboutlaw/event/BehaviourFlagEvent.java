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
    private double transactionAmount;
    private boolean isCriminal;

    public BehaviourFlagEvent(@NotNull BukkitOutlawProfile serializable,
                              @Nullable Law.Crime facingCharge,
                              double transactionAmount,
                              boolean isCriminal,
                              @Nullable Entity victim) {
        super(serializable, false);
        this.victim = victim;
        this.facingCharge = facingCharge;
        this.transactionAmount = transactionAmount;
        this.isCriminal = isCriminal;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public @Nullable Entity getVictim() {
        return victim;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public boolean isCriminal() {
        return isCriminal;
    }

    public void setCriminal(boolean criminal) {
        isCriminal = criminal;
    }

    public @Nullable Law.Crime getFacingCharge() {
        return facingCharge;
    }
}
