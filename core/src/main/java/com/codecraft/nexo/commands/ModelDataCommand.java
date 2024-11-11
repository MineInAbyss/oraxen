package com.codecraft.nexo.commands;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.items.ItemBuilder;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ModelDataCommand {
    CommandAPICommand getHighestModelDataCommand() {
        return new CommandAPICommand("highest_modeldata")
                .withAliases("h_md")
                .withPermission("nexo.command.debug")
                .executes((sender, args) -> {
                    Map<Material, Integer> itemMap = new HashMap<>();
                    for (ItemBuilder builder : NexoItems.items()) {
                        int currentModelData = builder.nexoMeta().customModelData();
                        Material type = builder.build().getType();

                        if (currentModelData != 0) itemMap.putIfAbsent(type, currentModelData);
                        if (itemMap.containsKey(type) && itemMap.get(type) < currentModelData) {
                            itemMap.put(type, currentModelData);
                        }
                    }
                    Component report = Component.empty();
                    for (Map.Entry<Material, Integer> entry : itemMap.entrySet()) {
                        String message = (ChatColor.DARK_AQUA + entry.getKey().name() + ": " + ChatColor.DARK_GREEN + entry.getValue().toString() + "\n");
                        report = report.append(Component.text(message));
                    }
                    NexoPlugin.get().audience().sender(sender).sendMessage(report);
                });
    }
}
