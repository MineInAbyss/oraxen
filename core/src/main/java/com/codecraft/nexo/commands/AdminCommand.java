package com.codecraft.nexo.commands;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import com.codecraft.nexo.utils.AdventureUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

public class AdminCommand {

    CommandAPICommand getAdminCommand() {
        return new CommandAPICommand("admin")
                .withPermission("nexo.command.admin")
                .withSubcommands(getFurniturePlaceRemoveCommand(), getNoteblockPlaceRemoveCommand());
    }

    private CommandAPICommand getNoteblockPlaceRemoveCommand() {
        return new CommandAPICommand("block")
                .withArguments(new TextArgument("block").replaceSuggestions(ArgumentSuggestions.strings(NexoBlocks.blockIDs())))
                .withArguments(new TextArgument("type").replaceSuggestions(ArgumentSuggestions.strings("place", "remove")))
                .withOptionalArguments(new LocationArgument("location"))
                .withOptionalArguments(new IntegerArgument("radius"))
                .withOptionalArguments(new BooleanArgument("random"))
                .executesPlayer((player, args) -> {
                    String id = (String) args.get("block");
                    if (!NexoBlocks.isCustomBlock(id)) {
                        NexoPlugin.get().audience().player(player).sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<red>Unknown NexoBlock: <yellow>" + id));
                    } else {
                        Location loc = (Location) args.getOptional("location").orElse(player.getLocation());
                        String type = (String) args.get("type");
                        int radius = (int) args.getOptional("radius").orElse(1);
                        boolean isRandom = (boolean) args.getOptional("random").orElse(false);
                        for (Block block : getBlocks(loc, radius, isRandom)) {
                            if (type == null) continue;
                            if (type.equals("remove")) NexoBlocks.remove(block.getLocation(), null);
                            if (type.equals("place")) NexoBlocks.place(id, block.getLocation());
                        }
                    }
                });
    }

    private CommandAPICommand getFurniturePlaceRemoveCommand() {
        Set<String> furnitureIDs = NexoFurniture.getFurnitureIDs();
        furnitureIDs.add("all");
        return new CommandAPICommand("furniture")
                .withArguments(
                        new TextArgument("type").replaceSuggestions(ArgumentSuggestions.strings("place", "remove")),
                        new TextArgument("furniture").replaceSuggestions(ArgumentSuggestions.strings(furnitureIDs))
                )
                .withOptionalArguments(
                        new LocationArgument("location"),
                        new IntegerArgument("radius"),
                        new BooleanArgument("random")
                )
                .executesPlayer((player, args) -> {
                    String type = (String) args.get("type");
                    assert type != null;
                    String id = (String) args.getOrDefault("furniture", "");
                    if (!NexoFurniture.isFurniture(id))
                        NexoPlugin.get().audience().player(player).sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<red>Unknown Furniture: <yellow>" + id));
                    else {
                        Location loc = (Location) args.getOptional("location").orElse(player.getLocation());
                        int radius = (int) args.getOptional("radius").orElse(0);
                        boolean isRandom = (boolean) args.getOptional("random").orElse(false);
                        for (Block block : getBlocks(loc, radius, isRandom)) {
                            if (type.equals("remove")) {
                                FurnitureMechanic mechanic = NexoFurniture.getFurnitureMechanic(block.getLocation());
                                if (mechanic != null && (id.isEmpty() || id.equals("all") || mechanic.getItemID().equals(id)))
                                    NexoFurniture.remove(block.getLocation(), null);
                            }
                            if (type.equals("place")) NexoFurniture.place(id, block.getLocation(), 0f, BlockFace.NORTH);
                        }
                    }
                });
    }

    private Collection<Block> getBlocks(Location loc, int radius, boolean isRandom) {
        List<Block> blocks = new ArrayList<>();
        if (radius <= 0) return Collections.singletonList(loc.getBlock());
        for (int x = loc.getBlockX() - radius; x <= loc.getBlockX() + radius; x++)
            for (int z = loc.getBlockZ() - radius; z <= loc.getBlockZ() + radius; z++)
                for (int y = loc.getBlockY() - radius; y <= loc.getBlockY() + radius; y++) {
                    blocks.add(loc.getWorld().getBlockAt(x, y, z));
                }
        if (isRandom) return Collections.singletonList(blocks.get(new Random().nextInt(blocks.size())));
        return blocks;
    }
}
