package com.codecraft.nexo.commands;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.config.Message;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.config.SoundManager;
import com.codecraft.nexo.font.FontManager;
import com.codecraft.nexo.items.ItemUpdater;
import com.codecraft.nexo.mechanics.MechanicsManager;
import com.codecraft.nexo.mechanics.furniture.FurnitureFactory;
import com.codecraft.nexo.nms.NMSHandlers;
import com.codecraft.nexo.pack.PackGenerator;
import com.codecraft.nexo.pack.server.NexoPackServer;
import com.codecraft.nexo.recipes.RecipesManager;
import com.codecraft.nexo.utils.AdventureUtils;
import com.codecraft.nexo.utils.logs.Logs;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.TextArgument;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ReloadCommand {

    public static void reloadItems(@Nullable CommandSender sender) {
        Message.RELOAD.send(sender, AdventureUtils.tagResolver("reloaded", "items"));
        Optional.ofNullable(FurnitureFactory.get()).ifPresent(p -> p.packetManager().removeAllFurniturePackets());
        NexoItems.loadItems();
        NexoPlugin.get().invManager().regen();

        if (Settings.UPDATE_ITEMS.toBool() && Settings.UPDATE_ITEMS_ON_RELOAD.toBool()) {
            Logs.logInfo("Updating all items in player-inventories...");
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                PlayerInventory inventory = player.getInventory();
                Bukkit.getScheduler().runTaskAsynchronously(NexoPlugin.get(), () -> {
                    for (int i = 0; i < inventory.getSize(); i++) {
                        ItemStack oldItem = inventory.getItem(i);
                        ItemStack newItem = ItemUpdater.updateItem(oldItem);
                        if (oldItem == null || oldItem.equals(newItem)) continue;
                        inventory.setItem(i, newItem);
                    }
                });
            }
        }

        Logs.logInfo("Updating all placed furniture...");
        for (World world : Bukkit.getServer().getWorlds()) for (ItemDisplay baseEntity : world.getEntitiesByClass(ItemDisplay.class))
            NexoFurniture.updateFurniture(baseEntity);

    }

    public static void reloadPack(@Nullable CommandSender sender) {
        Message.PACK_REGENERATED.send(sender);
        NexoPlugin.get().fontManager(new FontManager(NexoPlugin.get().configsManager()));
        NexoPlugin.get().soundManager(new SoundManager(NexoPlugin.get().configsManager().getSounds()));
        NexoPlugin.get().packGenerator(new PackGenerator());
        NexoPlugin.get().packGenerator().generatePack();
    }

    public static void reloadRecipes(@Nullable CommandSender sender) {
        Message.RELOAD.send(sender, AdventureUtils.tagResolver("reloaded", "recipes"));
        RecipesManager.reload();
    }

    CommandAPICommand getReloadCommand() {
        return new CommandAPICommand("reload")
                .withAliases("rl")
                .withPermission("nexo.command.reload")
                .withArguments(new TextArgument("type").replaceSuggestions(
                        ArgumentSuggestions.strings("items", "pack", "recipes", "messages", "all")))
                .executes((sender, args) -> {
                    switch (((String) args.get("type")).toUpperCase()) {
                        case "ITEMS" -> reloadItems(sender);
                        case "PACK" -> reloadPack(sender);
                        case "RECIPES" -> reloadRecipes(sender);
                        case "CONFIGS" -> NexoPlugin.get().reloadConfigs();
                        default -> {
                            Optional.ofNullable(FurnitureFactory.get()).ifPresent(f -> f.packetManager().removeAllFurniturePackets());
                            MechanicsManager.unloadListeners();
                            MechanicsManager.unregisterTasks();
                            NMSHandlers.resetHandler();
                            NexoPlugin.get().reloadConfigs();
                            NexoPlugin.get().packServer(NexoPackServer.initializeServer());
                            MechanicsManager.registerNativeMechanics();
                            reloadItems(sender);
                            reloadRecipes(sender);
                            reloadPack(sender);
                        }
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        NexoPlugin.get().fontManager().sendGlyphTabCompletion(player);
                    }
                });
    }

}
