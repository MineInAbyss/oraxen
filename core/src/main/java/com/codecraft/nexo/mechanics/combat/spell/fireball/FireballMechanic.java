package com.codecraft.nexo.mechanics.combat.spell.fireball;

import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.combat.spell.SpellMechanic;
import org.bukkit.configuration.ConfigurationSection;

public class FireballMechanic extends SpellMechanic {

    private final double yield;
    private final double speed;

    public FireballMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);

        this.yield = section.getDouble("yield");
        this.speed = section.getDouble("speed");
    }

    public double getYield() {
        return yield;
    }

    public double getSpeed() {
        return speed;
    }
}
