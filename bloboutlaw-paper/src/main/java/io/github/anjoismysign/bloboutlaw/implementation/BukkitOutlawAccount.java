package io.github.anjoismysign.bloboutlaw.implementation;

import io.github.anjoismysign.bloblib.api.BlobLibProfileAPI;
import io.github.anjoismysign.bloblib.entities.PlayerDecorator;
import io.github.anjoismysign.bloblib.managers.cruder.Cruder;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawProfileManager;
import io.github.anjoismysign.bloboutlaw.entity.ProfileView;
import io.github.anjoismysign.psa.PostLoadable;
import io.github.anjoismysign.psa.PreUpdatable;
import io.github.anjoismysign.psa.crud.Crudable;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BukkitOutlawAccount implements Crudable, PostLoadable, PreUpdatable {
    private transient @NotNull BlobOutlaw plugin;
    private transient @NotNull OutlawProfileManager manager;

    private final @NotNull String identification;
    private final @NotNull List<ProfileView> profiles;
    private int currentProfileIndex;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private transient @NotNull BukkitOutlawProfile currentProfile;
    private transient @NotNull PlayerDecorator playerDecorator;

    public BukkitOutlawAccount(@NotNull String identification) {
        this.identification = identification;
        this.profiles = new ArrayList<>();
        onPostLoad();
    }

    @Override
    public void onPostLoad() {
        this.plugin = BlobOutlaw.getInstance();
        this.manager = plugin.getManagerDirector().getProfileManager();
        this.playerDecorator = manager.assignPlayerDecorator(identification);
        if (!profiles.isEmpty()) {
            if (currentProfileIndex < 0 || currentProfileIndex >= profiles.size()) {
                currentProfileIndex = 0;
            }
            ProfileView view = profiles.get(currentProfileIndex);
            setCurrentProfile(manager.getProfileCruder().readOrGenerate(view.identification()));
        } else {
            var profileAPI = BlobLibProfileAPI.getInstance();
            var provider = profileAPI.getProvider();
            var profileManagement = provider.getProfileManagement(UUID.fromString(identification));
            if (profileManagement == null) {
                return;
            }
            var profile = profileManagement.getProfiles().get(0);
            ProfileView view = new ProfileView(profile.getIdentification(), profile.getName());
            createProfile(view, true);
        }
    }

    @Override
    public void onPreUpdate() {
        save();
    }

    private void save() {
        manager.getProfileCruder().update(currentProfile);
    }

    public void createProfile(@NotNull ProfileView profileView,
                              boolean switchTo) {
        //noinspection ConstantValue
        if (switchTo && currentProfile != null) {
            save();
        }
        Cruder<BukkitOutlawProfile> profileCruder = manager.getProfileCruder();
        BukkitOutlawProfile profile = profileCruder.createAndUpdate(profileView.identification());
        this.profiles.add(profileView);
        if (!switchTo) {
            return;
        }
        int index = profiles.indexOf(profileView);
        setCurrentProfile(profile);
        currentProfileIndex = index;
    }

    public void switchToProfile(int index) {
        //noinspection ConstantValue
        if (currentProfileIndex != index && currentProfile != null && currentProfile.isValid()){
            currentProfile.cleanup();
        }
        ProfileView target = profiles.get(index);
        Runnable runnable = () -> {
            save();
            setCurrentProfile(manager.getProfileCruder().readOrGenerate(target.identification()));
            this.currentProfileIndex = index;
        };
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public @NotNull String getIdentification() {
        return identification;
    }

    public int getCurrentProfileIndex() {
        return currentProfileIndex;
    }

    public @NotNull List<ProfileView> getProfiles() {
        return profiles;
    }

    public void setCurrentProfile(@NotNull BukkitOutlawProfile profile) {
        this.currentProfile = profile;
        Runnable syncRunnable = () -> {
            profile.setPlayerDecorator(playerDecorator);
        };
        if (Bukkit.isPrimaryThread()){
            syncRunnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, syncRunnable);
        }
    }

    public @NotNull BukkitOutlawProfile getCurrentProfile() {
        return currentProfile;
    }
}
