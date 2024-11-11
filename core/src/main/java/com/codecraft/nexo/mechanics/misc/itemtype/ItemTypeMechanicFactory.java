package com.codecraft.nexo.mechanics.misc.itemtype;

import com.codecraft.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ItemTypeMechanicFactory extends MechanicFactory {

    private static ItemTypeMechanicFactory instance;
    public static ItemTypeMechanicFactory get() {
        return instance;
    }

    public ItemTypeMechanicFactory(ConfigurationSection section) {
        super(section);
        instance = this;
    }

    @Override
    public ItemTypeMechanic parse(ConfigurationSection section) {
        ItemTypeMechanic mechanic = new ItemTypeMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public ItemTypeMechanic getMechanic(String itemID) {
        return (ItemTypeMechanic) super.getMechanic(itemID);
    }

    @Override
    public ItemTypeMechanic getMechanic(ItemStack itemStack) {
        return (ItemTypeMechanic) super.getMechanic(itemStack);
    }

}
