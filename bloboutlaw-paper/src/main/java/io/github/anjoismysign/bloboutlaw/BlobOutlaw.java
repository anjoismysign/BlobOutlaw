package io.github.anjoismysign.bloboutlaw;

import io.github.anjoismysign.bloblib.entities.BlobScheduler;
import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.IManagerDirector;
import io.github.anjoismysign.bloblib.managers.PluginManager;
import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.bloblib.managers.cruder.BukkitCruder;
import io.github.anjoismysign.bloblib.managers.cruder.BukkitCruderBuilder;
import io.github.anjoismysign.bloboutlaw.director.OutlawManagerDirector;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitCell;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlaw;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitPrison;
import io.github.anjoismysign.bloboutlaw.legendaryanimal.LegendaryAnimal;
import io.github.anjoismysign.bloboutlaw.legendaryanimal.LegendaryAnimalSpawner;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class BlobOutlaw extends BlobPlugin {

    private IManagerDirector proxy;
    private BukkitIdentityManager<BukkitCell> bukkitCellManager;
    private BukkitIdentityManager<BukkitPrison> bukkitPrisonManager;
    private BukkitIdentityManager<LegendaryAnimal> legendaryAnimalManager;
    private BukkitIdentityManager<LegendaryAnimalSpawner> legendaryAnimalSpawnerManager;
    private BukkitCruder<BukkitOutlaw> outlawCruder;

    public static BlobOutlaw getInstance() {
        return JavaPlugin.getPlugin(BlobOutlaw.class);
    }

    @Override
    public void onEnable() {
        OutlawManagerDirector director = new OutlawManagerDirector(this);
        proxy = director.proxy();

        BlobScheduler scheduler = getScheduler();
        scheduler.sync(() -> {
            outlawCruder = new BukkitCruderBuilder<BukkitOutlaw>().plugin(this).crudableClass(BukkitOutlaw.class).build();
            outlawCruder.loadAll();
        });

        PluginManager pluginManager = PluginManager.getInstance();
        bukkitCellManager = pluginManager.addIdentityManager(BukkitCell.Info.class, this, "cell", true);
        bukkitPrisonManager = pluginManager.addIdentityManager(BukkitPrison.Info.class, this, "prison", true);
        legendaryAnimalManager = pluginManager.addIdentityManager(LegendaryAnimal.Info.class, this, "legendary animal", true);
        legendaryAnimalSpawnerManager = pluginManager.addIdentityManager(LegendaryAnimalSpawner.Info.class, this, "legendary animal spawner", true);
    }

    @NotNull
    public IManagerDirector getManagerDirector() {
        return proxy;
    }

    public BukkitIdentityManager<BukkitCell> getCellManager() {
        return bukkitCellManager;
    }

    public BukkitIdentityManager<BukkitPrison> getPrisonManager() {
        return bukkitPrisonManager;
    }

    public BukkitCruder<BukkitOutlaw> getOutlawCruder() {
        return outlawCruder;
    }

    public BukkitIdentityManager<LegendaryAnimal> getLegendaryAnimalManager() {
        return legendaryAnimalManager;
    }

    public BukkitIdentityManager<LegendaryAnimalSpawner> getLegendaryAnimalSpawnerManager(){
        return legendaryAnimalSpawnerManager;
    }
}
