package com.codecraft.nexo.mechanics.misc.commands;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class CommandsMechanicFactory extends MechanicFactory {

    public CommandsMechanicFactory(ConfigurationSection section) {
        super(section);
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new CommandsMechanicListener(this));
    }

    @Override
    public Mechanic parse(ConfigurationSection section) {
        Mechanic mechanic = new CommandsMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public CommandsMechanic getMechanic(String itemID) {
        return (CommandsMechanic) super.getMechanic(itemID);
    }

    @Override
    public CommandsMechanic getMechanic(ItemStack itemStack) {
        return (CommandsMechanic) super.getMechanic(itemStack);
    }
}
