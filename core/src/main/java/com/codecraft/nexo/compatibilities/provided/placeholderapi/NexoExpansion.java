package com.codecraft.nexo.compatibilities.provided.placeholderapi;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.font.Glyph;
import com.codecraft.nexo.font.Shift;
import com.codecraft.nexo.utils.ParseUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class NexoExpansion extends PlaceholderExpansion {

    private final NexoPlugin plugin;

    public NexoExpansion(final NexoPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "boy0000";
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "nexo";
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(final OfflinePlayer player, @NotNull final String params) {
        final Glyph glyph = plugin.fontManager().glyphFromName(params);

        if (!glyph.isRequired()) return glyph.font() == Key.key("default") ? glyph.character() : glyph.glyphTag();
        if (params.startsWith("shift_")) return Shift.of(ParseUtils.parseInt(StringUtils.substringAfter(params, "shift_"), 0));
        if (params.equals("pack_hash")) return plugin.packGenerator().builtPack().hash();

        return "";
    }
}
