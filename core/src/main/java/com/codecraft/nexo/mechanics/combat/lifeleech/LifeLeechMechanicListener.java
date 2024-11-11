package com.codecraft.nexo.mechanics.combat.lifeleech;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.utils.wrappers.AttributeWrapper;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LifeLeechMechanicListener implements Listener {

    private final LifeLeechMechanicFactory factory;

    public LifeLeechMechanicListener(LifeLeechMechanicFactory factory) {
        this.factory = factory;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCall(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        if (!ProtectionLib.canInteract(damager, event.getEntity().getLocation())) return;

        String itemID = NexoItems.idByItem(damager.getInventory().getItemInMainHand());
        if (!NexoItems.exists(itemID)) return;
        LifeLeechMechanic mechanic = factory.getMechanic(itemID);
        if (mechanic == null) return;

        double maxHealth = damager.getAttribute(AttributeWrapper.MAX_HEALTH).getValue();
        damager.setHealth(Math.min(damager.getHealth() + mechanic.getAmount(), maxHealth));
        livingEntity.setHealth(Math.max(livingEntity.getHealth() - mechanic.getAmount(), 0));
    }
}
