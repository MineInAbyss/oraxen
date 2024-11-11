package com.codecraft.nexo.mechanics.combat.spell.witherskull;

import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.combat.spell.SpellMechanic;
import com.codecraft.nexo.utils.timers.Timer;
import com.codecraft.nexo.utils.timers.TimersFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class WitherSkullMechanic extends SpellMechanic {

    private final TimersFactory timersFactory;
    public final boolean charged;

    public WitherSkullMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        this.timersFactory = new TimersFactory(section.getLong("delay"));
        this.charged = section.getBoolean("charged");
    }

    @Override
    public Timer getTimer(Player player) {
        return timersFactory.getTimer(player);
    }

}
