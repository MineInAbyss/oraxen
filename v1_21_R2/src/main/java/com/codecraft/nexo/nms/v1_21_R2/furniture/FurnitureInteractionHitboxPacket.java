package com.codecraft.nexo.nms.v1_21_R2.furniture;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;

import java.util.List;

public class FurnitureInteractionHitboxPacket {
    public Integer entityId;
    public ClientboundAddEntityPacket addEntity;
    public ClientboundSetEntityDataPacket metadata;

    public FurnitureInteractionHitboxPacket(int entityId, ClientboundAddEntityPacket addEntity, ClientboundSetEntityDataPacket metadata) {
        this.entityId = entityId;
        this.addEntity = addEntity;
        this.metadata = metadata;
    }

    public ClientboundBundlePacket bundlePackets() {
        return new ClientboundBundlePacket(List.of(new ClientboundRemoveEntitiesPacket(entityId), addEntity, metadata));
    }
}
