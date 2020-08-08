package io.th0rgal.oraxen.mechanics.provided.soulbound;

import io.th0rgal.oraxen.items.OraxenItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class SoulBoundMechanicListener implements Listener {
    private final SoulBoundMechanicFactory factory;
    private final HashMap<Player, List<ItemStack>> SOUL_BOUND_ITEMS = new HashMap<>();


    public SoulBoundMechanicListener(SoulBoundMechanicFactory factory) {
        this.factory = factory;
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory())
            return;
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack drop : event.getDrops()) {
            String itemID = OraxenItems.getIdByItem(drop);
            if (itemID == null)
                continue;
            if (!factory.isNotImplementedIn(itemID)) {
                SoulBoundMechanic mechanic = (SoulBoundMechanic) this.factory.getMechanic(itemID);
                if (new Random().nextInt(100) >= mechanic.getLoseChance() * 100)
                    items.add(drop);
            }
        }
        if (!items.isEmpty()) {
            this.SOUL_BOUND_ITEMS.put(event.getEntity(), items);
            event.getDrops().removeAll(items);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!this.SOUL_BOUND_ITEMS.containsKey(event.getPlayer()))
            return;
        Player player = event.getPlayer();
        for (ItemStack item : this.SOUL_BOUND_ITEMS.get(player)) {
            if (player.getInventory().firstEmpty() != -1)
                player.getInventory().addItem(item);
            else
                player.getWorld().dropItem(player.getLocation(), item);
        }
        this.SOUL_BOUND_ITEMS.remove(player);
    }
}
