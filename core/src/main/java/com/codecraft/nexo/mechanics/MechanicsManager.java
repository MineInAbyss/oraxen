package com.codecraft.nexo.mechanics;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.events.NexoMechanicsRegisteredEvent;
import com.codecraft.nexo.mechanics.combat.lifeleech.LifeLeechMechanicFactory;
import com.codecraft.nexo.mechanics.combat.spell.energyblast.EnergyBlastMechanicFactory;
import com.codecraft.nexo.mechanics.combat.spell.fireball.FireballMechanicFactory;
import com.codecraft.nexo.mechanics.combat.spell.thor.ThorMechanicFactory;
import com.codecraft.nexo.mechanics.combat.spell.witherskull.WitherSkullMechanicFactory;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockFactory;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanicFactory;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanicFactory;
import com.codecraft.nexo.mechanics.farming.bigmining.BigMiningMechanicFactory;
import com.codecraft.nexo.mechanics.farming.harvesting.HarvestingMechanicFactory;
import com.codecraft.nexo.mechanics.farming.smelting.SmeltingMechanicFactory;
import com.codecraft.nexo.mechanics.furniture.FurnitureFactory;
import com.codecraft.nexo.mechanics.misc.armor_effects.ArmorEffectsFactory;
import com.codecraft.nexo.mechanics.misc.backpack.BackpackMechanicFactory;
import com.codecraft.nexo.mechanics.misc.commands.CommandsMechanicFactory;
import com.codecraft.nexo.mechanics.misc.custom.CustomMechanicFactory;
import com.codecraft.nexo.mechanics.misc.itemtype.ItemTypeMechanicFactory;
import com.codecraft.nexo.mechanics.misc.misc.MiscMechanicFactory;
import com.codecraft.nexo.mechanics.misc.soulbound.SoulBoundMechanicFactory;
import com.codecraft.nexo.utils.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class MechanicsManager {

    private static final Map<String, MechanicFactory> FACTORIES_BY_MECHANIC_ID = new HashMap<>();
    public static final Map<String, List<Integer>> MECHANIC_TASKS = new HashMap<>();
    private static final Map<String, List<Listener>> MECHANICS_LISTENERS = new HashMap<>();

    public static void registerNativeMechanics() {
        // misc
        registerFactory("armor_effects", ArmorEffectsFactory::new);
        registerFactory("soulbound", SoulBoundMechanicFactory::new);
        registerFactory("itemtype", ItemTypeMechanicFactory::new);
        registerFactory("custom", CustomMechanicFactory::new);
        registerFactory("commands", CommandsMechanicFactory::new);
        registerFactory("backpack", BackpackMechanicFactory::new);
        registerFactory("misc", MiscMechanicFactory::new);

        // gameplay
        registerFactory("furniture", FurnitureFactory::new);
        registerFactory("noteblock", NoteBlockMechanicFactory::new);
        registerFactory("stringblock", StringBlockMechanicFactory::new);
        registerMechanicFactory(new CustomBlockFactory("custom_block"), true);

        // combat
        registerFactory("thor", ThorMechanicFactory::new);
        registerFactory("lifeleech", LifeLeechMechanicFactory::new);
        registerFactory("energyblast", EnergyBlastMechanicFactory::new);
        registerFactory("witherskull", WitherSkullMechanicFactory::new);
        registerFactory("fireball", FireballMechanicFactory::new);

        // farming
        registerFactory("bigmining", BigMiningMechanicFactory::new);
        registerFactory("smelting", SmeltingMechanicFactory::new);
        registerFactory("harvesting", HarvestingMechanicFactory::new);

        EventUtils.callEvent(new NexoMechanicsRegisteredEvent());
    }

    /**
     * Register a new MechanicFactory
     *
     * @param factory    the MechanicFactory of the mechanic
     * @param enabled    if the mechanic should be enabled by default or not
     */
    public static void registerMechanicFactory(MechanicFactory factory, boolean enabled) {
        if (enabled) FACTORIES_BY_MECHANIC_ID.put(factory.getMechanicID(), factory);
    }

    public static void unregisterMechanicFactory(String mechanicId) {
        FACTORIES_BY_MECHANIC_ID.remove(mechanicId);
        unloadListeners(mechanicId);
        unregisterTasks(mechanicId);
    }

    /**
     * This method is deprecated and will be removed in a future release.<br>
     * Use {@link #registerMechanicFactory(MechanicFactory, boolean)} instead.
     *
     * @param mechanicId  the id of the mechanic
     * @param constructor the constructor of the mechanic
     */
    @Deprecated(forRemoval = true, since = "1.158.0")
    public static void registerMechanicFactory(final String mechanicId, final FactoryConstructor constructor) {
        registerFactory(mechanicId, constructor);
    }

    private static void registerFactory(final String mechanicId, final FactoryConstructor constructor) {
        final Entry<File, YamlConfiguration> mechanicsEntry = NexoPlugin.get().resourceManager().getMechanicsEntry();
        final YamlConfiguration mechanicsConfig = mechanicsEntry.getValue();
        final boolean updated = false;
        ConfigurationSection factorySection = mechanicsConfig.getConfigurationSection(mechanicId);
        if (factorySection != null && factorySection.getBoolean("enabled"))
            FACTORIES_BY_MECHANIC_ID.put(mechanicId, constructor.create(factorySection));

        try {
            if (updated) mechanicsConfig.save(mechanicsEntry.getKey());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void registerTask(String mechanicId, BukkitTask task) {
        MECHANIC_TASKS.compute(mechanicId, (key, value) -> {
            if (value == null) value = new ArrayList<>();
            value.add(task.getTaskId());
            return value;
        });
    }

    public static void unregisterTasks() {
        MECHANIC_TASKS.values().forEach(tasks -> tasks.forEach(Bukkit.getScheduler()::cancelTask));
        MECHANIC_TASKS.clear();
    }

    public static void unregisterTasks(String mechanicId) {
        MECHANIC_TASKS.computeIfPresent(mechanicId, (key, value) -> {
            value.forEach(Bukkit.getScheduler()::cancelTask);
            return Collections.emptyList();
        });
    }

    public static void registerListeners(final JavaPlugin plugin, String mechanicId, final Listener... listeners) {
        for (final Listener listener : listeners)
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        MECHANICS_LISTENERS.compute(mechanicId, (key, value) -> {
            if (value == null) value = new ArrayList<>();
            value.addAll(Arrays.asList(listeners));
            return value;
        });
    }

    public static void unloadListeners() {
        for (final Listener listener : MECHANICS_LISTENERS.values().stream().flatMap(Collection::stream).toList())
            HandlerList.unregisterAll(listener);
    }

    public static void unloadListeners(String mechanicId) {
        for (final Listener listener : MECHANICS_LISTENERS.remove(mechanicId))
            HandlerList.unregisterAll(listener);

    }

    public static MechanicFactory getMechanicFactory(final String mechanicID) {
        return FACTORIES_BY_MECHANIC_ID.get(mechanicID);
    }

    @FunctionalInterface
    public interface FactoryConstructor {
        MechanicFactory create(ConfigurationSection section);
    }

}
