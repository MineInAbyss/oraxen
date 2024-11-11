package com.codecraft.nexo.mechanics.misc.custom.listeners;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.mechanics.misc.custom.fields.CustomEvent;
import com.codecraft.nexo.utils.actions.ClickAction;
import com.codecraft.nexo.utils.armorequipevent.ArmorEquipEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

public class UnequipListener extends CustomListener {

    public UnequipListener(String itemID, long cooldown, CustomEvent event, ClickAction clickAction) {
        super(itemID, cooldown, event, clickAction);
    }

    @EventHandler
    public void onUnEquipArmor(final ArmorEquipEvent event) {
        ItemStack oldArmor = event.getOldArmorPiece();
        if (oldArmor == null || !itemID.equals(NexoItems.idByItem(oldArmor))) return;
        perform(event.getPlayer(), oldArmor);
    }
}
