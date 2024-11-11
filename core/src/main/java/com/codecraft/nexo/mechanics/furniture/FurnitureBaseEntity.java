package com.codecraft.nexo.mechanics.furniture;

import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.items.ItemBuilder;
import com.codecraft.nexo.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class FurnitureBaseEntity {

    private ItemStack itemStack;
    private final UUID baseUuid;
    private final int baseId;
    private final FurnitureMechanic mechanic;
    private final String furnitureId;

    public FurnitureBaseEntity(ItemDisplay baseEntity, FurnitureMechanic mechanic) {
        this.mechanic = mechanic;
        this.furnitureId = mechanic.getItemID();
        ItemStack furnitureItem = NexoItems.optionalItemById(mechanic.getItemID()).orElse(new ItemBuilder(Material.BARRIER)).build().clone();
        ItemUtils.dyeItem(furnitureItem, FurnitureHelpers.furnitureDye(baseEntity));
        ItemUtils.displayName(furnitureItem, null);
        this.itemStack = furnitureItem;
        this.baseUuid = baseEntity.getUniqueId();
        this.baseId = baseEntity.getEntityId();
    }

    public ItemStack itemStack() {
        return this.itemStack;
    }
    public void itemStack(@NotNull ItemStack itemStack) {
        ItemUtils.displayName(itemStack.clone(), null);
        this.itemStack = itemStack;
    }

    public UUID baseUUID() {
        return baseUuid;
    }

    public ItemDisplay baseEntity() {
        return (ItemDisplay) Bukkit.getEntity(baseUuid);
    }

    public Integer baseId() {
        return baseId;
    }

    public FurnitureMechanic mechanic() {
        return Optional.ofNullable(NexoFurniture.getFurnitureMechanic(furnitureId)).orElse(Optional.ofNullable(mechanic).orElse(NexoFurniture.getFurnitureMechanic(baseEntity())));
    }

    public boolean equals(FurnitureBaseEntity baseEntity) {
        return this.baseUuid.equals(baseEntity.baseUuid) && this.baseId == baseEntity.baseId && mechanic.getItemID().equals(baseEntity.mechanic().getItemID());
    }
}
