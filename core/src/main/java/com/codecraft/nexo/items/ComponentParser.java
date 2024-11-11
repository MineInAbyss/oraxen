package com.codecraft.nexo.items;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.compatibilities.provided.ecoitems.WrappedEcoItem;
import com.codecraft.nexo.compatibilities.provided.mythiccrucible.WrappedCrucibleItem;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.nms.NMSHandlers;
import com.codecraft.nexo.utils.ParseUtils;
import com.codecraft.nexo.utils.VersionUtil;
import com.codecraft.nexo.utils.logs.Logs;
import net.Indyuce.mmoitems.MMOItems;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.tag.DamageTypeTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ComponentParser {

    private final ConfigurationSection section;
    @Nullable private final ConfigurationSection componentSection;
    private final String itemId;
    private final ItemBuilder itemBuilder;

    public ComponentParser(final ConfigurationSection itemSection, final ItemBuilder itemBuilder) {
        this.section = itemSection;
        this.componentSection = section.getConfigurationSection("Components");
        this.itemId = section.getName();
        this.itemBuilder = itemBuilder;
    }

    public void parseComponents() {
        if (componentSection == null || VersionUtil.below("1.20.5")) return;

        if (componentSection.contains("max_stack_size")) itemBuilder.maxStackSize(Math.clamp(componentSection.getInt("max_stack_size"), 1, 99));

        if (componentSection.contains("enchantment_glint_override")) itemBuilder.setEnchantmentGlindOverride(componentSection.getBoolean("enchantment_glint_override"));
        if (componentSection.contains("durability")) {
            itemBuilder.setDamagedOnBlockBreak(componentSection.getBoolean("durability.damage_block_break"));
            itemBuilder.setDamagedOnEntityHit(componentSection.getBoolean("durability.damage_entity_hit"));
            itemBuilder.setDurability(Math.max(componentSection.getInt("durability.value"), componentSection.getInt("durability", 1)));
        }
        if (componentSection.contains("rarity")) itemBuilder.setRarity(ItemRarity.valueOf(componentSection.getString("rarity")));
        if (componentSection.contains("fire_resistant")) itemBuilder.setFireResistant(componentSection.getBoolean("fire_resistant"));
        if (componentSection.contains("hide_tooltip")) itemBuilder.setHideToolTip(componentSection.getBoolean("hide_tooltip"));

        Optional.ofNullable(componentSection.getConfigurationSection("food")).ifPresent(food -> NMSHandlers.getHandler().foodComponent(itemBuilder, food));
        parseToolComponent();

        if (VersionUtil.below("1.21")) return;

        ConfigurationSection jukeboxSection = componentSection.getConfigurationSection("jukebox_playable");
        if (jukeboxSection != null) {
            JukeboxPlayableComponent jukeboxPlayable = new ItemStack(Material.MUSIC_DISC_CREATOR).getItemMeta().getJukeboxPlayable();
            jukeboxPlayable.setShowInTooltip(jukeboxSection.getBoolean("show_in_tooltip"));
            jukeboxPlayable.setSongKey(NamespacedKey.fromString(jukeboxSection.getString("song_key")));
            itemBuilder.setJukeboxPlayable(jukeboxPlayable);
        }

        if (VersionUtil.below("1.21.2")) return;
        Optional.ofNullable(componentSection.getConfigurationSection("equippable"))
                .ifPresent(equippable -> parseEquippableComponent(itemBuilder, equippable));

        Optional.ofNullable(componentSection.getConfigurationSection("use_cooldown")).ifPresent((cooldownSection) -> {
            UseCooldownComponent useCooldownComponent = new ItemStack(Material.PAPER).getItemMeta().getUseCooldown();
            String group = Optional.ofNullable(cooldownSection.getString("group")).orElse("oraxen:" + NexoItems.idByItem(itemBuilder));
            if (!group.isEmpty()) useCooldownComponent.setCooldownGroup(NamespacedKey.fromString(group));
            useCooldownComponent.setCooldownSeconds((float) Math.max(cooldownSection.getDouble("seconds", 1.0), 0f));
            itemBuilder.setUseCooldownComponent(useCooldownComponent);
        });

        Optional.ofNullable(componentSection.getConfigurationSection("use_remainder")).ifPresent(useRemainder -> parseUseRemainderComponent(itemBuilder, useRemainder));
        Optional.ofNullable(componentSection.getString("damage_resistant")).map(NamespacedKey::fromString).ifPresent(damageResistantKey ->
                itemBuilder.setDamageResistant(Bukkit.getTag(DamageTypeTags.REGISTRY_DAMAGE_TYPES, damageResistantKey, DamageType.class))
        );

        Optional.ofNullable(componentSection.getString("tooltip_style")).map(NamespacedKey::fromString).ifPresent(itemBuilder::setTooltipStyle);

        Optional.ofNullable(componentSection.getString("item_model")).map(NamespacedKey::fromString)
                .ifPresentOrElse(itemBuilder::setItemModel, () -> {
                    if (itemBuilder.nexoMeta() == null || !itemBuilder.nexoMeta().containsPackInfo()) return;
                    if (itemBuilder.nexoMeta().customModelData() != null) return;
                    Optional.ofNullable(componentSection.getString("item_model")).map(NamespacedKey::fromString).ifPresent(itemBuilder::setItemModel);
                });

        if (componentSection.contains("enchantable")) itemBuilder.setEnchantable(componentSection.getInt("enchantable"));
        if (componentSection.contains("glider")) itemBuilder.setGlider(componentSection.getBoolean("glider"));

        Optional.ofNullable(componentSection.getConfigurationSection("consumable")).ifPresent(consumableSection ->
                NMSHandlers.getHandler().consumableComponent(itemBuilder, consumableSection)
        );

    }

    private void parseUseRemainderComponent(ItemBuilder item, @NotNull ConfigurationSection useRemainderSection) {
        ItemStack result;
        int amount = useRemainderSection.getInt("amount", 1);

        if (useRemainderSection.contains("nexo_item"))
            result = ItemUpdater.updateItem(NexoItems.itemById(useRemainderSection.getString("nexo_item")).build());
        else if (useRemainderSection.contains("oraxen_item"))
            result = ItemUpdater.updateItem(NexoItems.itemById(useRemainderSection.getString("oraxen_item")).build());
        else if (useRemainderSection.contains("crucible_item"))
            result = new WrappedCrucibleItem(useRemainderSection.getString("crucible_item")).build();
        else if (useRemainderSection.contains("mmoitems_id") && useRemainderSection.isString("mmoitems_type"))
            result = MMOItems.plugin.getItem(useRemainderSection.getString("mmoitems_type"), useRemainderSection.getString("mmoitems_id"));
        else if (useRemainderSection.contains("ecoitem_id"))
            result = new WrappedEcoItem(useRemainderSection.getString("ecoitem_id")).build();
        else if (useRemainderSection.contains("minecraft_type")) {
            Material material = Material.getMaterial(useRemainderSection.getString("minecraft_type", "AIR"));
            if (material == null || material.isAir()) return;
            result = new ItemStack(material);
        } else result = useRemainderSection.getItemStack("minecraft_item");

        if (result != null) result.setAmount(amount);
        item.setUseRemainder(result);
    }

    private void parseEquippableComponent(ItemBuilder item, ConfigurationSection equippableSection) {
        EquippableComponent equippableComponent = new ItemStack(itemBuilder.getType()).getItemMeta().getEquippable();

        String slot = equippableSection.getString("slot");
        try {
            equippableComponent.setSlot(EquipmentSlot.valueOf(slot));
        } catch (Exception e) {
            Logs.logWarning("Error parsing equippable-component in %s...".formatted(section.getName()));
            Logs.logWarning("Invalid \"slot\"-value %s".formatted(slot));
            Logs.logWarning("Valid values are: %s".formatted(StringUtils.join(EquipmentSlot.values())));
            return;
        }

        List<EntityType> entityTypes = equippableSection.getStringList("allowed_entity_types").stream().map(e -> EnumUtils.getEnum(EntityType.class, e)).toList();
        if (equippableSection.contains("allowed_entity_types")) equippableComponent.setAllowedEntities(entityTypes.isEmpty() ? null : entityTypes);
        if (equippableSection.contains("damage_on_hurt")) equippableComponent.setDamageOnHurt(equippableSection.getBoolean("damage_on_hurt", true));
        if (equippableSection.contains("dispensable")) equippableComponent.setDispensable(equippableSection.getBoolean("dispensable", true));
        if (equippableSection.contains("swappable")) equippableComponent.setSwappable(equippableSection.getBoolean("swappable", true));

        Optional.ofNullable(equippableSection.getString("model")).map(NamespacedKey::fromString).ifPresent(equippableComponent::setModel);
        Optional.ofNullable(equippableSection.getString("camera_overlay")).map(NamespacedKey::fromString).ifPresent(equippableComponent::setCameraOverlay);
        Optional.ofNullable(equippableSection.getString("equip_sound")).map(Key::key).map(Registry.SOUNDS::get).ifPresent(equippableComponent::setEquipSound);

        item.setEquippableComponent(equippableComponent);
    }

    @SuppressWarnings({"UnstableApiUsage", "unchecked"})
    private void parseToolComponent() {
        ConfigurationSection toolSection = componentSection.getConfigurationSection("tool");
        if (toolSection == null) return;

        ToolComponent toolComponent = new ItemStack(Material.PAPER).getItemMeta().getTool();
        toolComponent.setDamagePerBlock(Math.max(toolSection.getInt("damage_per_block", 1), 0));
        toolComponent.setDefaultMiningSpeed(Math.max((float) toolSection.getDouble("default_mining_speed", 1.0), 0f));

        for (Map<?, ?> ruleEntry : toolSection.getMapList("rules")) {
            float speed = ParseUtils.parseFloat(String.valueOf(ruleEntry.get("speed")), 1.0f);
            boolean correctForDrops = Boolean.parseBoolean(String.valueOf(ruleEntry.get("correct_for_drops")));
            Set<Material> materials = new HashSet<>();
            Set<Tag<Material>> tags = new HashSet<>();

            if (ruleEntry.containsKey("material")) {
                try {
                    Material material = Material.valueOf(String.valueOf(ruleEntry.get("material")));
                    if (material.isBlock()) materials.add(material);
                } catch (Exception e) {
                    Logs.logWarning("Error parsing rule-entry in " + itemId);
                    Logs.logWarning("Malformed \"material\"-section");
                    if (Settings.DEBUG.toBool()) e.printStackTrace();
                }
            }

            if (ruleEntry.containsKey("materials")) {
                try {
                    List<String> materialIds = (List<String>) ruleEntry.get("materials");
                    for (String materialId : materialIds) {
                        Material material = Material.valueOf(materialId);
                        if (material.isBlock()) materials.add(material);
                    }
                } catch (Exception e) {
                    Logs.logWarning("Error parsing rule-entry in " + itemId);
                    Logs.logWarning("Malformed \"materials\"-section");
                    if (Settings.DEBUG.toBool()) e.printStackTrace();
                }
            }

            if (ruleEntry.containsKey("tag")) {
                try {
                    NamespacedKey tagKey = NamespacedKey.fromString(String.valueOf(ruleEntry.get("tag")));
                    if (tagKey != null) tags.add(Bukkit.getTag(Tag.REGISTRY_BLOCKS, tagKey, Material.class));
                } catch (Exception e) {
                    Logs.logWarning("Error parsing rule-entry in " + itemId);
                    Logs.logWarning("Malformed \"tag\"-section");
                    if (Settings.DEBUG.toBool()) e.printStackTrace();
                }
            }

            if (ruleEntry.containsKey("tags")) {
                try {
                    for (String tagString : (List<String>) ruleEntry.get("tags")) {
                        NamespacedKey tagKey = NamespacedKey.fromString(tagString);
                        if (tagKey != null) tags.add(Bukkit.getTag(Tag.REGISTRY_BLOCKS, tagKey, Material.class));
                    }
                } catch (Exception e) {
                    Logs.logWarning("Error parsing rule-entry in " + itemId);
                    Logs.logWarning("Malformed \"material\"-section");
                    if (Settings.DEBUG.toBool()) e.printStackTrace();
                }
            }

            if (!materials.isEmpty()) toolComponent.addRule(materials, speed, correctForDrops);
            for (Tag<Material> tag : tags) toolComponent.addRule(tag, speed, correctForDrops);
        }

        itemBuilder.setToolComponent(toolComponent);
    }
}
