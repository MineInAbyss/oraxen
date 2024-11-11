package com.codecraft.nexo.mechanics.farming.bigmining;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import com.codecraft.nexo.utils.NexoYaml;
import com.codecraft.nexo.utils.PluginUtils;
import com.codecraft.nexo.utils.logs.Logs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class BigMiningMechanicFactory extends MechanicFactory {

    private final boolean callEvents;

    public BigMiningMechanicFactory(ConfigurationSection section) {
        super(section);
        if (PluginUtils.isEnabled("AdvancedEnchantments") && section.getBoolean("call_events", true)) {
            Logs.logError("AdvancedEnchantment is enabled, disabling BigMining-Mechanic");
            section.set("call_events", false);
            NexoYaml.saveConfig(NexoPlugin.get().getDataFolder().toPath().resolve("mechanics.yml").toFile(), section);
            this.callEvents = false;
        } else this.callEvents = section.getBoolean("call_events", true);
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new BigMiningMechanicListener(this));
    }

    @Override
    public BigMiningMechanic parse(ConfigurationSection section) {
        BigMiningMechanic mechanic = new BigMiningMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public BigMiningMechanic getMechanic(String itemID) {
        return (BigMiningMechanic) super.getMechanic(itemID);
    }

    @Override
    public BigMiningMechanic getMechanic(ItemStack itemStack) {
        return (BigMiningMechanic) super.getMechanic(itemStack);
    }

    public boolean callEvents() {
        return callEvents;
    }

}
