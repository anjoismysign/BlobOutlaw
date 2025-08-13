package io.github.anjoismysign.bloboutlaw.listener;

import io.github.anjoismysign.bloblib.BlobLib;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawListenerManager;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlaw;
import io.github.anjoismysign.bloboutlaw.util.WeaponUtil;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StatusListener extends BlobOutlawListener {

    public static final Map<String, ProtectedWarmup> protectedWarmups = new HashMap();
    public static final Set<String> hostiles = new HashSet<>();

    public StatusListener(OutlawListenerManager listenerManager) {
        super(listenerManager);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onProtected(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (victim.getType() != EntityType.PLAYER)
            return;
        Player player = (Player) victim;
        BukkitOutlaw outlaw = BlobOutlaw.getInstance().getOutlawCruder().get(player);
        if (!outlaw.isProtected())
            return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHit(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager.getType() != EntityType.PLAYER)
            return;
        Player player = (Player) damager;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (WeaponUtil.INSTANCE.isWeapon(itemStack))
            return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        int newSlot = event.getNewSlot();
        @Nullable ItemStack itemStack = player.getInventory().getItem(newSlot);
        BukkitOutlaw outlaw = BlobOutlaw.getInstance().getOutlawCruder().get(player);
        if (itemStack == null) {

            if (!hostiles.contains(playerName)) {
                return;
            }
            @Nullable ProtectedWarmup lookup = protectedWarmups.get(playerName);
            if (lookup != null) {
                return;
            }
            protectedWarmups.put(playerName, new ProtectedWarmup(outlaw, playerName));
            return;
        }
        if (!WeaponUtil.INSTANCE.isWeapon(itemStack)) {
            return;
        }
        hostiles.add(playerName);
        outlaw.setHostile(true);
        @Nullable ProtectedWarmup lookup = protectedWarmups.get(playerName);
        if (lookup != null)
            lookup.task.cancel();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        hostiles.remove(name);
        protectedWarmups.remove(name);
    }

    public boolean isInSafeZone(@NotNull Location location) {
        Set<String> safeZones = getConfigManager().getSafeZones();
        return BlobLib.getInstance().getTranslatableAreaManager().unorderedContains(location)
                .stream()
                .map(DataAsset::identifier)
                .filter(safeZones::contains)
                .findFirst()
                .orElse(null)
                != null;
    }

    @Override
    public boolean checkIfShouldRegister() {
        return true;
    }

    public static class ProtectedWarmup {
        final BukkitOutlaw outlaw;
        final String name;
        final BukkitTask task;
        int remainingSeconds = 20;

        public ProtectedWarmup(@NotNull BukkitOutlaw outlaw,
                               @NotNull String name) {
            this.outlaw = outlaw;
            this.name = name;

            outlaw.setHostile(true);

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (remainingSeconds <= 0) {
                        hostiles.remove(name);
                        protectedWarmups.remove(name);
                        outlaw.setHostile(false);
                        cancel();
                        return;
                    }
                    remainingSeconds--;
                }
            }.runTaskTimer(BlobOutlaw.getInstance(), 0, 20);
        }

        public BukkitTask getTask(){
            return task;
        }
    }
}
