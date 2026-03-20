package io.github.anjoismysign.bloboutlaw.configuration;

import io.github.anjoismysign.bloblib.entities.CommandData;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;

import java.util.List;
import java.util.Set;

public class OutlawConfiguration {

    public static OutlawConfiguration getInstance(){
        return BlobOutlaw.getInstance().getManagerDirector().getConfigManager().getConfiguration();
    }

    private boolean tinyDebug;
    private long inhibitTimeOut;
    private double bountyGrantsMenace;
    private Set<String> safeZones;
    private long fallbackPrisonTerm;
    private List<CommandData> onReleaseRun;

    OutlawConfiguration() {
    }

    public boolean isTinyDebug() {
        return tinyDebug;
    }

    public void setTinyDebug(boolean tinyDebug) {
        this.tinyDebug = tinyDebug;
    }

    public long getInhibitTimeOut() {
        return inhibitTimeOut;
    }

    public void setInhibitTimeOut(long inhibitTimeOut) {
        this.inhibitTimeOut = inhibitTimeOut;
    }

    public double getBountyGrantsMenace() {
        return bountyGrantsMenace;
    }

    public void setBountyGrantsMenace(double bountyGrantsMenace) {
        this.bountyGrantsMenace = bountyGrantsMenace;
    }

    public Set<String> getSafeZones() {
        return safeZones;
    }

    public void setSafeZones(Set<String> safeZones) {
        this.safeZones = safeZones;
    }

    public long getFallbackPrisonTerm() {
        return fallbackPrisonTerm;
    }

    public void setFallbackPrisonTerm(long fallbackPrisonTerm) {
        this.fallbackPrisonTerm = fallbackPrisonTerm;
    }

    public List<CommandData> getOnReleaseRun() {
        return onReleaseRun;
    }

    public void setOnReleaseRun(List<CommandData> onReleaseRun) {
        this.onReleaseRun = onReleaseRun;
    }
}
