package io.github.anjoismysign.bloboutlaw.listener;

import io.github.anjoismysign.bloblib.itemapi.ItemMaterial;
import io.github.anjoismysign.bloblib.itemapi.ItemMaterialManager;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawListenerManager;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlaw;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.inventory.ItemStack;

public class BatonListener extends BlobOutlawListener {

    private static final String reference = "BlobOutlaw.Baton";

    public BatonListener(OutlawListenerManager listenerManager) {
        super(listenerManager);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSuppress(EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        if (damagerEntity.getType() != EntityType.PLAYER)
            return;
        Player damagerPlayer = (Player) damagerEntity;
        ItemStack mainHand = damagerPlayer.getInventory().getItemInMainHand();
        ItemMaterial material = ItemMaterialManager.getInstance().of(mainHand);
        if (material == null)
            return;
        if (!material.isInstance(reference))
            return;
        Entity victimEntity = event.getEntity();
        if (victimEntity.getType() != EntityType.PLAYER)
            return;
        Player victimPlayer = (Player) victimEntity;
        BukkitOutlaw victim = BlobOutlaw.getInstance().getOutlawCruder().get(victimPlayer);
        victim.suppress(getConfigManager().getConfiguration().getInhibitTimeOut());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInhibitedAttemptsDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager.getType() != EntityType.PLAYER)
            return;
        Player player = (Player) damager;
        BukkitOutlaw outlaw = BlobOutlaw.getInstance().getOutlawCruder().get(player);
        if (!outlaw.isSuppressed())
            return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInhibitedAttemptsMove(EntityDismountEvent event) {
        Entity rider = event.getEntity();
        if (rider.getType() != EntityType.PLAYER)
            return;
        Player player = (Player) rider;
        BukkitOutlaw outlaw = BlobOutlaw.getInstance().getOutlawCruder().get(player);
        if (!outlaw.isSuppressed())
            return;
        event.setCancelled(true);
    }

    @Override
    public boolean checkIfShouldRegister() {
        return true;
    }
}
