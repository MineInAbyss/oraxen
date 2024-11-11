package com.codecraft.nexo.utils;

import org.bukkit.Bukkit;

public class PluginUtils {

    public static boolean isEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }
}
