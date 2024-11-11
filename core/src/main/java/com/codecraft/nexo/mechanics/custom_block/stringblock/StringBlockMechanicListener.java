package com.codecraft.nexo.mechanics.custom_block.stringblock;

import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.api.events.custom_block.stringblock.NexoStringBlockPlaceEvent;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockHelpers;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockMechanic;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.util.List;
import java.util.Random;

public class StringBlockMechanicListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlacingString(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.STRING) return;
        if (!StringBlockMechanicFactory.get().disableVanillaString) return;

        event.setUseItemInHand(Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlaceableOnWater(PlayerInteractEvent event) {
        final ItemStack item = event.getItem();
        final String itemID = NexoItems.idByItem(item);
        final Player player = event.getPlayer();
        final RayTraceResult result = player.rayTraceBlocks(5.0, FluidCollisionMode.SOURCE_ONLY);
        Block placedAgainst = result != null ? result.getHitBlock() : null;
        CustomBlockMechanic mechanic = NexoBlocks.getCustomBlockMechanic(itemID);

        if (mechanic == null || placedAgainst == null || !placedAgainst.isLiquid()) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!(mechanic instanceof StringBlockMechanic stringMechanic)) return;
        if (stringMechanic.hasRandomPlace()) {
            List<String> randomList = stringMechanic.randomPlace();
            String randomBlock = randomList.get(new Random().nextInt(randomList.size()));
            stringMechanic = NexoBlocks.getStringMechanic(randomBlock);
        }
        if (stringMechanic != null) mechanic = stringMechanic;
        if (!((StringBlockMechanic) mechanic).isPlaceableOnWater()) return;
        Block target = placedAgainst.getRelative(BlockFace.UP);
        if (target.getType() != Material.AIR) return;

        CustomBlockHelpers.makePlayerPlaceBlock(player, event.getHand(), item, target, event.getBlockFace(), mechanic, mechanic.blockData());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreakingTall(final BlockBreakEvent event) {
        Block block = event.getBlock().getRelative(BlockFace.DOWN);
        if (event.getBlock().getType() != Material.TRIPWIRE || NexoBlocks.isNexoStringBlock(event.getBlock())) return;
        event.setDropItems(false);
        NexoBlocks.remove(block.getLocation(), event.getPlayer());
    }

    @EventHandler
    public void onPlaceStringOnString(NexoStringBlockPlaceEvent event) {
        Block block = event.getBlock();
        Block below = block.getRelative(BlockFace.DOWN);
        if (NexoBlocks.isNexoStringBlock(below)) event.setCancelled(true);
        if (below.getType() == Material.TRIPWIRE && NexoBlocks.isNexoStringBlock(below.getRelative(BlockFace.DOWN))) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWaterUpdate(final BlockFromToEvent event) {
        final Block changed = event.getToBlock();
        final Block changedBelow = changed.getRelative(BlockFace.DOWN);
        if (!event.getBlock().isLiquid() || changed.getType() != Material.TRIPWIRE) return;

        event.setCancelled(true);
        StringBlockMechanic mechanicBelow = NexoBlocks.getStringMechanic(changedBelow);
        if (NexoBlocks.isNexoStringBlock(changed))
            NexoBlocks.remove(changed.getLocation(), null, true);
        else if (mechanicBelow != null && mechanicBelow.isTall())
            NexoBlocks.remove(changedBelow.getLocation(), null, true);

    }
}
