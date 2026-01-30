package io.github.anjoismysign.bloboutlaw.director.manager;

import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.BlobPHExpansion;
import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.director.OutlawManagerDirector;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawProfile;
import io.github.anjoismysign.bloboutlaw.weaponmechanics.WeaponMechanicsHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

public class HookManager extends GenericManager<BlobOutlaw, OutlawManagerDirector> {

    public HookManager(OutlawManagerDirector director) {
        super(director);
        final BlobOutlaw plugin = getPlugin();
        if (Bukkit.getPluginManager().isPluginEnabled("WeaponMechanics")) {
            Bukkit.getPluginManager().registerEvents(new WeaponMechanicsHook(), plugin);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            BlobPHExpansion expansion = new BlobPHExpansion(plugin);
            expansion.putSimpleRelational("status", (player, playerTwo) -> {
                BukkitOutlawProfile outlaw = plugin.getOutlaw(player);
                return outlaw.getStatus().title(playerTwo);
            });
            expansion.putSimple("status",offlinePlayer -> {
                @Nullable Player player = offlinePlayer.getPlayer();
                if (player == null)
                    return "offline";
                BukkitOutlawProfile outlaw = plugin.getOutlaw(player);
                return outlaw.getStatus().title(player);
            });
            expansion.putSimple("bounty", offlinePlayer -> {
                @Nullable Player player = offlinePlayer.getPlayer();
                if (player == null)
                    return "offline";
                BukkitOutlawProfile outlaw = plugin.getOutlaw(player);
                if (!outlaw.isWanted())
                    return "";
                double bounty = outlaw.getBounty();
                String formatted = NumberFormat.getInstance(Locale.US).format(bounty);
                return BlobLibTranslatableAPI.getInstance()
                        .getTranslatableSnippet("BlobOutlaw.Bounty")
                        .modder()
                        .replace("%bounty%", formatted)
                        .get()
                        .get();
            });
        }
    }

    @Nullable
    private Player player(@NotNull OfflinePlayer offlinePlayer) {
        return Bukkit.getPlayer(offlinePlayer.getUniqueId());
    }
}
