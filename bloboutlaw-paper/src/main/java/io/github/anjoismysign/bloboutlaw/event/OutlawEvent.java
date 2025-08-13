package io.github.anjoismysign.bloboutlaw.event;

import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlaw;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class OutlawEvent extends Event {

    private final BukkitOutlaw outlaw;

    public OutlawEvent(@NotNull BukkitOutlaw outlaw,
                       boolean isAsync) {
        super(isAsync);
        this.outlaw = outlaw;
    }

    @NotNull
    public BukkitOutlaw getOutlaw() {
        return outlaw;
    }
}
