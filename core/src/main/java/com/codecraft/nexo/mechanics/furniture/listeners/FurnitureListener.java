package com.codecraft.nexo.mechanics.furniture.listeners;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.codecraft.nexo.api.events.furniture.NexoFurniturePlaceEvent;
import com.codecraft.nexo.config.Message;
import com.codecraft.nexo.items.ItemBuilder;
import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.furniture.BlockLocation;
import com.codecraft.nexo.mechanics.furniture.FurnitureFactory;
import com.codecraft.nexo.mechanics.furniture.FurnitureHelpers;
import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import com.codecraft.nexo.mechanics.furniture.seats.FurnitureSeat;
import com.codecraft.nexo.mechanics.limitedplacing.LimitedPlacing;
import com.codecraft.nexo.mechanics.storage.StorageMechanic;
import com.codecraft.nexo.utils.*;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

import java.util.Objects;
import java.util.Optional;

public class FurnitureListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onLimitedPlacing(final PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        BlockFace blockFace = event.getBlockFace();
        ItemStack item = event.getItem();

        if (item == null || block == null || event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        FurnitureMechanic mechanic = NexoFurniture.getFurnitureMechanic(NexoItems.idByItem(item));
        if (mechanic == null || !mechanic.hasLimitedPlacing()) return;

        if (!event.getPlayer().isSneaking() && BlockHelpers.isInteractable(block)) return;

        LimitedPlacing limitedPlacing = mechanic.limitedPlacing();
        Block belowPlaced = block.getRelative(blockFace).getRelative(BlockFace.DOWN);

        if (limitedPlacing.isNotPlacableOn(block, blockFace)) event.setCancelled(true);
        else if (limitedPlacing.isRadiusLimited()) {
            LimitedPlacing.RadiusLimitation radiusLimitation = limitedPlacing.getRadiusLimitation();
            int radius = radiusLimitation.getRadius();
            int amount = radiusLimitation.getAmount();
            if (block.getWorld().getNearbyEntities(block.getLocation(), radius, radius, radius).stream()
                    .filter(e -> Optional.ofNullable(NexoFurniture.getFurnitureMechanic(e)).map(Mechanic::getItemID).orElse("").equals(mechanic.getItemID()))
                    .filter(e -> e.getLocation().distanceSquared(block.getLocation()) <= radius * radius)
                    .count() >= amount) event.setCancelled(true);
        } else if (limitedPlacing.getType() == LimitedPlacing.LimitedPlacingType.ALLOW)
            if (!limitedPlacing.checkLimitedMechanic(belowPlaced)) event.setCancelled(true);
        else if (limitedPlacing.getType() == LimitedPlacing.LimitedPlacingType.DENY)
            if (limitedPlacing.checkLimitedMechanic(belowPlaced)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onFurniturePlace(final PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        final Player player = event.getPlayer();
        final Location targetLocation = Optional.ofNullable(event.getClickedBlock())
                .map(b -> b.getRelative(event.getBlockFace()).getLocation()).orElse(null);
        final EquipmentSlot hand = event.getHand();
        FurnitureMechanic mechanic = getMechanic(item, player, targetLocation);

        if (targetLocation == null || mechanic == null || item == null || hand != EquipmentSlot.HAND) return;
        if (event.useInteractedBlock() == Event.Result.DENY) return;
        if (event.useItemInHand() == Event.Result.DENY) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!player.isSneaking() && BlockHelpers.isInteractable(targetLocation.getBlock())) return;

        final Block block = targetLocation.getBlock();
        final BlockData currentBlockData = block.getBlockData();

        Block farm = block.getRelative(BlockFace.DOWN);
        if (mechanic.farmlandRequired && farm.getType() != Material.FARMLAND) return;

        BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(block, block.getState(), block.getRelative(event.getBlockFace()), item, player, true, hand);

        final Rotation rotation = getRotation(player.getEyeLocation().getYaw(), mechanic);
        final float yaw = FurnitureHelpers.correctedYaw(mechanic, FurnitureHelpers.rotationToYaw(rotation));

        if (player.getGameMode() == GameMode.ADVENTURE) blockPlaceEvent.setCancelled(true);
        if (mechanic.notEnoughSpace(block.getLocation(), yaw)) {
            blockPlaceEvent.setCancelled(true);
            Message.NOT_ENOUGH_SPACE.send(player);
        }

        if (!blockPlaceEvent.canBuild() || blockPlaceEvent.isCancelled()) {
            block.setBlockData(currentBlockData);
            return;
        }

        ItemDisplay baseEntity = mechanic.place(block.getLocation(), yaw, event.getBlockFace(), false);
        if (baseEntity == null) return;

        ItemUtils.dyeColor(item).ifPresent(color -> baseEntity.getPersistentDataContainer().set(FurnitureMechanic.FURNITURE_DYE_KEY, PersistentDataType.INTEGER, color.asRGB()));
        Utils.swingHand(player, event.getHand());

        final NexoFurniturePlaceEvent furniturePlaceEvent = new NexoFurniturePlaceEvent(mechanic, block, baseEntity, player, item, hand);
        if (!EventUtils.callEvent(furniturePlaceEvent)) {
            NexoFurniture.remove(baseEntity, null);
            block.setBlockData(currentBlockData);
            return;
        }

        if (!player.getGameMode().equals(GameMode.CREATIVE)) item.setAmount(item.getAmount() - 1);
        event.setUseInteractedBlock(Event.Result.DENY);
        if (VersionUtil.isPaperServer()) baseEntity.getWorld().sendGameEvent(player, GameEvent.BLOCK_PLACE, baseEntity.getLocation().toVector());
    }

    @EventHandler
    public void onPlaceAgainstFurniture(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack itemStack = event.getItem();
        Material type = Optional.ofNullable(itemStack).map(ItemStack::getType).orElse(Material.AIR);
        boolean isItemFrame = type.name().contains("ITEM_FRAME"), isArmorStand = type.name().contains("ARMOR_STAND");
        boolean isPainting = type.name().contains("PAINTING");

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || itemStack == null || block == null) return;
        if (!itemStack.getType().isBlock() && !isItemFrame && !isArmorStand && !isPainting) return;

        ItemDisplay baseEntity = FurnitureFactory.instance.packetManager().baseEntityFromHitbox(new BlockLocation(block.getLocation()));
        if (baseEntity == null) return;
        FurnitureMechanic mechanic = NexoFurniture.getFurnitureMechanic(baseEntity);
        if (mechanic == null) return;
        if (itemStack.getType().hasGravity() || isItemFrame || isArmorStand || isPainting) {
            event.setUseItemInHand(Event.Result.DENY);
            player.updateInventory();
            return;
        }


        // Since the server-side block is AIR by default, placing blocks acts weird
        // Temporarily set the block to a barrier, then schedule a task to revert it next tick and resend hitboxes
        block.setType(Material.BARRIER, false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(NexoPlugin.get(), () -> block.setType(Material.AIR, false));
        Bukkit.getScheduler().scheduleSyncDelayedTask(NexoPlugin.get(), () ->
                FurnitureFactory.instance.packetManager().sendBarrierHitboxPacket(baseEntity, mechanic, player),2L
        );
    }

    private FurnitureMechanic getMechanic(ItemStack item, Player player, Location placed) {
        final String itemID = NexoItems.idByItem(item);
        if (itemID == null || placed == null) return null;
        if (FurnitureFactory.get().isNotImplementedIn(itemID) || BlockHelpers.isStandingInside(player, placed.getBlock())) return null;
        if (!ProtectionLib.canBuild(player, placed)) return null;
        if (NexoFurniture.isFurniture(placed)) return null;

        return FurnitureFactory.get().getMechanic(item);
    }

    private Rotation getRotation(final double yaw, FurnitureMechanic mechanic) {
        FurnitureMechanic.RestrictedRotation restrictedRotation = mechanic.restrictedRotation();
        int id = (int) (((Location.normalizeYaw((float) yaw) + 180) * 8 / 360) + 0.5) % 8;
        int offset = restrictedRotation == FurnitureMechanic.RestrictedRotation.STRICT ? 0 : 1;
        if (restrictedRotation != FurnitureMechanic.RestrictedRotation.NONE && id % 2 != 0) id -= offset;
        return Rotation.values()[id];
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFurnitureInteract(NexoFurnitureInteractEvent event) {
        Player player = event.player();
        FurnitureMechanic mechanic = event.mechanic();
        ItemDisplay baseEntity = event.baseEntity();
        Location interactionPoint = event.interactionPoint();

        mechanic.runClickActions(player);

        if (mechanic.isRotatable() && player.isSneaking()) mechanic.rotateFurniture(baseEntity);
        else if (mechanic.isStorage() && !player.isSneaking()) {
            StorageMechanic storage = mechanic.storage();
            switch (storage.getStorageType()) {
                case STORAGE, SHULKER -> storage.openStorage(baseEntity, player);
                case PERSONAL -> storage.openPersonalStorage(player, baseEntity.getLocation(), baseEntity);
                case DISPOSAL -> storage.openDisposal(player, baseEntity.getLocation(), baseEntity);
                case ENDERCHEST -> player.openInventory(player.getEnderChest());
            }
        } else if (mechanic.hasSeats() && !player.isSneaking()) FurnitureSeat.sitOnSeat(baseEntity, player, interactionPoint);
        else if (mechanic.isRotatable()) mechanic.rotateFurniture(baseEntity);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMiddleClick(final InventoryCreativeEvent event) {
        final Player player = (Player) event.getInventory().getHolder();
        if (event.getClickedInventory() == null || player == null) return;
        if (event.getClick() != ClickType.CREATIVE) return;
        if (event.getSlotType() != InventoryType.SlotType.QUICKBAR) return;
        if (event.getCursor().getType() != Material.BARRIER) return;

        final RayTraceResult rayTraceResult = player.rayTraceBlocks(8.0);
        FurnitureMechanic mechanic = rayTraceResult != null ? NexoFurniture.getFurnitureMechanic(rayTraceResult.getHitPosition().toLocation(player.getWorld())) : null;
        if (mechanic == null) return;
        ItemBuilder builder = NexoItems.itemById(mechanic.getItemID());
        if (builder == null) return;

        ItemStack item = builder.build();
        for (int i = 0; i <= 8; i++) {
            if (Objects.equals(NexoItems.idByItem(player.getInventory().getItem(i)), mechanic.getItemID())) {
                player.getInventory().setHeldItemSlot(i);
                event.setCancelled(true);
                return;
            }
        }
        event.setCursor(item);
    }

    @EventHandler
    public void onPlayerQuitEvent(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (NexoFurniture.isFurniture(player.getVehicle())) player.leaveVehicle();
    }
}
