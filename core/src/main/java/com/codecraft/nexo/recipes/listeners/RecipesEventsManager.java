package com.codecraft.nexo.recipes.listeners;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.config.Settings;
import com.codecraft.nexo.mechanics.misc.misc.MiscMechanicFactory;
import com.codecraft.nexo.recipes.CustomRecipe;
import com.codecraft.nexo.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.Recipe;

import java.util.*;
import java.util.stream.Collectors;

public class RecipesEventsManager implements Listener {

    private static RecipesEventsManager instance;
    private Map<CustomRecipe, String> permissionsPerRecipe = new HashMap<>();
    private Set<CustomRecipe> whitelistedCraftRecipes = new HashSet<>();
    private ArrayList<CustomRecipe> whitelistedCraftRecipesOrdered = new ArrayList<>();

    public static RecipesEventsManager get() {
        if (instance == null) {
            instance = new RecipesEventsManager();
        }
        return instance;
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(instance, NexoPlugin.get());
        Bukkit.getPluginManager().registerEvents(new SmithingRecipeEvents(), NexoPlugin.get());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrade(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory instanceof MerchantInventory merchantInventory)) return;
        if (event.getSlot() != 2 || merchantInventory.getSelectedRecipe() == null) return;

        String first = NexoItems.idByItem(merchantInventory.getItem(0)), second = NexoItems.idByItem(merchantInventory.getItem(1));
        ArrayList<ItemStack> ingredients = new ArrayList<>(merchantInventory.getSelectedRecipe().getIngredients());
        String firstIngredient = ingredients.isEmpty() ? null : NexoItems.idByItem(ingredients.get(0));
        String secondIngredient = ingredients.size() < 2 ? null : NexoItems.idByItem(ingredients.get(1));
        if (!Objects.equals(first, firstIngredient) || !Objects.equals(second, secondIngredient)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCrafted(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        CustomRecipe customRecipe = CustomRecipe.fromRecipe(recipe);
        Player player = InventoryUtils.playerFromView(event);
        if (!hasPermission(player, customRecipe)) event.getInventory().setResult(null);

        ItemStack result = event.getInventory().getResult();
        if (result == null || recipe == null || customRecipe == null || customRecipe.isValidDyeRecipe() || MiscMechanicFactory.get() == null) return;
        if (Arrays.stream(event.getInventory().getMatrix()).noneMatch(NexoItems::exists)) return;
        if (whitelistedCraftRecipes.stream().anyMatch(customRecipe::equals)) return;

        event.getInventory().setResult(customRecipe.getResult());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!Settings.ADD_RECIPES_TO_BOOK.toBool()) return;
        Player player = event.getPlayer();
        player.discoverRecipes(getPermittedRecipes(player).stream().map(r -> NamespacedKey.fromString(r.getName(), NexoPlugin.get())).collect(Collectors.toSet()));
    }

    public void resetRecipes() {
        permissionsPerRecipe = new HashMap<>();
        whitelistedCraftRecipes = new HashSet<>();
        whitelistedCraftRecipesOrdered = new ArrayList<>();
    }

    public void addPermissionRecipe(CustomRecipe recipe, String permission) {
        permissionsPerRecipe.put(recipe, permission);
    }

    public void whitelistRecipe(CustomRecipe recipe) {
        whitelistedCraftRecipes.add(recipe);
        whitelistedCraftRecipesOrdered.add(recipe);
    }

    public List<CustomRecipe> getPermittedRecipes(CommandSender sender) {
        return whitelistedCraftRecipesOrdered
                .stream()
                .filter(customRecipe -> !permissionsPerRecipe.containsKey(customRecipe) || hasPermission(sender, customRecipe))
                .toList();
    }

    public String[] getPermittedRecipesName(CommandSender sender) {
        return getPermittedRecipes(sender)
                .stream()
                .map(CustomRecipe::getName)
                .toArray(String[]::new);
    }


    public boolean hasPermission(CommandSender sender, CustomRecipe recipe) {
        return !permissionsPerRecipe.containsKey(recipe) || sender.hasPermission(permissionsPerRecipe.get(recipe));
    }

}
