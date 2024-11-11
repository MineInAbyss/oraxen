package com.codecraft.nexo.recipes.builders;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.utils.NexoYaml;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class RecipeBuilder {

    private static final Map<UUID, RecipeBuilder> BUILDER_MAP = new HashMap<>();

    private Inventory inventory;
    private File configFile;
    private YamlConfiguration config;
    private final String inventoryTitle;
    private final Player player;
    private final String builderName;

    protected RecipeBuilder(Player player, String builderName) {
        this.player = player;
        this.builderName = builderName;
        this.inventoryTitle = player.getName() + " " + builderName + " builder";
        UUID uuid = player.getUniqueId();
        inventory = BUILDER_MAP.containsKey(uuid) && BUILDER_MAP.get(uuid).builderName.equals(builderName)
                ? BUILDER_MAP.get(uuid).inventory
                : createInventory(player, inventoryTitle);
        player.openInventory(inventory);
        BUILDER_MAP.put(uuid, this);
    }

    abstract Inventory createInventory(Player player, String inventoryTitle);

    void close() {
        BUILDER_MAP.remove(player.getUniqueId());
    }

    public abstract void saveRecipe(String name, String permission);

    protected Inventory getInventory() {
        return this.inventory;
    }

    protected void setItemStack(ConfigurationSection section, @NotNull ItemStack itemStack) {

        if (NexoItems.exists(itemStack))
            section.set("nexo_item", NexoItems.idByItem(itemStack));
        else if (itemStack.isSimilar(new ItemStack(itemStack.getType())))
            section.set("minecraft_type", itemStack.getType().name());
        else section.set("minecraft_item", itemStack);

        if (itemStack.getAmount() > 1) section.set("amount", itemStack.getAmount());
    }

    public YamlConfiguration getConfig() {
        if (configFile == null) {
            configFile = NexoPlugin.get().resourceManager()
                    .extractConfiguration("recipes/" + builderName + ".yml");
            config = NexoYaml.loadConfiguration(configFile);
        }
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
        BUILDER_MAP.put(player.getUniqueId(), this);
    }

    public String getInventoryTitle() {
        return inventoryTitle;
    }

    public Player getPlayer() {
        return player;
    }

    public void open() {
        player.openInventory(inventory);
    }

    public static RecipeBuilder get(UUID playerUUID) {
        return BUILDER_MAP.get(playerUUID);
    }
}
