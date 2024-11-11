package com.codecraft.nexo.api.events.custom_block.stringblock;

import com.codecraft.nexo.api.events.custom_block.NexoBlockDamageEvent;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired right before a player damages a StringBlock
 * If cancelled, the block will not be damaged.
 * @see StringBlockMechanic
 */
public class NexoStringBlockDamageEvent extends NexoBlockDamageEvent implements Cancellable {

    /**
     * @param mechanic The StringBlockMechanic of this block
     * @param block    The block that was damaged
     * @param player   The player who damaged this block
     */
    public NexoStringBlockDamageEvent(@NotNull StringBlockMechanic mechanic, @NotNull Block block, @NotNull Player player) {
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
