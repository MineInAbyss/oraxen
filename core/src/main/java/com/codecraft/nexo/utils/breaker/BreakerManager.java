package com.codecraft.nexo.utils.breaker;

import com.codecraft.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;

public interface BreakerManager {

    void startFurnitureBreak(Player player, ItemDisplay baseEntity, FurnitureMechanic mechanic, Block block);
    void startBlockBreak(Player player, Block block, CustomBlockMechanic mechanic);
    void stopBlockBreak(Player player);
}
