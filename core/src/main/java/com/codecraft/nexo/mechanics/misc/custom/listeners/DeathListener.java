package com.codecraft.nexo.mechanics.misc.custom.listeners;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.mechanics.misc.custom.fields.CustomEvent;
import com.codecraft.nexo.utils.actions.ClickAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DeathListener extends CustomListener {

    public DeathListener(String itemID, long cooldown, CustomEvent event, ClickAction clickAction) {
        super(itemID, cooldown, event, clickAction);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        for (ItemStack drop : event.getDrops()) {
            if (!itemID.equals(NexoItems.idByItem(drop))) continue;
            perform(event.getEntity().getPlayer(), drop);
        }
    }
}
