package com.codecraft.nexo.utils.armorequipevent;

import com.codecraft.nexo.utils.EventUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;

class DispenserArmorListener implements Listener {

    @EventHandler
    public void onArmorDispense(BlockDispenseArmorEvent event) {
        ArmorType type = ArmorType.matchType(event.getItem());
        if (type == null || !(event.getTargetEntity() instanceof Player p)) return;

        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, ArmorEquipEvent.EquipMethod.DISPENSER, type, null, event.getItem());
        if (!EventUtils.callEvent(armorEquipEvent)) event.setCancelled(true);
    }
}
