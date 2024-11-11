package com.codecraft.nexo.mechanics.farming.harvesting;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class HarvestingMechanicFactory extends MechanicFactory {

    private static HarvestingMechanicFactory instance;

    public HarvestingMechanicFactory(ConfigurationSection section) {
        super(section);
        instance = this;
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new HarvestingMechanicListener());
    }

    public static HarvestingMechanicFactory get() {
        return instance;
    }

    @Override
    public HarvestingMechanic parse(ConfigurationSection section) {
        HarvestingMechanic mechanic = new HarvestingMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public HarvestingMechanic getMechanic(String itemID) {
        return (HarvestingMechanic) super.getMechanic(itemID);
    }

    @Override
    public HarvestingMechanic getMechanic(ItemStack itemStack) {
        return (HarvestingMechanic) super.getMechanic(itemStack);
    }
}
