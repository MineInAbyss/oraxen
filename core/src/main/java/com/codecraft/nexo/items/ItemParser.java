package com.codecraft.nexo.items;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.compatibilities.provided.ecoitems.WrappedEcoItem;
import com.codecraft.nexo.compatibilities.provided.mmoitems.WrappedMMOItem;
import com.codecraft.nexo.compatibilities.provided.mythiccrucible.WrappedCrucibleItem;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import com.codecraft.nexo.utils.*;
import com.codecraft.nexo.utils.wrappers.AttributeWrapper;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class ItemParser {

    public static final Map<String, ModelData> MODEL_DATAS_BY_ID = new HashMap<>();

    private final NexoMeta nexoMeta;
    private final ConfigurationSection section;
    @Nullable private final ConfigurationSection packSection;
    private final String itemId;
    private final Material type;
    private WrappedMMOItem mmoItem;
    private WrappedCrucibleItem crucibleItem;
    private WrappedEcoItem ecoItem;
    private ItemParser templateItem;
    private boolean configUpdated = false;

    public ItemParser(ConfigurationSection section) {
        this.section = section;
        this.packSection = section.getConfigurationSection("Pack");
        this.itemId = section.getName();

        if (section.isString("template")) templateItem = ItemTemplate.parserTemplate(section.getString("template"));

        ConfigurationSection crucibleSection = section.getConfigurationSection("crucible");
        ConfigurationSection mmoSection = section.getConfigurationSection("mmoitem");
        ConfigurationSection ecoItemSection = section.getConfigurationSection("ecoitem");
        if (crucibleSection != null) crucibleItem = new WrappedCrucibleItem(crucibleSection);
        else if (section.isString("crucible_id")) crucibleItem = new WrappedCrucibleItem(section.getString("crucible_id"));
        else if (ecoItemSection != null) ecoItem = new WrappedEcoItem(ecoItemSection);
        else if (section.isString("ecoitem_id")) ecoItem = new WrappedEcoItem(section.getString("ecoitem_id"));
        else if (mmoSection != null) mmoItem = new WrappedMMOItem(mmoSection);

        Material material = Material.getMaterial(section.getString("material", ""));
        if (material == null) material = usesTemplate() ? templateItem.type : Material.PAPER;
        type = material;

        nexoMeta = templateItem != null ? templateItem.nexoMeta : new NexoMeta();
        if (packSection != null) {
            nexoMeta.packInfo(packSection);
            if (packSection.isInt("custom_model_data"))
                MODEL_DATAS_BY_ID.put(itemId, new ModelData(type, nexoMeta, packSection));
        }
    }

    public boolean usesMMOItems() {
        return crucibleItem == null && ecoItem == null  && mmoItem != null && mmoItem.build() != null;
    }

    public boolean usesCrucibleItems() {
        return mmoItem == null && ecoItem == null && crucibleItem != null && crucibleItem.build() != null;
    }

    public boolean usesEcoItems() {
        return mmoItem == null && crucibleItem == null && ecoItem != null && ecoItem.build() != null;
    }

    public boolean usesTemplate() {
        return templateItem != null;
    }

    public ItemBuilder buildItem() {
        ItemBuilder item;

        if (usesCrucibleItems()) item = new ItemBuilder(crucibleItem);
        else if (usesMMOItems()) item = new ItemBuilder(mmoItem);
        else if (usesEcoItems()) item = new ItemBuilder(ecoItem);
        else item = new ItemBuilder(type);

        // If item has a template, apply the template ontop of the builder made above
        return applyConfig(usesTemplate() ? templateItem.applyConfig(item) : item);
    }

    private ItemBuilder applyConfig(ItemBuilder item) {
        Optional.ofNullable(section.getString("itemname", section.getString("displayname"))).map(AdventureUtils.MINI_MESSAGE::deserialize).ifPresent(itemName -> {
            if (VersionUtil.atleast("1.20.5")) {
                if (section.contains("displayname")) configUpdated = true;
                ItemUtils.itemName(item, itemName);
            } else ItemUtils.displayName(item, itemName);
        });

        Optional.ofNullable(section.getString("customname")).map(AdventureUtils.MINI_MESSAGE::deserialize).ifPresent(customName -> {
            if (VersionUtil.below("1.20.5")) configUpdated = true;
            ItemUtils.displayName(item, customName);
        });

        if (section.contains("lore")) item.lore(section.getStringList("lore").stream().map(AdventureUtils.MINI_MESSAGE::deserialize).toList());
        if (section.contains("unbreakable")) item.setUnbreakable(section.getBoolean("unbreakable", false));
        if (section.contains("unstackable")) item.setUnstackable(section.getBoolean("unstackable", false));
        if (section.contains("color")) item.setColor(Utils.toColor(section.getString("color", "#FFFFFF")));
        if (section.contains("trim_pattern")) item.setTrimPattern(Key.key(section.getString("trim_pattern", "")));

        new ComponentParser(section, item).parseComponents();
        parseMiscOptions(item);
        parseVanillaSections(item);
        parseNexoSection(item);
        item.nexoMeta(nexoMeta);
        return item;
    }

    private void parseMiscOptions(ItemBuilder item) {
        nexoMeta.noUpdate(section.getBoolean("no_auto_update", false));
        nexoMeta.disableEnchanting(section.getBoolean("disable_enchanting", false));
        nexoMeta.excludedFromInventory(section.getBoolean("excludeFromInventory", false));
        nexoMeta.excludedFromCommands(section.getBoolean("excludeFromCommands", false));

        if (section.getBoolean("injectId", true))
            item.customTag(NexoItems.ITEM_ID, PersistentDataType.STRING, itemId);
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private void parseVanillaSections(ItemBuilder item) {
        ConfigurationSection section = mergeWithTemplateSection();

        if (section.contains("ItemFlags")) {
            List<String> itemFlags = section.getStringList("ItemFlags");
            for (String itemFlag : itemFlags)
                item.addItemFlags(ItemFlag.valueOf(itemFlag));
        }

        if (section.contains("PotionEffects")) {
            @SuppressWarnings("unchecked") // because this sections must always return a List<LinkedHashMap<String, ?>>
            List<LinkedHashMap<String, Object>> potionEffects = (List<LinkedHashMap<String, Object>>) section
                    .getList("PotionEffects");
            if (potionEffects == null) return;
            for (Map<String, Object> serializedPotionEffect : potionEffects) {
                PotionEffectType effect = PotionUtils.getEffectType((String) serializedPotionEffect.getOrDefault("type", ""));
                if (effect == null) return;
                int duration = (int) serializedPotionEffect.getOrDefault("duration", 60);
                int amplifier = (int) serializedPotionEffect.getOrDefault("amplifier", 0);
                boolean ambient = (boolean) serializedPotionEffect.getOrDefault("ambient", true);
                boolean particles = (boolean) serializedPotionEffect.getOrDefault("particles", true);
                boolean icon = (boolean) serializedPotionEffect.getOrDefault("icon", true);
                item.addPotionEffect(new PotionEffect(effect, duration, amplifier, ambient, particles, icon));
            }
        }

        if (section.contains("PersistentData")) {
            try {
                List<LinkedHashMap<String, Object>> dataHolder = (List<LinkedHashMap<String, Object>>) section
                        .getList("PersistentData");
                for (LinkedHashMap<String, Object> attributeJson : dataHolder) {
                    String[] keyContent = ((String) attributeJson.get("key")).split(":");
                    final Object persistentDataType = PersistentDataType.class
                            .getDeclaredField((String) attributeJson.get("type")).get(null);
                    item.addCustomTag(new NamespacedKey(keyContent[0], keyContent[1]),
                            (PersistentDataType) persistentDataType,
                            attributeJson.get("value"));
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        if (section.contains("AttributeModifiers")) {
            @SuppressWarnings("unchecked") // because this sections must always return a List<LinkedHashMap<String, ?>>
            List<LinkedHashMap<String, Object>> attributes = (List<LinkedHashMap<String, Object>>) section.getList("AttributeModifiers");
            if (attributes != null) for (LinkedHashMap<String, Object> attributeJson : attributes) {
                attributeJson.putIfAbsent("uuid", UUID.randomUUID().toString());
                attributeJson.putIfAbsent("name", "oraxen:modifier");
                attributeJson.putIfAbsent("key", "oraxen:modifier");
                AttributeModifier attributeModifier = AttributeModifier.deserialize(attributeJson);
                Attribute attribute = AttributeWrapper.fromString((String) attributeJson.get("attribute"));
                item.addAttributeModifiers(attribute, attributeModifier);
            }
        }

        if (section.contains("Enchantments")) {
            ConfigurationSection enchantSection = section.getConfigurationSection("Enchantments");
            if (enchantSection != null) for (String enchant : enchantSection.getKeys(false))
                item.addEnchant(EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchant)),
                        enchantSection.getInt(enchant));
        }
    }

    private void parseNexoSection(ItemBuilder item) {
        ConfigurationSection merged = mergeWithTemplateSection();
        ConfigurationSection mechanicsSection = merged.getConfigurationSection("Mechanics");
        if (mechanicsSection != null) for (String mechanicID : mechanicsSection.getKeys(false)) {
            MechanicFactory factory = MechanicsManager.getMechanicFactory(mechanicID);

            if (factory != null) {
                ConfigurationSection mechanicSection = mechanicsSection.getConfigurationSection(mechanicID);
                if (mechanicSection == null) continue;
                Mechanic mechanic = factory.parse(mechanicSection);
                if (mechanic == null) continue;
                // Apply item modifiers
                for (Function<ItemBuilder, ItemBuilder> itemModifier : mechanic.getItemModifiers())
                    item = itemModifier.apply(item);
            }
        }

        if (nexoMeta.containsPackInfo()) {
            Integer customModelData;
            if (MODEL_DATAS_BY_ID.containsKey(section.getName())) {
                customModelData = MODEL_DATAS_BY_ID.get(section.getName()).modelData();
            } else if (!item.hasItemModel()) {
                customModelData = ModelData.generateId(nexoMeta.modelKey(), type);
                configUpdated = true;
                if (!Settings.DISABLE_AUTOMATIC_MODEL_DATA.toBool())
                    Optional.ofNullable(section.getConfigurationSection("Pack"))
                            .ifPresent(c -> c.set("custom_model_data", customModelData));
            } else customModelData = null;

            if (customModelData != null) {
                item.customModelData(customModelData);
                nexoMeta.customModelData(customModelData);
            }
        }
    }

    private ConfigurationSection mergeWithTemplateSection() {
        if (section == null || templateItem == null || templateItem.section == null) return section;

        ConfigurationSection merged = new YamlConfiguration().createSection(section.getName());
        NexoYaml.copyConfigurationSection(templateItem.section, merged);
        NexoYaml.copyConfigurationSection(section, merged);

        return merged;
    }

    public boolean isConfigUpdated() {
        return configUpdated;
    }

}
