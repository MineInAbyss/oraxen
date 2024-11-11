package com.codecraft.nexo.utils.inventories;

import com.codecraft.nexo.recipes.CustomRecipe;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InvManager {

    private final Map<UUID, PaginatedGui> itemsViews = new HashMap<>();

    public InvManager() {
        regen();
    }

    public void regen() {
        itemsViews.clear();
    }

    public PaginatedGui getItemsView(Player player) {
        return itemsViews.computeIfAbsent(player.getUniqueId(), uuid -> new ItemsView().create());
    }


    public ChestGui getRecipesShowcase(final int page, final List<CustomRecipe> filteredRecipes) {
        return new RecipesView().create(page, filteredRecipes);
    }
}
