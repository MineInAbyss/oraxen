package com.codecraft.nexo.font;

import com.codecraft.nexo.NexoPlugin;
import com.codecraft.nexo.config.ConfigsManager;
import com.codecraft.nexo.config.Settings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.font.BitMapFontProvider;
import team.unnamed.creative.font.FontProvider;

import java.util.*;
import java.util.stream.Stream;

public class FontManager {

    public static Map<String, GlyphBitMap> glyphBitMaps = new HashMap<>();
    private final Map<String, Glyph> glyphMap;
    private final Map<String, Glyph> glyphByPlaceholder;
    private final Map<Character, String> reverse;
    private final FontEvents fontEvents;
    private boolean useNmsGlyphs;

    public FontManager(final ConfigsManager configsManager) {
        final Configuration fontConfiguration = configsManager.getFont();
        final ConfigurationSection bitmapSection = fontConfiguration.getConfigurationSection("bitmaps");
        if (bitmapSection != null) {
            glyphBitMaps = bitmapSection.getKeys(false).stream().collect(HashMap::new, (map, key) -> {
                final ConfigurationSection section = bitmapSection.getConfigurationSection(key);
                if (section != null) {
                    map.put(key, new GlyphBitMap(
                            Key.key(section.getString("font", "minecraft:default")),
                            Key.key(section.getString("texture", "").replaceAll("^(?!.*\\.png$)", "") + ".png"),
                            section.getInt("rows"), section.getInt("columns"),
                            section.getInt("height", 8), section.getInt("ascent", 8)
                    ));
                }
            }, HashMap::putAll);
        }
        glyphMap = new LinkedHashMap<>();
        glyphByPlaceholder = new LinkedHashMap<>();
        reverse = new LinkedHashMap<>();
        fontEvents = new FontEvents(this);
        loadGlyphs(configsManager.parseGlyphConfigs());


    }

    public static GlyphBitMap getGlyphBitMap(String id) {
        return id != null ? glyphBitMaps.getOrDefault(id, null) : null;
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(fontEvents, NexoPlugin.get());
        fontEvents.registerChatHandlers();
    }

    public void unregisterEvents() {
        HandlerList.unregisterAll(fontEvents);
        fontEvents.unregisterChatHandlers();
    }

    public FontEvents getFontEvents() {
        return fontEvents;
    }

    private void loadGlyphs(Collection<Glyph> glyphs) {
        for (Glyph glyph : glyphs) {
            if (glyph.character().isBlank()) continue;
            glyphMap.put(glyph.id(), glyph);
            reverse.put(glyph.character().charAt(0), glyph.id());
            for (final String placeholder : glyph.placeholders())
                glyphByPlaceholder.put(placeholder, glyph);
        }
    }

    public final Collection<Glyph> glyphs() {
        return glyphMap.values();
    }

    public final Collection<Glyph> emojis() {
        return glyphMap.values().stream().filter(Glyph::isEmoji).toList();
    }

    /**
     * Get a Glyph from a given Glyph-ID
     *
     * @param id The Glyph-ID
     * @return Returns the Glyph if it exists, otherwise the required Glyph
     */
    @NotNull
    public Glyph glyphFromName(final String id) {
        return glyphMap.get(id) != null ? glyphMap.get(id) : glyphMap.get("required");
    }

    /**
     * Get a Glyph from a given Glyph-ID
     *
     * @param id The Glyph-ID
     * @return Returns the Glyph if it exists, otherwise null
     */
    @Nullable
    public Glyph glyphFromID(final String id) {
        return glyphMap.get(id);
    }

    public Glyph glyphFromPlaceholder(final String word) {
        return glyphByPlaceholder.get(word);
    }

    public Map<String, Glyph> glyphByPlaceholderMap() {
        return glyphByPlaceholder;
    }

    public Map<Character, String> reverseMap() {
        return reverse;
    }

    private final Map<UUID, List<String>> currentGlyphCompletions = new HashMap<>();

    public void sendGlyphTabCompletion(Player player) {
        List<String> completions = glyphByPlaceholderMap().values().stream()
                .filter(Glyph::hasTabCompletion)
                .flatMap(glyph -> Settings.UNICODE_COMPLETIONS.toBool()
                        ? Stream.of(glyph.character())
                        : Arrays.stream(glyph.placeholders()))
                .toList();

        player.removeCustomChatCompletions(currentGlyphCompletions.getOrDefault(player.getUniqueId(), new ArrayList<>()));
        player.addCustomChatCompletions(completions);
        currentGlyphCompletions.put(player.getUniqueId(), completions);
    }

    public void clearGlyphTabCompletions(Player player) {
        this.currentGlyphCompletions.remove(player.getUniqueId());
    }

    public record GlyphBitMap(Key font, Key texture, int rows, int columns, int height, int ascent) {

        public BitMapFontProvider fontProvider() {
            List<Glyph> bitmapGlyphs = NexoPlugin.get().fontManager().glyphs().stream().filter(Glyph::hasBitmap).filter(g -> g.bitmap() != null && g.bitmap().equals(this)).toList();
            List<String> charMap = new ArrayList<>(rows());

            for (int i = 1; i <= rows(); i++) {
                int currentRow = i;
                List<Glyph> glyphsInRow = bitmapGlyphs.stream().filter(g -> g.getBitmapEntry().row() == currentRow).toList();
                StringBuilder charRow = new StringBuilder();
                for (int j = 1; j <= columns(); j++) {
                    int currentColumn = j;
                    Glyph glyph = glyphsInRow.stream().filter(g -> g.getBitmapEntry().column() == currentColumn).findFirst().orElse(null);
                    charRow.append(glyph != null ? glyph.character() : Glyph.WHITESPACE_GLYPH);
                }
                charMap.add(i - 1, charRow.toString());
            }

            return FontProvider.bitMap(texture, height, ascent, charMap);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            JsonArray chars = new JsonArray();

            List<Glyph> bitmapGlyphs = NexoPlugin.get().fontManager().glyphs().stream().filter(Glyph::hasBitmap).filter(g -> g.bitmap() != null && g.bitmap().equals(this)).toList();

            for (int i = 1; i <= rows(); i++) {
                int currentRow = i;
                List<Glyph> glyphsInRow = bitmapGlyphs.stream().filter(g -> g.getBitmapEntry().row() == currentRow).toList();
                StringBuilder charRow = new StringBuilder();
                for (int j = 1; j <= columns(); j++) {
                    int currentColumn = j;
                    Glyph glyph = glyphsInRow.stream().filter(g -> g.getBitmapEntry().column() == currentColumn).findFirst().orElse(null);
                    charRow.append(glyph != null ? glyph.character() : Glyph.WHITESPACE_GLYPH);
                }
                chars.add(""); // Add row
                chars.set(i - 1, new JsonPrimitive(charRow.toString()));
            }
            json.add("chars", chars);

            json.addProperty("type", "bitmap");
            json.addProperty("ascent", ascent);
            json.addProperty("height", height);
            json.addProperty("file", texture.asString());

            return json;
        }
    }
}
