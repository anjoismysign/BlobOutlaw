package io.github.anjoismysign.outlaw;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public interface Prison {

    @NotNull
    Map<String, Cell> cells();

    @Nullable
    default Prisoner look(@NotNull UUID uuid) {
        for (Cell cell : cells().values()) {
            Prisoner prisoner = cell.prisoners().get(uuid);
            if (prisoner != null)
                return prisoner;
        }
        return null;
    }

    @NotNull
    default Prisoner get(@NotNull UUID uuid) {
        return Objects.requireNonNull(look(uuid), "Doesn't seem to be a prisoner by UUID: '" + uuid + "'");
    }

}
