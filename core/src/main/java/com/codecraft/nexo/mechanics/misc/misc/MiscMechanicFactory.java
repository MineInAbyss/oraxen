package com.codecraft.nexo.mechanics.misc.misc;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import com.codecraft.nexo.utils.VersionUtil;
import com.codecraft.nexo.utils.logs.Logs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class MiscMechanicFactory extends MechanicFactory {

    private static MiscMechanicFactory instance;
    public MiscMechanicFactory(ConfigurationSection section) {
        super(section);
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new MiscListener(this));
        instance = this;
    }

    @Override
    public Mechanic parse(ConfigurationSection section) {
        MiscMechanic mechanic = new MiscMechanic(this, section);

        if (VersionUtil.atleast("1.21.2")) {
            if (!mechanic.burnsInLava() || !mechanic.burnsInLava()) {
                Logs.logWarning(mechanic.getItemID() + " is using deprecated Misc-Mechanic burns_in_fire/lava...");
                Logs.logWarning("It is heavily advised to swap to the new `damage_resistant`-component on 1.21.2+ servers...");
            } else if (!mechanic.breaksFromCactus()) {
                Logs.logWarning(mechanic.getItemID() + " is using deprecated Misc-Mechanic breaks_from_cactus...");
                Logs.logWarning("It is heavily advised to swap to the new `damage_resistant`-component on 1.21.2+ servers...");
            }
        }

        addToImplemented(mechanic);
        return mechanic;
    }

    public static MiscMechanicFactory get() {
        return instance;
    }

    @Override
    public MiscMechanic getMechanic(String itemID) {
        return (MiscMechanic) super.getMechanic(itemID);
    }

    @Override
    public MiscMechanic getMechanic(ItemStack itemStack) {
        return (MiscMechanic) super.getMechanic(itemStack);
    }
}
