package io.github.anjoismysign.bloboutlaw.director;

import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;

public class OutlawManager extends GenericManager<BlobOutlaw, OutlawManagerDirector> {

    public OutlawManager(OutlawManagerDirector managerDirector) {
        super(managerDirector);
    }
}