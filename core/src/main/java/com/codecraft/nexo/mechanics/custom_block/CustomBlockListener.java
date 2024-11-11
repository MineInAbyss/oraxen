package com.codecraft.nexo.mechanics.custom_block;

import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.api.events.custom_block.NexoBlockInteractEvent;
import com.codecraft.nexo.api.events.custom_block.noteblock.NexoNoteBlockInteractEvent;
import com.codecraft.nexo.api.events.custom_block.stringblock.NexoStringBlockInteractEvent;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.codecraft.nexo.mechanics.limitedplacing.LimitedPlacing;
import com.codecraft.nexo.utils.BlockHelpers;
import com.codecraft.nexo.utils.EventUtils;
import io.th0rgal.protectionlib.ProtectionLib;
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
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CustomBlockListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void callInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock().getType() != Material.NOTE_BLOCK) return;
        CustomBlockMechanic mechanic = NexoBlocks.getCustomBlockMechanic(block.getBlockData());

        Event customBlockEvent;
        if (mechanic instanceof NoteBlockMechanic noteMechanic)
            customBlockEvent = new NexoNoteBlockInteractEvent(noteMechanic, event.getPlayer(), event.getItem(), event.getHand(), block, event.getBlockFace(), event.getAction());
        else if (mechanic instanceof StringBlockMechanic stringMechanic)
            customBlockEvent = new NexoStringBlockInteractEvent(stringMechanic, event.getPlayer(), event.getItem(), event.getHand(), block, event.getBlockFace(), event.getAction());
        else return;

        if (!EventUtils.callEvent(customBlockEvent)) event.setUseInteractedBlock(Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractedNexoBlock(NexoBlockInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        CustomBlockMechanic mechanic = event.getMechanic();
        if (!ProtectionLib.canInteract(player, block.getLocation())) event.setCancelled(true);
        else if (!player.isSneaking() && mechanic.hasClickActions()) {
            mechanic.runClickActions(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLimitedPlacing(final PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        BlockFace blockFace = event.getBlockFace();
        ItemStack item = event.getItem();

        if (item == null || block == null || event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        CustomBlockMechanic mechanic = NexoBlocks.getCustomBlockMechanic(NexoItems.idByItem(item));
        if (mechanic == null || !mechanic.hasLimitedPlacing()) return;

        if (!event.getPlayer().isSneaking() && BlockHelpers.isInteractable(block)) return;

        LimitedPlacing limitedPlacing = mechanic.limitedPlacing();
        Block belowPlaced = block.getRelative(blockFace).getRelative(BlockFace.DOWN);

        if (limitedPlacing.isNotPlacableOn(block, blockFace)) event.setCancelled(true);
        else if (limitedPlacing.isRadiusLimited()) {
            LimitedPlacing.RadiusLimitation radiusLimitation = limitedPlacing.getRadiusLimitation();
            int rad = radiusLimitation.getRadius();
            int amount = radiusLimitation.getAmount();
            int count = 0;
            for (int x = -rad; x <= rad; x++)
                for (int y = -rad; y <= rad; y++)
                    for (int z = -rad; z <= rad; z++) {
                        StringBlockMechanic relativeMechanic = NexoBlocks.getStringMechanic(block.getRelative(x, y, z));
                        if (relativeMechanic == null || !relativeMechanic.getItemID().equals(mechanic.getItemID()))
                            continue;
                        count++;
                    }
            if (count >= amount) event.setCancelled(true);
        } else if (limitedPlacing.getType() == LimitedPlacing.LimitedPlacingType.ALLOW) {
            if (!limitedPlacing.checkLimitedMechanic(belowPlaced))
                event.setCancelled(true);
        } else if (limitedPlacing.getType() == LimitedPlacing.LimitedPlacingType.DENY) {
            if (limitedPlacing.checkLimitedMechanic(belowPlaced))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrePlacingCustomBlock(final PlayerInteractEvent event) {
        final ItemStack item = event.getItem();
        final String itemID = NexoItems.idByItem(item);
        final Player player = event.getPlayer();
        final Block placedAgainst = event.getClickedBlock();
        BlockFace face = event.getBlockFace();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || placedAgainst == null) return;

        CustomBlockMechanic mechanic = NexoBlocks.getCustomBlockMechanic(itemID);
        if (mechanic == null) return;
        if (!player.isSneaking() && BlockHelpers.isInteractable(placedAgainst)) return;

        // Change mechanic according to subMechanic changes
        if (mechanic instanceof NoteBlockMechanic noteMechanic && noteMechanic.isDirectional()) {
            if (noteMechanic.directional().isParentBlock()) {
                noteMechanic = noteMechanic.directional().directionMechanic(face, player);
                if (noteMechanic != null) mechanic = noteMechanic;
            } else {
                noteMechanic = noteMechanic.directional().getParentMechanic();
                if (noteMechanic != null) mechanic = noteMechanic;
            }
        } else if (mechanic instanceof StringBlockMechanic stringMechanic) {
            if (stringMechanic.hasRandomPlace()) {
                List<String> randomList = stringMechanic.randomPlace();
                String randomBlock = randomList.get(new Random().nextInt(randomList.size()));
                stringMechanic = NexoBlocks.getStringMechanic(randomBlock);
            }
            if (stringMechanic != null) mechanic = stringMechanic;
            if (placedAgainst.getRelative(face).isLiquid()) return;
        }

        CustomBlockHelpers.makePlayerPlaceBlock(player, event.getHand(), item, placedAgainst, face, mechanic, mechanic.blockData());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreakingCustomBlock(final BlockBreakEvent event) {
        if (NexoBlocks.isCustomBlock(event.getBlock())) event.setDropItems(false);
        NexoBlocks.remove(event.getBlock().getLocation(), event.getPlayer());
    }

    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        for (Block block : new HashSet<>(event.blockList())) {
            CustomBlockMechanic mechanic = NexoBlocks.getCustomBlockMechanic(block.getBlockData());
            if (mechanic == null) continue;
            else if (!mechanic.isBlastResistant()) NexoBlocks.remove(block.getLocation(), null);
            event.blockList().remove(block);
        }
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        for (Block block : new HashSet<>(event.blockList())) {
            CustomBlockMechanic mechanic = NexoBlocks.getCustomBlockMechanic(block.getBlockData());
            if (mechanic == null) continue;
            else if (!mechanic.isBlastResistant()) NexoBlocks.remove(block.getLocation(), null);
            event.blockList().remove(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlacingVanilla(final BlockPlaceEvent event) {
        Block placedBlock = event.getBlockPlaced();
        if (placedBlock.getType() != Material.TRIPWIRE && placedBlock.getType() != Material.NOTE_BLOCK) return;
        if (NexoBlocks.isCustomBlock(event.getItemInHand())) return;

        // Placing string, meant for the first blockstate as invisible string
        placedBlock.setBlockData(placedBlock.getType().createBlockData(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMiddleClick(final InventoryCreativeEvent event) {
        if (event.getClick() != ClickType.CREATIVE) return;
        final Player player = (Player) event.getInventory().getHolder();
        if (player == null) return;
        if (event.getCursor().getType() == Material.NOTE_BLOCK) {
            final RayTraceResult rayTraceResult = player.rayTraceBlocks(6.0);
            if (rayTraceResult == null) return;
            final Block block = rayTraceResult.getHitBlock();
            if (block == null) return;

            CustomBlockMechanic mechanic = NexoBlocks.getCustomBlockMechanic(block.getBlockData());

            if (mechanic == null) {
                StringBlockMechanic mechanicBelow = NexoBlocks.getStringMechanic(block.getRelative(BlockFace.DOWN));
                if (mechanicBelow == null || !mechanicBelow.isTall()) return;
                mechanic = mechanicBelow;
            }

            ItemStack item = NexoItems.itemById(mechanic.getItemID()).build();

            if (mechanic instanceof NoteBlockMechanic noteMechanic) {
                if (noteMechanic.isDirectional() && !noteMechanic.directional().isParentBlock())
                    item = NexoItems.itemById(noteMechanic.directional().getParentBlock()).build();
            }

            for (int i = 0; i <= 8; i++) {
                if (player.getInventory().getItem(i) == null) continue;
                if (Objects.equals(NexoItems.idByItem(player.getInventory().getItem(i)), NexoItems.idByItem(item))) {
                    player.getInventory().setHeldItemSlot(i);
                    event.setCancelled(true);
                    return;
                }
            }
            event.setCursor(item);
        }
    }
}
