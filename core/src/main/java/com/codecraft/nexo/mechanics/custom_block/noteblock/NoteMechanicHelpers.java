package com.codecraft.nexo.mechanics.custom_block.noteblock;

import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.utils.BlockHelpers;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

public class NoteMechanicHelpers {

    // Not exclusively in NoteBlockMechanic as future blocks might want to support this aswell
    public static void handleFallingNexoBlockAbove(Block block) {
        Block blockAbove = block.getRelative(BlockFace.UP);
        NoteBlockMechanic mechanic = NexoBlocks.getNoteBlockMechanic(blockAbove);
        if (mechanic == null || !mechanic.isFalling()) return;
        Location fallingLocation = BlockHelpers.toCenterBlockLocation(blockAbove.getLocation());
        BlockData fallingData = NexoBlocks.getBlockData(mechanic.getItemID());
        if (fallingData == null) return;
        NexoBlocks.remove(blockAbove.getLocation(), null);
        blockAbove.getWorld().spawnFallingBlock(fallingLocation, fallingData);
        handleFallingNexoBlockAbove(blockAbove);
    }
}
