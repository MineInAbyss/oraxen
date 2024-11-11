package com.codecraft.nexo.mechanics.farming.smelting;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class SmeltingMechanicFactory extends MechanicFactory {

    private static SmeltingMechanicFactory instance;

    public SmeltingMechanicFactory(ConfigurationSection section) {
        super(section);
        instance = this;
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new SmeltingMechanicListener(this));
    }

    @Override
    public Mechanic parse(ConfigurationSection section) {
        Mechanic mechanic = new SmeltingMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public SmeltingMechanic getMechanic(String itemID) {
        return (SmeltingMechanic) super.getMechanic(itemID);
    }

    @Override
    public SmeltingMechanic getMechanic(ItemStack itemStack) {
        return (SmeltingMechanic) super.getMechanic(itemStack);
    }

    public static SmeltingMechanicFactory get() {
        return instance;
    }

}
