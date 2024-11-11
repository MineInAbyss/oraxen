package com.codecraft.nexo.utils.breaker;

import com.codecraft.nexo.mechanics.BreakableMechanic;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import com.codecraft.nexo.utils.wrappers.AttributeWrapper;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModernBreakerManager implements BreakerManager {

    private final ConcurrentHashMap<UUID, AttributeModifier> modifierMap;

    public ModernBreakerManager(ConcurrentHashMap<UUID, AttributeModifier> modifierMap) {
        this.modifierMap = modifierMap;
    }

    @Override
    public void startFurnitureBreak(Player player, ItemDisplay baseEntity, FurnitureMechanic mechanic, Block block) {
        //TODO See if this can be handled even with packet-barriers
    }

    @Override
    public void startBlockBreak(Player player, Block block, CustomBlockMechanic mechanic) {
        removeTransientModifier(player);
        if (player.getGameMode() == GameMode.CREATIVE) return;

        addTransientModifier(player, createBreakingModifier(player, block, mechanic.breakable()));
    }

    @Override
    public void stopBlockBreak(Player player) {
        removeTransientModifier(player);
    }

    private AttributeModifier createBreakingModifier(Player player, Block block, BreakableMechanic breakable) {
        return AttributeModifier.deserialize(
                Map.of(
                        "slot", EquipmentSlot.HAND,
                        "uuid", UUID.nameUUIDFromBytes(block.toString().getBytes()).toString(),
                        "name", "nexo:custom_break_speed",
                        "operation", AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                        "amount", (defaultBlockHardness(block) / breakable.hardness() * breakable.speedMultiplier(player)) - 1
                )
        );
    }

    private double defaultBlockHardness(Block block) {
        if (block.getType() == Material.NOTE_BLOCK) return 0.8;
        else if (block.getType() == Material.TRIPWIRE) return 1;
        else return 1;
    }

    private void addTransientModifier(Player player, AttributeModifier modifier) {
        removeTransientModifier(player);
        modifierMap.put(player.getUniqueId(), modifier);
        Optional.ofNullable(player.getAttribute(AttributeWrapper.BLOCK_BREAK_SPEED)).ifPresent(a -> a.addTransientModifier(modifier));
    }

    private void removeTransientModifier(Player player) {
        Optional.ofNullable(modifierMap.remove(player.getUniqueId())).ifPresent(modifier ->
                Optional.ofNullable(player.getAttribute(AttributeWrapper.BLOCK_BREAK_SPEED)).ifPresent(a -> a.removeModifier(modifier))
        );
    }
}
