package com.codecraft.nexo.mechanics.furniture;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import com.codecraft.nexo.mechanics.furniture.evolution.EvolutionListener;
import com.codecraft.nexo.mechanics.furniture.evolution.EvolutionTask;
import com.codecraft.nexo.mechanics.furniture.jukebox.JukeboxListener;
import com.codecraft.nexo.mechanics.furniture.listeners.FurnitureListener;
import com.codecraft.nexo.mechanics.furniture.listeners.FurniturePacketListener;
import com.codecraft.nexo.mechanics.furniture.listeners.FurnitureSoundListener;
import com.codecraft.nexo.nms.EmptyFurniturePacketManager;
import com.codecraft.nexo.nms.NMSHandler;
import com.codecraft.nexo.nms.NMSHandlers;
import com.codecraft.nexo.utils.PluginUtils;
import com.codecraft.nexo.utils.VersionUtil;
import com.codecraft.nexo.utils.logs.Logs;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Optional;

public class FurnitureFactory extends MechanicFactory {

    public static FurnitureFactory instance;
    public final List<String> toolTypes;
    public final int evolutionCheckDelay;
    private boolean evolvingFurnitures;
    private static EvolutionTask evolutionTask;
    public double simulationRadius = Math.pow((Bukkit.getServer().getSimulationDistance() * 16.0), 2.0);

    public FurnitureFactory(ConfigurationSection section) {
        super(section);
        instance = this;
        toolTypes = section.getStringList("tool_types");
        evolutionCheckDelay = section.getInt("evolution_check_delay");
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(),
                new FurnitureListener(),
                new EvolutionListener(),
                new JukeboxListener()
        );

        if (VersionUtil.isPaperServer()) {
            MechanicsManager.registerListeners(NexoPlugin.get(), "furniture", new FurniturePacketListener());
        } else {
            Logs.logWarning("Seems that your server is a Spigot-server");
            Logs.logWarning("FurnitureHitboxes will not work due to it relying on Paper-only events");
            Logs.logWarning("It is heavily recommended to make the upgrade to Paper");
        }

        evolvingFurnitures = false;

        if (PluginUtils.isEnabled("AxiomPaper"))
            MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new AxiomCompatibility());

        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new FurnitureSoundListener());
    }

    public IFurniturePacketManager packetManager() {
        return Optional.of(NMSHandlers.getHandler()).map(NMSHandler::furniturePacketManager).orElse(new EmptyFurniturePacketManager());
    }

    @Override
    public Mechanic parse(ConfigurationSection section) {
        Mechanic mechanic = new FurnitureMechanic(this, section);
        addToImplemented(mechanic);
        return mechanic;
    }

    public static boolean isEnabled() {
        return instance != null;
    }

    public static FurnitureFactory get() {
        return instance;
    }

    public void registerEvolution() {
        if (evolvingFurnitures)
            return;
        if (evolutionTask != null)
            evolutionTask.cancel();
        evolutionTask = new EvolutionTask(this, evolutionCheckDelay);
        BukkitTask task = evolutionTask.runTaskTimer(NexoPlugin.get(), 0, evolutionCheckDelay);
        MechanicsManager.registerTask(getMechanicID(), task);
        evolvingFurnitures = true;
    }

    public static void unregisterEvolution() {
        if (evolutionTask != null)
            evolutionTask.cancel();
    }

    public static void removeAllFurniturePackets() {
        if (instance == null) return;
        instance.packetManager().removeAllFurniturePackets();
    }

    @Override
    public FurnitureMechanic getMechanic(String itemID) {
        return (FurnitureMechanic) super.getMechanic(itemID);
    }

    @Override
    public FurnitureMechanic getMechanic(ItemStack itemStack) {
        return (FurnitureMechanic) super.getMechanic(itemStack);
    }

}
