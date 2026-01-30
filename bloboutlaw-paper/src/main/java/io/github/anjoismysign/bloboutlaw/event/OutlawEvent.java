package io.github.anjoismysign.bloboutlaw.event;

import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawProfile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class OutlawEvent extends Event {

    private final BukkitOutlawProfile outlaw;

    public OutlawEvent(@NotNull BukkitOutlawProfile outlaw,
                       boolean isAsync) {
        super(isAsync);
        this.outlaw = outlaw;
    }

    @NotNull
    public BukkitOutlawProfile getOutlaw() {
        return outlaw;
    }
}
