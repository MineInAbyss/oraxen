package com.codecraft.nexo.mechanics.custom_block.noteblock;

import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.events.custom_block.noteblock.NexoNoteBlockInteractEvent;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockHelpers;
import com.codecraft.nexo.mechanics.storage.StorageMechanic;
import com.codecraft.nexo.utils.BlockHelpers;
import com.codecraft.nexo.utils.EventUtils;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteBlockMechanicListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlaceAgainstNoteBlock(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        EquipmentSlot hand = event.getHand();

        if (hand == null || event.getAction() != Action.RIGHT_CLICK_BLOCK || block == null || block.getType() != Material.NOTE_BLOCK) return;
        if (!player.isSneaking() && BlockHelpers.isInteractable(block)) return;
        if (event.useInteractedBlock() == Event.Result.DENY || !NexoBlocks.isNexoNoteBlock(block)) return;

        if (NexoBlocks.isNexoNoteBlock(item)) return;
        if (item == null) return;

        Material type = item.getType();
        if (type == Material.AIR) return;

        event.setUseInteractedBlock(Event.Result.DENY);
        BlockData newData = type.isBlock() ? type.createBlockData() : null;
        CustomBlockHelpers.makePlayerPlaceBlock(player, event.getHand(), item, block, event.getBlockFace(), null, newData);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(NexoNoteBlockInteractEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        NoteBlockMechanic mechanic = event.getMechanic();
        StorageMechanic storageMechanic = mechanic.storage();
        if (storageMechanic == null) return;

        switch (storageMechanic.getStorageType()) {
            case STORAGE, SHULKER -> storageMechanic.openStorage(block, player);
            case PERSONAL -> storageMechanic.openPersonalStorage(player, block.getLocation(), null);
            case DISPOSAL -> storageMechanic.openDisposal(player, block.getLocation(), null);
            case ENDERCHEST -> player.openInventory(player.getEnderChest());
        }
        event.setCancelled(true);
    }

    // If block is not a custom block, play the correct sound according to the below block or default
    @EventHandler(priority = EventPriority.NORMAL)
    public void onNotePlayed(final NotePlayEvent event) {
        if (NexoBlocks.isNexoNoteBlock(event.getBlock())) event.setCancelled(true);
        else if (!NoteBlockMechanicFactory.get().reimplementNoteblockFeatures) {
            if (instrumentMap.isEmpty()) instrumentMap = getInstrumentMap();
            String blockType = event.getBlock().getRelative(BlockFace.DOWN).getType().toString().toLowerCase();
            Instrument fakeInstrument = instrumentMap.entrySet().stream().filter(e -> e.getValue().contains(blockType)).map(Map.Entry::getKey).findFirst().orElse(Instrument.PIANO);
            // This is deprecated, but seems to be without reason
            event.setInstrument(fakeInstrument);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFallingNexoBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock fallingBlock) {
            BlockData blockData = fallingBlock.getBlockData();
            NoteBlockMechanic mechanic = NexoBlocks.getNoteBlockMechanic(blockData);
            if (mechanic == null) return;
            NexoBlocks.place(mechanic.getItemID(), event.getBlock().getLocation());
            fallingBlock.setDropItem(false);
        }
    }

    @EventHandler
    public void onBreakBeneathFallingNexoBlock(BlockBreakEvent event) {
        NoteMechanicHelpers.handleFallingNexoBlockAbove(event.getBlock());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSetFire(final PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();
        if (block == null || block.getType() != Material.NOTE_BLOCK) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getBlockFace() != BlockFace.UP) return;
        if (item == null) return;

        NoteBlockMechanic mechanic = NexoBlocks.getNoteBlockMechanic(block);
        if (mechanic == null) return;
        if (!mechanic.canIgnite()) return;
        if (item.getType() != Material.FLINT_AND_STEEL && item.getType() != Material.FIRE_CHARGE) return;

        EventUtils.callEvent(new BlockIgniteEvent(block, BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL, event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCatchFire(final BlockIgniteEvent event) {
        Block block = event.getBlock();
        NoteBlockMechanic mechanic = NexoBlocks.getNoteBlockMechanic(block);
        if (mechanic == null) return;
        if (!mechanic.canIgnite()) event.setCancelled(true);
        else {
            block.getWorld().playSound(block.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1, 1);
            block.getRelative(BlockFace.UP).setType(Material.FIRE);
        }
    }

    // Used to determine what instrument to use when playing a note depending on below block
    public static Map<Instrument, List<String>> instrumentMap = new HashMap<>();

    private static Map<Instrument, List<String>> getInstrumentMap() {
        Map<Instrument, List<String>> map = new HashMap<>();
        map.put(Instrument.BELL, List.of("gold_block"));
        map.put(Instrument.BASS_DRUM, Arrays.asList("stone", "netherrack", "bedrock", "observer", "coral", "obsidian", "anchor", "quartz"));
        map.put(Instrument.FLUTE, List.of("clay"));
        map.put(Instrument.CHIME, List.of("packed_ice"));
        map.put(Instrument.GUITAR, List.of("wool"));
        map.put(Instrument.XYLOPHONE, List.of("bone_block"));
        map.put(Instrument.IRON_XYLOPHONE, List.of("iron_block"));
        map.put(Instrument.COW_BELL, List.of("soul_sand"));
        map.put(Instrument.DIDGERIDOO, List.of("pumpkin"));
        map.put(Instrument.BIT, List.of("emerald_block"));
        map.put(Instrument.BANJO, List.of("hay_bale"));
        map.put(Instrument.PLING, List.of("glowstone"));
        map.put(Instrument.BASS_GUITAR, List.of("wood"));
        map.put(Instrument.SNARE_DRUM, Arrays.asList("sand", "gravel", "concrete_powder", "soul_soil"));
        map.put(Instrument.STICKS, Arrays.asList("glass", "sea_lantern", "beacon"));

        return map;
    }
}
