package com.codecraft.nexo.api.events.custom_block.stringblock;

import com.codecraft.nexo.api.events.custom_block.NexoBlockInteractEvent;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NexoStringBlockInteractEvent extends NexoBlockInteractEvent implements Cancellable {

    public NexoStringBlockInteractEvent(@NotNull CustomBlockMechanic mechanic, @NotNull Player player, @Nullable ItemStack itemInHand, @NotNull EquipmentSlot hand, @NotNull Block block, @NotNull BlockFace blockFace, @NotNull Action action) {
        super(mechanic, player, itemInHand, hand, block, blockFace, action);
    }

    /**
     * @return The StringBlockMechanic of this block
     */
    @NotNull
    @Override
    public StringBlockMechanic getMechanic() {
        return (StringBlockMechanic) super.getMechanic();
    }
}