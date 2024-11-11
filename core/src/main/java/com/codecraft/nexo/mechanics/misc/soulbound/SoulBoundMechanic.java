package com.codecraft.nexo.mechanics.misc.soulbound;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class SoulBoundMechanic extends Mechanic {

    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(NexoPlugin.get(), "soulbound");
    private final double loseChance;

    public SoulBoundMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        this.loseChance = section.getDouble("lose_chance");
    }

    public double getLoseChance() {
        return loseChance;
    }
}
