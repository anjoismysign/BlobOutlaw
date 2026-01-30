package io.github.anjoismysign.bloboutlaw.implementation;

import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.entities.BlobScheduler;
import io.github.anjoismysign.bloblib.entities.PlayerDecorator;
import io.github.anjoismysign.bloboutlaw.BlobOutlaw;
import io.github.anjoismysign.bloboutlaw.event.BountyClaimEvent;
import io.github.anjoismysign.bloboutlaw.law.Law;
import io.github.anjoismysign.outlaw.Outlaw;
import io.github.anjoismysign.outlaw.Suppressible;
import io.github.anjoismysign.psa.PostLoadable;
import io.github.anjoismysign.psa.crud.Crudable;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class BukkitOutlawProfile implements Crudable, Outlaw, Suppressible, PostLoadable {
    private static final BlobOutlaw PLUGIN = BlobOutlaw.getInstance();
    private static final BlobScheduler SCHEDULER = PLUGIN.getScheduler();

    private final String identification;
    private List<Law.Crime> crimes;
    private double bounty;
    private boolean isHostile;
    private Law.Status status;

    private transient @Nullable PlayerDecorator playerDecorator;
    private transient boolean isInSafeZone;
    private transient boolean isSuppressed;
    private transient ArmorStand seat;

    public BukkitOutlawProfile(String identification) {
        this.identification = identification;
        this.isHostile = false;
        this.crimes = new ArrayList<>();
        this.status = Law.Status.NONE;
        onPostLoad();
    }

    @Override
    public void onPostLoad() {
        this.isInSafeZone = true;
        this.isSuppressed = false;
    }

    public void cleanup(){
        Runnable syncRunnable = () -> {
            removeSuppressible();
        };
        if (Bukkit.isPrimaryThread()){
            syncRunnable.run();
        } else {
            Bukkit.getScheduler().runTask(BlobOutlaw.getInstance(), syncRunnable);
        }
    }

    public void arrest(@NotNull BukkitOutlawProfile wanted) {
        Player player = wanted.player();
        @Nullable BukkitPrison prison = BukkitPrison.getNearest(player.getLocation());
        Logger logger = BlobOutlaw.getInstance().getLogger();
        if (prison == null) {
            logger.severe("There are no BukkitPrison set up yet, while players are trying to arrest between them");
            return;
        }
        prison.addPrisoner(player, wanted.crimes);
        claimBounty(Law.BountyClaim.ALIVE, wanted);
    }

    public void claimBounty(@NotNull Law.BountyClaim claim,
                            @NotNull BukkitOutlawProfile wanted) {
        Player player = wanted.player();
        double amount = wanted.getBounty();
        if (claim == Law.BountyClaim.DEAD)
            bounty = bounty / 2;
        player.getInventory().clear();
        wanted.clearCharges();
        BountyClaimEvent bountyClaimEvent = new BountyClaimEvent(this, amount);
        Bukkit.getPluginManager().callEvent(bountyClaimEvent);
        amount = bountyClaimEvent.getAmount();
        BlobLibEconomyAPI.getInstance().getElasticEconomy().getDefault().depositPlayer(player, amount);
    }

    public void clearCharges() {
        reset();
        getCrimes().clear();
        setStatus(Law.Status.NONE);
    }

    public void warrant(@NotNull Law.Crime pressedCharge) {
        Objects.requireNonNull(pressedCharge, "'pressedCharge' cannot be null");

        boolean isMenace = pressedCharge.equals(Law.Crime.MURDER) && (crimes.stream().filter(charge -> charge.equals(Law.Crime.MURDER)).toList().size() > 3 || bounty >= 750.0);
        if (isMenace) {
            pressedCharge = Law.Crime.MENACE;
            setStatus(Law.Status.MENACE);
        }
        else
            setStatus(Law.Status.KILLER);
        double raise = pressedCharge.getWarrant();
        double bounty = getBounty();

        bounty = bounty + (raise * getBountyMultiplier());
        setBounty(bounty);
        getCrimes().add(pressedCharge);
    }

    public boolean isValid(){
        return playerDecorator != null && playerDecorator.isValid();
    }

    @NotNull
    public Player player() {
        return Objects.requireNonNull(playerDecorator.address().look(), "why is 'player' null?");
    }

    public double getBounty() {
        return bounty;
    }

    @Override
    public void setBounty(double bounty) {
        this.bounty = bounty;
    }

    public int getBountyMultiplier() {
        String get = playerDecorator.getPermissible().getStartsWithPermission(PLUGIN.getChildPermission("bounty.multiplier"));
        if (get == null)
            return 1;
        int bountyMultiplier;
        try {
            bountyMultiplier = Integer.parseInt(get);
        } catch ( InputMismatchException exception ) {
            exception.printStackTrace();
            return 1;
        }
        if (bountyMultiplier <= 0)
            return 1;
        return bountyMultiplier;
    }

    public @NotNull Law.Status getStatus() {
        if (this.status == null){
            this.status = Law.Status.NONE;
        }
        Law.Status status = this.status;
        if (isProtected())
            status = Law.Status.PROTECTED;
        return status;
    }

    public void setStatus(@NotNull Law.Status status) {
        this.status = status;
    }

    public @NotNull List<Law.Crime> getCrimes() {
        if (crimes == null){
            crimes = new ArrayList<>();
        }
        return crimes;
    }
    @Override
    public boolean isSuppressed() {
        return isSuppressed;
    }

    @Override
    public void suppress(long period) {
        if (isSuppressed)
            throw new RuntimeException("Suppressible#isInhibited must be called before!");
        isSuppressed = true;
        Player player = player();
        seat = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        seat.setInvisible(true);
        seat.setSmall(true);
        seat.setInvulnerable(true);
        seat.setSilent(true);
        seat.setPersistent(false);
        seat.addPassenger(player);
        SCHEDULER.syncLater(this::removeSuppressible, period);
    }

    @Override
    public void removeSuppressible() {
        if (seat == null)
            return;
        isSuppressed = false;
        seat.remove();
    }

    public boolean isInSafeZone() {
        return isInSafeZone;
    }

    public void setInSafeZone(boolean inSafeZone) {
        isInSafeZone = inSafeZone;
    }

    public boolean isHostile() {
        return isHostile;
    }

    public void setHostile(boolean hostile) {
        isHostile = hostile;
    }

    public boolean isProtected() {
        return isInSafeZone && !isHostile && status == Law.Status.NONE && !isWanted();
    }

    @Override
    public @NotNull String getIdentification() {
        return identification;
    }

    protected void setPlayerDecorator(PlayerDecorator playerDecorator) {
        if (this.playerDecorator != null){
            return;
        }
        this.playerDecorator = playerDecorator;
    }
}