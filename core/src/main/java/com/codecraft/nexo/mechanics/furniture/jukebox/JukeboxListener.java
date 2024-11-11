package com.codecraft.nexo.mechanics.furniture.jukebox;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoFurniture;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.codecraft.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.codecraft.nexo.config.Message;
import com.codecraft.nexo.mechanics.furniture.FurnitureMechanic;
import com.codecraft.nexo.utils.AdventureUtils;
import com.codecraft.nexo.utils.BlockHelpers;
import com.codecraft.nexo.utils.ItemUtils;
import com.codecraft.nexo.utils.VersionUtil;
import com.jeff_media.morepersistentdatatypes.DataType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import javax.annotation.Nullable;
import java.util.Locale;

import static com.codecraft.nexo.mechanics.furniture.jukebox.JukeboxBlock.MUSIC_DISC_KEY;

public class JukeboxListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInsertDisc(NexoFurnitureInteractEvent event) {
        Entity baseEntity = event.baseEntity();
        Player player = event.player();
        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (event.hand() != EquipmentSlot.HAND) return;

        boolean played = insertAndPlayDisc(baseEntity, itemStack, player);
        if (!played) return;
        player.swingMainHand();

        String displayName = null;
        if (itemStack.hasItemMeta()) {
            assert itemStack.getItemMeta() != null;
            if (itemStack.getItemMeta().hasLore()) {
                assert itemStack.getItemMeta().getLore() != null;
                displayName = itemStack.getItemMeta().getLore().get(0);
            } else if (NexoItems.exists(itemStack) && itemStack.getItemMeta().hasDisplayName()) {
                displayName = itemStack.getItemMeta().getDisplayName();
            }
        }

        if (displayName != null) {
            Component message = AdventureUtils.MINI_MESSAGE.deserialize(Message.MECHANICS_JUKEBOX_NOW_PLAYING.toString(), AdventureUtils.tagResolver("disc", displayName));
            NexoPlugin.get().audience().player(player).sendActionBar(message);
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEjectDisc(NexoFurnitureInteractEvent event) {
        if (!ejectAndStopDisc(event.baseEntity(), event.player())) return;
        event.player().swingMainHand();
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJukeboxBreak(NexoFurnitureBreakEvent event) {
        ejectAndStopDisc(event.getBaseEntity(), null);
    }

    private boolean insertAndPlayDisc(Entity baseEntity, ItemStack disc, @Nullable Player player) {
        PersistentDataContainer pdc = baseEntity.getPersistentDataContainer();
        FurnitureMechanic furnitureMechanic = NexoFurniture.getFurnitureMechanic(baseEntity);
        Location loc = BlockHelpers.toCenterLocation(baseEntity.getLocation());

        if (furnitureMechanic == null || !furnitureMechanic.isJukebox()) return false;
        if (pdc.has(MUSIC_DISC_KEY, DataType.ITEM_STACK) || !ItemUtils.isMusicDisc(disc)) return false;
        JukeboxBlock jukebox = furnitureMechanic.jukebox();
        if (!jukebox.hasPermission(player)) return false;
        ItemStack insertedDisc = disc.clone();
        insertedDisc.setAmount(1);
        if (player != null && player.getGameMode() != GameMode.CREATIVE)
            disc.setAmount(disc.getAmount() - insertedDisc.getAmount());
        pdc.set(MUSIC_DISC_KEY, DataType.ITEM_STACK, insertedDisc);
        baseEntity.getWorld().playSound(loc, jukebox.getPlayingSong(baseEntity), SoundCategory.RECORDS, jukebox.getVolume(), jukebox.getPitch());
        return true;
    }

    private boolean ejectAndStopDisc(Entity baseEntity, @Nullable Player player) {
        PersistentDataContainer pdc = baseEntity.getPersistentDataContainer();
        ItemStack item = pdc.get(MUSIC_DISC_KEY, DataType.ITEM_STACK);
        FurnitureMechanic furnitureMechanic = NexoFurniture.getFurnitureMechanic(baseEntity);
        Location loc = BlockHelpers.toCenterLocation(baseEntity.getLocation());

        if (furnitureMechanic == null || !furnitureMechanic.isJukebox() || item == null) return false;
        if (!pdc.has(MUSIC_DISC_KEY, DataType.ITEM_STACK) || !ItemUtils.isMusicDisc(item)) return false;

        JukeboxBlock jukebox = furnitureMechanic.jukebox();
        if (!jukebox.hasPermission(player)) return false;

        baseEntity.getWorld().getNearbyEntities(loc, 32, 32, 32).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .forEach(p -> {
                    Key songKey = getSongFromDisc(item);
                    if (songKey == null) return;
                    NexoPlugin.get().audience().player(p).stopSound(Sound.sound(songKey, Sound.Source.RECORD, jukebox.getVolume(), jukebox.getPitch()));
                });
        baseEntity.getWorld().dropItemNaturally(loc, item);
        pdc.remove(MUSIC_DISC_KEY);
        return true;
    }

    @Nullable
    private Key getSongFromDisc(ItemStack disc) {
        if (VersionUtil.atOrAbove("1.21")) {
            return disc.hasItemMeta() && disc.getItemMeta().hasJukeboxPlayable()
                    ? disc.getItemMeta().getJukeboxPlayable().getSongKey().key()
                    : null;
        } else {
            return Key.key("minecraft", "music_disc." + disc.getType().toString().toLowerCase(Locale.ROOT).split("music_disc_")[1]);
        }
    }
}
