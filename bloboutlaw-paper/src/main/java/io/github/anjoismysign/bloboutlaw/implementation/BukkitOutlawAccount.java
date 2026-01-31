package io.github.anjoismysign.bloboutlaw.implementation;

import io.github.anjoismysign.bloblib.entities.AccountCrudable;

public class BukkitOutlawAccount extends AccountCrudable<BukkitOutlawProfile> {
    public BukkitOutlawAccount(String identification) {
        super(identification);
    }
}
