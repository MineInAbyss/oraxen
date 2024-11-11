package com.codecraft.nexo.items;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.nms.NMSHandlers;
import com.codecraft.nexo.utils.AdventureUtils;
import com.codecraft.nexo.utils.ItemUtils;
import com.codecraft.nexo.utils.VersionUtil;
import com.jeff_media.morepersistentdatatypes.DataType;
import com.jeff_media.persistentdataserializer.PersistentDataSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.codecraft.nexo.items.ItemBuilder.ORIGINAL_NAME_KEY;
import static com.codecraft.nexo.items.ItemBuilder.UNSTACKABLE_KEY;

public class ItemUpdater implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Settings.UPDATE_ITEMS.toBool()) return;

        PlayerInventory inventory = event.getPlayer().getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack oldItem = inventory.getItem(i);
            ItemStack newItem = ItemUpdater.updateItem(oldItem);
            if (oldItem == null || oldItem.equals(newItem)) continue;
            inventory.setItem(i, newItem);
        }
    }

    @EventHandler
    public void onPlayerPickUp(EntityPickupItemEvent event) {
        if (!Settings.UPDATE_ITEMS.toBool()) return;
        if (!(event.getEntity() instanceof Player)) return;

        ItemStack oldItem = event.getItem().getItemStack();
        ItemStack newItem = ItemUpdater.updateItem(oldItem);
        if (oldItem.equals(newItem)) return;
        event.getItem().setItemStack(newItem);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemEnchant(PrepareItemEnchantEvent event) {
        String id = NexoItems.idByItem(event.getItem());
        ItemBuilder builder = NexoItems.itemById(id);
        if (builder == null || !builder.hasNexoMeta()) return;

        if (builder.nexoMeta().disableEnchanting()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemEnchant(PrepareAnvilEvent event) {
        ItemStack item = event.getInventory().getItem(0);
        ItemStack result = event.getResult();
        String id = NexoItems.idByItem(item);
        ItemBuilder builder = NexoItems.itemById(id);
        if (builder == null || !builder.hasNexoMeta()) return;

        if (builder.nexoMeta().disableEnchanting()) {
            if (result == null || item == null) return;
            if (!result.getEnchantments().equals(item.getEnchantments()))
                event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUseMaxDamageItem(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (VersionUtil.below("1.20.5") || player.getGameMode() == GameMode.CREATIVE) return;
        if (ItemUtils.isEmpty(itemStack) || ItemUtils.isTool(itemStack)) return;
        if (!(itemStack.getItemMeta() instanceof Damageable damageable) || !damageable.hasMaxDamage()) return;

        Optional.ofNullable(NexoItems.builderByItem(itemStack)).ifPresent(i -> {
                if (i.isDamagedOnBlockBreak()) itemStack.damage(1, player);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUseMaxDamageItem(EntityDamageByEntityEvent event) {
        if (VersionUtil.below("1.20.5") || VersionUtil.atleast("1.21.2")) return;
        if (!(event.getDamager() instanceof LivingEntity entity)) return;
        ItemStack itemStack = Optional.ofNullable(entity.getEquipment()).map(EntityEquipment::getItemInMainHand).orElse(null);

        if (entity instanceof Player player && player.getGameMode() == GameMode.CREATIVE) return;
        if (ItemUtils.isEmpty(itemStack) || ItemUtils.isTool(itemStack)) return;
        if (!(itemStack.getItemMeta() instanceof Damageable damageable) || !damageable.hasMaxDamage()) return;

        Optional.ofNullable(NexoItems.builderByItem(itemStack)).ifPresent(i -> {
            if (i.isDamagedOnEntityHit()) itemStack.damage(1, entity);
        });
    }

    // Until Paper changes getReplacement to use food-component, this is the best way
    @EventHandler(ignoreCancelled = true)
    public void onUseConvertedTo(PlayerItemConsumeEvent event) {
        ItemStack itemStack = event.getItem();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!VersionUtil.atleast("1.21") && itemMeta == null) return;
        ItemStack usingConvertsTo = ItemUtils.getUsingConvertsTo(itemMeta);
        if (usingConvertsTo == null || !itemStack.isSimilar(ItemUpdater.updateItem(usingConvertsTo))) return;

        PlayerInventory inventory = event.getPlayer().getInventory();
        if (inventory.firstEmpty() == -1) event.setItem(event.getItem().add(usingConvertsTo.getAmount()));
        else Bukkit.getScheduler().runTask(NexoPlugin.get(), () -> {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack oldItem = inventory.getItem(i);
                ItemStack newItem = ItemUpdater.updateItem(oldItem);
                if (!itemStack.isSimilar(newItem)) continue;

                // Remove the item and add it to fix stacking
                inventory.setItem(i, null);
                inventory.addItem(newItem);
            }
        });
    }

    private static final NamespacedKey IF_UUID = Objects.requireNonNull(NamespacedKey.fromString("nexo:if-uuid"));
    private static final NamespacedKey MF_GUI = Objects.requireNonNull(NamespacedKey.fromString("nexo:mf-gui"));
    public static ItemStack updateItem(ItemStack oldItem) {
        String id = NexoItems.idByItem(oldItem);
        if (id == null) return oldItem;

        ItemUtils.editItemMeta(oldItem, itemMeta -> {
            itemMeta.getPersistentDataContainer().remove(IF_UUID);
            itemMeta.getPersistentDataContainer().remove(MF_GUI);
        });

        Optional<ItemBuilder> optionalBuilder = NexoItems.optionalItemById(id);
        if (optionalBuilder.isEmpty() || optionalBuilder.get().nexoMeta().noUpdate()) return oldItem;
        ItemBuilder newItemBuilder = optionalBuilder.get();

        ItemStack newItem = NMSHandlers.getHandler().copyItemNBTTags(oldItem, newItemBuilder.build().clone());
        newItem.setAmount(oldItem.getAmount());

        ItemUtils.editItemMeta(newItem, itemMeta -> {
            ItemMeta oldMeta = oldItem.getItemMeta();
            ItemMeta newMeta = newItem.getItemMeta();
            if (oldMeta == null || newMeta == null) return;
            PersistentDataContainer oldPdc = oldMeta.getPersistentDataContainer();
            PersistentDataContainer itemPdc = itemMeta.getPersistentDataContainer();

            // Transfer over all PDC entries from oldItem to newItem
            List<Map<?, ?>> oldPdcMap = PersistentDataSerializer.toMapList(oldPdc);
            PersistentDataSerializer.fromMapList(oldPdcMap, itemPdc);

            // Add all enchantments from oldItem and add all from newItem aslong as it is not the same Enchantments
            for (Map.Entry<Enchantment, Integer> entry : oldMeta.getEnchants().entrySet())
                itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            for (Map.Entry<Enchantment, Integer> entry : newMeta.getEnchants().entrySet().stream().filter(e -> !oldMeta.getEnchants().containsKey(e.getKey())).toList())
                itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);

            Integer cmd = newMeta.hasCustomModelData() ? (Integer) newMeta.getCustomModelData() : oldMeta.hasCustomModelData() ? (Integer) oldMeta.getCustomModelData() : null;
            itemMeta.setCustomModelData(cmd);

            ItemUtils.lore(itemMeta, Settings.OVERRIDE_ITEM_LORE.toBool() ? newMeta : oldMeta);

            if (newMeta.hasAttributeModifiers()) itemMeta.setAttributeModifiers(newMeta.getAttributeModifiers());
            else if (oldMeta.hasAttributeModifiers()) itemMeta.setAttributeModifiers(oldMeta.getAttributeModifiers());

            if (itemMeta instanceof Damageable damageable && oldMeta instanceof Damageable oldDmg) {
                if (oldDmg.hasDamage()) damageable.setDamage(oldDmg.getDamage());
            }

            if (oldMeta.isUnbreakable()) itemMeta.setUnbreakable(true);

            if (itemMeta instanceof LeatherArmorMeta leatherMeta && oldMeta instanceof LeatherArmorMeta oldLeatherMeta && newMeta instanceof LeatherArmorMeta newLeatherMeta) {
                // If it is not custom armor, keep color
                if (oldItem.getType() == Material.LEATHER_HORSE_ARMOR) leatherMeta.setColor(oldLeatherMeta.getColor());
                // If it is custom armor we use newLeatherMeta color, since the builder would have been altered
                // in the process of creating the shader images. Then we just save the builder to update the config
                else {
                    leatherMeta.setColor(newLeatherMeta.getColor());
                    newItemBuilder.save();
                }
            }

            if (itemMeta instanceof PotionMeta potionMeta && oldMeta instanceof PotionMeta oldPotionMeta) {
                potionMeta.setColor(oldPotionMeta.getColor());
            }

            if (itemMeta instanceof MapMeta mapMeta && oldMeta instanceof MapMeta oldMapMeta) {
                mapMeta.setColor(oldMapMeta.getColor());
            }

            if (itemMeta instanceof ArmorMeta armorMeta && oldMeta instanceof ArmorMeta oldArmorMeta) {
                armorMeta.setTrim(oldArmorMeta.getTrim());
            }

            if (VersionUtil.atleast("1.20.5")) {
                if (newMeta.hasFood()) itemMeta.setFood(newMeta.getFood());
                else if (oldMeta.hasFood()) itemMeta.setFood(oldMeta.getFood());

                if (newMeta.hasEnchantmentGlintOverride()) itemMeta.setEnchantmentGlintOverride(newMeta.getEnchantmentGlintOverride());
                else if (oldMeta.hasEnchantmentGlintOverride()) itemMeta.setEnchantmentGlintOverride(oldMeta.getEnchantmentGlintOverride());

                if (newMeta.hasMaxStackSize()) itemMeta.setMaxStackSize(newMeta.getMaxStackSize());
                else if (oldMeta.hasMaxStackSize()) itemMeta.setMaxStackSize(oldMeta.getMaxStackSize());

                if (newMeta.hasItemName()) ItemUtils.itemName(itemMeta, newMeta);
                else if (oldMeta.hasItemName()) ItemUtils.itemName(oldMeta, newMeta);

                if (newMeta.hasDisplayName()) ItemUtils.displayName(itemMeta, newMeta);
                else if (oldMeta.hasDisplayName()) ItemUtils.displayName(itemMeta, oldMeta);
            }

            if (VersionUtil.atleast("1.21")) {
                if (newMeta.hasJukeboxPlayable()) itemMeta.setJukeboxPlayable(newMeta.getJukeboxPlayable());
                else if (oldMeta.hasJukeboxPlayable()) itemMeta.setJukeboxPlayable(oldMeta.getJukeboxPlayable());
            }

            if (VersionUtil.atleast("1.21.2")) {
                if (newMeta.hasEquippable()) itemMeta.setEquippable(newMeta.getEquippable());
                else if (oldMeta.hasEquippable()) itemMeta.setEquippable(newMeta.getEquippable());

                if (newMeta.isGlider()) itemMeta.setGlider(true);
                else if (oldMeta.isGlider()) itemMeta.setGlider(true);

                if (newMeta.hasItemModel()) itemMeta.setItemModel(newMeta.getItemModel());
                else if (oldMeta.hasItemModel()) itemMeta.setItemModel(oldMeta.getItemModel());

                if (newMeta.hasUseCooldown()) itemMeta.setUseCooldown(newMeta.getUseCooldown());
                else if (oldMeta.hasUseCooldown()) itemMeta.setUseCooldown(oldMeta.getUseCooldown());

                if (newMeta.hasUseRemainder()) itemMeta.setUseRemainder(newMeta.getUseRemainder());
                else if (oldMeta.hasUseRemainder()) itemMeta.setUseRemainder(oldMeta.getUseRemainder());

                if (newMeta.hasDamageResistant()) itemMeta.setDamageResistant(newMeta.getDamageResistant());
                else if (oldMeta.hasDamageResistant()) itemMeta.setDamageResistant(oldMeta.getDamageResistant());

                if (newMeta.hasTooltipStyle()) itemMeta.setTooltipStyle(newMeta.getTooltipStyle());
                else if (oldMeta.hasTooltipStyle()) itemMeta.setTooltipStyle(oldMeta.getTooltipStyle());

                if (newMeta.hasEnchantable()) itemMeta.setEnchantable(newMeta.getEnchantable());
                else if (oldMeta.hasEnchantable()) itemMeta.setEnchantable(oldMeta.getEnchantable());
            }

            // On 1.20.5+ we use ItemName which is different from userchanged displaynames
            // Thus removing the need for this logic
            if (VersionUtil.below("1.20.5")) {

                String oldDisplayName = oldMeta.hasDisplayName() ? AdventureUtils.parseLegacy(VersionUtil.isPaperServer() ? AdventureUtils.MINI_MESSAGE.serialize(oldMeta.displayName()) : AdventureUtils.parseLegacy(oldMeta.getDisplayName())) : null;
                String originalName = AdventureUtils.parseLegacy(oldPdc.getOrDefault(ORIGINAL_NAME_KEY, DataType.STRING, ""));

                if (Settings.OVERRIDE_RENAMED_ITEMS.toBool()) {
                    ItemUtils.displayName(itemMeta, newMeta);
                } else if (!originalName.equals(oldDisplayName)) {
                    ItemUtils.displayName(itemMeta, oldMeta);
                } else {
                    ItemUtils.displayName(itemMeta, newMeta);
                }

                itemPdc.set(ORIGINAL_NAME_KEY, DataType.STRING, VersionUtil.isPaperServer() ?
                        AdventureUtils.MINI_MESSAGE.serialize(newMeta.hasDisplayName() ? newMeta.displayName() : translatable(newItem.getType()))
                        : newMeta.hasDisplayName() ? newMeta.getDisplayName() : AdventureUtils.MINI_MESSAGE.serialize(translatable(newItem.getType()))
                );
            }

            // If the item is not unstackable, we should remove the unstackable tag
            // Also remove it on 1.20.5+ due to maxStackSize component
            if (VersionUtil.atleast("1.20.5") || !newItemBuilder.isUnstackable()) itemPdc.remove(UNSTACKABLE_KEY);
            else itemPdc.set(UNSTACKABLE_KEY, DataType.UUID, UUID.randomUUID());
        });

        return newItem;
    }

    private static @NotNull Component translatable(@NotNull Material type) {
        return Component.translatable((type.isBlock() ? "block" : "item") + ".minecraft." + type.name().toLowerCase());
    }

}
