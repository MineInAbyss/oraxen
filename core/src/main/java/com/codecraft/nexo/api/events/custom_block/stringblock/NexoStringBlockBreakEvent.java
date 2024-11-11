package com.codecraft.nexo.api.events.custom_block.stringblock;

import com.codecraft.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public class NexoStringBlockBreakEvent extends NexoBlockBreakEvent implements Cancellable {

    public NexoStringBlockBreakEvent(@NotNull StringBlockMechanic mechanic, @NotNull Block block, @NotNull Player player) {
        super(mechanic, block, player);
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
