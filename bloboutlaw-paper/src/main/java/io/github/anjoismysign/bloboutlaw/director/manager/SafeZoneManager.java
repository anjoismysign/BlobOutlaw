package io.github.anjoismysign.bloboutlaw.director.manager;

import io.github.anjoismysign.bloboutlaw.director.OutlawManager;
import io.github.anjoismysign.bloboutlaw.director.OutlawManagerDirector;
import io.github.anjoismysign.bloboutlaw.event.OutlawJoinEvent;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlaw;
import io.github.anjoismysign.bloboutlaw.util.SafeZoneUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SafeZoneManager extends OutlawManager implements Listener {
    private final Map<String, BukkitOutlaw> outlaws = new HashMap<>();
    private final SafeZoneUtil util;

    public SafeZoneManager(OutlawManagerDirector managerDirector) {
        super(managerDirector);
        this.util = new SafeZoneUtil(managerDirector.getConfigManager());
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
            outlaws.values().forEach(outlaw -> {
                Player player = outlaw.player();
                Location playerLocation = player.getLocation();
                outlaw.setInSafeZone(util.isInSafeZone(playerLocation));
            });
        }, 0, 10);
    }

    @EventHandler
    public void onJoin(OutlawJoinEvent event) {
        BukkitOutlaw outlaw = event.getOutlaw();
        Player player = outlaw.player();
        String name = player.getName();
        outlaws.put(name, outlaw);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        outlaws.remove(name);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void respawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        @Nullable Location nearest = util.getNearest(location);
        if (nearest == null)
            return;
        event.setRespawnLocation(nearest);
    }

}