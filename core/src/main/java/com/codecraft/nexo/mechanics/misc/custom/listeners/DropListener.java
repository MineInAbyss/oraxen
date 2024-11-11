package com.codecraft.nexo.mechanics.misc.custom.listeners;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.mechanics.misc.custom.fields.CustomEvent;
import com.codecraft.nexo.utils.actions.ClickAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class DropListener extends CustomListener {


    public DropListener(String itemID, long cooldown, CustomEvent event, ClickAction clickAction) {
        super(itemID, cooldown, event, clickAction);
    }

    @EventHandler
    public void onDropped(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (!itemID.equals(NexoItems.idByItem(item))) return;
        perform(event.getPlayer(), item);
    }

}
