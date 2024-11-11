package com.codecraft.nexo.utils.actions;

import com.codecraft.nexo.compatibilities.CompatibilitiesManager;
import com.codecraft.nexo.utils.actions.impl.command.ConsoleAction;
import com.codecraft.nexo.utils.actions.impl.command.PlayerAction;
import com.codecraft.nexo.utils.actions.impl.message.ActionBarAction;
import com.codecraft.nexo.utils.actions.impl.message.MessageAction;
import com.codecraft.nexo.utils.actions.impl.other.SoundAction;
import me.gabytm.util.actions.placeholders.PlaceholderProvider;
import me.gabytm.util.actions.spigot.actions.SpigotActionManager;
import me.gabytm.util.actions.spigot.placeholders.PlaceholderAPIProvider;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ClickActionManager extends SpigotActionManager {

    public ClickActionManager(@NotNull JavaPlugin plugin) {
        super(plugin);
        registerDefaults(Player.class);
        getComponentParser().registerDefaults(Player.class);

        // Placeholders
        if (CompatibilitiesManager.hasPlugin("PlaceholderAPI")) {
            getPlaceholderManager().register(new PlaceholderAPIProvider());
        }

        getPlaceholderManager().register(new PlayerNamePlaceholderProvider());
        //-----

        // Commands
        register(Player.class, ConsoleAction.IDENTIFIER, ConsoleAction::new);
        register(Player.class, PlayerAction.IDENTIFIER, PlayerAction::new);
        //-----

        // Messages
        register(Player.class, ActionBarAction.IDENTIFIER, ActionBarAction::new);
        register(Player.class, MessageAction.IDENTIFIER, MessageAction::new);
        //-----

        // Other
        register(Player.class, SoundAction.IDENTIFIER, SoundAction::new);
        //-----
    }

    private static class PlayerNamePlaceholderProvider implements PlaceholderProvider<Player> {

        @Override
        public @NotNull Class<Player> getType() {
            return Player.class;
        }

        @Override
        public @NotNull String replace(@NotNull Player player, @NotNull String input) {
            return StringUtils.replace(input, "<player>", player.getName());
        }

    }

}
