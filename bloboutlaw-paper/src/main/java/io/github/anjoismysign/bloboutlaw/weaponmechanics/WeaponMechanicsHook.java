package io.github.anjoismysign.bloboutlaw.weaponmechanics;

import io.github.anjoismysign.bloboutlaw.entity.WeaponHandler;
import io.github.anjoismysign.bloboutlaw.util.WeaponUtil;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import org.bukkit.event.Listener;

public class WeaponMechanicsHook implements Listener {

    public WeaponMechanicsHook(){
        WeaponUtil.INSTANCE.addWeaponHandler(generateWeaponHandler());
    }

    private WeaponHandler generateWeaponHandler(){
        return itemStack -> WeaponMechanicsAPI.getWeaponTitle(itemStack) != null;
    }

}
