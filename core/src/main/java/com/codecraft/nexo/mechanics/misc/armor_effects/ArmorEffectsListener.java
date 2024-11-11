package com.codecraft.nexo.mechanics.misc.armor_effects;

import com.codecraft.nexo.utils.armorequipevent.ArmorEquipEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ArmorEffectsListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemEquipped(ArmorEquipEvent event) {
        ArmorEffectsMechanic.addEffects(event.getPlayer());
    }

}
