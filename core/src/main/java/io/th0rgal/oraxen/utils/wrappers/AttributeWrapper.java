package io.th0rgal.oraxen.utils.wrappers;

import io.th0rgal.oraxen.utils.VersionUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;

public class AttributeWrapper {

    public static final Attribute MAX_HEALTH = VersionUtil.atOrAbove("1.21.2") ? Attribute.MAX_HEALTH : Registry.ATTRIBUTE.get(NamespacedKey.fromString("generic.max_health"));
}
