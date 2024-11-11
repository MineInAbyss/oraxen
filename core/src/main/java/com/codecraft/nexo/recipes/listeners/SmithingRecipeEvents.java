package com.codecraft.nexo.recipes.listeners;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.mechanics.misc.misc.MiscMechanic;
import com.codecraft.nexo.mechanics.misc.misc.MiscMechanicFactory;
import com.codecraft.nexo.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.SmithingTransformRecipe;

import java.util.Arrays;

public class SmithingRecipeEvents implements Listener {

    @EventHandler
    public void onSmithingRecipe(PrepareSmithingEvent event) {
        SmithingInventory inventory = event.getInventory();
        ItemStack template = inventory.getInputTemplate();
        ItemStack material = inventory.getInputMineral();
        ItemStack input = inventory.getInputEquipment();

        if (ItemUtils.isEmpty(template) || ItemUtils.isEmpty(material) || ItemUtils.isEmpty(input)) return;
        if (Arrays.stream(inventory.getContents()).anyMatch(ItemUtils::isEmpty)) return;
        if (Arrays.stream(inventory.getContents()).noneMatch(NexoItems::exists)) return;

        String nexoItemId = NexoItems.idByItem(input);
        if (nexoItemId == null) return;
        MiscMechanic mechanic = MiscMechanicFactory.get().getMechanic(input);
        if (mechanic != null && mechanic.isAllowedInVanillaRecipes()) return;

        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (!(recipe instanceof SmithingTransformRecipe smithing)) return;
            if (!smithing.getTemplate().test(template) || !smithing.getAddition().test(material)) return;
            if (nexoItemId.equals(NexoItems.idByItem(smithing.getBase().getItemStack()))) return;

            event.setResult(null);
        });
    }
}
