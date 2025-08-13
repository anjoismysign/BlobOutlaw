package io.github.anjoismysign.outlaw;

public interface Suppressible {

    boolean isSuppressed();

    void suppress(long period);

    void removeSuppressible();

}
