package com.codecraft.nexo.mechanics.custom_block.stringblock.sapling;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.compatibilities.provided.worldedit.WrappedWorldEdit;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.codecraft.nexo.utils.BlockHelpers;
import com.codecraft.nexo.utils.PluginUtils;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class SaplingTask extends BukkitRunnable {

    private final int delay;

    public SaplingTask(int delay) {
        this.delay = delay;
    }

    @Override
    public void run() {
        if (!PluginUtils.isEnabled("WorldEdit")) return;
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Block block : CustomBlockData.getBlocksWithCustomData(NexoPlugin.get(), chunk)) {
                    PersistentDataContainer pdc = BlockHelpers.getPDC(block);
                    if (pdc.has(SaplingMechanic.SAPLING_KEY, PersistentDataType.INTEGER) && block.getType() == Material.TRIPWIRE) {
                        StringBlockMechanic string = NexoBlocks.getStringMechanic(block);
                        if (string == null || !string.isSapling()) return;

                        SaplingMechanic sapling = string.sapling();
                        if (sapling == null || !sapling.hasSchematic()) continue;
                        if (!sapling.canGrowNaturally()) continue;
                        if (sapling.requiresWaterSource() && !sapling.isUnderWater(block)) continue;
                        if (sapling.requiresLight() && block.getLightLevel() < sapling.getMinLightLevel()) continue;
                        if (!sapling.replaceBlocks() && !WrappedWorldEdit.getBlocksInSchematic(block.getLocation(), sapling.getSchematic()).isEmpty()) continue;

                        int growthTimeRemains = pdc.getOrDefault(SaplingMechanic.SAPLING_KEY, PersistentDataType.INTEGER, 0) - delay;
                        if (growthTimeRemains <= 0) {
                            block.setType(Material.AIR, false);
                            if (sapling.hasGrowSound())
                                block.getWorld().playSound(block.getLocation(), sapling.getGrowSound(), 1.0f, 0.8f);
                            WrappedWorldEdit.pasteSchematic(block.getLocation(), sapling.getSchematic(), sapling.replaceBlocks(), sapling.copyBiomes(), sapling.copyEntities());
                        } else pdc.set(SaplingMechanic.SAPLING_KEY, PersistentDataType.INTEGER, growthTimeRemains);
                    }
                    else if (pdc.has(SaplingMechanic.SAPLING_KEY, PersistentDataType.INTEGER) && block.getType() != Material.TRIPWIRE) {
                        pdc.remove(SaplingMechanic.SAPLING_KEY);
                    }
                }
            }
        }
    }
}
