package com.codecraft.nexo.utils.drops;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.compatibilities.provided.ecoitems.WrappedEcoItem;
import com.codecraft.nexo.compatibilities.provided.mythiccrucible.WrappedCrucibleItem;
import com.codecraft.nexo.items.ItemUpdater;
import com.codecraft.nexo.utils.Utils;
import dev.jorel.commandapi.wrappers.IntegerRange;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Loot {

    private final String sourceID;
    private ItemStack itemStack;
    private final double probability;
    private final IntegerRange amount;
    private LinkedHashMap<String, Object> config;

    public Loot(LinkedHashMap<String, Object> config, String sourceID) {
        this.probability = Double.parseDouble(config.getOrDefault("probability", 1).toString());
        if (config.getOrDefault("amount", "") instanceof String amount && amount.contains("..")) {
            this.amount = Utils.parseToRange(amount);
        } else this.amount = new IntegerRange(1,1);
        this.config = config;
        this.sourceID = sourceID;
    }

    public Loot(ItemStack itemStack, double probability) {
        this.itemStack = itemStack;
        this.probability = Math.min(1.0, probability);
        this.amount = new IntegerRange(1,1);
        this.sourceID = null;
    }

    public Loot(String sourceID, ItemStack itemStack, double probability, int minAmount, int maxAmount) {
        this.sourceID = sourceID;
        this.itemStack = itemStack;
        this.probability = Math.min(1.0, probability);
        this.amount = new IntegerRange(minAmount, maxAmount);
    }

    public Loot(String sourceID, ItemStack itemStack, double probability, IntegerRange amount) {
        this.sourceID = sourceID;
        this.itemStack = itemStack;
        this.probability = Math.min(1.0, probability);
        this.amount = amount;
    }

    public ItemStack itemStack() {
        if (itemStack != null) return ItemUpdater.updateItem(itemStack);

        if (config.containsKey("nexo_item")) {
            String itemId = config.get("nexo_item").toString();
            itemStack = NexoItems.itemById(itemId).build();
        } else if (config.containsKey("oraxen_item")) {
            String itemId = config.get("oraxen_item").toString();
            itemStack = NexoItems.itemById(itemId).build();
        } else if (config.containsKey("crucible_item")) {
            itemStack = new WrappedCrucibleItem(config.get("crucible_item").toString()).build();
        } else if (config.containsKey("mmoitems_id") && config.containsKey("mmoitems_type")) {
            String type = config.get("mmoitems_type").toString();
            String id = config.get("mmoitems_id").toString();
            itemStack = MMOItems.plugin.getItem(type, id);
        } else if (config.containsKey("ecoitem")) {
            itemStack = new WrappedEcoItem(config.get("ecoitem").toString()).build();
        } else if (config.containsKey("minecraft_type")) {
            String itemType = config.get("minecraft_type").toString();
            Material material = Material.getMaterial(itemType);
            itemStack = material != null ? new ItemStack(material) : null;
        } else if (config.containsKey("minecraft_item")) {
            itemStack = (ItemStack) config.get("minecraft_item");
        }

        if (itemStack == null) itemStack = NexoItems.itemById(sourceID).build();

        return ItemUpdater.updateItem(itemStack);
    }

    public Loot itemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    public String sourceID() {
        return sourceID;
    }

    public double probability() {
        return probability;
    }

    public IntegerRange amount() {
        return this.amount;
    }

    public int dropNaturally(Location location, int amountMultiplier) {
        if (Math.random() <= probability) {
            return dropItems(location, amountMultiplier);
        }
        return 0;
    }

    public ItemStack getItem(int amountMultiplier) {
        ItemStack stack = itemStack().clone();
        int dropAmount = ThreadLocalRandom.current().nextInt(amount.getLowerBound(), amount.getUpperBound() + 1);
        stack.setAmount(stack.getAmount() * amountMultiplier * dropAmount);
        return ItemUpdater.updateItem(stack);
    }

    private int dropItems(Location location, int amountMultiplier) {
        ItemStack item = getItem(amountMultiplier);
        if (location.getWorld() != null) {
            location.getWorld().dropItemNaturally(location, item);
            return item.getAmount();
        }
        return 0;
    }
}
