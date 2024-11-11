package com.codecraft.nexo.utils;

import com.codecraft.nexo.config.Message;
import com.codecraft.nexo.font.GlyphTag;
import com.codecraft.nexo.font.ShiftTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class AdventureUtils {

    private AdventureUtils() {
    }

    public static final MiniMessage MINI_MESSAGE_EMPTY = MiniMessage.miniMessage();

    public static final TagResolver NexoTagResolver = TagResolver.resolver(TagResolver.standard(),
            GlyphTag.RESOLVER, ShiftTag.RESOLVER, TagResolver.resolver("prefix", Tag.selfClosingInserting(MINI_MESSAGE_EMPTY.deserialize(Message.PREFIX.toString())))
    );

    public static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(NexoTagResolver).build();


    public static MiniMessage MINI_MESSAGE_PLAYER(Player player) {
        return MiniMessage.builder().tags(TagResolver.resolver(TagResolver.standard(), GlyphTag.getResolverForPlayer(player))).build();
    }

    public static String parseMiniMessage(String message, @Nullable TagResolver tagResolver) {
        return MINI_MESSAGE.serialize((tagResolver != null ? MINI_MESSAGE.deserialize(message, tagResolver) : MINI_MESSAGE.deserialize(message))).replaceAll("\\\\(?!u)(?!n)(?!\")", "");
    }

    /**
     * Parses the string by deserializing it to a legacy component, then serializing it to a string via MiniMessage
     * @param message The string to parse
     * @return The parsed string
     */
    public static String parseLegacy(String message) {
        return MINI_MESSAGE.serialize(LEGACY_SERIALIZER.deserialize(message)).replaceAll("\\\\(?!u)(?!n)(?!\")", "");
    }

    public static Component parseLegacy(Component message) {
        return MINI_MESSAGE.deserialize(LEGACY_SERIALIZER.serialize(message));
    }

    /**
     * Parses a string through both legacy and minimessage serializers.
     * This is useful for parsing strings that may contain legacy formatting codes and modern adventure-tags.
     * @param message The component to parse
     * @return The parsed string
     */
    public static String parseLegacyThroughMiniMessage(String message) {
        return LEGACY_SERIALIZER.serialize(MINI_MESSAGE.deserialize(MINI_MESSAGE.serialize(LEGACY_SERIALIZER.deserialize(message)).replaceAll("\\\\(?!u)(?!n)(?!\")", "")));
    }

    public static TagResolver tagResolver(String string, String tag) {
        return TagResolver.resolver(string, Tag.selfClosingInserting(AdventureUtils.MINI_MESSAGE.deserialize(tag)));
    }

    public static TagResolver tagResolver(String string, Component tag) {
        return TagResolver.resolver(string, Tag.selfClosingInserting(tag));
    }
}
