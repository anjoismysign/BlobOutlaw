package io.github.anjoismysign.bloboutlaw.law;

public enum SimpleLaw implements Law {
    INSTANCE;

    @Override
    public int getMaxStars() {
        return 5;
    }
}
