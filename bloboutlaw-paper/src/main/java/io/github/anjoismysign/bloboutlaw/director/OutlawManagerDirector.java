package io.github.anjoismysign.bloboutlaw.director;

import io.github.anjoismysign.bloblib.entities.GenericManagerDirector;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.director.manager.HookManager;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawConfigManager;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawListenerManager;
import io.github.anjoismysign.bloboutlaw.director.manager.SafeZoneManager;
import io.github.anjoismysign.bloboutlaw.law.Law;
import io.github.anjoismysign.bloboutlaw.legendaryanimal.LegendaryAnimalManager;
import org.jetbrains.annotations.NotNull;

public class OutlawManagerDirector extends GenericManagerDirector<BlobOutlaw> {

    public OutlawManagerDirector(BlobOutlaw plugin) {
        super(plugin);
        addManager("ConfigManager",
                new OutlawConfigManager(this));
        addManager("ListenerManager",
                new OutlawListenerManager(this));
        addManager("SafeZoneManager",
                new SafeZoneManager(this));
        addManager("LegendaryAnimalManager",
                new LegendaryAnimalManager(this));
        addManager("HookManager",
                new HookManager(this));
    }

    /**
     * From top to bottom, follow the order.
     */
    @Override
    public void reload() {
        Law.Crimes.readData();
        getConfigManager().reload();
        getListenerManager().reload();
        getLegendaryAnimalManager().reload();
    }

    @Override
    public void unload(){
        getPlugin().getOutlawCruder().saveAll();
    }

    @NotNull
    public final OutlawConfigManager getConfigManager() {
        return getManager("ConfigManager", OutlawConfigManager.class);
    }

    @NotNull
    public final OutlawListenerManager getListenerManager() {
        return getManager("ListenerManager", OutlawListenerManager.class);
    }

    @NotNull
    public final SafeZoneManager getSafeZoneManager() {
        return getManager("SafeZoneManager", SafeZoneManager.class);
    }

    @NotNull
    public final LegendaryAnimalManager getLegendaryAnimalManager() {
        return getManager("LegendaryAnimalManager", LegendaryAnimalManager.class);
    }
}