package com.codecraft.nexo.compatibilities.provided.mythicmobs;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.compatibilities.CompatibilityProvider;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.utils.numbers.RandomDouble;
import io.lumine.mythic.core.drops.Drop;
import io.lumine.mythic.core.drops.droppables.VanillaItemDrop;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MythicMobsCompatibility extends CompatibilityProvider<MythicBukkit> {

    @EventHandler
    public void onMythicDropLoadEvent(MythicDropLoadEvent event) {
        if (!event.getDropName().equalsIgnoreCase("nexo")) return;

        String line = event.getContainer().getLine();
        String[] lines = line.split(" ");
        String itemId = lines.length == 4 ? lines[1] : lines.length == 3 ? lines[2] : "";
        String amountRange = Arrays.stream(lines).filter(s -> s.contains("-")).findFirst().orElse("1-1");
        ItemStack nexoItem = NexoItems.itemById(itemId).build();
        if (nexoItem == null) return;

        Drop drop = new VanillaItemDrop(line, event.getConfig(), new ItemComponentBukkitItemStack(nexoItem), new RandomDouble(amountRange));
        event.register(drop);
    }
}
