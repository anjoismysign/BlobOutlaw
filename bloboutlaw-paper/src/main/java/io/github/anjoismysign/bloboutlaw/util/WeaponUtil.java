package io.github.anjoismysign.bloboutlaw.util;

import io.github.anjoismysign.bloboutlaw.entity.WeaponHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum WeaponUtil {
    INSTANCE;

    private final List<WeaponHandler> weaponHandlers = new ArrayList<>();

    public boolean isWeapon(@NotNull ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "'itemStack' cannot be null");
        for (WeaponHandler weaponHandler : weaponHandlers) {
            if (weaponHandler.isWeapon(itemStack)){
                return true;
            }
        }
        return false;
    }

    public void addWeaponHandler(@NotNull WeaponHandler weaponHandler){
        Objects.requireNonNull(weaponHandler, "'weaponHandler' cannot be null");
        weaponHandlers.add(weaponHandler);
    }

}
