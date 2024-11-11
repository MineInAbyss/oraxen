package com.codecraft.nexo.compatibilities;

import com.codecraft.nexo.NexoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class CompatibilityListener implements Listener {

    public CompatibilityListener() {
        Bukkit.getPluginManager().registerEvents(this, NexoPlugin.get());
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        CompatibilitiesManager.enableCompatibility(event.getPlugin().getName());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        CompatibilitiesManager.disableCompatibility(event.getPlugin().getName());
    }

}
