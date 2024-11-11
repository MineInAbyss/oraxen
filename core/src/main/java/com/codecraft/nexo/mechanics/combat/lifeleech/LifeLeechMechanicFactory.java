package com.codecraft.nexo.mechanics.combat.lifeleech;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class LifeLeechMechanicFactory extends MechanicFactory {

    public LifeLeechMechanicFactory(ConfigurationSection section) {
        super(section);
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new LifeLeechMechanicListener(this));
    }

    @Override
    public LifeLeechMechanic parse(ConfigurationSection section) {
        LifeLeechMechanic mechanic = new LifeLeechMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public LifeLeechMechanic getMechanic(String itemID) {
        return (LifeLeechMechanic) super.getMechanic(itemID);
    }

    @Override
    public LifeLeechMechanic getMechanic(ItemStack itemStack) {
        return (LifeLeechMechanic) super.getMechanic(itemStack);
    }

}
