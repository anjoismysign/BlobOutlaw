package io.github.anjoismysign.bloboutlaw.director.manager;

import io.github.anjoismysign.bloboutlaw.director.OutlawManager;
import io.github.anjoismysign.bloboutlaw.director.OutlawManagerDirector;
import io.github.anjoismysign.bloboutlaw.util.SafeZoneUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.Nullable;

public class SafeZoneManager extends OutlawManager implements Listener {
    private final SafeZoneUtil util;

    public SafeZoneManager(OutlawManagerDirector managerDirector) {
        super(managerDirector);
        this.util = new SafeZoneUtil(managerDirector.getConfigManager());
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
            managerDirector.getProfileManager().getActiveProfiles().forEach(outlaw -> {
                Player player = outlaw.player();
                Location playerLocation = player.getLocation();
                outlaw.setInSafeZone(util.isInSafeZone(playerLocation));
            });
        }, 0, 10);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void respawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        @Nullable Location nearest = util.getNearest(location);
        if (nearest == null)
            return;
        event.setRespawnLocation(nearest);
    }

}