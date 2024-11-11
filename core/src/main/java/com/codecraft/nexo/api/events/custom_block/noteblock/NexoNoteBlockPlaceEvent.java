package com.codecraft.nexo.api.events.custom_block.noteblock;

import com.codecraft.nexo.api.events.custom_block.NexoBlockPlaceEvent;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NexoNoteBlockPlaceEvent extends NexoBlockPlaceEvent implements Cancellable {

    public NexoNoteBlockPlaceEvent(@NotNull CustomBlockMechanic mechanic, @NotNull Block block, @NotNull Player player, @NotNull ItemStack itemInHand, @NotNull EquipmentSlot hand) {
        super(mechanic, block, player, itemInHand, hand);
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
