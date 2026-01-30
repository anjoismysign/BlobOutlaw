package io.github.anjoismysign.bloboutlaw.director.manager;

import com.google.common.collect.Maps;
import io.github.anjoismysign.bloblib.api.BlobLibProfileAPI;
import io.github.anjoismysign.bloblib.entities.PlayerDecorator;
import io.github.anjoismysign.bloblib.events.ProfileLoadEvent;
import io.github.anjoismysign.bloblib.events.ProfileManagementQuitEvent;
import io.github.anjoismysign.bloblib.managers.cruder.Cruder;
import io.github.anjoismysign.bloblib.middleman.profile.Profile;
import io.github.anjoismysign.bloboutlaw.director.OutlawManager;
import io.github.anjoismysign.bloboutlaw.director.OutlawManagerDirector;
import io.github.anjoismysign.bloboutlaw.entity.ProfileView;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawAccount;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class OutlawProfileManager extends OutlawManager implements Listener {
    private final @NotNull Cruder<BukkitOutlawAccount> accountCruder;
    private final @NotNull Cruder<BukkitOutlawProfile> profileCruder;

    private final @NotNull Map<UUID, BukkitOutlawAccount> accounts = Maps.newConcurrentMap();
    private final @NotNull Map<String, PlayerDecorator> playerDecorators = Maps.newConcurrentMap();

    public OutlawProfileManager(@NotNull OutlawManagerDirector director) {
        super(director);
        var plugin = director.getPlugin();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        var profileAPI = BlobLibProfileAPI.getInstance();
        var provider = profileAPI.getProvider();
        var providerName = provider.getProviderName();
        var customDirectory = providerName.equals("AbsentProfileProvider") ?
                plugin.getDataFolder()
                :
                new File(plugin.getDataFolder(), providerName);
        if (!customDirectory.isDirectory()){
            customDirectory.mkdirs();
        }
        profileCruder = Cruder.of(plugin, BukkitOutlawProfile.class, BukkitOutlawProfile::new, customDirectory);
        accountCruder = Cruder.of(plugin, BukkitOutlawAccount.class, BukkitOutlawAccount::new, customDirectory);
    }

    @EventHandler
    public void onLoad(ProfileLoadEvent event){
        Profile profile = event.getProfile();
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        String playerIdentification = uniqueId.toString();
        playerDecorators.put(playerIdentification, PlayerDecorator.of(player));
        Runnable asyncRunnable = () -> {
            if (!player.isConnected()){
                playerDecorators.remove(playerIdentification);
                return;
            }
            @Nullable BukkitOutlawAccount account = accounts.get(uniqueId);
            if (account == null) {
                account = accountCruder.readOrGenerate(playerIdentification);
                accounts.put(uniqueId, account);
            }
            List<ProfileView> profiles = account.getProfiles();
            if (!profiles.isEmpty()){
                String identification = profile.getIdentification();
                for (int index = 0; index < profiles.size(); index++) {
                    ProfileView view = profiles.get(index);
                    if (!view.identification().equals(identification)){
                        continue;
                    }
                    account.switchToProfile(index);
                    return;
                }
                ProfileView view = new ProfileView(profile.getIdentification(), profile.getName());
                account.createProfile(view, true);
            } else {
                ProfileView view = new ProfileView(profile.getIdentification(), profile.getName());
                account.createProfile(view, true);
            }
        };
        if (Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), asyncRunnable);
        } else {
            asyncRunnable.run();
        }
    }

    @EventHandler
    public void onQuit(ProfileManagementQuitEvent event){
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        BukkitOutlawAccount account = Objects.requireNonNull(accounts.get(uniqueId), "player is not cached");
        accountCruder.update(account);
        accounts.remove(uniqueId);
    }

    public @NotNull Cruder<BukkitOutlawProfile> getProfileCruder() {
        return profileCruder;
    }

    private @Nullable BukkitOutlawAccount getAccount(@NotNull UUID uniqueIdentifier) {
        return accounts.get(uniqueIdentifier);
    }

    public @NotNull BukkitOutlawAccount getAccount(@NotNull Player player) {
        return Objects.requireNonNull(getAccount(player.getUniqueId()), "Player is not cached");
    }

    public @NotNull Collection<BukkitOutlawProfile> getActiveProfiles(){
        return accounts.values()
                .stream()
                .map(BukkitOutlawAccount::getCurrentProfile)
                .filter(BukkitOutlawProfile::isValid)
                .toList();
    }

    @Override
    public void unload() {
        accounts.forEach(((uuid, proprietorAccount) -> {
            accountCruder.update(proprietorAccount);
        }));
    }

    @NotNull
    public PlayerDecorator assignPlayerDecorator(@NotNull String identification){
        PlayerDecorator decorator = Objects.requireNonNull(playerDecorators.get(identification), "'"+identification+"' doesn't point to a PlayerDecorator");
        playerDecorators.remove(identification);
        return decorator;
    }
}
