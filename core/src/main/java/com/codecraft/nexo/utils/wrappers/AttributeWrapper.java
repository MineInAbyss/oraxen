package com.codecraft.nexo.utils.wrappers;

import com.codecraft.nexo.utils.VersionUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class AttributeWrapper {

    public static final Attribute MAX_HEALTH = VersionUtil.atleast("1.21.2") ? Attribute.MAX_HEALTH : Registry.ATTRIBUTE.get(NamespacedKey.fromString("generic.max_health"));
    public static final Attribute BLOCK_BREAK_SPEED = VersionUtil.atleast("1.21.2") ? Attribute.BLOCK_BREAK_SPEED : Registry.ATTRIBUTE.get(NamespacedKey.fromString("player.max_health"));

    @Nullable
    public static Attribute fromString(@NotNull String attribute) {
        return Registry.ATTRIBUTE.get(NamespacedKey.fromString(attribute.toLowerCase(Locale.ENGLISH)));
    }
}
