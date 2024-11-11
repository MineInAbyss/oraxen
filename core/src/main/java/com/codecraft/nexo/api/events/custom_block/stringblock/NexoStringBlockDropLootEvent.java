package com.codecraft.nexo.api.events.custom_block.stringblock;

import com.codecraft.nexo.api.events.custom_block.NexoCustomBlockDropLootEvent;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.codecraft.nexo.utils.drops.DroppedLoot;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NexoStringBlockDropLootEvent extends NexoCustomBlockDropLootEvent {
    public NexoStringBlockDropLootEvent(@NotNull StringBlockMechanic mechanic, @NotNull Block block, @NotNull Player player, @NotNull List<DroppedLoot> loots) {
        super(mechanic, block, player, loots);
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
