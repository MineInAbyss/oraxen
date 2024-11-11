package com.codecraft.nexo.mechanics.misc.backpack;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class BackpackMechanicFactory extends MechanicFactory {

        public BackpackMechanicFactory(ConfigurationSection section) {
            super(section);
            MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new BackpackListener(this));
        }

        @Override
        public BackpackMechanic parse(ConfigurationSection section) {
            BackpackMechanic mechanic = new BackpackMechanic(this, section);
            addToImplemented(mechanic);
            return mechanic;
        }

    @Override
    public BackpackMechanic getMechanic(String itemID) {
        return (BackpackMechanic) super.getMechanic(itemID);
    }

    @Override
    public BackpackMechanic getMechanic(ItemStack itemStack) {
        return (BackpackMechanic) super.getMechanic(itemStack);
    }

}
