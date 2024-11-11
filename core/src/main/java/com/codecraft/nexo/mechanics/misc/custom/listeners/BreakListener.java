package com.codecraft.nexo.mechanics.misc.custom.listeners;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.mechanics.misc.custom.fields.CustomEvent;
import com.codecraft.nexo.utils.actions.ClickAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BreakListener extends CustomListener {

    public BreakListener(String itemID, long cooldown, CustomEvent event, ClickAction clickAction) {
        super(itemID, cooldown, event, clickAction);
    }

    @EventHandler
    public void onBroken(PlayerItemBreakEvent event) {
        ItemStack item = event.getBrokenItem();
        if (!itemID.equals(NexoItems.idByItem(item)))
            return;
        perform(event.getPlayer(), item);
    }
}
