package com.codecraft.nexo.mechanics.farming.smelting;

import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;

public class SmeltingMechanic extends Mechanic {

    private final boolean playSound;

    public SmeltingMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        playSound = section.getBoolean("play_sound");
    }

    public boolean playSound() {
        return this.playSound;
    }

}