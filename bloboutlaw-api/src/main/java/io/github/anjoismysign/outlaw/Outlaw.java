package io.github.anjoismysign.outlaw;

/**
 * Interface for outlaw-related data.
 */
public interface Outlaw {

    /**
     * Whether the Outlaw is currently wanted.
     * Being not wanted equals being lawful.
     * Being wanted means that systematically, the Outlaw could end up getting either jailed or killed
     *
     * @return true if is indeed wanted, false otherwise
     */
    default boolean isWanted() {
        return getBounty() >= 0.0000001;
    }

    /**
     * Returns the bounty amount of the outlaw.
     *
     * @return the bounty amount
     */
    double getBounty();

    /**
     * Sets the bounty amount of the outlaw.
     *
     * @param bounty the new bounty amount
     */
    void setBounty(double bounty);

    /**
     * Resets the outlaw's stars and bounty to their initial values.
     */
    default void reset() {
        setBounty(0);
    }
}
