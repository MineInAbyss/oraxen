package com.codecraft.nexo.mechanics.combat.spell.thor;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ThorMechanicFactory extends MechanicFactory {

    public ThorMechanicFactory(ConfigurationSection section) {
        super(section);
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new ThorMechanicListener(this));
    }

    @Override
    public ThorMechanic parse(ConfigurationSection section) {
        ThorMechanic mechanic = new ThorMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public ThorMechanic getMechanic(String itemID) {
        return (ThorMechanic) super.getMechanic(itemID);
    }

    @Override
    public ThorMechanic getMechanic(ItemStack itemStack) {
        return (ThorMechanic) super.getMechanic(itemStack);
    }

}
