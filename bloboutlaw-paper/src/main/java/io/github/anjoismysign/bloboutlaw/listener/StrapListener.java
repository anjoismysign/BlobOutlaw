package io.github.anjoismysign.bloboutlaw.listener;

import io.github.anjoismysign.bloblib.itemapi.ItemMaterial;
import io.github.anjoismysign.bloblib.itemapi.ItemMaterialManager;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawListenerManager;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawProfile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class StrapListener extends BlobOutlawListener {

    private static final String reference = "BlobOutlaw.Strap";

    public StrapListener(OutlawListenerManager listenerManager) {
        super(listenerManager);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArrest(EntityDamageByEntityEvent event) {
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
        BukkitOutlawProfile damager = BlobOutlaw.getInstance().getOutlaw(damagerPlayer);
        if (damager.isWanted())
            return;
        BukkitOutlawProfile victim = BlobOutlaw.getInstance().getOutlaw(victimPlayer);
        if (!victim.isWanted())
            return;
        if (!victim.isSuppressed())
            return;
        damager.arrest(victim);
    }

    @Override
    public boolean checkIfShouldRegister() {
        return true;
    }
}
