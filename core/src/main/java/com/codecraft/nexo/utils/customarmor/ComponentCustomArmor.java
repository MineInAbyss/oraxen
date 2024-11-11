package com.codecraft.nexo.utils.customarmor;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.items.ItemBuilder;
import com.codecraft.nexo.utils.logs.Logs;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.texture.Texture;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;
import java.util.stream.Collectors;

public class ComponentCustomArmor {

    public void generatePackFiles(ResourcePack resourcePack) {
        Set<String> armorPrefixes = armorPrefixes(resourcePack);
        writeArmorModels(resourcePack, armorPrefixes);
        copyArmorLayerTextures(resourcePack);
        checkOraxenArmorItems(armorPrefixes);
    }

    private void writeArmorModels(ResourcePack resourcePack, Set<String> armorPrefixes) {
        for (String armorprefix : armorPrefixes) {
            JsonObject armorModel = Json.createObjectBuilder().add("texture", "oraxen:" + armorprefix).build();
            JsonArray armorModelArray = Json.createArrayBuilder().add(armorModel).build();
            JsonObject equipmentModel = Json.createObjectBuilder().add("layers", Json.createObjectBuilder()
                    .add("humanoid", armorModelArray)
                    .add("humanoid_leggings", armorModelArray).build()
            ).build();

            if (resourcePack.unknownFiles().containsKey("assets/nexo/models/equipment/" + armorprefix)) continue;
            resourcePack.unknownFile("assets/nexo/models/equipment/" + armorprefix, Writable.stringUtf8(equipmentModel.toString()));
        }
    }

    private void copyArmorLayerTextures(ResourcePack resourcePack) {
        for (Texture texture : new ArrayList<>(resourcePack.textures())) {
            String armorFolder = texture.key().asString().endsWith("_armor_layer_1.png") ? "humanoid" : "humanoid_leggings";
            String armorPrefix = armorPrefix(texture);
            if (armorPrefix.isEmpty()) continue;

            resourcePack.removeTexture(texture.key());
            resourcePack.texture(Key.key("nexo:entity/equipment/%s/%s.png".formatted(armorFolder, armorPrefix)), texture.data());
        }
    }

    private void checkOraxenArmorItems(Set<String> armorPrefixes) {
        // No need to log for all 4 armor pieces, so skip to minimise log spam
        List<String> skippedArmorType = new ArrayList<>();
        for (Map.Entry<String, ItemBuilder> entry : NexoItems.entries()) {
            String itemId = entry.getKey();
            ItemBuilder itemBuilder = entry.getValue();
            ItemStack itemStack = itemBuilder.referenceClone();
            String armorPrefix = StringUtils.substringBeforeLast(itemId, "_");
            EquipmentSlot slot = slotFromItem(itemId);

            if (!armorPrefixes.contains(armorPrefix) || skippedArmorType.contains(armorPrefix) || slot == null) continue;
            if (itemStack == null || !itemStack.hasItemMeta()) continue;

            if (!itemBuilder.hasEquippableComponent() || itemBuilder.getEquippableComponent().getModel() == null) {
                if (!Settings.CUSTOM_ARMOR_COMPONENT_ASSIGN.toBool()) {
                    Logs.logWarning("Item " + itemId + " does not have an equippable-component configured properly.");
                    Logs.logWarning("Oraxen has been configured to use Components for custom-armor due to " + Settings.CUSTOM_ARMOR_TYPE.getPath() + " setting");
                    Logs.logWarning("Custom Armor will not work unless an equippable-component is set.", true);
                    skippedArmorType.add(armorPrefix);
                } else {
                    EquippableComponent component = Optional.ofNullable(itemBuilder.getEquippableComponent()).orElse(new ItemStack(Material.PAPER).getItemMeta().getEquippable());
                    NamespacedKey modelKey = NamespacedKey.fromString("oraxen:" + armorPrefix);
                    if (component.getModel() == null) component.setModel(modelKey);
                    component.setSlot(slotFromItem(itemId));
                    itemBuilder.setEquippableComponent(component);

                    itemBuilder.save();
                    Logs.logWarning("Item " + itemId + " does not have an equippable-component set.");
                    Logs.logInfo("Configured Components.equippable.model to %s for %s".formatted(modelKey.toString(), itemId), true);
                }
            }
        }
    }

    @Nullable
    private EquipmentSlot slotFromItem(String itemId) {
        return switch (StringUtils.substringAfterLast(itemId, "_").toUpperCase(Locale.ENGLISH)) {
            case "HELMET" -> EquipmentSlot.HEAD;
            case "CHESTPLATE" -> EquipmentSlot.CHEST;
            case "LEGGINGS" -> EquipmentSlot.LEGS;
            case "BOOTS" -> EquipmentSlot.FEET;
            default -> null;
        };
    }

    private LinkedHashSet<String> armorPrefixes(ResourcePack resourcePack) {
        return resourcePack.textures().stream().map(this::armorPrefix).filter(StringUtils::isNotBlank).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String armorPrefix(Texture texture) {
        String textureKey = texture.key().asString();
        return textureKey.endsWith("_armor_layer_1.png")
                ? StringUtils.substringAfterLast(StringUtils.substringBefore(textureKey, "_armor_layer_1.png"), "/")
                : textureKey.endsWith("_armor_layer_2.png")
                ? StringUtils.substringAfterLast(StringUtils.substringBefore(textureKey, "_armor_layer_2.png"), "/")
                : textureKey.endsWith("_layer_1.png")
                ? StringUtils.substringAfterLast(StringUtils.substringBefore(textureKey, "_layer_1.png"), "/")
                : textureKey.endsWith("_layer_2.png")
                ? StringUtils.substringAfterLast(StringUtils.substringBefore(textureKey, "_layer_2.png"), "/")
                : "";
    }
}
