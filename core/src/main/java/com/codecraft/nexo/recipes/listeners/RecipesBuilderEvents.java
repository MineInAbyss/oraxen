package com.codecraft.nexo.recipes.listeners;

import com.codecraft.nexo.recipes.builders.RecipeBuilder;
import com.codecraft.nexo.utils.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class RecipesBuilderEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    @SuppressWarnings("deprecation")
    public void setCursor(InventoryClickEvent event) {
        String recipeBuilderTitle = Optional.ofNullable(RecipeBuilder.get(event.getWhoClicked().getUniqueId())).map(RecipeBuilder::getInventoryTitle).orElse(null);
        if (!InventoryUtils.getTitleFromView(event).equals(recipeBuilderTitle) || event.getSlotType() != InventoryType.SlotType.RESULT) return;

        event.setCancelled(true);
        ItemStack empty = new ItemStack(Material.AIR);
        ItemStack currentResult =  Optional.ofNullable(event.getCurrentItem()).orElse(empty).clone();
        ItemStack currentCursor = Optional.ofNullable(event.getCursor()).orElse(empty).clone();
        event.setCurrentItem(currentCursor);
        event.setCursor(currentResult);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClosed(InventoryCloseEvent event) {
        RecipeBuilder recipeBuilder = RecipeBuilder.get(event.getPlayer().getUniqueId());
        if (recipeBuilder == null || !InventoryUtils.getTitleFromView(event).equals(recipeBuilder.getInventoryTitle()))
            return;

        recipeBuilder.setInventory(event.getInventory());
    }
}
