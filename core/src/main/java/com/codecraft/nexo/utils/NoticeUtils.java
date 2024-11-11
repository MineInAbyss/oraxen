package com.codecraft.nexo.utils;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.utils.logs.Logs;
import org.bukkit.Bukkit;

public class NoticeUtils {

    public static void compileNotice() {
        Logs.logError("This is a compiled version of Nexo.");
        Logs.logWarning("Compiled versions come without Default assets and support is not provided.");
        Logs.logWarning("Consider purchasing Nexo on SpigotMC or Polymart for access to the full version.");
    }

    public static void leakNotice() {
        Logs.logError("This is a leaked version of Nexo");
        Logs.logError("Piracy is not supported, shutting down plugin.");
        Logs.logError("Consider purchasing Nexo on SpigotMC or Polymart if you want a working version.");
        Bukkit.getPluginManager().disablePlugin(NexoPlugin.get());
    }
}
