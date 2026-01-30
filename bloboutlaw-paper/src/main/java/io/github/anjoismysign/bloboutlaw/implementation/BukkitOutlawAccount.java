package io.github.anjoismysign.bloboutlaw.implementation;

import io.github.anjoismysign.bloblib.api.BlobLibProfileAPI;
import io.github.anjoismysign.bloblib.entities.AccountCrudable;
import io.github.anjoismysign.bloblib.entities.PlayerDecorator;
import io.github.anjoismysign.bloblib.managers.cruder.ProfileCruder;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.psa.PostLoadable;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BukkitOutlawAccount implements AccountCrudable<BukkitOutlawProfile>, PostLoadable {
    private transient @NotNull BlobOutlaw plugin;

    private final @NotNull String identification;
    private final @NotNull List<BukkitOutlawProfile> profiles;
    private int currentProfileIndex;

    private transient @NotNull PlayerDecorator playerDecorator;

    public BukkitOutlawAccount(@NotNull String identification) {
        this.identification = identification;
        this.profiles = new ArrayList<>();
        onPostLoad();
    }

    @Override
    public void onPostLoad() {
        this.plugin = BlobOutlaw.getInstance();
        @NotNull ProfileCruder<BukkitOutlawAccount, BukkitOutlawProfile> cruder = plugin.getProfileCruder();
        this.playerDecorator = cruder.assignPlayerDecorator(identification);
        if (!profiles.isEmpty()) {
            if (currentProfileIndex < 0 || currentProfileIndex >= profiles.size()) {
                currentProfileIndex = 0;
            }
            BukkitOutlawProfile profile = profiles.get(currentProfileIndex);
            switchToProfile(profile.getIdentification());
        } else {
            var profileAPI = BlobLibProfileAPI.getInstance();
            var provider = profileAPI.getProvider();
            var profileManagement = provider.getProfileManagement(UUID.fromString(identification));
            if (profileManagement == null) {
                return;
            }
            var profile = profileManagement.getProfiles().get(0);
            createProfile(profile.getIdentification(), true);
        }
    }

    public void createProfile(String identification,
                              boolean switchTo) {
        BukkitOutlawProfile profile = new BukkitOutlawProfile(identification);
        this.profiles.add(profile);
        if (!switchTo) {
            return;
        }
        switchToProfile(identification);
    }

    public void switchToProfile(String identification) {
        var currentProfile = profiles.get(currentProfileIndex);
        if (!currentProfile.getIdentification().equals(identification) && currentProfile.isValid()){
            currentProfile.cleanup();
        }
        @Nullable BukkitOutlawProfile target = profiles.stream().filter(profile->profile.getIdentification().equals(identification)).findFirst().orElse(null);
        if (target == null){
            return;
        }
        int index = profiles.indexOf(target);
        Runnable syncRunnable = () -> {
            target.setPlayerDecorator(playerDecorator);
            this.currentProfileIndex = index;
        };
        if (Bukkit.isPrimaryThread()) {
            syncRunnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, syncRunnable);
        }
    }

    @Override
    public @NotNull String getIdentification() {
        return identification;
    }

    public @NotNull List<BukkitOutlawProfile> getProfiles() {
        return profiles;
    }

    public @NotNull BukkitOutlawProfile getCurrentProfile() {
        return profiles.get(currentProfileIndex);
    }
}
