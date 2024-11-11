package com.codecraft.nexo.mechanics.combat.spell.witherskull;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class WitherSkullMechanicFactory extends MechanicFactory {
    public WitherSkullMechanicFactory(ConfigurationSection section) {
        super(section);
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new WitherSkullMechanicListener(this));
    }

    @Override
    public WitherSkullMechanic parse(ConfigurationSection section) {
        WitherSkullMechanic mechanic = new WitherSkullMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public WitherSkullMechanic getMechanic(String itemID) {
        return (WitherSkullMechanic) super.getMechanic(itemID);
    }

    @Override
    public WitherSkullMechanic getMechanic(ItemStack itemStack) {
        return (WitherSkullMechanic) super.getMechanic(itemStack);
    }

}
