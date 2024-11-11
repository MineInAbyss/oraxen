package com.codecraft.nexo.commands;

import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.config.Message;
import com.codecraft.nexo.items.ItemUpdater;
import com.codecraft.nexo.utils.AdventureUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class UpdateCommand {

    CommandAPICommand getUpdateCommand() {
        return new CommandAPICommand("update")
                .withPermission("nexo.command.update")
                .withSubcommands(getFurnitureUpdateCommand(), getItemUpdateCommand());
    }

    @SuppressWarnings("unchecked")
    private CommandAPICommand getItemUpdateCommand() {
        return new CommandAPICommand("item")
                .withArguments(new EntitySelectorArgument.ManyEntities("targets"))
                .executesPlayer((player, args) -> {
                    final Collection<Player> targets = ((Collection<Entity>) args.get("targets")).stream().filter(entity -> entity instanceof Player).map(e -> (Player) e).toList();
                    for (Player p : targets) {
                        int updated = 0;
                        for (int i = 0; i < p.getInventory().getSize(); i++) {
                            final ItemStack oldItem = p.getInventory().getItem(i);
                            final ItemStack newItem = ItemUpdater.updateItem(oldItem);
                            if (oldItem == null || oldItem.equals(newItem)) continue;
                            p.getInventory().setItem(i, newItem);
                            updated++;
                        }
                        p.updateInventory();
                        Message.UPDATED_ITEMS.send(player, AdventureUtils.tagResolver("amount", String.valueOf(updated)),
                                AdventureUtils.tagResolver("player", p.getDisplayName()));
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private CommandAPICommand getFurnitureUpdateCommand() {
        return new CommandAPICommand("furniture")
                .withOptionalArguments(new IntegerArgument("radius"))
                .executesPlayer((player, args) -> {
                    int radius = (int) args.getOptional("radius").orElse(10);
                    final Collection<ItemDisplay> targets = ((Collection<ItemDisplay>) args.getOptional("targets").orElse(player.getNearbyEntities(radius, radius, radius))).stream().filter(NexoFurniture::isFurniture).toList();
                    for (ItemDisplay entity : targets) NexoFurniture.updateFurniture(entity);
                });
    }
}
