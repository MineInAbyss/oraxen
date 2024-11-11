package com.codecraft.nexo.compatibilities.provided.placeholderapi;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.compatibilities.CompatibilityProvider;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPICompatibility extends CompatibilityProvider<PlaceholderAPIPlugin> {
    public static PlaceholderExpansion expansion = null;
    public PlaceholderAPICompatibility() {
        expansion = new NexoExpansion(NexoPlugin.get());
        expansion.register();
    }

    @Override
    public void disable() {
        super.disable();
        if (expansion != null)
            expansion.unregister();
    }

}
