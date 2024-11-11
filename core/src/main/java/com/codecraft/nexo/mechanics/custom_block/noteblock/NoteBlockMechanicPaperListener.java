package com.codecraft.nexo.mechanics.custom_block.noteblock;

import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.NexoItems;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class NoteBlockMechanicPaperListener implements Listener {

    @EventHandler
    public void onFallingBlockLandOnCarpet(EntityRemoveFromWorldEvent event) {
        if (!(event.getEntity() instanceof FallingBlock fallingBlock)) return;
        NoteBlockMechanic mechanic = NexoBlocks.getNoteBlockMechanic(fallingBlock.getBlockData());
        if (mechanic == null || Objects.equals(NexoBlocks.getCustomBlockMechanic(fallingBlock.getLocation()), mechanic))
            return;
        if (mechanic.isDirectional() && !mechanic.directional().isParentBlock())
            mechanic = mechanic.directional().getParentMechanic();

        ItemStack itemStack = NexoItems.itemById(mechanic.getItemID()).build();
        fallingBlock.setDropItem(false);
        fallingBlock.getWorld().dropItemNaturally(fallingBlock.getLocation(), itemStack);
    }
}
