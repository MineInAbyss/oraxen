package com.codecraft.nexo.mechanics.misc.itemtype;

import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;

public class ItemTypeMechanic extends Mechanic {

    public final String itemType;

    public ItemTypeMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        this.itemType = section.getString("value");
    }

}
