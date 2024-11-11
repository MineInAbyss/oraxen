package com.codecraft.nexo.pack;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.utils.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PackListener implements Listener {

    public static NamespacedKey CONFIG_PHASE_PACKET_LISTENER = NamespacedKey.fromString("configuration_listener", NexoPlugin.get());

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConnect(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!Settings.PACK_SEND_ON_JOIN.toBool() || (Settings.PACK_SEND_PRE_JOIN.toBool() && VersionUtil.isPaperServer() && player.hasResourcePack())) return;

        int delay = (int) Settings.PACK_SEND_DELAY.getValue();
        if (delay <= 0) NexoPlugin.get().packServer().sendPack(player);
        else Bukkit.getScheduler().runTaskLaterAsynchronously(NexoPlugin.get(), () ->
                NexoPlugin.get().packServer().sendPack(player), delay * 20L);
    }
}
