package com.codecraft.nexo.utils.breaker;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoBlocks;
import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.api.events.custom_block.noteblock.NexoNoteBlockDamageEvent;
import com.codecraft.nexo.api.events.custom_block.stringblock.NexoStringBlockDamageEvent;
import com.codecraft.nexo.api.events.furniture.NexoFurnitureDamageEvent;
import com.codecraft.nexo.mechanics.BreakableMechanic;
import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockFactory;
import com.codecraft.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import com.codecraft.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import com.codecraft.nexo.nms.NMSHandlers;
import com.codecraft.nexo.utils.BlockHelpers;
import com.codecraft.nexo.utils.EventUtils;
import com.codecraft.nexo.utils.ItemUtils;
import com.codecraft.nexo.utils.ParseUtils;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class LegacyBreakerManager implements BreakerManager {

    private static final Random SOURCE_RANDOM = new Random();
    private final Map<UUID, ActiveBreakerData> activeBreakerDataMap;

    public LegacyBreakerManager(Map<UUID, ActiveBreakerData> activeBreakerDataMap) {
        this.activeBreakerDataMap = activeBreakerDataMap;
    }

    @Nullable
    public ActiveBreakerData activeBreakerData(Player player) {
        return this.activeBreakerDataMap.get(player.getUniqueId());
    }

    @Override
    public void startFurnitureBreak(Player player, ItemDisplay baseEntity, FurnitureMechanic mechanic, Block block) {
        stopBlockBreak(player);

        NexoFurnitureDamageEvent damageEvent = new NexoFurnitureDamageEvent(mechanic, baseEntity, player, block);
        if (!EventUtils.callEvent(damageEvent)) return;

        int breakTime = mechanic.breakable().breakTime(player);
        NMSHandlers.getHandler().applyMiningEffect(player);
        ActiveBreakerData activeBreakerData = new ActiveBreakerData(player, block.getLocation(), mechanic, mechanic.breakable(), breakTime, 0, createBreakScheduler(breakTime, player.getUniqueId()), createBreakSoundScheduler(player.getUniqueId()));
        activeBreakerDataMap.put(player.getUniqueId(), activeBreakerData);
    }

    @Override
    public void startBlockBreak(Player player, Block block, CustomBlockMechanic mechanic) {
        stopBlockBreak(player);

        Event customBlockEvent;
        if (mechanic instanceof NoteBlockMechanic noteMechanic)
            customBlockEvent = new NexoNoteBlockDamageEvent(noteMechanic, block, player);
        else if (mechanic instanceof StringBlockMechanic stringMechanic)
            customBlockEvent = new NexoStringBlockDamageEvent(stringMechanic, block, player);
        else return;
        if (!EventUtils.callEvent(customBlockEvent)) return;

        int breakTime = mechanic.breakable().breakTime(player);
        NMSHandlers.getHandler().applyMiningEffect(player);
        ActiveBreakerData activeBreakerData = new ActiveBreakerData(player, block.getLocation(), mechanic, mechanic.breakable(), breakTime, 0, createBreakScheduler(breakTime, player.getUniqueId()), createBreakSoundScheduler(player.getUniqueId()));
        activeBreakerDataMap.put(player.getUniqueId(), activeBreakerData);
    }

    @Override
    public void stopBlockBreak(Player player) {
        final ActiveBreakerData activeBreakerData = activeBreakerDataMap.get(player.getUniqueId());
        if (activeBreakerData == null) return;

        activeBreakerData.cancelTasks();
        activeBreakerDataMap.remove(player.getUniqueId());
        if (player.isOnline()) {
            NMSHandlers.getHandler().removeMiningEffect(player);
            activeBreakerData.resetProgress();
            activeBreakerData.sendBreakProgress();
        }
    }

    private BukkitTask createBreakScheduler(double blockBreakTime, UUID breakerUUID) {

        return Bukkit.getScheduler().runTaskTimer(NexoPlugin.get(), () -> {
            final ActiveBreakerData activeBreakerData = this.activeBreakerDataMap.get(breakerUUID);
            if (activeBreakerData == null) return;
            Player player = activeBreakerData.breaker;
            final Block block = activeBreakerData.location.getBlock();
            CustomBlockMechanic blockMechanic = NexoBlocks.getCustomBlockMechanic(block.getBlockData());
            FurnitureMechanic furnitureMechanic = NexoFurniture.getFurnitureMechanic(block);
            BreakableMechanic breakable = blockMechanic != null ? blockMechanic.breakable() : furnitureMechanic != null ? furnitureMechanic.breakable() : null;

            if (!player.isOnline() || breakable == null) {
                stopBlockBreak(player);
            } else if (!activeBreakerData.mechanic.equals(blockMechanic) && !activeBreakerData.mechanic.equals(furnitureMechanic)) {
                stopBlockBreak(player);
            } else if (blockMechanic != null && activeBreakerData.mechanic instanceof CustomBlockMechanic cm && blockMechanic.customVariation() != cm.customVariation()) {
                stopBlockBreak(player);
            } else if (furnitureMechanic != null && activeBreakerData.mechanic instanceof FurnitureMechanic fm && !fm.getItemID().equals(furnitureMechanic.getItemID())) {
                stopBlockBreak(player);
            } else if (!activeBreakerData.isBroken()) {
                activeBreakerData.addBreakTimeProgress(blockBreakTime / breakable.breakTime(player));
                activeBreakerData.sendBreakProgress();
            } else if (EventUtils.callEvent(new BlockBreakEvent(block, player)) && ProtectionLib.canBreak(player, block.getLocation())) {
                NMSHandlers.getHandler().removeMiningEffect(player);
                activeBreakerData.resetProgress();
                activeBreakerData.sendBreakProgress();

                for (ActiveBreakerData alterBreakerData : activeBreakerDataMap.values()) {
                    if (alterBreakerData.breaker.getUniqueId().equals(breakerUUID)) continue;
                    if (!alterBreakerData.location.equals(activeBreakerData.location)) continue;

                    stopBlockBreak(alterBreakerData.breaker);
                }

                ItemUtils.damageItem(player, player.getInventory().getItemInMainHand());
                block.setType(Material.AIR);
                activeBreakerData.cancelTasks();
                this.activeBreakerDataMap.remove(breakerUUID);
            } else stopBlockBreak(player);
        }, 1,1);
    }

    private BukkitTask createBreakSoundScheduler(UUID breakerUUID) {
        return Bukkit.getScheduler().runTaskTimer(NexoPlugin.get(), () -> {
            final ActiveBreakerData activeBreakerData = this.activeBreakerDataMap.get(breakerUUID);
            if (activeBreakerData == null) return;
            Player player = activeBreakerData.breaker;
            final Block block = activeBreakerData.location.getBlock();
            CustomBlockMechanic blockMechanic = NexoBlocks.getCustomBlockMechanic(block.getBlockData());
            FurnitureMechanic furnitureMechanic = NexoFurniture.getFurnitureMechanic(block);


            if (!player.isOnline() || (blockMechanic == null && furnitureMechanic == null)) {
                stopBlockBreak(player);
            } else if (blockMechanic != null) {
                if (!(activeBreakerData.mechanic instanceof CustomBlockMechanic cm)) {
                    stopBlockBreak(player);
                } else if (blockMechanic.customVariation() != cm.customVariation()) {
                    stopBlockBreak(player);
                } else if (!blockMechanic.hasBlockSounds() || !blockMechanic.blockSounds().hasHitSound()) {
                    activeBreakerData.breakerSoundTask.cancel();
                } else {
                    //TODO Allow for third party blocks to handle this somehow
                    String sound = "";
                    if (blockMechanic.type() == CustomBlockFactory.get().NOTEBLOCK)
                        sound = blockMechanic.hasBlockSounds() && blockMechanic.blockSounds().hasHitSound() ? blockMechanic.blockSounds().getHitSound() : "required.wood.hit";
                    else if (blockMechanic.type() == CustomBlockFactory.get().STRINGBLOCK)
                        sound = blockMechanic.hasBlockSounds() && blockMechanic.blockSounds().hasHitSound() ? blockMechanic.blockSounds().getHitSound() : "block.tripwire.detach";
                    BlockHelpers.playCustomBlockSound(block.getLocation(), sound, blockMechanic.blockSounds().getHitVolume(), blockMechanic.blockSounds().getHitPitch());
                }
            } else {
                if (!(activeBreakerData.mechanic instanceof FurnitureMechanic fm)) {
                    stopBlockBreak(player);
                } else if (!furnitureMechanic.getItemID().equals(fm.getItemID())) {
                    activeBreakerData.breakerSoundTask.cancel();
                } else if (furnitureMechanic.hasBlockSounds() && furnitureMechanic.blockSounds().hasHitSound()) {
                    String sound = furnitureMechanic.blockSounds().getHitSound();
                }
            }
        }, 0, 4L);
    }

    public static class ActiveBreakerData {
        public static final float MAX_DAMAGE = 1f;
        public static final float MIN_DAMAGE = 0f;
        private final int sourceId;
        private final Player breaker;
        private final Location location;
        private final Mechanic mechanic;
        private final BreakableMechanic breakable;
        private final int totalBreakTime;
        private double breakTimeProgress;
        private final BukkitTask breakerTask;
        private final BukkitTask breakerSoundTask;

        public ActiveBreakerData(
                Player breaker,
                Location location,
                Mechanic mechanic,
                BreakableMechanic breakable,
                int totalBreakTime,
                int breakTimeProgress,
                BukkitTask breakerTask,
                BukkitTask breakerSoundTask
        ) {
            this.sourceId = SOURCE_RANDOM.nextInt();
            this.breaker = breaker;
            this.location = location;
            this.mechanic = mechanic;
            this.breakable = breakable;
            this.totalBreakTime = totalBreakTime;
            this.breakTimeProgress = breakTimeProgress;
            this.breakerTask = breakerTask;
            this.breakerSoundTask = breakerSoundTask;
        }

        public int totalBreakTime() {
            return totalBreakTime;
        }

        public double breakTimeProgress() {
            return breakTimeProgress;
        }

        public void breakTimeProgress(double breakTimeProgress) {
            this.breakTimeProgress = breakTimeProgress;
        }

        public void addBreakTimeProgress(double breakTimeProgress) {
            this.breakTimeProgress = Math.min(this.breakTimeProgress + breakTimeProgress, this.totalBreakTime);
        }

        public void sendBreakProgress() {
            breaker.sendBlockDamage(location, calculateDamage(), sourceId);
        }

        public float calculateDamage() {
            final double percentage = this.breakTimeProgress / this.totalBreakTime;
            final float damage = (float) (MAX_DAMAGE * percentage);
            return ParseUtils.clamp(damage, MIN_DAMAGE, MAX_DAMAGE);
        }

        public boolean isBroken() {
            return breakTimeProgress >= this.totalBreakTime;
        }

        public void resetProgress() {
            this.breakTimeProgress = 0;
        }

        public void cancelTasks() {
            if (breakerTask != null) breakerTask.cancel();
            if (breakerSoundTask != null) breakerSoundTask.cancel();
        }
    }
}
