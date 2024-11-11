package com.codecraft.nexo.api;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.events.NexoItemsLoadedEvent;
import com.codecraft.nexo.compatibilities.provided.ecoitems.WrappedEcoItem;
import com.codecraft.nexo.compatibilities.provided.mythiccrucible.WrappedCrucibleItem;
import com.codecraft.nexo.items.ItemBuilder;
import com.codecraft.nexo.items.ItemParser;
import com.codecraft.nexo.items.ModelData;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.MechanicsManager;
import com.codecraft.nexo.utils.EventUtils;
import com.codecraft.nexo.utils.NexoYaml;
import com.codecraft.nexo.utils.VersionUtil;
import com.codecraft.nexo.utils.logs.Logs;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NexoItems {

    public static final NamespacedKey ITEM_ID = new NamespacedKey(NexoPlugin.get(), "id");
    private static Map<File, Map<String, ItemBuilder>> map = new LinkedHashMap<>();
    private static Set<String> items = new LinkedHashSet<>();

    public static void loadItems() {
        ItemParser.MODEL_DATAS_BY_ID.clear();
        ModelData.DATAS.clear();
        NexoPlugin.get().configsManager().assignAllUsedModelDatas();
        NexoPlugin.get().configsManager().parseAllItemTemplates();
        map = NexoPlugin.get().configsManager().parseItemConfig();
        items = new HashSet<>();
        for (final Map<String, ItemBuilder> subMap : map.values())
            items.addAll(subMap.keySet());

        ensureComponentDataHandled();
        EventUtils.callEvent(new NexoItemsLoadedEvent());
    }

    /**
     * Primarily for handling data that requires NexoItem's<br>
     * For example FoodComponent#getUsingConvertsTo
     */
    private static void ensureComponentDataHandled() {
        if (VersionUtil.atleast("1.21")) for (final Entry<File, Map<String, ItemBuilder>> entry : map.entrySet()) {
            Map<String, ItemBuilder> subMap = entry.getValue();
            for (final Entry<String, ItemBuilder> subEntry : subMap.entrySet()) {
                String itemId = subEntry.getKey();
                ItemBuilder itemBuilder = subEntry.getValue();
                if (itemBuilder == null) continue;
                FoodComponent foodComponent = itemBuilder.getFoodComponent();
                if (foodComponent == null) continue;

                ConfigurationSection section = NexoYaml.loadConfiguration(entry.getKey()).getConfigurationSection(itemId + ".Components.food.replacement");
                ItemStack replacementItem = parseFoodComponentReplacement(section);
                //foodComponent.setUsingConvertsTo(replacementItem);
                itemBuilder.setFoodComponent(foodComponent).regen();
            }
        }
    }

    @Nullable
    private static ItemStack parseFoodComponentReplacement(@Nullable ConfigurationSection section) {
        if (section == null) return null;

        ItemStack replacementItem;
        if (section.isString("minecraft_type")) {
            Material material = Material.getMaterial(Objects.requireNonNull(section.getString("minecraft_type")));
            if (material == null) {
                Logs.logError("Invalid material: " + section.getString("minecraft_type"));
                replacementItem = null;
            } else replacementItem = new ItemStack(material);
        } else if (section.isString("nexo_item"))
            replacementItem = NexoItems.itemById(section.getString("nexo_item")).build();
        else if (section.isString("nexo_item"))
            replacementItem = NexoItems.itemById(section.getString("nexo_item")).build();
        else if (section.isString("crucible_item"))
            replacementItem = new WrappedCrucibleItem(section.getString("crucible_item")).build();
        else if (section.isString("mmoitems_id") && section.isString("mmoitems_type"))
            replacementItem = MMOItems.plugin.getItem(section.getString("mmoitems_type"), section.getString("mmoitems_id"));
        else if (section.isString("ecoitem_id"))
            replacementItem = new WrappedEcoItem(section.getString("ecoitem_id")).build();
        else if (section.isItemStack("minecraft_item"))
            replacementItem = section.getItemStack("minecraft_item");
        else replacementItem = null;

        return replacementItem;
    }

    public static String idByItem(final ItemBuilder item) {
        return item.customTag(ITEM_ID, PersistentDataType.STRING);
    }

    public static String idByItem(final ItemStack item) {
        return (item == null || item.getItemMeta() == null || item.getItemMeta().getPersistentDataContainer().isEmpty()) ? null
                : item.getItemMeta().getPersistentDataContainer().get(ITEM_ID, PersistentDataType.STRING);
    }

    public static boolean exists(final String itemId) {
        return items.contains(itemId);
    }

    public static boolean exists(final ItemStack itemStack) {
        return items.contains(NexoItems.idByItem(itemStack));
    }

    public static Optional<ItemBuilder> optionalItemById(final String id) {
        return entryStream().filter(entry -> entry.getKey().equals(id)).findFirst().map(Entry::getValue);
    }

    @Nullable
    public static ItemBuilder itemById(final String id) {
        return optionalItemById(id).orElse(null);
    }

    public static ItemBuilder builderByItem(ItemStack item) {
        return itemById(idByItem(item));
    }

    public static List<ItemBuilder> unexcludedItems(final File file) {
        return map.get(file).values().stream().filter(item -> item.hasNexoMeta() && !item.nexoMeta().excludedFromInventory()).toList();
    }

    public static boolean hasMechanic(String itemID, String mechanicID) {
        MechanicFactory factory = MechanicsManager.getMechanicFactory(mechanicID);
        return factory != null && factory.getMechanic(itemID) != null;
    }

    public static Map<File, Map<String, ItemBuilder>> map() {
        return map != null ? map : new HashMap<>();
    }

    public static Map<String, ItemBuilder> entriesAsMap() {
        return entryStream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public static Set<Entry<String, ItemBuilder>> entries() {
        return entryStream().collect(Collectors.toSet());
    }

    public static Collection<ItemBuilder> items() {
        return itemStream().toList();
    }

    public static Set<String> names() {
        return nameStream().collect(Collectors.toSet());
    }

    public static Stream<String> nameStream() {
        return entryStream().map(Entry::getKey);
    }

    public static Stream<ItemBuilder> itemStream() {
        return entryStream().map(Entry::getValue);
    }

    public static Stream<Entry<String, ItemBuilder>> entryStream() {
        return map == null ? Stream.empty() : map.values().stream().flatMap(map -> map.entrySet().stream());
    }

    public static String[] itemNames() {
        return items.stream().filter(item -> {
            ItemBuilder builder = NexoItems.itemById(item);
            return builder != null && builder.hasNexoMeta() && !builder.nexoMeta().excludedFromCommands();
        }).toArray(String[]::new);
    }

}
