package com.codecraft.nexo.mechanics.misc.custom.listeners;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.mechanics.misc.custom.fields.CustomEvent;
import com.codecraft.nexo.utils.actions.ClickAction;
import com.codecraft.nexo.utils.armorequipevent.ArmorEquipEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

public class EquipListener extends CustomListener {

    public EquipListener(String itemID, long cooldown, CustomEvent event, ClickAction clickAction) {
        super(itemID, cooldown, event, clickAction);
    }

    @EventHandler
    public void onEquipArmor(final ArmorEquipEvent event) {
        ItemStack newArmor = event.getNewArmorPiece();
        if (newArmor == null || !itemID.equals(NexoItems.idByItem(newArmor))) return;
        perform(event.getPlayer(), newArmor);
    }
}
