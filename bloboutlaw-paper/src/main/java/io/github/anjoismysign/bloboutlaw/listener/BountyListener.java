package io.github.anjoismysign.bloboutlaw.listener;

import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.director.manager.OutlawListenerManager;
import io.github.anjoismysign.bloboutlaw.event.BehaviourFlagEvent;
import io.github.anjoismysign.bloboutlaw.implementation.BukkitOutlawProfile;
import io.github.anjoismysign.bloboutlaw.law.Law;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.Nullable;

public class BountyListener extends BlobOutlawListener {

    public BountyListener(OutlawListenerManager listenerManager) {
        super(listenerManager);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player potentialVictim = event.getEntity();
        @Nullable Player potentialKiller = potentialVictim.getKiller();
        if (potentialKiller == null) {
            return;
        }
        BukkitOutlawProfile victim = BlobOutlaw.getInstance().getOutlaw(potentialVictim);
        BukkitOutlawProfile killer = BlobOutlaw.getInstance().getOutlaw(potentialKiller);

        boolean raiseBounty = !victim.isWanted() && killer.isWanted();
        Law.Crime facingCharge = Law.Crime.MURDER;
        if (raiseBounty) {
            BehaviourFlagEvent behaviourFlagEvent = new BehaviourFlagEvent(killer, facingCharge, 0, true, potentialVictim);
            Bukkit.getPluginManager().callEvent(behaviourFlagEvent);
            killer.warrant(facingCharge);
            return;
        }
        if (victim.isWanted() && killer.isWanted()) {
            return;
        }
        boolean claimsBounty = victim.isWanted();
        if (!claimsBounty) {
            BehaviourFlagEvent behaviourFlagEvent = new BehaviourFlagEvent(killer, facingCharge, 0, true, potentialVictim);
            Bukkit.getPluginManager().callEvent(behaviourFlagEvent);
            killer.warrant(facingCharge);
            return;
        }
        killer.claimBounty(Law.BountyClaim.DEAD, victim);
    }

    @Override
    public boolean checkIfShouldRegister() {
        return true;
    }
}
