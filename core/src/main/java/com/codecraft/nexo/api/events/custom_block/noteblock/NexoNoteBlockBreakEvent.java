package com.codecraft.nexo.api.events.custom_block.noteblock;

import com.codecraft.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public class NexoNoteBlockBreakEvent extends NexoBlockBreakEvent implements Cancellable {

    /**
     * @param mechanic The CustomBlockMechanic of this block
     * @param block    The block that was damaged
     * @param player   The player who damaged this block
     */
    public NexoNoteBlockBreakEvent(@NotNull NoteBlockMechanic mechanic, @NotNull Block block, @NotNull Player player) {
        super(mechanic, block, player);
    }

    /**
     * @return The NoteBlockMechanic of this block
     */
    @NotNull
    @Override
    public NoteBlockMechanic getMechanic() {
        return (NoteBlockMechanic) super.getMechanic();
    }

}
