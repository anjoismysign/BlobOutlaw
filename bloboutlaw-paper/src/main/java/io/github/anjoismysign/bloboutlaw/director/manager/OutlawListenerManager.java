package io.github.anjoismysign.bloboutlaw.director.manager;

import io.github.anjoismysign.bloblib.entities.ListenerManager;
import io.github.anjoismysign.bloboutlaw.director.OutlawManagerDirector;
import io.github.anjoismysign.bloboutlaw.listener.BatonListener;
import io.github.anjoismysign.bloboutlaw.listener.BountyListener;
import io.github.anjoismysign.bloboutlaw.listener.StatusListener;
import io.github.anjoismysign.bloboutlaw.listener.StrapListener;

public class OutlawListenerManager extends ListenerManager {
    private final OutlawManagerDirector managerDirector;

    public OutlawListenerManager(OutlawManagerDirector managerDirector) {
        super(managerDirector);
        this.managerDirector = managerDirector;
        add(new BatonListener(this));
        add(new StrapListener(this));
        add(new BountyListener(this));
        add(new StatusListener(this));

        reload();
    }

    @Override
    public OutlawManagerDirector getManagerDirector() {
        return managerDirector;
    }
}