package com.codecraft.nexo.mechanics.furniture;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.compatibilities.provided.blocklocker.BlockLockerMechanic;
import com.codecraft.nexo.items.ItemBuilder;
import com.codecraft.nexo.mechanics.BreakableMechanic;
import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.furniture.evolution.EvolvingFurniture;
import com.codecraft.nexo.mechanics.furniture.hitbox.FurnitureHitbox;
import com.codecraft.nexo.mechanics.furniture.jukebox.JukeboxBlock;
import com.codecraft.nexo.mechanics.furniture.seats.FurnitureSeat;
import com.codecraft.nexo.mechanics.light.LightMechanic;
import com.codecraft.nexo.mechanics.limitedplacing.LimitedPlacing;
import com.codecraft.nexo.mechanics.storage.StorageMechanic;
import com.codecraft.nexo.utils.BlockHelpers;
import com.codecraft.nexo.utils.EntityUtils;
import com.codecraft.nexo.utils.ItemUtils;
import com.codecraft.nexo.utils.PluginUtils;
import com.codecraft.nexo.utils.actions.ClickAction;
import com.codecraft.nexo.utils.blocksounds.BlockSounds;
import com.codecraft.nexo.utils.logs.Logs;
import com.jeff_media.morepersistentdatatypes.DataType;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FurnitureMechanic extends Mechanic {

    public static final NamespacedKey FURNITURE_KEY = new NamespacedKey(NexoPlugin.get(), "furniture");
    public static final NamespacedKey FURNITURE_DYE_KEY = new NamespacedKey(NexoPlugin.get(), "furniture_dye");
    public static final NamespacedKey MODELENGINE_KEY = new NamespacedKey(NexoPlugin.get(), "modelengine");
    public static final NamespacedKey EVOLUTION_KEY = new NamespacedKey(NexoPlugin.get(), "evolution");


    private final LimitedPlacing limitedPlacing;
    private final StorageMechanic storage;
    private final BlockSounds blockSounds;
    private final JukeboxBlock jukebox;
    public final boolean farmlandRequired;
    private final EvolvingFurniture evolvingFurniture;
    private final LightMechanic light;
    private final String modelEngineID;
    private final String placedItemId;
    private final List<FurnitureSeat> seats = new ArrayList<>();
    private final List<ClickAction> clickActions;
    private final FurnitureProperties furnitureProperties;
    private final boolean isRotatable;
    private final BlockLockerMechanic blockLocker;
    private final RestrictedRotation restrictedRotation;
    private final BreakableMechanic breakable;

    @NotNull
    private final FurnitureHitbox hitbox;

    public enum RestrictedRotation {
        NONE, STRICT, VERY_STRICT;

        public static RestrictedRotation fromString(String string) {
            return Arrays.stream(RestrictedRotation.values())
                    .filter(e -> e.name().equals(string))
                    .findFirst()
                    .orElseGet(() -> {
                        Logs.logError("Invalid restricted rotation: " + string);
                        Logs.logError("Allowed ones are: " + Arrays.toString(RestrictedRotation.values()));
                        Logs.logWarning("Setting to STRICT");
                        return STRICT;
                    });
        }
    }

    public FurnitureMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section, itemBuilder -> itemBuilder.customTag(FURNITURE_KEY, PersistentDataType.BYTE, (byte) 1));

        placedItemId = section.getString("item", "");
        modelEngineID = section.getString("modelengine_id", null);
        farmlandRequired = section.getBoolean("farmland_required", false);
        light = new LightMechanic(section);
        breakable = new BreakableMechanic(section);
        restrictedRotation = RestrictedRotation.fromString(section.getString("restricted_rotation", "STRICT"));
        furnitureProperties = new FurnitureProperties(section.getConfigurationSection("properties"));

        ConfigurationSection hitboxSection = section.getConfigurationSection("hitbox");
        hitbox = hitboxSection != null ? new FurnitureHitbox(hitboxSection) : FurnitureHitbox.EMPTY;

        for (Object seatEntry : section.getList("seats", new ArrayList<>())) {
            FurnitureSeat seat = FurnitureSeat.getSeat(seatEntry);
            if (seat == null) continue;
            seats.add(seat);
        }

        ConfigurationSection evoSection = section.getConfigurationSection("evolution");
        evolvingFurniture = evoSection != null ? new EvolvingFurniture(getItemID(), evoSection) : null;
        if (evolvingFurniture != null) ((FurnitureFactory) getFactory()).registerEvolution();

        ConfigurationSection limitedPlacingSection = section.getConfigurationSection("limited_placing");
        limitedPlacing = limitedPlacingSection != null ? new LimitedPlacing(limitedPlacingSection) : null;

        ConfigurationSection storageSection = section.getConfigurationSection("storage");
        storage = storageSection != null ? new StorageMechanic(storageSection) : null;

        ConfigurationSection blockSoundsSection = section.getConfigurationSection("block_sounds");
        blockSounds = blockSoundsSection != null ? new BlockSounds(blockSoundsSection) : null;

        ConfigurationSection jukeboxSection = section.getConfigurationSection("jukebox");
        jukebox = jukeboxSection != null ? new JukeboxBlock(jukeboxSection) : null;

        clickActions = ClickAction.parseList(section);

        if (section.getBoolean("rotatable", false)) {
            if (hitbox.barrierHitboxes().stream().anyMatch(b -> b.getX() != 0 || b.getZ() != 0)) {
                Logs.logWarning("Furniture <gold>" + getItemID() + " </gold>has barriers with non-zero X or Z coordinates.");
                Logs.logWarning("Furniture rotation will be disabled for this furniture.");
                isRotatable = false;
            } else isRotatable = true;
        } else isRotatable = false;

        ConfigurationSection blockLockerSection = section.getConfigurationSection("blocklocker");
        blockLocker = blockLockerSection != null ? new BlockLockerMechanic(blockLockerSection) : null;
    }

    public boolean isModelEngine() {
        return modelEngineID != null;
    }

    public String getModelEngineID() {
        return modelEngineID;
    }

    public boolean hasLimitedPlacing() {
        return limitedPlacing != null;
    }

    public LimitedPlacing limitedPlacing() {
        return limitedPlacing;
    }

    public boolean isStorage() {
        return storage != null;
    }

    public StorageMechanic storage() {
        return storage;
    }

    public boolean hasBlockSounds() {
        return blockSounds != null;
    }

    public BlockSounds blockSounds() {
        return blockSounds;
    }

    public boolean isJukebox() {
        return jukebox != null;
    }

    public JukeboxBlock jukebox() {
        return jukebox;
    }

    public FurnitureHitbox hitbox() {
        return hitbox;
    }

    public boolean hasSeats() {
        return !seats.isEmpty();
    }

    public List<FurnitureSeat> seats() {
        return seats;
    }

    public BreakableMechanic breakable() {
        return breakable;
    }

    public boolean hasEvolution() {
        return evolvingFurniture != null;
    }

    public EvolvingFurniture evolution() {
        return evolvingFurniture;
    }

    public boolean isRotatable() {
        return isRotatable;
    }

    public boolean isInteractable() {
        return isRotatable || hasSeats() || isStorage();
    }
    public Entity place(Location location) {
        return place(location, 0f, BlockFace.NORTH, true);
    }

    public ItemDisplay place(Location location, float yaw, BlockFace facing) {
        return place(location, yaw, facing, true);
    }

    public ItemDisplay place(Location location, float yaw, BlockFace facing, boolean checkSpace) {
        if (!location.isWorldLoaded()) return null;
        if (checkSpace && this.notEnoughSpace(location, yaw)) return null;
        assert location.getWorld() != null;

        ItemStack item = NexoItems.optionalItemById(placedItemId).orElse(NexoItems.itemById(getItemID())).build().clone();
        ItemUtils.editItemMeta(item, meta -> ItemUtils.displayName(meta, Component.empty()));
        item.setAmount(1);

        ItemDisplay baseEntity = location.getWorld().spawn(correctedSpawnLocation(location, facing), ItemDisplay.class, e -> setBaseFurnitureData(e, yaw, facing));

        if (this.isModelEngine() && PluginUtils.isEnabled("ModelEngine")) spawnModelEngineFurniture(baseEntity);
        FurnitureSeat.spawnSeats(baseEntity, this);

        return baseEntity;
    }

    private Location correctedSpawnLocation(Location baseLocation, BlockFace facing) {
        boolean isWall = hasLimitedPlacing() && limitedPlacing.isWall();
        boolean isRoof = hasLimitedPlacing() && limitedPlacing.isRoof();
        boolean isFixed = hasSpecifiedProperties() && furnitureProperties.displayTransform() == ItemDisplay.ItemDisplayTransform.FIXED;
        Location correctedLocation = (isFixed && facing == BlockFace.UP) ? BlockHelpers.toCenterBlockLocation(baseLocation) : BlockHelpers.toCenterLocation(baseLocation);

        if (!hasSpecifiedProperties()) return correctedLocation;
        if (furnitureProperties.isNoneTransform() && !isWall && !isRoof) return correctedLocation;
        float scale = furnitureProperties.scale().y();
        // Since roof-furniture need to be more or less flipped, we have to add 0.5 (0.49 or it is "inside" the block above) to the Y coordinate
        if (isFixed && isWall && facing.getModY() == 0) correctedLocation.add(-facing.getModX() * (0.49 * scale), 0, -facing.getModZ() * (0.49 * scale));

        float hitboxOffset = (float) (hitbox().hitboxHeight() - 1);
        double yCorrection = (isRoof && facing == BlockFace.DOWN ? isFixed ? 0.49 : -1 * hitboxOffset : 0);

        return correctedLocation.add(0,  yCorrection, 0);
    }

    public void setBaseFurnitureData(@NotNull ItemDisplay baseEntity, float yaw, BlockFace blockFace) {
        baseEntity.setPersistent(true);
        baseEntity.setInvulnerable(true);
        baseEntity.setSilent(true);
        baseEntity.setCustomNameVisible(false);
        ItemBuilder i = NexoItems.itemById(getItemID());
        Component customName = Optional.ofNullable(i.itemName()).orElse(Optional.ofNullable(i.displayName()).orElse(Component.text(getItemID())));
        EntityUtils.customName(baseEntity, customName);

        float pitch;
        if (hasLimitedPlacing() && furnitureProperties.isFixedTransform()) {
            if (limitedPlacing.isFloor() && blockFace == BlockFace.UP) pitch = -90;
            else if (limitedPlacing.isRoof() && blockFace == BlockFace.DOWN) pitch = 90;
            else pitch = 0;

            if (limitedPlacing.isWall() && blockFace.getModY() == 0) yaw = 90f * blockFace.ordinal() - 180;
        } else pitch = 0;

        baseEntity.setRotation(yaw, pitch);

        PersistentDataContainer pdc = baseEntity.getPersistentDataContainer();
        pdc.set(FURNITURE_KEY, PersistentDataType.STRING, getItemID());
        if (hasEvolution()) pdc.set(EVOLUTION_KEY, PersistentDataType.INTEGER, 0);
        if (isStorage() && storage().getStorageType() == StorageMechanic.StorageType.STORAGE) {
            pdc.set(StorageMechanic.STORAGE_KEY, DataType.ITEM_STACK_ARRAY, new ItemStack[]{});
        }
    }

    private void spawnModelEngineFurniture(@NotNull ItemDisplay entity) {
        ModeledEntity modelEntity = ModelEngineAPI.getOrCreateModeledEntity(entity);
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(getModelEngineID());
        modelEntity.addModel(activeModel, true);
        modelEntity.setModelRotationLocked(false);
        modelEntity.setBaseEntityVisible(false);
    }

    public void removeBaseEntity(@NotNull ItemDisplay baseEntity) {
        if (hasSeats()) removeFurnitureSeats(baseEntity);
        IFurniturePacketManager packetManager = FurnitureFactory.instance.packetManager();
        packetManager.removeFurnitureEntityPacket(baseEntity, this);
        packetManager.removeInteractionHitboxPacket(baseEntity, this);
        packetManager.removeBarrierHitboxPacket(baseEntity, this);
        packetManager.removeLightMechanicPacket(baseEntity, this);

        if (!baseEntity.isDead()) baseEntity.remove();
    }

    private void removeFurnitureSeats(ItemDisplay baseEntity) {
        List<Entity> seats = baseEntity.getPersistentDataContainer()
                .getOrDefault(FurnitureSeat.SEAT_KEY, DataType.asList(DataType.UUID), new ArrayList<>())
                .stream().map(Bukkit::getEntity).filter(Objects::nonNull).filter(e -> e instanceof Interaction).toList();

        for (Entity seat : seats) {
            seat.getPassengers().forEach(seat::removePassenger);
            if (!seat.isDead()) seat.remove();
        }
    }

    public boolean notEnoughSpace(Location rootLocation, float yaw) {
        List<Location> hitboxLocations = hitbox.hitboxLocations(rootLocation.clone(), yaw);
        if (!hitboxLocations.isEmpty()) return !hitboxLocations.stream().allMatch(l -> l.getBlock().isReplaceable());
        else return false;
    }

    public void runClickActions(final Player player) {
        for (final ClickAction action : clickActions) if (action.canRun(player)) action.performActions(player);
    }

    @Nullable
    public ItemDisplay baseEntity(Block block) {
        if (block == null) return null;
        return FurnitureFactory.instance.packetManager().baseEntityFromHitbox(new BlockLocation(block.getLocation()));
    }

    @Nullable
    public ItemDisplay baseEntity(Location location) {
        if (location == null) return null;
        BlockLocation blockLocation = new BlockLocation(location);
        return FurnitureFactory.instance.packetManager().baseEntityFromHitbox(blockLocation);
    }

    @Nullable
    ItemDisplay baseEntity(BlockLocation blockLocation) {
        return FurnitureFactory.instance.packetManager().baseEntityFromHitbox(blockLocation);
    }

    @Nullable
    ItemDisplay baseEntity(int interactionId) {
        return FurnitureFactory.instance.packetManager().baseEntityFromHitbox(interactionId);
    }

    public boolean hasSpecifiedProperties() {
        return furnitureProperties != null;
    }

    public FurnitureProperties properties() {
        return furnitureProperties;
    }

    public RestrictedRotation restrictedRotation() {
        return restrictedRotation;
    }

    public void rotateFurniture(ItemDisplay baseEntity) {
        float yaw = baseEntity.getLocation().getYaw() + (restrictedRotation == RestrictedRotation.VERY_STRICT ? 45F : 22.5F);
        FurnitureHelpers.furnitureYaw(baseEntity, yaw);

        hitbox.handleHitboxes(baseEntity, this);
    }

    public BlockLockerMechanic blocklocker() {
        return blockLocker;
    }

    public LightMechanic light() {
        return light;
    }
}
