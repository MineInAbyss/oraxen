package com.codecraft.nexo.api;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import com.codecraft.nexo.api.events.custom_block.noteblock.NexoNoteBlockDropLootEvent;
import com.codecraft.nexo.api.events.custom_block.stringblock.NexoStringBlockBreakEvent;
import com.codecraft.nexo.api.events.custom_block.stringblock.NexoStringBlockDropLootEvent;
import com.codecraft.nexo.mechanics.BreakableMechanic;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanicFactory;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanicFactory;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringMechanicHelpers;
import com.codecraft.nexo.mechanics.custom_block.stringblock.sapling.SaplingMechanic;
import com.codecraft.nexo.mechanics.storage.StorageMechanic;
import com.codecraft.nexo.utils.BlockHelpers;
import com.codecraft.nexo.utils.EventUtils;
import com.codecraft.nexo.utils.ItemUtils;
import com.codecraft.nexo.utils.VersionUtil;
import com.codecraft.nexo.utils.drops.Drop;
import com.codecraft.nexo.utils.drops.DroppedLoot;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.codecraft.nexo.mechanics.custom_block.stringblock.sapling.SaplingMechanic.SAPLING_KEY;

public class NexoBlocks {

    /**
     * Get all NexoItem ID's that have either a NoteBlockMechanic or a StringBlockMechanic
     *
     * @return A set of all NexoItem ID's that have either a NoteBlockMechanic or a StringBlockMechanic
     */
    public static Set<String> blockIDs() {
        return Arrays.stream(NexoItems.itemNames()).filter(NexoBlocks::isCustomBlock).collect(Collectors.toSet());
    }

    /**
     * Get all NexoItem ID's that have a NoteBlockMechanic
     *
     * @return A set of all NexoItem ID's that have a NoteBlockMechanic
     */
    public static Set<String> noteBlockIDs() {
        return Arrays.stream(NexoItems.itemNames()).filter(NexoBlocks::isNexoNoteBlock).collect(Collectors.toSet());
    }

    /**
     * Get all NexoItem ID's that have a StringBlockMechanic
     *
     * @return A set of all NexoItem ID's that have a StringBlockMechanic
     */
    public static Set<String> stringBlockIDs() {
        return Arrays.stream(NexoItems.itemNames()).filter(NexoBlocks::isNexoStringBlock).collect(Collectors.toSet());
    }

    /**
     * Check if a block is an instance of an NexoBlock
     *
     * @param block The block to check
     * @return true if the block is an instance of an NexoBlock, otherwise false
     */
    public static boolean isCustomBlock(Block block) {
        if (block == null) return false;
        return switch (block.getType()) {
            case NOTE_BLOCK -> getNoteBlockMechanic(block) != null;
            case TRIPWIRE -> getStringMechanic(block) != null;
            default -> false;
        };
    }

    public static boolean isCustomBlock(ItemStack itemStack) {
        if (itemStack == null) return false;
        String itemId = NexoItems.idByItem(itemStack);
        return isCustomBlock(itemId);
    }

    /**
     * Check if an itemID is an instance of an NexoBlock
     *
     * @param itemId The ID to check
     * @return true if the itemID is an instance of an NexoBlock, otherwise false
     */
    public static boolean isCustomBlock(String itemId) {
        return NexoItems.hasMechanic(itemId, "noteblock")
                || NexoItems.hasMechanic(itemId, "stringblock");
    }

    /**
     * Check if a block is an instance of a NoteBlock
     *
     * @param block The block to check
     * @return true if the block is an instance of an NoteBlock, otherwise false
     */
    public static boolean isNexoNoteBlock(Block block) {
        return block.getType() == Material.NOTE_BLOCK && getNoteBlockMechanic(block) != null;
    }

    /**
     * Check if an itemID has a NoteBlockMechanic
     *
     * @param itemID The itemID to check
     * @return true if the itemID has a NoteBlockMechanic, otherwise false
     */
    public static boolean isNexoNoteBlock(String itemID) {
        return !NoteBlockMechanicFactory.get().isNotImplementedIn(itemID);
    }

    public static boolean isNexoNoteBlock(ItemStack item) {
        return isNexoNoteBlock(NexoItems.idByItem(item));
    }

    /**
     * Check if a block is an instance of a StringBlock
     *
     * @param block The block to check
     * @return true if the block is an instance of a StringBlock, otherwise false
     */
    public static boolean isNexoStringBlock(Block block) {
        return block.getType() == Material.TRIPWIRE && getStringMechanic(block) != null;
    }

    /**
     * Check if an itemID has a StringBlockMechanic
     *
     * @param itemID The itemID to check
     * @return true if the itemID has a StringBlockMechanic, otherwise false
     */
    public static boolean isNexoStringBlock(String itemID) {
        return StringBlockMechanicFactory.isEnabled() && !StringBlockMechanicFactory.get().isNotImplementedIn(itemID);
    }

    public static void place(String itemID, Location location) {

        if (isNexoNoteBlock(itemID)) {
            placeNoteBlock(location, itemID);
        } else if (isNexoStringBlock(itemID)) {
            placeStringBlock(location, itemID);
        }
    }

    /**
     * Get the BlockData assosiated with
     *
     * @param itemID The ItemID of the NexoBlock
     * @return The BlockData assosiated with the ItemID, can be null
     */
    @Nullable
    public static BlockData getBlockData(String itemID) {
        if (isNexoNoteBlock(itemID)) {
            return NoteBlockMechanicFactory.get().getMechanic(itemID).blockData();
        } else if (isNexoStringBlock(itemID)) {
            return StringBlockMechanicFactory.get().getMechanic(itemID).blockData();
        } else return null;
    }

    private static void placeNoteBlock(Location location, String itemID) {
        NoteBlockMechanicFactory.setBlockModel(location.getBlock(), itemID);
        Block block = location.getBlock();
        PersistentDataContainer pdc = BlockHelpers.getPDC(block);
        NoteBlockMechanic mechanic = getNoteBlockMechanic(block);
        if (mechanic == null) return;

        if (mechanic.isStorage() && mechanic.storage().getStorageType() == StorageMechanic.StorageType.STORAGE) {
            pdc.set(StorageMechanic.STORAGE_KEY, DataType.ITEM_STACK_ARRAY, new ItemStack[]{});
        }
        checkNoteBlockAbove(location);
    }

    private static void checkNoteBlockAbove(final Location loc) {
        final Block block = loc.getBlock().getRelative(BlockFace.UP);
        if (block.getType() == Material.NOTE_BLOCK)
            block.getState().update(true, true);
        final Block nextBlock = loc.getBlock().getRelative(BlockFace.UP, 2);
        if (nextBlock.getType() == Material.NOTE_BLOCK)
            checkNoteBlockAbove(block.getLocation());
    }

    private static void placeStringBlock(Location location, String itemID) {
        Block block = location.getBlock();
        Block blockAbove = block.getRelative(BlockFace.UP);
        StringBlockMechanicFactory.setBlockModel(block, itemID);
        StringBlockMechanic mechanic = getStringMechanic(location.getBlock());
        if (mechanic == null) return;
        if (mechanic.isTall()) {
            if (!BlockHelpers.REPLACEABLE_BLOCKS.contains(blockAbove.getType())) return;
            else blockAbove.setType(Material.TRIPWIRE);
        }

        if (mechanic.isSapling()) {
            SaplingMechanic sapling = mechanic.sapling();
            if (sapling != null && sapling.canGrowNaturally())
                BlockHelpers.getPDC(block).set(SAPLING_KEY, PersistentDataType.INTEGER, sapling.getNaturalGrowthTime());
        }
    }

    /**
     * Breaks an NexoBlock at the given location
     *
     * @param location The location of the NexoBlock
     * @param player   The player that broke the block, can be null
     * @return True if the block was broken, false if the block was not an NexoBlock or could not be broken
     */
    public static boolean remove(Location location, @Nullable Player player) {
        return remove(location, player, null);
    }

    /**
     * Breaks an NexoBlock at the given location
     *
     * @param location  The location of the NexoBlock
     * @param player    The player that broke the block, can be null
     * @param forceDrop Whether to force the block to drop, even if player is null or in creative mode
     * @return True if the block was broken, false if the block was not an NexoBlock or could not be broken
     */
    public static boolean remove(Location location, @Nullable Player player, boolean forceDrop) {
        Block block = location.getBlock();

        NoteBlockMechanic noteMechanic = getNoteBlockMechanic(block);
        StringBlockMechanic stringMechanic = getStringMechanic(block);
        BreakableMechanic breakable = noteMechanic != null ? noteMechanic.breakable() : stringMechanic != null ? stringMechanic.breakable() : null;
        Drop overrideDrop = !forceDrop ? null : breakable != null ? breakable.drop() : null;
        return remove(location, player, overrideDrop);
    }

    /**
     * Breaks an NexoBlock at the given location
     *
     * @param location     The location of the NexoBlock
     * @param player       The player that broke the block, can be null
     * @param overrideDrop Drop to override the default drop, can be null
     * @return True if the block was broken, false if the block was not an NexoBlock or could not be broken
     */
    public static boolean remove(Location location, @Nullable Player player, @Nullable Drop overrideDrop) {
        Block block = location.getBlock();

        if (isNexoNoteBlock(block)) return removeNoteBlock(block, player, overrideDrop);
        if (isNexoStringBlock(block)) return removeStringBlock(block, player, overrideDrop);
        return false;
    }

    private static boolean removeNoteBlock(Block block, @Nullable Player player, Drop overrideDrop) {
        ItemStack itemInHand = player != null ? player.getInventory().getItemInMainHand() : new ItemStack(Material.AIR);
        NoteBlockMechanic mechanic = getNoteBlockMechanic(block);
        if (mechanic == null) return false;
        if (mechanic.isDirectional() && !mechanic.directional().isParentBlock())
            mechanic = mechanic.directional().getParentMechanic();

        Location loc = block.getLocation();
        boolean hasOverrideDrop = overrideDrop != null;
        Drop drop = hasOverrideDrop ? overrideDrop : mechanic.breakable().drop();
        if (player != null) {
            NexoNoteBlockBreakEvent noteBlockBreakEvent = new NexoNoteBlockBreakEvent(mechanic, block, player);
            if (!EventUtils.callEvent(noteBlockBreakEvent)) return false;

            if (player.getGameMode() == GameMode.CREATIVE) drop = null;
            else if (hasOverrideDrop || player.getGameMode() != GameMode.CREATIVE) drop = noteBlockBreakEvent.getDrop();

            World world = block.getWorld();

            if (VersionUtil.isPaperServer()) world.sendGameEvent(player, GameEvent.BLOCK_DESTROY, loc.toVector());
            world.playEffect(loc, Effect.STEP_SOUND, block.getBlockData());
        }

        if (drop != null) {
            List<DroppedLoot> loots = drop.spawns(loc, itemInHand);
            if (!loots.isEmpty() && player != null) {
                EventUtils.callEvent(new NexoNoteBlockDropLootEvent(mechanic, block, player, loots));
                ItemUtils.damageItem(player, itemInHand);
                block.setType(Material.AIR);
            }
        }

        if (mechanic.isStorage() && mechanic.storage().getStorageType() == StorageMechanic.StorageType.STORAGE) {
            mechanic.storage().dropStorageContent(block);
        }
        checkNoteBlockAbove(loc);
        return true;
    }


    private static boolean removeStringBlock(Block block, @Nullable Player player, @Nullable Drop overrideDrop) {

        StringBlockMechanic mechanic = getStringMechanic(block);
        ItemStack itemInHand = player != null ? player.getInventory().getItemInMainHand() : new ItemStack(Material.AIR);
        if (mechanic == null) return false;

        boolean hasDropOverride = overrideDrop != null;
        Drop drop = hasDropOverride ? overrideDrop : mechanic.breakable().drop();
        if (player != null) {
            NexoStringBlockBreakEvent wireBlockBreakEvent = new NexoStringBlockBreakEvent(mechanic, block, player);
            if (!EventUtils.callEvent(wireBlockBreakEvent)) return false;

            if (player.getGameMode() == GameMode.CREATIVE)
                drop = null;
            else if (hasDropOverride || player.getGameMode() != GameMode.CREATIVE)
                drop = wireBlockBreakEvent.getDrop();

            if (VersionUtil.isPaperServer()) block.getWorld().sendGameEvent(player, GameEvent.BLOCK_DESTROY, block.getLocation().toVector());
        }

        if (drop != null) {
            List<DroppedLoot> loots = drop.spawns(block.getLocation(), itemInHand);
            if (!loots.isEmpty() && player != null) {
                EventUtils.callEvent(new NexoStringBlockDropLootEvent(mechanic, block, player, loots));
            }
        }

        final Block blockAbove = block.getRelative(BlockFace.UP);
        if (mechanic.isTall()) blockAbove.setType(Material.AIR);
        block.setType(Material.AIR);
        Bukkit.getScheduler().runTaskLater(NexoPlugin.get(), () -> {
            StringMechanicHelpers.fixClientsideUpdate(block.getLocation());
            if (blockAbove.getType() == Material.TRIPWIRE)
                removeStringBlock(blockAbove, player, overrideDrop);
        }, 1L);
        return true;
    }

    /**
     * Get the NexoBlock at a location
     *
     * @param location The location to check
     * @return The Mechanic of the NexoBlock at the location, or null if there is no NexoBlock at the location.
     * Keep in mind that this method returns the base Mechanic, not the type. Therefore, you will need to cast this to the type you need
     */
    @Nullable
    public static CustomBlockMechanic getCustomBlockMechanic(Location location) {
        return !isCustomBlock(location.getBlock()) ? null :
                switch (location.getBlock().getType()) {
                    case NOTE_BLOCK -> getNoteBlockMechanic(location.getBlock());
                    case TRIPWIRE -> getStringMechanic(location.getBlock());
                    default -> null;
                };
    }

    @Nullable
    public static CustomBlockMechanic getCustomBlockMechanic(BlockData blockData) {
        return switch (blockData.getMaterial()) {
            case NOTE_BLOCK -> getNoteBlockMechanic(blockData);
            case TRIPWIRE -> getStringMechanic(blockData);
            default -> null;
        };
    }

    @Nullable
    public static CustomBlockMechanic getCustomBlockMechanic(String itemID) {
        CustomBlockMechanic mechanic = null;
        if (NoteBlockMechanicFactory.isEnabled()) mechanic = NoteBlockMechanicFactory.get().getMechanic(itemID);
        if (mechanic != null) return mechanic;
        if (StringBlockMechanicFactory.isEnabled()) mechanic = StringBlockMechanicFactory.get().getMechanic(itemID);

        return mechanic;
    }

    public static NoteBlockMechanic getNoteBlockMechanic(BlockData data) {
        if (!NoteBlockMechanicFactory.isEnabled()) return null;
        if (!(data instanceof NoteBlock noteBlock)) return null;
        return NoteBlockMechanicFactory.getMechanic(noteBlock);
    }

    public static NoteBlockMechanic getNoteBlockMechanic(Block block) {
        if (!NoteBlockMechanicFactory.isEnabled()) return null;
        if (!(block.getBlockData() instanceof NoteBlock noteBlock)) return null;
        return NoteBlockMechanicFactory.getMechanic(noteBlock);
    }

    @Nullable
    public static NoteBlockMechanic getNoteBlockMechanic(String itemID) {
        if (!NoteBlockMechanicFactory.isEnabled()) return null;
        return NoteBlockMechanicFactory.get().getMechanic(itemID);
    }

    @Nullable
    public static StringBlockMechanic getStringMechanic(BlockData blockData) {
        if (!StringBlockMechanicFactory.isEnabled()) return null;
        if (!(blockData instanceof Tripwire tripwire)) return null;
        return StringBlockMechanicFactory.getMechanic(tripwire);
    }

    @Nullable
    public static StringBlockMechanic getStringMechanic(Block block) {
        if (!StringBlockMechanicFactory.isEnabled()) return null;
        if (!(block.getBlockData() instanceof Tripwire tripwire)) return null;
        return StringBlockMechanicFactory.getMechanic(tripwire);
    }

    @Nullable
    public static StringBlockMechanic getStringMechanic(String itemID) {
        if (!StringBlockMechanicFactory.isEnabled()) return null;
        return StringBlockMechanicFactory.get().getMechanic(itemID);
    }
}
