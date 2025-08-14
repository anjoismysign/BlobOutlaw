package io.github.anjoismysign.bloboutlaw.listener;

import io.github.anjoismysign.bloblib.entities.BlobListener;
import io.github.anjoismysign.bloboutlaw.director.manager.ConfigurationManager;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawListenerManager;

public abstract class BlobOutlawListener implements BlobListener {
    private final OutlawListenerManager listenerManager;

    public BlobOutlawListener(OutlawListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    public OutlawListenerManager getListenerManager() {
        return listenerManager;
    }

    public ConfigurationManager getConfigManager() {
        return getListenerManager().getManagerDirector().getConfigManager();
    }

}
