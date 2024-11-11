package com.codecraft.nexo.mechanics;

import com.codecraft.nexo.api.NexoItems;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class MechanicFactory {

    private final Map<String, Mechanic> mechanicByItem = new HashMap<>();
    private final String mechanicId;
    private final ConfigurationSection section;

    protected MechanicFactory(ConfigurationSection section) {
        this.section = section;
        this.mechanicId = section.getName();
    }

    protected MechanicFactory(String mechanicId) {
        this.mechanicId = mechanicId;
        this.section = null;
    }

    protected ConfigurationSection getSection() {
        return this.section;
    }

    public abstract Mechanic parse(ConfigurationSection section);

    protected void addToImplemented(Mechanic mechanic) {
        if (mechanic == null) return;
        mechanicByItem.put(mechanic.getItemID(), mechanic);
    }

    public Set<String> getItems() {
        return mechanicByItem.keySet();
    }

    public boolean isNotImplementedIn(String itemID) {
        return !mechanicByItem.containsKey(itemID);
    }

    public boolean isNotImplementedIn(ItemStack itemStack) {
        return !mechanicByItem.containsKey(NexoItems.idByItem(itemStack));
    }

    public Mechanic getMechanic(String itemID) {
        return mechanicByItem.get(itemID);
    }

    public Mechanic getMechanic(ItemStack itemStack) {
        return mechanicByItem.get(NexoItems.idByItem(itemStack));
    }

    public String getMechanicID() {
        return mechanicId;
    }

}
