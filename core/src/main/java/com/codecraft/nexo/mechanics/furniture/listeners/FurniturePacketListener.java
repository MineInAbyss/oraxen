package com.codecraft.nexo.mechanics.furniture.listeners;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.api.events.NexoMechanicsRegisteredEvent;
import com.codecraft.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.codecraft.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.codecraft.nexo.mechanics.furniture.FurnitureFactory;
import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import com.codecraft.nexo.mechanics.furniture.IFurniturePacketManager;
import com.codecraft.nexo.mechanics.furniture.seats.FurnitureSeat;
import com.codecraft.nexo.utils.EventUtils;
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import io.papermc.paper.event.player.PlayerUntrackEntityEvent;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Optional;

public class FurniturePacketListener implements Listener {

    @EventHandler
    public void onPlayerTrackFurniture(PlayerTrackEntityEvent event) {
        Player player = event.getPlayer();
        if (!(event.getEntity() instanceof ItemDisplay itemDisplay)) return;
        FurnitureMechanic mechanic = NexoFurniture.getFurnitureMechanic(itemDisplay);
        IFurniturePacketManager packetManager = FurnitureFactory.instance.packetManager();
        if (mechanic == null) return;

        Bukkit.getScheduler().runTaskLater(NexoPlugin.get(), () -> {
            packetManager.sendFurnitureEntityPacket(itemDisplay, mechanic, player);
            packetManager.sendInteractionEntityPacket(itemDisplay, mechanic, player);
            packetManager.sendBarrierHitboxPacket(itemDisplay, mechanic, player);
            packetManager.sendLightMechanicPacket(itemDisplay, mechanic, player);
        }, 2L);
    }

    @EventHandler
    public void onPlayerUntrackFurniture(PlayerUntrackEntityEvent event) {
        Player player = event.getPlayer();
        if (!(event.getEntity() instanceof ItemDisplay itemDisplay)) return;
        FurnitureMechanic mechanic = NexoFurniture.getFurnitureMechanic(itemDisplay);
        IFurniturePacketManager packetManager = FurnitureFactory.instance.packetManager();
        if (mechanic == null) return;

        packetManager.removeFurnitureEntityPacket(itemDisplay, mechanic, player);
        packetManager.removeInteractionHitboxPacket(itemDisplay, mechanic, player);
        packetManager.removeBarrierHitboxPacket(itemDisplay, mechanic, player);
        packetManager.removeLightMechanicPacket(itemDisplay, mechanic, player);
    }

    @EventHandler
    public void onFurnitureFactory(NexoMechanicsRegisteredEvent event) {
        double r = FurnitureFactory.get().simulationRadius;
        for (Player player : Bukkit.getOnlinePlayers())
            for (ItemDisplay baseEntity : player.getLocation().getNearbyEntitiesByType(ItemDisplay.class, r, r, r)) {
                FurnitureMechanic mechanic = NexoFurniture.getFurnitureMechanic(baseEntity);
                IFurniturePacketManager packetManager = FurnitureFactory.instance.packetManager();
                if (mechanic == null || FurnitureSeat.isSeat(baseEntity)) continue;

                packetManager.sendFurnitureEntityPacket(baseEntity, mechanic, player);
                packetManager.sendLightMechanicPacket(baseEntity, mechanic, player);
                packetManager.sendInteractionEntityPacket(baseEntity, mechanic, player);
                packetManager.sendBarrierHitboxPacket(baseEntity, mechanic, player);
            }
    }

    @EventHandler
    public void onUseUnknownEntity(PlayerUseUnknownEntityEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack itemInHand = hand == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        Vector relativePosition = event.getClickedRelativePosition();
        Location interactionPoint = relativePosition != null ? relativePosition.toLocation(player.getWorld()) : null;
        IFurniturePacketManager packetManager = FurnitureFactory.instance.packetManager();

        ItemDisplay baseEntity = packetManager.baseEntityFromHitbox(event.getEntityId());
        if (baseEntity == null) return;
        FurnitureMechanic mechanic = NexoFurniture.getFurnitureMechanic(baseEntity);
        if (mechanic == null || FurnitureSeat.isSeat(baseEntity)) return;

        if (ProtectionLib.canBreak(player, baseEntity.getLocation()) && event.isAttack()) {
            NexoFurniture.remove(baseEntity, player);
            EventUtils.callEvent(new NexoFurnitureBreakEvent(mechanic, baseEntity, player, null));
        } else if (ProtectionLib.canInteract(player, baseEntity.getLocation()))
            EventUtils.callEvent(new NexoFurnitureInteractEvent(mechanic, baseEntity, player, itemInHand, hand, interactionPoint));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractBarrierHitbox(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        Location interactionPoint = event.getInteractionPoint();
        FurnitureMechanic mechanic = Optional.ofNullable(NexoFurniture.getFurnitureMechanic(clickedBlock))
                .orElse(NexoFurniture.getFurnitureMechanic(interactionPoint));
        if (mechanic == null) return;

        ItemDisplay baseEntity = Optional.ofNullable(mechanic.baseEntity(clickedBlock)).orElse(mechanic.baseEntity(interactionPoint));
        if (baseEntity == null) return;


        if (action == Action.RIGHT_CLICK_BLOCK && ProtectionLib.canInteract(player, baseEntity.getLocation()))
            EventUtils.callEvent(new NexoFurnitureInteractEvent(mechanic, baseEntity, player, event.getItem(), event.getHand(), interactionPoint));
        else if (action == Action.LEFT_CLICK_BLOCK && ProtectionLib.canBreak(player, baseEntity.getLocation())) {
            NexoFurniture.remove(baseEntity, player);
            EventUtils.callEvent(new NexoFurnitureBreakEvent(mechanic, baseEntity, player, event.getClickedBlock()));
        }
        // Resend the hitbox as client removes the "ghost block"
        Bukkit.getScheduler().runTaskLater(NexoPlugin.get(), () ->
                FurnitureFactory.instance.packetManager().sendBarrierHitboxPacket(baseEntity, mechanic, player), 1L);
    }
}
