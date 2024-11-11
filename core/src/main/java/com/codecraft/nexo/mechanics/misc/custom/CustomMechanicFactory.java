package com.codecraft.nexo.mechanics.misc.custom;

import com.codecraft.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class CustomMechanicFactory extends MechanicFactory {

    public CustomMechanicFactory(ConfigurationSection section) {
        super(section);
    }

    @Override
    public CustomMechanic parse(ConfigurationSection section) {
        CustomMechanic mechanic = new CustomMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public CustomMechanic getMechanic(String itemID) {
        return (CustomMechanic) super.getMechanic(itemID);
    }

    @Override
    public CustomMechanic getMechanic(ItemStack itemStack) {
        return (CustomMechanic) super.getMechanic(itemStack);
    }
}
