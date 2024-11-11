package com.codecraft.nexo.mechanics.custom_block;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.utils.VersionUtil;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class CustomBlockMiningListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageCustomBlock(BlockDamageEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        CustomBlockMechanic mechanic = NexoBlocks.getCustomBlockMechanic(block.getBlockData());
        if (mechanic == null || player.getGameMode() == GameMode.CREATIVE) return;

        if (VersionUtil.below("1.20.5")) event.setCancelled(true);
        NexoPlugin.get().breakerManager().startBlockBreak(player, block, mechanic);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageAbort(BlockDamageAbortEvent event) {
        NexoPlugin.get().breakerManager().stopBlockBreak(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (VersionUtil.atleast("1.20.5")) NexoPlugin.get().breakerManager().stopBlockBreak(event.getPlayer());
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        NexoPlugin.get().breakerManager().stopBlockBreak(event.getPlayer());
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        NexoPlugin.get().breakerManager().stopBlockBreak(event.getPlayer());
    }

    @EventHandler
    public void onDropHand(PlayerDropItemEvent event) {
        NexoPlugin.get().breakerManager().stopBlockBreak(event.getPlayer());
    }
}
