package io.github.anjoismysign.outlaw;

import org.jetbrains.annotations.NotNull;

public interface Conviction {

    @NotNull
    String getName();

    long getTerm();

    default boolean equals(@NotNull Conviction other){
        return getName().equals(other.getName());
    }

}
