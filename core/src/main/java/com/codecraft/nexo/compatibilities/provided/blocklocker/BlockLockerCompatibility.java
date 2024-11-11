package com.codecraft.nexo.compatibilities.provided.blocklocker;

import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.compatibilities.CompatibilityProvider;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import nl.rutgerkok.blocklocker.BlockLockerAPIv2;
import nl.rutgerkok.blocklocker.ProtectableBlocksSettings;
import nl.rutgerkok.blocklocker.ProtectionType;
import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl;
import org.bukkit.block.Block;

public class BlockLockerCompatibility extends CompatibilityProvider<BlockLockerPluginImpl> {
    public BlockLockerCompatibility() {
        BlockLockerAPIv2.getPlugin().getChestSettings().getExtraProtectables().add(new ProtectableBlocksSettings() {

            @Override
            public boolean canProtect(Block block) {
                BlockLockerMechanic blockLocker = null;
                CustomBlockMechanic customBlockMechanic = NexoBlocks.getCustomBlockMechanic(block.getBlockData());
                if (customBlockMechanic != null) blockLocker = customBlockMechanic.blockLocker();

                if (blockLocker == null) {
                    FurnitureMechanic furnitureMechanic = NexoFurniture.getFurnitureMechanic(block.getLocation());
                    if (furnitureMechanic != null) blockLocker = furnitureMechanic.blocklocker();
                }

                return blockLocker != null && blockLocker.canProtect();
            }

            @Override
            public boolean canProtect(ProtectionType type, Block block) {
                BlockLockerMechanic blockLocker = null;
                NoteBlockMechanic noteMechanic = NexoBlocks.getNoteBlockMechanic(block);
                if (noteMechanic != null) blockLocker = noteMechanic.blockLocker();
                StringBlockMechanic stringMechanic = NexoBlocks.getStringMechanic(block);
                if (stringMechanic != null) blockLocker = stringMechanic.blockLocker();
                FurnitureMechanic furnitureMechanic = NexoFurniture.getFurnitureMechanic(block.getLocation());
                if (furnitureMechanic != null) blockLocker = furnitureMechanic.blocklocker();

                return blockLocker != null && blockLocker.canProtect() && blockLocker.getProtectionType() == type;
            }});
    }


}
