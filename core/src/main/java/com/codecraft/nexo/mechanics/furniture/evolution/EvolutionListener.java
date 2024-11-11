package com.codecraft.nexo.mechanics.furniture.evolution;

import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.codecraft.nexo.mechanics.furniture.FurnitureFactory;
import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EvolutionListener implements Listener {

    public EvolutionListener() {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBoneMeal(NexoFurnitureInteractEvent event) {
        if (event.hand() != EquipmentSlot.HAND) return;
        Entity baseEntity = event.baseEntity();
        FurnitureMechanic mechanic = event.mechanic();

        if (!mechanic.hasEvolution()) return;
        PersistentDataContainer cropPDC = baseEntity.getPersistentDataContainer();
        if (!cropPDC.has(FurnitureMechanic.EVOLUTION_KEY, PersistentDataType.INTEGER)) return;

        ItemStack itemInteracted = event.itemInHand();
        if (itemInteracted.getType() != Material.BONE_MEAL) return;

        event.setCancelled(true);
        EvolvingFurniture evolution = mechanic.evolution();
        if (!evolution.isBoneMeal() || evolution.getNextStage() == null) return;
        FurnitureMechanic nextMechanic = FurnitureFactory.instance.getMechanic(evolution.getNextStage());
        if (nextMechanic == null) return;

        itemInteracted.setAmount(itemInteracted.getAmount() - 1);
        baseEntity.getWorld().playEffect(baseEntity.getLocation(), Effect.BONE_MEAL_USE, 3);
        if (randomChance(evolution.getBoneMealChance())) {

            NexoFurniture.remove(baseEntity, null);
            nextMechanic.place(baseEntity.getLocation());
        }
    }

    public boolean randomChance(double chance) {
        return Math.random() <= chance;
    }
}
