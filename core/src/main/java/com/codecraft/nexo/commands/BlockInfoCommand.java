package com.codecraft.nexo.commands;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.items.ItemBuilder;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanicFactory;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanicFactory;
import com.codecraft.nexo.utils.AdventureUtils;
import com.codecraft.nexo.utils.logs.Logs;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.audience.Audience;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Tripwire;

import java.util.Map;

public class BlockInfoCommand {

    CommandAPICommand getBlockInfoCommand() {
        return new CommandAPICommand("blockinfo")
                .withPermission("nexo.command.blockinfo")
                .withArguments(new StringArgument("itemid").replaceSuggestions(ArgumentSuggestions.strings(NexoItems.itemNames())))
                .executes((commandSender, args) -> {
                    String argument = (String) args.get("itemid");
                    Audience audience = NexoPlugin.get().audience().sender(commandSender);
                    if (argument == null) return;
                    if (argument.equals("all")) {
                        for (Map.Entry<String, ItemBuilder> entry : NexoItems.entries()) {
                            if (!NexoBlocks.isCustomBlock(entry.getKey())) continue;
                            sendBlockInfo(audience, entry.getKey());
                        }
                    } else {
                        ItemBuilder ib = NexoItems.itemById(argument);
                        if (ib == null)
                            audience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<red>No block found with ID</red> <dark_red>" + argument));
                        else sendBlockInfo(audience, argument);
                    }
                });
    }

    private void sendBlockInfo(Audience sender, String itemId) {
        sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_aqua>ItemID: <aqua>" + itemId));
        if (NexoBlocks.isNexoNoteBlock(itemId)) {
            NoteBlockMechanic mechanic = NoteBlockMechanicFactory.get().getMechanic(itemId);
            if (mechanic == null) return;
            NoteBlock data = mechanic.blockData();
            sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_aqua>Instrument: " + data.getInstrument()));
            sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_aqua>Note: " + data.getNote().getId()));
            sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_aqua>Powered: " + data.isPowered()));
        } else if (NexoBlocks.isNexoStringBlock(itemId)) {
            StringBlockMechanic mechanic = StringBlockMechanicFactory.get().getMechanic(itemId);
            if (mechanic == null) return;
            Tripwire data = mechanic.blockData();
            sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_aqua>Facing: " + data.getFaces()));
            sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_aqua>Powered: " + data.isPowered()));
            sender.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<dark_aqua>Disarmed: " + data.isDisarmed()));
        }
        Logs.newline();
    }
}
