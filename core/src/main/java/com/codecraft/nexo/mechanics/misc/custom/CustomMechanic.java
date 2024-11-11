package com.codecraft.nexo.mechanics.misc.custom;

import com.codecraft.nexo.mechanics.Mechanic;
import com.codecraft.nexo.mechanics.MechanicFactory;
import com.codecraft.nexo.mechanics.misc.custom.fields.CustomEvent;
import com.codecraft.nexo.mechanics.misc.custom.listeners.CustomListener;
import com.codecraft.nexo.utils.actions.ClickAction;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class CustomMechanic extends Mechanic {

    private static final Map<String, CustomListener> LOADED_VARIANTS = new HashMap<>();

    public CustomMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);

        for (String subMechanicName : section.getKeys(false)) {
            ConfigurationSection subsection = section.getConfigurationSection(subMechanicName);
            if (subsection == null) continue;
            String key = subsection.getCurrentPath();

            CustomListener loadedListener = LOADED_VARIANTS.get(key);
            if (loadedListener != null) {
                loadedListener.unregister();
            }

            ClickAction clickAction = ClickAction.from(subsection);

            if (clickAction == null) continue;

            CustomListener listener = new CustomEvent(
                    subsection.getString("event", ""),
                    subsection.getBoolean("one_usage", false)
            ).getListener(getItemID(), subsection.getLong("cooldown"), clickAction);

            listener.register();
            LOADED_VARIANTS.put(key, listener);
        }
    }

}
