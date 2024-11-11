package com.codecraft.nexo.commands;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.config.Message;
import com.codecraft.nexo.utils.AdventureUtils;
import dev.jorel.commandapi.CommandAPICommand;

public class VersionCommand {

    CommandAPICommand getVersionCommand() {
        return new CommandAPICommand("version")
                .withPermission("nexo.command.version")
                .executes((sender, args) -> {
                    Message.VERSION.send(sender, AdventureUtils.tagResolver("version", NexoPlugin.get().getDescription().getVersion()));
                });
    }
}
