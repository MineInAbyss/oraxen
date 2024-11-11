package com.codecraft.nexo.compatibilities.provided.worldedit;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WorldEditHandlers {


    public WorldEditHandlers(boolean register) {
        if (register) WorldEdit.getInstance().getEventBus().register(this);
        else WorldEdit.getInstance().getEventBus().unregister(this);
    }


    @Subscribe @SuppressWarnings("unused")
    public void onEditSession(EditSessionEvent event) {
        if (event.getWorld() == null) return;

        event.setExtent(new AbstractDelegateExtent(event.getExtent()) {

            @Override @SuppressWarnings("unchecked")
            public Entity createEntity(com.sk89q.worldedit.util.Location location, BaseEntity baseEntity) {
                if (!Settings.WORLDEDIT_FURNITURE.toBool()) return super.createEntity(location, baseEntity);
                if (baseEntity == null || baseEntity.getType() == BukkitAdapter.adapt(EntityType.INTERACTION)) return null;
                if (!baseEntity.hasNbtData() || baseEntity.getType() != BukkitAdapter.adapt(EntityType.ITEM_DISPLAY))
                    return super.createEntity(location, baseEntity);

                Location bukkitLocation = BukkitAdapter.adapt(BukkitAdapter.adapt(event.getWorld()), location);
                FurnitureMechanic mechanic = getFurnitureMechanic(baseEntity);
                if (mechanic == null) return super.createEntity(location, baseEntity);

                // Remove interaction-tag from baseEntity-nbt
                CompoundTag compoundTag = baseEntity.getNbtData();
                if (compoundTag == null) return super.createEntity(location, baseEntity);
                Map<String, Tag> compoundTagMap = new HashMap<>(compoundTag.getValue());
                Map<String, Tag> bukkitValues = new HashMap<>((Map<String, Tag>) compoundTagMap.get("BukkitValues").getValue());
                bukkitValues.remove("nexo:interaction");
                compoundTagMap.put("BukkitValues", new CompoundTag(bukkitValues));
                baseEntity.setNbtData(new CompoundTag(compoundTagMap));

                return super.createEntity(location, baseEntity);
            }

            @Override
            public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
                BlockData blockData = BukkitAdapter.adapt(block);
                World world = Bukkit.getWorld(event.getWorld().getName());
                Location loc = new Location(world, pos.getX(), pos.getY(), pos.getZ());
                Mechanic mechanic = NexoBlocks.getCustomBlockMechanic(blockData);
                if (blockData.getMaterial() == Material.NOTE_BLOCK) {
                    if (mechanic != null && Settings.WORLDEDIT_NOTEBLOCKS.toBool()) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(NexoPlugin.get(), () -> NexoBlocks.place(mechanic.getItemID(), loc), 1L);
                    }
                } else if (blockData.getMaterial() == Material.TRIPWIRE) {
                    if (mechanic != null && Settings.WORLDEDIT_STRINGBLOCKS.toBool()) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(NexoPlugin.get(), () -> NexoBlocks.place(mechanic.getItemID(), loc), 1L);
                    }
                } else {
                    if (world == null) return super.setBlock(pos, block);
                    Mechanic replacingMechanic = NexoBlocks.getCustomBlockMechanic(loc);
                    if (replacingMechanic == null) return super.setBlock(pos, block);
                    if (replacingMechanic instanceof StringBlockMechanic && !Settings.WORLDEDIT_STRINGBLOCKS.toBool())
                        return super.setBlock(pos, block);
                    if (replacingMechanic instanceof NoteBlockMechanic && !Settings.WORLDEDIT_NOTEBLOCKS.toBool())
                        return super.setBlock(pos, block);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(NexoPlugin.get(), () -> NexoBlocks.remove(loc, null), 1L);
                }

                return super.setBlock(pos, block);
            }

            @Nullable @SuppressWarnings("unchecked")
            private FurnitureMechanic getFurnitureMechanic(@NotNull BaseEntity entity) {
                if (!entity.hasNbtData() || entity.getType() != BukkitAdapter.adapt(EntityType.ITEM_DISPLAY)) return null;
                CompoundTag tag = entity.getNbtData();
                if (tag == null) return null;
                Map<String, Tag> bukkitValues = null;
                try {
                    bukkitValues = (Map<String, Tag>) tag.getValue().get("BukkitValues").getValue();
                } catch (Exception ignored) {
                }
                if (bukkitValues == null) return null;
                Tag furnitureTag = bukkitValues.get("nexo:furniture");
                if (furnitureTag == null) return null;

                String furnitureId = furnitureTag.getValue().toString();
                return NexoFurniture.getFurnitureMechanic(furnitureId);
            }
        });
    }
}
