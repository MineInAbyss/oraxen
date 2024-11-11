package com.codecraft.nexo.mechanics.misc.soulbound;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class SoulBoundMechanicFactory extends MechanicFactory {
    public SoulBoundMechanicFactory(ConfigurationSection section) {
        super(section);
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new SoulBoundMechanicListener(this));
    }

    @Override
    public SoulBoundMechanic parse(ConfigurationSection section) {
        SoulBoundMechanic mechanic = new SoulBoundMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public SoulBoundMechanic getMechanic(String itemID) {
        return (SoulBoundMechanic) super.getMechanic(itemID);
    }

    @Override
    public SoulBoundMechanic getMechanic(ItemStack itemStack) {
        return (SoulBoundMechanic) super.getMechanic(itemStack);
    }
}
