package io.github.anjoismysign.bloboutlaw;

import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.PluginManager;
import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.bloblib.managers.cruder.AccountCruder;
import io.github.anjoismysign.bloboutlaw.director.OutlawManagerDirector;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitCell;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawAccount;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawProfile;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitPrison;
import io.github.anjoismysign.bloboutlaw.legendaryanimal.LegendaryAnimal;
import io.github.anjoismysign.bloboutlaw.legendaryanimal.LegendaryAnimalSpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class BlobOutlaw extends BlobPlugin {

    private static BlobOutlaw INSTANCE;

    private OutlawManagerDirector director;
    private BukkitIdentityManager<BukkitCell> bukkitCellManager;
    private BukkitIdentityManager<BukkitPrison> bukkitPrisonManager;
    private BukkitIdentityManager<LegendaryAnimal> legendaryAnimalManager;
    private BukkitIdentityManager<LegendaryAnimalSpawner> legendaryAnimalSpawnerManager;
    private AccountCruder<BukkitOutlawAccount, BukkitOutlawProfile> accountCruder;

    public static BlobOutlaw getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        director = new OutlawManagerDirector(this);

        PluginManager pluginManager = PluginManager.getInstance();
        bukkitCellManager = pluginManager.addIdentityManager(BukkitCell.Info.class, this, "cell", true);
        bukkitPrisonManager = pluginManager.addIdentityManager(BukkitPrison.Info.class, this, "prison", true);
        legendaryAnimalManager = pluginManager.addIdentityManager(LegendaryAnimal.Info.class, this, "legendary animal", true);
        legendaryAnimalSpawnerManager = pluginManager.addIdentityManager(LegendaryAnimalSpawner.Info.class, this, "legendary animal spawner", true);

        Bukkit.getScheduler().runTask(this, ()->{
           accountCruder = new AccountCruder<>(this, BukkitOutlawAccount.class, BukkitOutlawProfile.class);
        });
    }

    @Override
    public void onDisable(){
        super.onDisable();
        accountCruder.shutdown();
    }

    @NotNull
    public OutlawManagerDirector getManagerDirector() {
        return director;
    }

    public BukkitIdentityManager<BukkitCell> getCellManager() {
        return bukkitCellManager;
    }

    public BukkitIdentityManager<BukkitPrison> getPrisonManager() {
        return bukkitPrisonManager;
    }

    public BukkitIdentityManager<LegendaryAnimal> getLegendaryAnimalManager() {
        return legendaryAnimalManager;
    }

    public BukkitIdentityManager<LegendaryAnimalSpawner> getLegendaryAnimalSpawnerManager(){
        return legendaryAnimalSpawnerManager;
    }

    @NotNull
    public AccountCruder<BukkitOutlawAccount, BukkitOutlawProfile> getAccountCruder(){
        return accountCruder;
    }

    @Nullable
    public BukkitOutlawProfile getOutlaw(@NotNull Player player){
        Objects.requireNonNull(player, "player cannot be null");
        return accountCruder.getAccount(player);
    }
}
