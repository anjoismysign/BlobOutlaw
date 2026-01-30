package io.github.anjoismysign.bloboutlaw.implementation;

import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.BlobScheduler;
import io.github.anjoismysign.bloblib.entities.message.BlobMessage;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatablePositionable;
import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.DataAssetEntry;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import io.github.anjoismysign.outlaw.Cell;
import io.github.anjoismysign.outlaw.Conviction;
import io.github.anjoismysign.outlaw.Prison;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record BukkitPrison(@NotNull String identifier,
                           @NotNull Map<String, Cell> cells,
                           @NotNull TranslatablePositionable positionable) implements Prison, DataAsset {
    private static final BlobOutlaw PLUGIN = BlobOutlaw.getInstance();
    private static final BlobScheduler SCHEDULER = PLUGIN.getScheduler();

    @Nullable
    public static BukkitPrison getNearest(@NotNull Location location) {
        BukkitIdentityManager<BukkitPrison> manager = PLUGIN.getPrisonManager();
        return manager.stream()
                .min(Comparator.comparingDouble(prison -> {
                    Location relative = prison.positionable.get().toLocation(location.getWorld());
                    return relative.distanceSquared(location);
                }))
                .orElse(null);
    }

    @NotNull
    public Location getOutside(@Nullable World world) {
        return positionable.get().toLocation(world);
    }

    @NotNull
    public BukkitCell getCell(@NotNull String name) {
        Cell cell = cells.get(name);
        if (cell != null)
            return (BukkitCell) cell;
        throw new NullPointerException("'" + name + "' doesn't seem to be a cell at '" + identifier + "' BukkitPrison");
    }

    @Nullable
    private BukkitCell lowestPopulated() {
        return (BukkitCell) cells.values().stream()
                .filter(cell -> cell.prisoners().isEmpty()) // Assuming `occupants()` returns the Map in a Cell
                .findFirst()
                .orElseGet(() ->
                        cells.values().stream()
                                .min(Comparator.comparingInt(cell -> cell.prisoners().size()))
                                .orElseThrow(() -> new IllegalStateException("No cells available in the prison"))
                );
    }

    public void addPrisoner(@NotNull Entity entity,
                            @Nullable Conviction... convictions) {
        addPrisoner(entity, List.of(convictions));
    }

    public void addPrisoner(@NotNull Entity entity,
                            @Nullable List<? extends Conviction> convictions) {
        long term = convictions.stream()
                .mapToLong(Conviction::getTerm)
                .sum();
        BukkitCell lowestPopulated = Objects.requireNonNull(lowestPopulated(), "there's no Cells at '" + identifier + "' BukkitPrison");
        World world = entity.getWorld();
        Location lowestPopulatedLocation = lowestPopulated.positionable().get().toLocation(world);
        Location location = getOutside(world);
        lowestPopulated.addPrisoner(entity);
        entity.teleport(lowestPopulatedLocation);
        if (entity instanceof Player player) {
            int seconds = (int) (term);
            startCountdown(player, seconds);
        }
        SCHEDULER.syncLater(() -> {
            entity.teleport(location);
        }, term);
        lowestPopulated.prisoners().remove(entity.getUniqueId());
    }

    private void startCountdown(Player player, int seconds) {
        new BukkitRunnable() {
            int timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    @Nullable BlobMessage blobMessage = BlobLibMessageAPI.getInstance().getMessage("BlobOutlaw.Serving", player.getLocale());
                    if (blobMessage != null)
                        blobMessage.modder()
                                .replace("%seconds%", timeLeft + "")
                                .get()
                                .handle(player);
                    timeLeft--;
                } else {
                    @Nullable BlobMessage blobMessage = BlobLibMessageAPI.getInstance().getMessage("BlobOutlaw.Served", player.getLocale());
                    if (blobMessage != null)
                        blobMessage.handle(player);
                    cancel();
                }
            }
        }.runTaskTimer(JavaPlugin.getPlugin(BlobOutlaw.class), 0L, 20L); // Schedule task to run every second (20 ticks)
    }

    public static final class Info implements IdentityGenerator<BukkitPrison> {
        private List<String> cells;

        public Info() {
        }

        public Info(@NotNull List<String> cells) {
            this.cells = cells;
        }

        @Override
        public @NotNull BukkitPrison generate(@NotNull String identifier) {
            TranslatablePositionable positionable = Objects.requireNonNull(BlobLibTranslatableAPI.getInstance().getTranslatablePositionable(identifier), "'" + identifier + "' doesn't point to a TranslatablePositionable");
            List<BukkitCell> bukkitCells = cells.stream()
                    .map(key -> Objects.requireNonNull(PLUGIN.getCellManager(), "'BukkitCell' GeneratorManager is null").fetchGeneration(key))
                    .filter(Objects::nonNull)
                    .map(DataAssetEntry::asset)
                    .toList();
            Map<String, Cell> cells = new HashMap<>();
            bukkitCells.forEach(bukkitCell -> {
                cells.put(bukkitCell.name(), bukkitCell);
            });
            return new BukkitPrison(identifier, cells, positionable);
        }

        public @NotNull List<String> cells() {
            return cells;
        }

        public void setCells(@NotNull List<String> cells) {
            this.cells = cells;
        }
    }

}
