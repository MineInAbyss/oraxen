package com.codecraft.nexo.nms;

import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import com.codecraft.nexo.mechanics.furniture.IFurniturePacketManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EmptyFurniturePacketManager implements IFurniturePacketManager {
    @Override
    public int nextEntityId() {
        return -1;
    }

    @Override
    public Entity getEntity(int entityId) {
        return null;
    }

    @Override
    public void sendInteractionEntityPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player) {

    }

    @Override
    public void removeInteractionHitboxPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic) {

    }

    @Override
    public void removeInteractionHitboxPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player) {

    }

    @Override
    public void sendBarrierHitboxPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player) {

    }

    @Override
    public void removeBarrierHitboxPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic) {

    }

    @Override
    public void removeBarrierHitboxPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player) {

    }
}
