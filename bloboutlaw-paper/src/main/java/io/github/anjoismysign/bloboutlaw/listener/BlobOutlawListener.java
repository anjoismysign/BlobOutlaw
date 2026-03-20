package io.github.anjoismysign.bloboutlaw.listener;

import io.github.anjoismysign.bloblib.entities.BlobListener;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawConfigurationManager;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawListenerManager;

public abstract class BlobOutlawListener implements BlobListener {
    private final OutlawListenerManager listenerManager;

    public BlobOutlawListener(OutlawListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    public OutlawListenerManager getListenerManager() {
        return listenerManager;
    }

    public OutlawConfigurationManager getConfigManager() {
        return getListenerManager().getManagerDirector().getConfigManager();
    }

}
