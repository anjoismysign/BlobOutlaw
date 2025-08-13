package io.github.anjoismysign.bloboutlaw.util;

import io.github.anjoismysign.bloblib.BlobLib;
import io.github.anjoismysign.bloblib.entities.positionable.Positionable;
import io.github.anjoismysign.bloblib.entities.positionable.PositionableType;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatablePositionable;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawConfigManager;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record SafeZoneUtil(@NotNull OutlawConfigManager configManager) {

    @Nullable
    public Location getNearest(@NotNull Location location) {
        List<TranslatablePositionable> spawnPoints = getSpawnPoints();
        @Nullable Map.Entry<Location, Double> entry = spawnPoints
                .stream()
                .map(TranslatablePositionable::get)
                .filter(positionable -> positionable.getPositionableType() == PositionableType.LOCATABLE)
                .map(Positionable::toLocation)
                .map(other -> Map.entry(other, location.distanceSquared(other)))
                .min((e1, e2) -> {
                    double distance1 = e1.getValue();
                    double distance2 = e2.getValue();
                    return Double.compare(distance2, distance1);
                })
                .orElse(null);
        if (entry == null)
            return null;
        return entry.getKey();
    }

    @NotNull
    public List<TranslatablePositionable> getSpawnPoints() {
        Set<String> safeZones = configManager.getSafeZones();
        return BlobLib.getInstance().getTranslatablePositionableManager().getAssets()
                .stream()
                .filter(positionable -> safeZones.contains(positionable.identifier()))
                .toList();
    }

    public boolean isInSafeZone(@NotNull Location location) {
        Set<String> safeZones = configManager.getSafeZones();
        return BlobLib.getInstance().getTranslatableAreaManager().unorderedContains(location)
                .stream()
                .map(DataAsset::identifier)
                .filter(safeZones::contains)
                .findFirst()
                .orElse(null)
                != null;
    }
}
