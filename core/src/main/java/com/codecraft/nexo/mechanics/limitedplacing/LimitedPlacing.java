package com.codecraft.nexo.mechanics.limitedplacing;

import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.mechanics.Mechanic;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LimitedPlacing {
    private final LimitedPlacingType type;
    private final List<Material> blockTypes;
    private final Set<Tag<Material>> blockTags;
    private final List<String> nexoBlocks;
    private final boolean floor;
    private final boolean roof;
    private final boolean wall;
    private final RadiusLimitation radiusLimitation;

    public static final class RadiusLimitation {
        private final int radius;
        private final int amount;

        public RadiusLimitation(ConfigurationSection section) {
            radius = section.getInt("radius", -1);
            amount = section.getInt("amount", -1);
        }

        public int getRadius() {
            return radius;
        }

        public int getAmount() {
            return amount;
        }
    }

    public LimitedPlacing(ConfigurationSection section) {
        floor = section.getBoolean("floor", true);
        roof = section.getBoolean("roof", true);
        wall = section.getBoolean("wall", true);
        type = LimitedPlacingType.valueOf(section.getString("type", "DENY"));
        blockTypes = getLimitedBlockMaterials(section.getStringList("block_types"));
        blockTags = getLimitedBlockTags(section.getStringList("block_tags"));
        nexoBlocks =  getLimitedNexoBlocks(section.getStringList("nexo_blocks"));

        ConfigurationSection radiusSection = section.getConfigurationSection("radius_limitation");
        radiusLimitation = radiusSection != null ? new RadiusLimitation(radiusSection) : null;
    }

    public boolean isRadiusLimited() {
        return radiusLimitation != null && radiusLimitation.getRadius() != -1 && radiusLimitation.getAmount() != -1;
    }

    public RadiusLimitation getRadiusLimitation() {
        return radiusLimitation;
    }

    private List<Material> getLimitedBlockMaterials(List<String> list) {
        return list.stream().map(Material::getMaterial).filter(Objects::nonNull).toList();
    }

    private List<String> getLimitedNexoBlocks(List<String> list) {
        return list.stream().filter(e -> NexoBlocks.isCustomBlock(e) || NexoFurniture.isFurniture(e)).toList();
    }

    private Set<Tag<Material>> getLimitedBlockTags(List<String> list) {
        Set<Tag<Material>> tags = new HashSet<>();
        for (String string : list) {
            Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(string), Material.class);
            if (tag != null) tags.add(tag);
        }
        return tags;
    }

    public LimitedPlacingType getType() { return type; }

    public boolean isNotPlacableOn(Block block, BlockFace blockFace) {
        Block placedBlock = block.getRelative(blockFace);
        Block blockBelow = placedBlock.getRelative(BlockFace.DOWN);
        Block blockAbove = placedBlock.getRelative(BlockFace.UP);

        if (wall && block.getType().isSolid() && blockFace.getModY() == 0) return false;
        if (floor && (blockFace == BlockFace.UP || blockBelow.getType().isSolid())) return false;
        if (roof && blockFace == BlockFace.DOWN) return false;
        return !roof || !blockAbove.getType().isSolid();
    }

    public List<Material> getLimitedBlocks() {
        return blockTypes;
    }

    public List<String> getLimitedNexoBlockIds() {
        return nexoBlocks;
    }

    public Set<Tag<Material>> getLimitedTags() {
        return blockTags;
    }

    public boolean checkLimitedMechanic(Block block) {
        if (blockTypes.isEmpty() && blockTags.isEmpty() && nexoBlocks.isEmpty()) return type == LimitedPlacingType.ALLOW;
        String nexoId = checkIfNexoItem(block);
        if (nexoId == null) {
            if (blockTypes.contains(block.getType())) return true;
            for (Tag<Material> tag : blockTags) {
                if (tag.isTagged(block.getType())) return true;
            }
        }

        return (nexoId != null && !nexoBlocks.isEmpty() && nexoBlocks.contains(nexoId));
    }

    private String checkIfNexoItem(Block block) {
        Mechanic mechanic = NexoBlocks.getCustomBlockMechanic(block.getBlockData());
        if (mechanic == null) mechanic = NexoFurniture.getFurnitureMechanic(block.getLocation());

        return mechanic != null ? mechanic.getItemID() : null;
    }

    public enum LimitedPlacingType {
        ALLOW, DENY
    }

    public boolean isFloor() {
        return floor;
    }
    public boolean isRoof() {
        return roof;
    }
    public boolean isWall() {
        return wall;
    }
}
