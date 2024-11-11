package com.codecraft.nexo.compatibilities.provided.worldedit;

import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.NexoItems;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WorldEditListener implements Listener {
    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent event) {
        List<String> args = Arrays.stream(event.getBuffer().split(" ")).toList();
        if (!event.getBuffer().startsWith("//") || args.isEmpty()) return;

        List<String> ids = nexoBlockIDs.stream()
                .filter(id -> ("nexo:" + id).startsWith(args.get(args.size() - 1)))
                .map("nexo:"::concat).collect(Collectors.toList());
        ids.addAll(event.getCompletions());
        event.setCompletions(ids);
    }

    private static final List<String> nexoBlockIDs = NexoItems.entries().stream()
            .map(entry -> entry.getKey().toLowerCase(Locale.ROOT)).filter(NexoBlocks::isCustomBlock).toList();
}
