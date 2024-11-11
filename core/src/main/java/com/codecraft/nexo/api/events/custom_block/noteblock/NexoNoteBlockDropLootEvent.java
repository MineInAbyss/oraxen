package com.codecraft.nexo.api.events.custom_block.noteblock;

import com.codecraft.nexo.api.events.custom_block.NexoCustomBlockDropLootEvent;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import com.codecraft.nexo.utils.drops.DroppedLoot;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NexoNoteBlockDropLootEvent extends NexoCustomBlockDropLootEvent {
    public NexoNoteBlockDropLootEvent(@NotNull NoteBlockMechanic mechanic, @NotNull Block block, @NotNull Player player, @NotNull List<DroppedLoot> loots) {
        super(mechanic, block, player, loots);
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
