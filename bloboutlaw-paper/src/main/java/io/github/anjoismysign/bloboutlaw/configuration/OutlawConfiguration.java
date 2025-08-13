package io.github.anjoismysign.bloboutlaw.configuration;

import java.util.Set;

public class OutlawConfiguration {

    private boolean tinyDebug;
    private long inhibitTimeOut;
    private double bountyGrantsMenace;
    private Set<String> safeZones;

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
}
