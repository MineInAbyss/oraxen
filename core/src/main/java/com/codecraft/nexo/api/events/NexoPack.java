package com.codecraft.nexo.api.events;

import com.codecraft.nexo.NexoPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.BuiltResourcePack;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.atlas.Atlas;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.font.Font;
import team.unnamed.creative.metadata.overlays.OverlaysMeta;
import team.unnamed.creative.metadata.pack.PackMeta;
import team.unnamed.creative.model.Model;
import team.unnamed.creative.sound.SoundEvent;
import team.unnamed.creative.sound.SoundRegistry;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

public class NexoPack {

    public static void sendPack(Player player) {
        NexoPlugin.get().packServer().sendPack(player);
    }

    public static ResourcePack resourcePack() {
        return NexoPlugin.get().packGenerator().resourcePack();
    }

    @Nullable
    public static BuiltResourcePack builtResourcePack() {
        return NexoPlugin.get().packGenerator().builtPack();
    }

    public static void mergePackFromZip(@NotNull File zipFile) {
        if (!zipFile.exists()) return;
        ResourcePack zipPack = NexoPlugin.get().packGenerator().getPackReader().readFromZipFile(zipFile);
        mergePack(resourcePack(), zipPack);
    }

    public static void mergePackFromDirectory(@NotNull File directory) {
        if (!directory.exists() || !directory.isDirectory()) return;
        ResourcePack zipPack = NexoPlugin.get().packGenerator().getPackReader().readFromDirectory(directory);
        mergePack(resourcePack(), zipPack);
    }

    public static void mergePack(ResourcePack resourcePack, ResourcePack importedPack) {
        importedPack.textures().forEach(resourcePack::texture);
        importedPack.sounds().forEach(resourcePack::sound);
        importedPack.unknownFiles().forEach(resourcePack::unknownFile);

        PackMeta packMeta = Optional.ofNullable(importedPack.packMeta()).orElse(resourcePack.packMeta());
        if (packMeta != null) resourcePack.packMeta(packMeta);
        Writable packIcon = Optional.ofNullable(importedPack.icon()).orElse(resourcePack.icon());
        if (packIcon != null) resourcePack.icon(packIcon);
        OverlaysMeta overlaysMeta = Optional.ofNullable(importedPack.overlaysMeta()).orElse(resourcePack.overlaysMeta());
        if (overlaysMeta != null) resourcePack.overlaysMeta(overlaysMeta);

        importedPack.models().forEach(model -> Optional.ofNullable(resourcePack.model(model.key())).ifPresentOrElse(base -> {
                    Model.Builder builder = model.toBuilder();
                    base.overrides().forEach(builder::addOverride);
                    builder.build().addTo(resourcePack);
                }, () -> model.addTo(resourcePack)));

        importedPack.fonts().forEach(font -> Optional.ofNullable(resourcePack.font(font.key())).ifPresentOrElse(base -> {
            Font.Builder builder = font.toBuilder();
            base.providers().forEach(builder::addProvider);
            builder.build().addTo(resourcePack);
        }, () -> font.addTo(resourcePack)));

        importedPack.soundRegistries().forEach(soundRegistry -> {
            SoundRegistry baseRegistry = resourcePack.soundRegistry(soundRegistry.namespace());
            if (baseRegistry != null) {
                Collection<SoundEvent> mergedEvents = new LinkedHashSet<>(baseRegistry.sounds());
                mergedEvents.addAll(soundRegistry.sounds());
                SoundRegistry.soundRegistry(baseRegistry.namespace(), mergedEvents).addTo(resourcePack);
            } else soundRegistry.addTo(resourcePack);
        });

        importedPack.atlases().forEach(atlas -> Optional.ofNullable(resourcePack.atlas(atlas.key())).ifPresentOrElse(base -> {
            Atlas.Builder builder = atlas.toBuilder();
            base.sources().forEach(builder::addSource);
            builder.build().addTo(resourcePack);
        }, () -> atlas.addTo(resourcePack)));

        importedPack.languages().forEach(language -> {
            Optional.ofNullable(resourcePack.language(language.key())).ifPresent(base -> {
                for (Map.Entry<String, String> entry : base.translations().entrySet())
                    language.translations().putIfAbsent(entry.getKey(), entry.getValue());
            });
            language.addTo(resourcePack);
        });

        importedPack.blockStates().forEach(blockState -> {
            Optional.ofNullable(resourcePack.blockState(blockState.key())).ifPresent(base -> {
                blockState.multipart().addAll(base.multipart());
                blockState.variants().putAll(base.variants());
            });
            blockState.addTo(resourcePack);
        });
    }
}
