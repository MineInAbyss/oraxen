package com.codecraft.nexo.mechanics.combat.spell.energyblast;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class EnergyBlastMechanicFactory extends MechanicFactory {
    public EnergyBlastMechanicFactory(ConfigurationSection section) {
        super(section);
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new EnergyBlastMechanicManager(this));
    }

    @Override
    public EnergyBlastMechanic parse(ConfigurationSection section) {
        EnergyBlastMechanic mechanic = new EnergyBlastMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public EnergyBlastMechanic getMechanic(String itemID) {
        return (EnergyBlastMechanic) super.getMechanic(itemID);
    }

    @Override
    public EnergyBlastMechanic getMechanic(ItemStack itemStack) {
        return (EnergyBlastMechanic) super.getMechanic(itemStack);
    }
}
