package com.codecraft.nexo.font;

import com.codecraft.nexo.utils.ParseUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Set;
import java.util.regex.Pattern;

public class ShiftTag {
    private static final String SHIFT = "shift";
    private static final String SHIFT_SHORT = "s";
    public static final Pattern PATTERN = Pattern.compile("<shift:(-?\\d+)>");

    public static final TagResolver RESOLVER = TagResolver.resolver(Set.of(SHIFT, SHIFT_SHORT), (args, ctx) -> shiftTag(args));

    public static final TextReplacementConfig REPLACEMENT_CONFIG = TextReplacementConfig.builder()
            .match(PATTERN)
            .replacement((r, b) -> Component.text(Shift.of(Integer.parseInt(r.group(1)))))
            .build();

    private static Tag shiftTag(final ArgumentQueue args) {
        int shift = ParseUtils.parseInt(args.popOr("A shift value is required").value(), 0);
        return Tag.selfClosingInserting(Component.text(Shift.of(shift)).font(Key.key("default")));
    }
}
