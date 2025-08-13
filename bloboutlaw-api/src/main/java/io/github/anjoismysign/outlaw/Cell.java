package io.github.anjoismysign.outlaw;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface Cell {

    @NotNull
    Map<UUID, Prisoner> prisoners();

    @NotNull
    String name();

}
