package com.codecraft.nexo.commands;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.items.ItemBuilder;
import com.codecraft.nexo.utils.AdventureUtils;
import com.codecraft.nexo.utils.logs.Logs;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.audience.Audience;

import java.util.Map;

public class ItemInfoCommand {

    CommandAPICommand getItemInfoCommand() {
        return new CommandAPICommand("iteminfo")
                .withPermission("nexo.command.iteminfo")
                .withArguments(new StringArgument("itemid").replaceSuggestions(ArgumentSuggestions.strings(NexoItems.itemNames())))
                .executes((commandSender, args) -> {
                    String argument = (String) args.get("itemid");
                    Audience audience = NexoPlugin.get().audience().sender(commandSender);
                    if (argument.equals("all")) {
                        for (Map.Entry<String, ItemBuilder> entry : NexoItems.entries()) {
                            sendItemInfo(audience, entry.getValue(), entry.getKey());
                        }
                    } else {
                        ItemBuilder ib = NexoItems.itemById(argument);
                        if (ib == null)
                            audience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<red>No item found with ID</red> <dark_red>" + argument));
                        else sendItemInfo(audience, ib, argument);
                    }
                });
    }

    private void sendItemInfo(Audience sender, ItemBuilder builder, String itemId) {
        sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_aqua>ItemID: <aqua>" + itemId));
        sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_green>CustomModelData: <green>" + builder.nexoMeta().customModelData()));
        sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_green>Material: <green>" + builder.referenceClone().getType()));
        sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_green>Model Name: <green>" + builder.nexoMeta().modelKey().asString()));
        Logs.newline();
    }
}
