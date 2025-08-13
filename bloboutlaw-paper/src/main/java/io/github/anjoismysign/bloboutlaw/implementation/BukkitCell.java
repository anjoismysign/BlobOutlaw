package io.github.anjoismysign.bloboutlaw.implementation;

import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatablePositionable;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import io.github.anjoismysign.outlaw.Cell;
import io.github.anjoismysign.outlaw.Conviction;
import io.github.anjoismysign.outlaw.Prisoner;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record BukkitCell(@NotNull String identifier,
                         @NotNull Map<UUID, Prisoner> prisoners,
                         @NotNull String positionableKey,
                         int capacity) implements Cell, DataAsset {

    @NotNull
    public TranslatablePositionable positionable() {
        return Objects.requireNonNull(BlobLibTranslatableAPI.getInstance().getTranslatablePositionable(identifier), "'" + identifier + "' doesn't point to a TranslatablePositionable");
    }

    @Override
    public @NotNull String name() {
        return identifier;
    }

    public void addPrisoner(@NotNull Entity entity,
                            @Nullable Conviction... convictions) {
        UUID uuid = entity.getUniqueId();
        Prisoner prisoner = () -> List.of(convictions);
        prisoners.put(uuid, prisoner);
    }

    public void addPrisoner(@NotNull Entity entity,
                            @Nullable List<Conviction> convictions) {
        prisoners.put(entity.getUniqueId(), () -> convictions);
    }

    public record Info(int capacity) implements IdentityGenerator<BukkitCell> {

        @Override
        public @NotNull BukkitCell generate(@NotNull String identifier) {
            return new BukkitCell(identifier, new HashMap<>(), identifier, capacity);
        }
    }

}
