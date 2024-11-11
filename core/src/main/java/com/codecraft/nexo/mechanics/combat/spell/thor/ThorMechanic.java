package com.codecraft.nexo.mechanics.combat.spell.thor;

import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.combat.spell.SpellMechanic;
import com.codecraft.nexo.utils.timers.Timer;
import com.codecraft.nexo.utils.timers.TimersFactory;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ThorMechanic extends SpellMechanic {

    private final int lightningBoltsAmount;
    private final double randomLocationVariation;
    private final TimersFactory timersFactory;

    public ThorMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        this.lightningBoltsAmount = section.getInt("lightning_bolts_amount");
        this.randomLocationVariation = section.getDouble("random_location_variation");
        this.timersFactory = new TimersFactory(section.getLong("delay"));
    }

    public int getLightningBoltsAmount() {
        return lightningBoltsAmount;
    }

    public Location getRandomizedLocation(Location location) {
        Random random = ThreadLocalRandom.current();
        location.setX(location.getX() + (random.nextDouble() * randomLocationVariation) - randomLocationVariation / 2);
        location.setY(location.getY() + (random.nextDouble() * randomLocationVariation) - randomLocationVariation / 2);
        return location;
    }

    @Override
    public Timer getTimer(Player player) {
        return timersFactory.getTimer(player);
    }
}
