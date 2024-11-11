package com.codecraft.nexo.mechanics.combat.lifeleech;

import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;

public class LifeLeechMechanic extends Mechanic {

    private final int amount;

    public LifeLeechMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        this.amount = section.getInt("amount");
    }

    public int getAmount() {
        return amount;
    }

}
