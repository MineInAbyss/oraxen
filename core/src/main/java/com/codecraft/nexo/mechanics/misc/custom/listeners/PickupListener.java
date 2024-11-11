package com.codecraft.nexo.mechanics.misc.custom.listeners;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.mechanics.misc.custom.fields.CustomEvent;
import com.codecraft.nexo.utils.actions.ClickAction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class PickupListener extends CustomListener {


    public PickupListener(String itemID, long cooldown, CustomEvent event, ClickAction clickAction) {
        super(itemID, cooldown, event, clickAction);
    }

    @EventHandler
    public void onPickedUp(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        if (!itemID.equals(NexoItems.idByItem(item)))
            return;
        perform(player, item);
    }

}
