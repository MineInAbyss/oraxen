package com.codecraft.nexo.items;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.compatibilities.provided.ecoitems.WrappedEcoItem;
import com.codecraft.nexo.compatibilities.provided.mmoitems.WrappedMMOItem;
import com.codecraft.nexo.compatibilities.provided.mythiccrucible.WrappedCrucibleItem;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.utils.*;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.jeff_media.morepersistentdatatypes.DataType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.components.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("ALL")
public class ItemBuilder {

    public static final NamespacedKey UNSTACKABLE_KEY = new NamespacedKey(NexoPlugin.get(), "unstackable");
    public static final NamespacedKey ORIGINAL_NAME_KEY = new NamespacedKey(NexoPlugin.get(), "original_name");

    private final ItemStack itemStack;
    private final Map<PersistentDataSpace, Object> persistentDataMap = new HashMap<>();
    private final PersistentDataContainer persistentDataContainer;
    private final Map<Enchantment, Integer> enchantments;
    private NexoMeta nexoMeta;
    private Material type;
    private int amount;
    private Color color; // LeatherArmor-, Potion-, Map- & FireWorkEffect-Meta
    private Key trimPattern;
    private PotionType basePotionType;
    private List<PotionEffect> customPotionEffects;
    private boolean unbreakable;
    private boolean unstackable;
    private Set<ItemFlag> itemFlags;
    private boolean hasAttributeModifiers;
    private Multimap<Attribute, AttributeModifier> attributeModifiers;
    @Nullable private Integer customModelData;
    private Component displayName;
    private List<Component> lore;
    private ItemStack finalItemStack;

    // 1.20.5+ properties
    @Nullable
    private FoodComponent foodComponent;
    @Nullable
    private ToolComponent toolComponent;
    @Nullable
    private Boolean enchantmentGlintOverride;
    @Nullable
    private Integer maxStackSize;
    @Nullable
    private Component itemName;
    @Nullable
    private Boolean fireResistant;
    @Nullable
    private Boolean hideToolTip;
    @Nullable
    private ItemRarity rarity;
    @Nullable
    private Integer durability;
    private boolean damagedOnBlockBreak;
    private boolean damagedOnEntityHit;

    // 1.21+ properties
    @Nullable
    private JukeboxPlayableComponent jukeboxPlayable;

    // 1.21.2+ properties
    @Nullable
    private EquippableComponent equippableComponent;
    @Nullable
    private Boolean isGlider;
    @Nullable
    private UseCooldownComponent useCooldownComponent;
    @Nullable
    private ItemStack useRemainder;
    @Nullable
    private Tag<DamageType> damageResistant;
    @Nullable
    private NamespacedKey tooltipStyle;
    @Nullable
    private NamespacedKey itemModel;
    @Nullable
    private Integer enchantable;


    public ItemBuilder(final Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(WrappedMMOItem wrapped) {
        this(wrapped.build());
    }

    public ItemBuilder(WrappedCrucibleItem wrapped) {
        this(wrapped.build());
    }

    public ItemBuilder(WrappedEcoItem wrapped) {
        this(wrapped.build());
    }

    public ItemBuilder(@NotNull ItemStack itemStack) {

        this.itemStack = itemStack;

        type = itemStack.getType();

        amount = itemStack.getAmount();

        final ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;

        if (itemMeta instanceof LeatherArmorMeta leatherArmorMeta)
            color = leatherArmorMeta.getColor();

        if (itemMeta instanceof PotionMeta potionMeta) {
            color = potionMeta.getColor();
            basePotionType = potionMeta.getBasePotionType();
            customPotionEffects = new ArrayList<>(potionMeta.getCustomEffects());
        }

        if (itemMeta instanceof MapMeta mapMeta)
            color = mapMeta.getColor();

        if (itemMeta instanceof FireworkEffectMeta effectMeta)
            color = effectMeta.hasEffect() ? Utils.getOrDefault(effectMeta.getEffect().getColors(), 0, Color.WHITE) : Color.WHITE;

        if (itemMeta instanceof ArmorMeta armorMeta && armorMeta.hasTrim())
            trimPattern = armorMeta.getTrim().getMaterial().key();

        if (itemMeta.hasDisplayName()) {
            if (VersionUtil.isPaperServer()) displayName = itemMeta.displayName();
            else displayName = AdventureUtils.LEGACY_SERIALIZER.deserialize(itemMeta.getDisplayName());
        }

        if (itemMeta.hasLore()) {
            if (VersionUtil.isPaperServer()) lore = itemMeta.lore();
            else lore = itemMeta.getLore().stream().map(l -> AdventureUtils.LEGACY_SERIALIZER.deserialize(l).asComponent()).toList();
        }

        unbreakable = itemMeta.isUnbreakable();
        unstackable = itemMeta.getPersistentDataContainer().has(UNSTACKABLE_KEY, DataType.UUID);

        if (!itemMeta.getItemFlags().isEmpty())
            itemFlags = itemMeta.getItemFlags();

        hasAttributeModifiers = itemMeta.hasAttributeModifiers();
        if (hasAttributeModifiers)
            attributeModifiers = itemMeta.getAttributeModifiers();


        customModelData = itemMeta.hasCustomModelData() ? itemMeta.getCustomModelData() : null;

        persistentDataContainer = itemMeta.getPersistentDataContainer();

        enchantments = new HashMap<>();

        if (VersionUtil.atleast("1.20.5")) {
            if (itemMeta.hasItemName()) {
                if (VersionUtil.isPaperServer()) itemName = itemMeta.itemName();
                else itemName = AdventureUtils.LEGACY_SERIALIZER.deserialize(itemMeta.getItemName());
            } else itemName = null;

            durability = (itemMeta instanceof Damageable damageable) && damageable.hasMaxDamage() ? damageable.getMaxDamage() : null;
            fireResistant = itemMeta.isFireResistant() ? true : null;
            hideToolTip = itemMeta.isHideTooltip() ? true : null;
            foodComponent = itemMeta.hasFood() ? itemMeta.getFood() : null;
            toolComponent = itemMeta.hasTool() ? itemMeta.getTool() : null;
            enchantmentGlintOverride = itemMeta.hasEnchantmentGlintOverride() ? itemMeta.getEnchantmentGlintOverride() : null;
            rarity = itemMeta.hasRarity() ? itemMeta.getRarity() : null;
            maxStackSize = itemMeta.hasMaxStackSize() ? itemMeta.getMaxStackSize() : null;
            if (maxStackSize != null && maxStackSize == 1) unstackable = true;
        }

        if (VersionUtil.atleast("1.21")) {
            jukeboxPlayable = itemMeta.hasJukeboxPlayable() ? itemMeta.getJukeboxPlayable() : null;
        }

        if (VersionUtil.atleast("1.21.2")) {
            equippableComponent = itemMeta.hasEquippable() ? itemMeta.getEquippable() : null;
            useCooldownComponent = itemMeta.hasUseCooldown() ? itemMeta.getUseCooldown() : null;
            useRemainder = itemMeta.hasUseRemainder() ? itemMeta.getUseRemainder() : null;
            damageResistant = itemMeta.hasDamageResistant() ? itemMeta.getDamageResistant() : null;
            itemModel = itemMeta.hasItemModel() ? itemMeta.getItemModel() : null;
            enchantable = itemMeta.hasEnchantable() ? itemMeta.getEnchantable() : null;
            isGlider = itemMeta.isGlider() ? true : null;
        }

    }

    public Material getType() {
        return type;
    }

    public ItemBuilder setType(final Material type) {
        this.type = type;
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        if (amount > type.getMaxStackSize())
            amount = type.getMaxStackSize();
        this.amount = amount;
        return this;
    }

    @Deprecated @Nullable
    public String getDisplayName() {
        return displayName != null ? AdventureUtils.MINI_MESSAGE.serialize(displayName) : null;
    }

    @Nullable
    public Component displayName() {
        return displayName;
    }

    @Deprecated
    public ItemBuilder setDisplayName(String displayName) {
        this.displayName = AdventureUtils.LEGACY_SERIALIZER.deserialize(displayName);
        return this;
    }
    public ItemBuilder displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public boolean hasItemName() {
        return itemName != null;
    }

    @Deprecated @Nullable
    public String getItemName() {
        return itemName != null ? AdventureUtils.MINI_MESSAGE.serialize(itemName) : null;
    }

    @Nullable
    public Component itemName() {
        return itemName;
    }

    @Deprecated
    public ItemBuilder setItemName(String itemName) {
        this.itemName = AdventureUtils.LEGACY_SERIALIZER.deserialize(itemName);
        return this;
    }

    public ItemBuilder itemName(Component itemName) {
        this.itemName = itemName;
        return this;
    }

    public boolean hasLores() {
        return lore != null && !lore.isEmpty();
    }

    @Deprecated
    public List<String> getLore() {
        return lore != null ? lore.stream().map(AdventureUtils.MINI_MESSAGE::serialize).toList() : new ArrayList<>();
    }

    public List<Component> lore() {
        return lore != null ? lore : new ArrayList<>();
    }

    @Deprecated
    public ItemBuilder setLore(final List<String> lore) {
        this.lore = lore.stream().map(l -> AdventureUtils.LEGACY_SERIALIZER.deserialize(l).asComponent()).toList();
        return this;
    }

    public ItemBuilder lore(final List<Component> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder setUnbreakable(final boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public boolean isUnstackable() {
        return unstackable;
    }

    public ItemBuilder setUnstackable(final boolean unstackable) {
        this.unstackable = unstackable;
        if (unstackable && VersionUtil.atleast("1.20.5")) maxStackSize = 1;
        return this;
    }

    @Nullable
    public Integer getDurability() {
        return durability;
    }

    public ItemBuilder setDurability(@Nullable Integer durability) {
        this.durability = durability;
        return this;
    }

    public boolean isDamagedOnBlockBreak() {
        return damagedOnBlockBreak;
    }

    public void setDamagedOnBlockBreak(boolean damagedOnBlockBreak) {
        this.damagedOnBlockBreak = damagedOnBlockBreak;
    }

    public boolean isDamagedOnEntityHit() {
        return damagedOnEntityHit;
    }

    public void setDamagedOnEntityHit(boolean damagedOnEntityHit) {
        this.damagedOnEntityHit = damagedOnEntityHit;
    }

    /**
     * Check if the ItemBuilder has color.
     *
     * @return true if the ItemBuilder has color that is not default LeatherMetaColor
     */
    public boolean hasColor() {
        return color != null && !color.equals(Bukkit.getItemFactory().getDefaultLeatherColor());
    }

    public Color getColor() {
        return color;
    }

    public ItemBuilder setColor(final Color color) {
        this.color = color;
        return this;
    }

    public boolean hasTrimPattern() {
        return trimPattern != null && getTrimPattern() != null;
    }

    @Nullable
    public Key getTrimPatternKey() {
        if (!Tag.ITEMS_TRIMMABLE_ARMOR.isTagged(type)) return null;
        return trimPattern;
    }

    @Nullable
    public TrimPattern getTrimPattern() {
        if (!Tag.ITEMS_TRIMMABLE_ARMOR.isTagged(type)) return null;
        if (trimPattern == null) return null;
        NamespacedKey key = NamespacedKey.fromString(trimPattern.asString());
        if (key == null) return null;
        return Registry.TRIM_PATTERN.get(key);
    }

    public ItemBuilder setTrimPattern(final Key trimKey) {
        if (!Tag.ITEMS_TRIMMABLE_ARMOR.isTagged(type)) return this;
        this.trimPattern = trimKey;
        return this;
    }

    public boolean hasItemModel() {
        return VersionUtil.atleast("1.21.2") && itemModel != null;
    }

    @Nullable
    public NamespacedKey getItemModel() {
        return itemModel;
    }

    public ItemBuilder setItemModel(final NamespacedKey itemModel) {
        this.itemModel = itemModel;
        return this;
    }

    public boolean hasTooltipStyle() {
        return VersionUtil.atleast("1.21.2") && tooltipStyle != null;
    }

    public NamespacedKey getTooltipStyle() {
        return tooltipStyle;
    }

    public ItemBuilder setTooltipStyle(NamespacedKey tooltipStyle) {
        this.tooltipStyle = tooltipStyle;
        return this;
    }

    public boolean hasEnchantable() {
        return VersionUtil.atleast("1.21.2") && enchantable != null;
    }

    @Nullable
    public Integer getEnchantable() {
        return enchantable;
    }

    public ItemBuilder setEnchantable(Integer enchantable) {
        this.enchantable = enchantable;
        return this;
    }

    public boolean hasDamageResistant() {
        return VersionUtil.atleast("1.21.2") && damageResistant != null;
    }

    public Tag<DamageType> getDamageResistant() {
        return damageResistant;
    }

    public ItemBuilder setDamageResistant(final Tag<DamageType> damageResistant) {
        this.damageResistant = damageResistant;
        return this;
    }

    public ItemBuilder setGlider(final boolean glider) {
        this.isGlider = glider;
        return this;
    }

    public boolean hasUseRemainder() {
        return VersionUtil.atleast("1.21.2") && useRemainder != null;
    }

    @Nullable
    public ItemStack getUseRemainder() {
        return useRemainder;
    }

    public ItemBuilder setUseRemainder(@Nullable final ItemStack itemStack) {
        this.useRemainder = itemStack;
        return this;
    }

    public boolean hasUseCooldownComponent() {
        return VersionUtil.atleast("1.21.2") && useCooldownComponent != null;
    }

    @Nullable
    public UseCooldownComponent getUseCooldownComponent() {
        return useCooldownComponent;
    }

    public ItemBuilder setUseCooldownComponent(@Nullable final UseCooldownComponent useCooldownComponent) {
        this.useCooldownComponent = useCooldownComponent;
        return this;
    }

    public boolean hasEquippableComponent() {
        return VersionUtil.atleast("1.21.2") && equippableComponent != null;
    }

    @Nullable
    public EquippableComponent getEquippableComponent() {
        return equippableComponent;
    }

    public ItemBuilder setEquippableComponent(@Nullable final EquippableComponent equippableComponent) {
        this.equippableComponent = equippableComponent;
        return this;
    }

    public boolean hasFoodComponent() {
        return VersionUtil.atleast("1.20.5") && foodComponent != null;
    }

    @Nullable
    public FoodComponent getFoodComponent() {
        return foodComponent;
    }

    public ItemBuilder setFoodComponent(@Nullable FoodComponent foodComponent) {
        this.foodComponent = foodComponent;
        return this;
    }

    public boolean hasToolComponent() {
        return VersionUtil.atleast("1.20.5") && toolComponent != null;
    }

    @Nullable
    public ToolComponent getToolComponent() {
        return toolComponent;
    }

    public ItemBuilder setToolComponent(@Nullable ToolComponent toolComponent) {
        this.toolComponent = toolComponent;
        return this;
    }

    public boolean hasJukeboxPlayable() {
        return VersionUtil.atleast("1.21") && jukeboxPlayable != null;
    }

    @Nullable
    public JukeboxPlayableComponent getJukeboxPlayable() {
        return jukeboxPlayable;
    }

    public ItemBuilder setJukeboxPlayable(@Nullable JukeboxPlayableComponent jukeboxPlayable) {
        this.jukeboxPlayable = jukeboxPlayable;
        return this;
    }

    public boolean hasEnchantmentGlindOverride() {
        return VersionUtil.atleast("1.20.5") && enchantmentGlintOverride != null;
    }

    @Nullable
    public Boolean getEnchantmentGlindOverride() {
        return enchantmentGlintOverride;
    }

    public ItemBuilder setEnchantmentGlindOverride(@Nullable Boolean enchantmentGlintOverride) {
        this.enchantmentGlintOverride = enchantmentGlintOverride;
        return this;
    }

    public boolean hasRarity() {
        return VersionUtil.atleast("1.20.5") && rarity != null;
    }

    @Nullable
    public ItemRarity getRarity() {
        return rarity;
    }

    public ItemBuilder setRarity(@Nullable ItemRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public ItemBuilder setFireResistant(boolean fireResistant) {
        this.fireResistant = fireResistant;
        return this;
    }

    public ItemBuilder setHideToolTip(boolean hideToolTip) {
        this.hideToolTip = hideToolTip;
        return this;
    }

    public boolean hasMaxStackSize() {
        return VersionUtil.atleast("1.20.5") && maxStackSize != null;
    }

    @Nullable
    public Integer maxStackSize() {
        return maxStackSize;
    }


    public ItemBuilder maxStackSize(@Nullable Integer maxStackSize) {
        this.maxStackSize = maxStackSize;
        this.setUnstackable(maxStackSize != null && maxStackSize == 1);
        return this;
    }

    public ItemBuilder basePotionType(final PotionType potionType) {
        this.basePotionType = potionType;
        return this;
    }

    public ItemBuilder addPotionEffect(final PotionEffect potionEffect) {
        if (customPotionEffects == null)
            customPotionEffects = new ArrayList<>();
        customPotionEffects.add(potionEffect);
        return this;
    }

    public <T, Z> ItemBuilder customTag(final NamespacedKey namespacedKey, final PersistentDataType<T, Z> dataType, final Z data) {
        persistentDataMap.put(new PersistentDataSpace(namespacedKey, dataType), data);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T, Z> Z customTag(final NamespacedKey namespacedKey, final PersistentDataType<T, Z> dataType) {
        for (final Map.Entry<PersistentDataSpace, Object> dataSpace : persistentDataMap.entrySet())
            if (dataSpace.getKey().namespacedKey().equals(namespacedKey)
                    && dataSpace.getKey().dataType().equals(dataType))
                return (Z) dataSpace.getValue();
        return null;
    }

    public boolean hasCustomTag() {
        return !persistentDataContainer.isEmpty();
    }


    public <T, Z> void addCustomTag(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        persistentDataContainer.set(key, type, value);
    }

    public ItemBuilder removeCustomTag(NamespacedKey key) {
        persistentDataContainer.remove(key);
        return this;
    }

    public ItemBuilder customModelData(final int customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    public ItemBuilder addItemFlags(final ItemFlag... itemFlags) {
        if (this.itemFlags == null)
            this.itemFlags = new HashSet<>();
        this.itemFlags.addAll(Arrays.asList(itemFlags));
        return this;
    }

    public List<ItemFlag> itemFlags() {
        return itemFlags != null ? new ArrayList<>(itemFlags) : new ArrayList<>();
    }

    public ItemBuilder addAttributeModifiers(final Attribute attribute, final AttributeModifier attributeModifier) {
        if (!hasAttributeModifiers) {
            hasAttributeModifiers = true;
            attributeModifiers = HashMultimap.create();
        }
        attributeModifiers.put(attribute, attributeModifier);
        return this;
    }

    public ItemBuilder addAttributeModifiers(final Multimap<Attribute, AttributeModifier> attributeModifiers) {
        if (!hasAttributeModifiers)
            hasAttributeModifiers = true;
        this.attributeModifiers.putAll(attributeModifiers);
        return this;
    }

    public ItemBuilder addEnchant(final Enchantment enchant, final int level) {
        enchantments.put(enchant, level);
        return this;
    }

    public ItemBuilder addEnchants(final Map<Enchantment, Integer> enchants) {
        for (final Map.Entry<Enchantment, Integer> enchant : enchants.entrySet())
            addEnchant(enchant.getKey(), enchant.getValue());
        return this;
    }

    public boolean hasNexoMeta() {
        return nexoMeta != null;
    }

    public NexoMeta nexoMeta() {
        return nexoMeta;
    }

    public ItemBuilder nexoMeta(final NexoMeta itemResources) {
        nexoMeta = itemResources;
        return this;
    }

    public ItemStack referenceClone() {
        return itemStack.clone();
    }

    public ItemBuilder clone() {
        return new ItemBuilder(itemStack.clone());
    }

    @SuppressWarnings("unchecked")
    public ItemBuilder regen() {
        final ItemStack itemStack = this.itemStack;
        if (type != null)
            itemStack.setType(type);
        if (amount != itemStack.getAmount())
            itemStack.setAmount(amount);

        ItemMeta itemMeta = itemStack.getItemMeta();

        // 1.20.5+ properties
        if (VersionUtil.atleast("1.20.5")) {
            if (itemMeta instanceof Damageable damageable) damageable.setMaxDamage(durability);
            if (itemName != null) {
                if (VersionUtil.isPaperServer()) itemMeta.itemName(itemName);
                else itemMeta.setItemName(AdventureUtils.LEGACY_SERIALIZER.serialize(itemName));
            }
            if (hasMaxStackSize()) itemMeta.setMaxStackSize(maxStackSize);
            if (hasEnchantmentGlindOverride()) itemMeta.setEnchantmentGlintOverride(enchantmentGlintOverride);
            if (hasRarity()) itemMeta.setRarity(rarity);
            if (hasFoodComponent()) itemMeta.setFood(foodComponent);
            if (hasToolComponent()) itemMeta.setTool(toolComponent);
            if (fireResistant != null) itemMeta.setFireResistant(fireResistant);
            if (hideToolTip != null) itemMeta.setHideTooltip(hideToolTip);
        }

        if (VersionUtil.atleast("1.21")) {
            if (hasJukeboxPlayable()) itemMeta.setJukeboxPlayable(jukeboxPlayable);
        }

        if (VersionUtil.atleast("1.21.2")) {
            if (hasEquippableComponent()) itemMeta.setEquippable(equippableComponent);
            if (hasUseCooldownComponent()) itemMeta.setUseCooldown(useCooldownComponent);
            if (hasDamageResistant()) itemMeta.setDamageResistant(damageResistant);
            if (hasTooltipStyle()) itemMeta.setTooltipStyle(tooltipStyle);
            if (hasUseRemainder()) itemMeta.setUseRemainder(useRemainder);
            if (hasEnchantable()) itemMeta.setEnchantable(enchantable);
            if (itemModel != null) itemMeta.setItemModel(itemModel);
            if (isGlider != null) itemMeta.setGlider(isGlider);
        }

        handleVariousMeta(itemMeta);
        itemMeta.setUnbreakable(unbreakable);

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        if (displayName != null) {
            if (VersionUtil.below("1.20.5")) pdc.set(ORIGINAL_NAME_KEY, DataType.STRING, AdventureUtils.MINI_MESSAGE.serialize(displayName));
            if (VersionUtil.isPaperServer()) {
                itemMeta.displayName(displayName
                        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .colorIfAbsent(NamedTextColor.WHITE)
                );
            } else itemMeta.setDisplayName(AdventureUtils.LEGACY_SERIALIZER.serialize(displayName));
        }

        if (enchantments.size() > 0) {
            for (final Map.Entry<Enchantment, Integer> enchant : enchantments.entrySet()) {
                if (enchant.getKey() == null) continue;
                int lvl = enchant.getValue() != null ? enchant.getValue() : 1;
                itemMeta.addEnchant(enchant.getKey(), lvl, true);
            }
        }

        if (itemFlags != null) itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
        if (hasAttributeModifiers) itemMeta.setAttributeModifiers(attributeModifiers);
         itemMeta.setCustomModelData(customModelData);

        if (!persistentDataMap.isEmpty())
            for (final Map.Entry<PersistentDataSpace, Object> dataSpace : persistentDataMap.entrySet())
                pdc.set(dataSpace.getKey().namespacedKey(), (PersistentDataType<?, Object>) dataSpace.getKey().dataType(), dataSpace.getValue());

        ItemUtils.lore(itemMeta, lore);

        itemStack.setItemMeta(itemMeta);

        finalItemStack = itemStack;
        return this;
    }

    public void save() {
        regen();
        NexoItems.map().entrySet().stream().filter(entry -> entry.getValue().containsValue(this)).findFirst().ifPresent(entry -> {
            YamlConfiguration yamlConfiguration = NexoYaml.loadConfiguration(entry.getKey());
            String itemId = NexoItems.idByItem(this);
            if (this.hasColor()) {
                String color = this.color.getRed() + "," + this.color.getGreen() + "," + this.color.getBlue();
                yamlConfiguration.set(itemId + ".color", color);
            }
            if (this.hasTrimPattern()) {
                String trimPattern = this.getTrimPatternKey().asString();
                yamlConfiguration.set(itemId + ".trim_pattern", trimPattern);
            }
            if (!itemFlags.isEmpty()) yamlConfiguration.set(itemId + ".ItemFlags", this.itemFlags.stream().map(ItemFlag::name).toList());
            if (hasEquippableComponent()) {
                yamlConfiguration.set(itemId + ".Components.equippable.slot", this.equippableComponent.getSlot().name());
                yamlConfiguration.set(itemId + ".Components.equippable.model", this.equippableComponent.getModel().toString());
            }
            try {
                yamlConfiguration.save(entry.getKey());
            } catch (IOException e) {
                if (Settings.DEBUG.toBool()) e.printStackTrace();
            }
        });
    }

    private void handleVariousMeta(ItemMeta itemMeta) {
        if (itemMeta instanceof LeatherArmorMeta leatherArmorMeta && color != null && !color.equals(leatherArmorMeta.getColor())) {
            leatherArmorMeta.setColor(color);
        } else if (itemMeta instanceof PotionMeta potionMeta) {
            handlePotionMeta(potionMeta);
        } else if (itemMeta instanceof MapMeta mapMeta && color != null && !color.equals(mapMeta.getColor())) {
            mapMeta.setColor(color);
        } else if (itemMeta instanceof FireworkEffectMeta effectMeta) {
            FireworkEffect.Builder fireWorkBuilder = effectMeta.clone().hasEffect() ? effectMeta.getEffect().builder() : FireworkEffect.builder();
            if (color != null) fireWorkBuilder.withColor(color);

            // If both above fail, the below will throw an exception as builder needs atleast one color
            // If so return the base meta
            try {
                effectMeta.setEffect(fireWorkBuilder.build());
            } catch (IllegalStateException ignored) {
            }
        } else if (itemMeta instanceof ArmorMeta armorMeta && hasTrimPattern()) {
            armorMeta.setTrim(new ArmorTrim(TrimMaterial.REDSTONE, getTrimPattern()));
        }
    }

    private ItemMeta handlePotionMeta(PotionMeta potionMeta) {
        if (color != null && !color.equals(potionMeta.getColor()))
            potionMeta.setColor(color);

        if (basePotionType != null && !basePotionType.equals(potionMeta.getBasePotionType()))
            potionMeta.setBasePotionType(basePotionType);

        if (!customPotionEffects.equals(potionMeta.getCustomEffects()))
            for (final PotionEffect potionEffect : customPotionEffects)
                potionMeta.addCustomEffect(potionEffect, true);

        return potionMeta;
    }

    public ItemStack[] buildArray(final int amount) {
        final ItemStack built = build();
        final int max = hasMaxStackSize() ? maxStackSize : type != null ? type.getMaxStackSize() : itemStack.getType().getMaxStackSize();
        final int rest = max == amount ? amount : amount % max;
        final int iterations = amount > max ? (amount - rest) / max : 0;
        final ItemStack[] output = new ItemStack[iterations + (rest > 0 ? 1 : 0)];
        for (int index = 0; index < iterations; index++) {
            ItemStack clone = built.clone();
            clone.setAmount(max);
            if (unstackable) clone = handleUnstackable(clone);
            output[index] = ItemUpdater.updateItem(clone);
        }
        if (rest != 0) {
            ItemStack clone = built.clone();
            clone.setAmount(rest);
            if (unstackable) clone = handleUnstackable(clone);
            output[iterations] = ItemUpdater.updateItem(clone);
        }
        return output;
    }

    public ItemStack build() {
        if (finalItemStack == null) regen();
        if (unstackable) return handleUnstackable(finalItemStack);
        else return finalItemStack.clone();
    }

    private ItemStack handleUnstackable(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || VersionUtil.atleast("1.20.5")) return item;
        meta.getPersistentDataContainer().set(UNSTACKABLE_KEY, DataType.UUID, UUID.randomUUID());
        item.setItemMeta(meta);
        item.setAmount(1);
        return item;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
