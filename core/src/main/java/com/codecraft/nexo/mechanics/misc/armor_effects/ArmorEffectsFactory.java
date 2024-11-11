package com.codecraft.nexo.mechanics.misc.armor_effects;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class ArmorEffectsFactory extends MechanicFactory {

    private static ArmorEffectsFactory instance;
    private ArmorEffectsTask armorEffectTask;
    private final int delay;

    public ArmorEffectsFactory(ConfigurationSection section) {
        super(section);
        this.delay = section.getInt("delay_in_ticks", 20);
        instance = this;
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new ArmorEffectsListener());
    }

    public static ArmorEffectsFactory get() {
        return instance;
    }

    @Override
    public ArmorEffectsMechanic parse(ConfigurationSection section) {
        ArmorEffectsMechanic mechanic = new ArmorEffectsMechanic(this, section);
        addToImplemented(mechanic);
        Optional.ofNullable(armorEffectTask).ifPresent(BukkitRunnable::cancel);
        armorEffectTask = new ArmorEffectsTask();
        MechanicsManager.registerTask(instance.getMechanicID(), armorEffectTask.runTaskTimer(NexoPlugin.get(), 0, delay));
        return mechanic;
    }

    @Override
    public ArmorEffectsMechanic getMechanic(String itemID) {
        return (ArmorEffectsMechanic) super.getMechanic(itemID);
    }

    @Override
    public ArmorEffectsMechanic getMechanic(ItemStack itemStack) {
        return (ArmorEffectsMechanic) super.getMechanic(itemStack);
    }

    public int getDelay() {
        return delay;
    }
}
