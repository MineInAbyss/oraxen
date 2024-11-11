package com.codecraft.nexo.api.events.custom_block;

import com.codecraft.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.codecraft.nexo.utils.drops.DroppedLoot;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NexoCustomBlockDropLootEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final CustomBlockMechanic mechanic;
    private final Player player;
    private final Block block;
    private final List<DroppedLoot> loots;

    public NexoCustomBlockDropLootEvent(@NotNull final CustomBlockMechanic mechanic, @NotNull final Block block, @NotNull final Player player, @NotNull List<DroppedLoot> loots) {
        this.mechanic = mechanic;
        this.block = block;
        this.player = player;
        this.loots = loots;
    }

    public Player getPlayer() {
        return player;
    }

    public Block getBlock() {
        return block;
    }

    public CustomBlockMechanic getMechanic() {
        return mechanic;
    }

    public List<DroppedLoot> getLoots() {
        return loots;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
