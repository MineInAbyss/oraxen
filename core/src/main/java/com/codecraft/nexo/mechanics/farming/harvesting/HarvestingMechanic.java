package com.codecraft.nexo.mechanics.farming.harvesting;

import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.utils.timers.Timer;
import com.codecraft.nexo.utils.timers.TimersFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class HarvestingMechanic extends Mechanic {

    private final int radius;
    private final int height;
    private final boolean lowerItemDurability;
    private final TimersFactory timersFactory;

    public HarvestingMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        this.radius = section.getInt("radius");
        this.height = section.getInt("height");
        this.lowerItemDurability = section.getBoolean("lower_item_durability", true);

        this.timersFactory = new TimersFactory(section.isInt("cooldown") ? section.getInt("cooldown") : 0);
    }

    public int getRadius() {
        return this.radius;
    }

    public int getHeight() {
        return height;
    }

    public boolean shouldLowerItemDurability() { return lowerItemDurability; }

    public Timer getTimer(Player player) {
        return timersFactory.getTimer(player);
    }
}
