package io.github.anjoismysign.bloboutlaw.entity;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface WeaponHandler {

    boolean isWeapon(@NotNull ItemStack itemStack);

}
