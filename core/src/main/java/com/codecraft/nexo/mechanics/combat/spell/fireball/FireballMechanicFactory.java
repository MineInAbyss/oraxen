package com.codecraft.nexo.mechanics.combat.spell.fireball;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class FireballMechanicFactory extends MechanicFactory {
    public FireballMechanicFactory(ConfigurationSection section) {
        super(section);
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new FireballMechanicManager(this));
    }

    @Override
    public FireballMechanic parse(ConfigurationSection section) {
        FireballMechanic mechanic = new FireballMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public FireballMechanic getMechanic(String itemID) {
        return (FireballMechanic) super.getMechanic(itemID);
    }

    @Override
    public FireballMechanic getMechanic(ItemStack itemStack) {
        return (FireballMechanic) super.getMechanic(itemStack);
    }
}
