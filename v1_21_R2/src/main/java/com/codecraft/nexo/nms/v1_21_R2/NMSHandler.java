package com.codecraft.nexo.nms.v1_21_R2;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.items.ItemBuilder;
import com.codecraft.nexo.mechanics.furniture.IFurniturePacketManager;
import com.codecraft.nexo.nms.GlyphHandler;
import com.codecraft.nexo.nms.v1_21_R2.furniture.FurniturePacketManager;
import com.codecraft.nexo.pack.server.NexoPackServer;
import com.codecraft.nexo.utils.BlockHelpers;
import com.codecraft.nexo.utils.InteractionResult;
import com.codecraft.nexo.utils.VersionUtil;
import com.codecraft.nexo.utils.logs.Logs;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.consume_effects.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class NMSHandler implements com.codecraft.nexo.nms.NMSHandler {

    private final GlyphHandler glyphHandler;
    private final FurniturePacketManager furniturePacketManager = new FurniturePacketManager();

    public NMSHandler() {
        this.glyphHandler = new com.codecraft.nexo.nms.v1_21_R2.GlyphHandler();
    }

    @Override
    public GlyphHandler glyphHandler() {
        return glyphHandler;
    }

    @Override
    public IFurniturePacketManager furniturePacketManager() {
        return furniturePacketManager;
    }

    private static Field serverResourcePackInfo;

    static {
        try {
            serverResourcePackInfo = DedicatedServerProperties.class.getDeclaredField("serverResourcePackInfo");
            serverResourcePackInfo.setAccessible(true);
        } catch (Exception ignored) {}
    }

    @Override
    public void setServerResourcePack() {
        DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getHandle().getServer();
        NexoPackServer packServer = NexoPlugin.get().packServer();
        try {
            serverResourcePackInfo.set(dedicatedServer.settings.getProperties(), Optional.ofNullable(packServer.packInfo()).map(packInfo ->
                    new MinecraftServer.ServerResourcePackInfo(
                            packInfo.id(), packServer.packUrl(), packInfo.hash(), packServer.mandatory,
                            PaperAdventure.asVanilla(packServer.prompt)
                    )
            ));
        } catch (IllegalAccessException e) {
            Logs.logWarning("Failed to set ServerResourcePack...");
            if (Settings.DEBUG.toBool()) e.printStackTrace();
        }
    }

    @Override
    public boolean tripwireUpdatesDisabled() {
        return VersionUtil.isPaperServer() && GlobalConfiguration.get().blockUpdates.disableTripwireUpdates;
    }

    @Override
    public boolean noteblockUpdatesDisabled() {
        return VersionUtil.isPaperServer() && GlobalConfiguration.get().blockUpdates.disableNoteblockUpdates;
    }

    @Override
    public ItemStack copyItemNBTTags(@NotNull ItemStack oldItem, @NotNull ItemStack newItem) {
        //Converts to minecraft ItemStack
        net.minecraft.world.item.ItemStack newNmsItem = CraftItemStack.asNMSCopy(newItem);
        net.minecraft.world.item.ItemStack oldItemStack = CraftItemStack.asNMSCopy(oldItem);

        //Gets data component's nbt data.
        DataComponentType<CustomData> type = DataComponents.CUSTOM_DATA;
        CustomData oldData = oldItemStack.getComponents().get(type);
        CustomData newData = newNmsItem.getComponents().get(type);

        //Cancels if null.
        if (oldData == null || newData == null) return newItem;
        //Creates new nbt compound.
        CompoundTag oldTag = oldData.copyTag();
        CompoundTag newTag = newData.copyTag();

        for (String key : oldTag.getAllKeys()) {
            if (vanillaKeys.contains(key)) continue;
            Tag value = oldTag.get(key);
            if (value != null) newTag.put(key, value);
            else newTag.remove(key);
        }

        newNmsItem.set(type, CustomData.of(newTag));
        return CraftItemStack.asBukkitCopy(newNmsItem);
    }

    @Override
    @Nullable
    public InteractionResult correctBlockStates(Player player, EquipmentSlot slot, ItemStack itemStack) {
        InteractionHand hand = slot == EquipmentSlot.HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        BlockHitResult hitResult = getPlayerPOVHitResult(serverPlayer.level(), serverPlayer, ClipContext.Fluid.NONE);
        BlockPlaceContext placeContext = new BlockPlaceContext(serverPlayer.level(), serverPlayer, hand, nmsStack, hitResult);

        if (!(nmsStack.getItem() instanceof BlockItem blockItem)) {
            InteractionResult result = InteractionResult.fromNms(nmsStack.getItem().useOn(new UseOnContext(serverPlayer, hand, hitResult)).getClass());
            return player.isSneaking() && player.getGameMode() != GameMode.CREATIVE ? result
                    : InteractionResult.fromNms(serverPlayer.gameMode.useItem(serverPlayer, serverPlayer.level(), nmsStack, hand).getClass());
        }

        InteractionResult result = InteractionResult.fromNms(blockItem.place(placeContext).getClass());
        if (result == InteractionResult.FAIL) return null;

        if (!player.isSneaking()) {
            World world = player.getWorld();
            BlockPos clickPos = placeContext.getClickedPos();
            Block block = world.getBlockAt(clickPos.getX(), clickPos.getY(), clickPos.getZ());
            SoundGroup sound = block.getBlockData().getSoundGroup();

            world.playSound(
                    BlockHelpers.toCenterBlockLocation(block.getLocation()), sound.getPlaceSound(),
                    SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F
            );
        }

        return result;
    }

    public BlockHitResult getPlayerPOVHitResult(Level world, net.minecraft.world.entity.player.Player player, ClipContext.Fluid fluidHandling) {
        float f = player.getXRot();
        float g = player.getYRot();
        Vec3 vec3 = player.getEyePosition();
        float h = Mth.cos(-g * ((float) Math.PI / 180F) - (float) Math.PI);
        float i = Mth.sin(-g * ((float) Math.PI / 180F) - (float) Math.PI);
        float j = -Mth.cos(-f * ((float) Math.PI / 180F));
        float k = Mth.sin(-f * ((float) Math.PI / 180F));
        float l = i * j;
        float n = h * j;
        double d = 5.0D;
        Vec3 vec32 = vec3.add((double) l * d, (double) k * d, (double) n * d);
        return world.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluidHandling, player));
    }

    @Override
    public int playerProtocolVersion(Player player) {
        return ((CraftPlayer) player).getHandle().connection.connection.protocolVersion;
    }

    @Override
    public boolean getSupported() {
        return true;
    }

    @Override
    public String getNoteBlockInstrument(Block block) {
        return ((CraftBlock) block).getNMS().instrument().toString();
    }

    @Override
    public int mcmetaVersion() {
        return 34;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void foodComponent(ItemBuilder item, ConfigurationSection foodSection) {
        FoodComponent foodComponent = new ItemStack(item.getType()).getItemMeta().getFood();
        foodComponent.setNutrition(foodSection.getInt("nutrition"));
        foodComponent.setSaturation((float) foodSection.getDouble("saturation", 0.0));
        foodComponent.setCanAlwaysEat(foodSection.getBoolean("can_always_eat"));

        item.setFoodComponent(foodComponent);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void consumableComponent(ItemBuilder item, ConfigurationSection section) {

        Consumable.Builder consumable = Consumable.builder();
        Consumable template = Optional.ofNullable(CraftItemStack.asNMSCopy(new ItemStack(item.getType())).getComponents().get(DataComponents.CONSUMABLE)).orElse(Consumable.builder().build());

        consumable.consumeSeconds((float) section.getDouble("consume_seconds", template.consumeSeconds()));
        consumable.animation(Optional.ofNullable(EnumUtils.getEnum(ItemUseAnimation.class, section.getString("animation"))).orElse(template.animation()));
        consumable.hasConsumeParticles(section.getBoolean("consume_particles", template.hasConsumeParticles()));
        consumable.sound(template.sound());

        List<Map<?, ?>> effectsMap = section.getMapList("effects");
        if (effectsMap.isEmpty()) for (ConsumeEffect effect : template.onConsumeEffects()) consumable.onConsume(effect);
        else for (Map<?, ?> effectSection : effectsMap) {
            String type = Optional.ofNullable(effectSection.get("type")).map(Object::toString).orElse("");
            if (type.equals("APPLY_EFFECTS") && effectSection.getOrDefault("effects", null) instanceof Map<?, ?> effects) {
                for (Map.Entry<String, ConfigurationSection> effectMap : effects.entrySet().stream().map(o -> (Map.Entry<String, ConfigurationSection>) o).collect(Collectors.toSet())) {
                    ConfigurationSection applyEffectSection = effectMap.getValue();
                    Key effect;
                    try {
                        effect = Key.key(effectMap.getKey());
                    } catch (Exception e) {
                        Logs.logError("Invalid potion effect: " + effectMap.getKey() + ", in consumable-property!");
                        if (Settings.DEBUG.toBool()) e.printStackTrace();
                        continue;
                    }

                    Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT).get(effect))
                            .map(CraftPotionEffectType::bukkitToMinecraft)
                            .map(Holder::direct)
                            .ifPresentOrElse(mobEffect -> {
                                int duration = applyEffectSection.getInt("duration", 1) * 20;
                                int amplifier = applyEffectSection.getInt("amplifier", 0);
                                boolean ambient = applyEffectSection.getBoolean("ambient", true);
                                boolean particles = applyEffectSection.getBoolean("show_particles", true);
                                boolean icon = applyEffectSection.getBoolean("show_icon", true);
                                float probability = (float) applyEffectSection.getDouble("probability", 1.0);
                                MobEffectInstance instance = new MobEffectInstance(mobEffect, duration, amplifier, ambient, particles, icon);

                                consumable.onConsume(new ApplyStatusEffectsConsumeEffect(instance, probability));
                            }, () -> Logs.logError("Invalid potion effect: " + effect.asString() + ", in consumable-property!"));
                }
            } else if (type.equals("REMOVE_EFFECTS") && effectSection.getOrDefault("effects", null) instanceof ArrayList<?> effects) {
                List<Holder<MobEffect>> mobEffects = new ArrayList<>();
                for (Object object : effects) {
                    Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT).get(Key.key(String.valueOf(object))))
                            .map(CraftPotionEffectType::bukkitToMinecraft)
                            .map(Holder::direct)
                            .ifPresent(mobEffects::add);
                }
                consumable.onConsume(new RemoveStatusEffectsConsumeEffect(HolderSet.direct(mobEffects)));
            } else if (type.equals("CLEAR_ALL_EFFECTS")) {
                consumable.onConsume(new ClearAllStatusEffectsConsumeEffect());
            } else if (type.equals("TELEPORT_RANDOMLY")) {
                float diameter = (effectSection.getOrDefault("diameter", null) instanceof Float d) ? d : 16f;
                consumable.onConsume(new TeleportRandomlyConsumeEffect(diameter));
            } else if (type.equals("PLAY_SOUND")) {
                try {
                    Key soundKey = Key.key(String.valueOf(effectSection.getOrDefault("sound", null)));
                    Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).get(soundKey))
                            .map(CraftSound::bukkitToMinecraft)
                            .map(Holder::direct)
                            .ifPresent(sound -> consumable.onConsume(new PlaySoundConsumeEffect(sound)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else Logs.logWarning("Invalid ConsumeEffect-Type " + type);
        }
    }
}
