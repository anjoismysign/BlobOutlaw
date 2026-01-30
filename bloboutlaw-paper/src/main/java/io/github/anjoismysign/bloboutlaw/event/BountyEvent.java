package io.github.anjoismysign.bloboutlaw.event;

import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawProfile;
import org.jetbrains.annotations.NotNull;

public abstract class BountyEvent extends OutlawEvent {
    double amount;

    public BountyEvent(@NotNull BukkitOutlawProfile serializable,
                       double amount) {
        super(serializable, false);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
