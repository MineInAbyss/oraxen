package com.codecraft.nexo.utils.actions.impl.message;

import com.codecraft.nexo.NexoPlugin;
import me.gabytm.util.actions.actions.Action;
import me.gabytm.util.actions.actions.ActionMeta;
import me.gabytm.util.actions.actions.Context;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageAction extends Action<Player> {

    public static final String IDENTIFIER = "message";

    public MessageAction(@NotNull ActionMeta<Player> meta) {
        super(meta);
    }

    @Override
    public void run(@NotNull Player player, @NotNull Context<Player> context) {
        final Component message = LegacyComponentSerializer.legacySection().deserialize(getMeta().getParsedData(player, context));
        NexoPlugin.get().audience().player(player).sendMessage(message);
    }

}
