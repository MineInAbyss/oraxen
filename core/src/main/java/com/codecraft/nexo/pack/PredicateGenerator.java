package com.codecraft.nexo.pack;

import com.codecraft.nexo.api.NexoItems;
import com.codecraft.nexo.items.ItemBuilder;
import com.codecraft.nexo.items.NexoMeta;
import com.codecraft.nexo.utils.KeyUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class PredicateGenerator {

    private final ResourcePack resourcePack;

    public PredicateGenerator(ResourcePack resourcePack) {
        this.resourcePack = resourcePack;
    }

    /**
     * Generates the base model overrides for the given material
     * This looks up all ItemBuilders using this material and generates the overrides for them
     * This includes CustomModelData, ItemPredicates like pulling, blocking, charged, cast, firework and damage
     * @param material the material to generate the overrides for
     * @return the generated overrides
     */
    public List<ItemOverride> generateBaseModelOverrides(Material material) {
        LinkedHashSet<ItemBuilder> itemBuilders = NexoItems.items().stream().filter(i -> i.getType() == material).collect(Collectors.toCollection(LinkedHashSet::new));
        List<ItemOverride> overrides = Optional.ofNullable(DefaultResourcePackExtractor.vanillaResourcePack.model(Key.key("item/" + material.toString().toLowerCase(Locale.ENGLISH)))).map(Model::overrides).orElse(new ArrayList<>());

        for (ItemBuilder itemBuilder : itemBuilders) {
            if (itemBuilder == null) continue;
            NexoMeta nexoMeta = itemBuilder.nexoMeta();
            if (nexoMeta == null || !nexoMeta.containsPackInfo()) continue;
            Key itemModelKey = nexoMeta.modelKey();
            ItemPredicate cmdPredicate = ItemPredicate.customModelData(nexoMeta.customModelData());
            overrides.add(ItemOverride.of(itemModelKey, cmdPredicate));

            if (nexoMeta.hasBlockingModel()) addMissingOverrideModel(nexoMeta.blockingModel(), nexoMeta.parentModelKey());
            if (nexoMeta.hasChargedModel()) addMissingOverrideModel(nexoMeta.chargedModel(), nexoMeta.parentModelKey());
            if (nexoMeta.hasCastModel()) addMissingOverrideModel(nexoMeta.castModel(), nexoMeta.parentModelKey());
            if (nexoMeta.hasFireworkModel()) addMissingOverrideModel(nexoMeta.fireworkModel(), nexoMeta.parentModelKey());
            for (Key pullingKey : nexoMeta.pullingModels()) addMissingOverrideModel(pullingKey, nexoMeta.parentModelKey());
            for (Key damagedKey : nexoMeta.damagedModels()) addMissingOverrideModel(damagedKey, nexoMeta.parentModelKey());

            if (nexoMeta.hasBlockingModel()) overrides.add(ItemOverride.of(nexoMeta.blockingModel(), ItemPredicate.blocking(), cmdPredicate));
            if (nexoMeta.hasChargedModel()) overrides.add(ItemOverride.of(nexoMeta.chargedModel(), ItemPredicate.charged(), cmdPredicate));
            if (nexoMeta.hasCastModel()) overrides.add(ItemOverride.of(nexoMeta.castModel(), ItemPredicate.cast(), cmdPredicate));
            if (nexoMeta.hasFireworkModel()) overrides.add(ItemOverride.of(nexoMeta.fireworkModel(), ItemPredicate.firework(), cmdPredicate));

            List<Key> pullingModels = nexoMeta.pullingModels();
            if (!pullingModels.isEmpty()) for (int i = 1; i <= pullingModels.size(); i++) {
                float pull = Math.min((float) i / pullingModels.size(), 0.99f);
                overrides.add(ItemOverride.of(pullingModels.get(i-1), ItemPredicate.pulling(), ItemPredicate.pull(pull), cmdPredicate));
            }

            List<Key> damagedModels = nexoMeta.damagedModels();
            if (!damagedModels.isEmpty()) for (int i = 1; i <= damagedModels.size(); i++) {
                float damage = Math.min((float) i / damagedModels.size(), 0.99f);
                overrides.add(ItemOverride.of(damagedModels.get(i-1), ItemPredicate.damage(damage), cmdPredicate));
            }
        }

        return overrides;
    }

    private void addMissingOverrideModel(Key modelKey, Key parentKey) {
        resourcePack.model(Optional.ofNullable(resourcePack.model(modelKey)).orElse(
                Model.model().key(modelKey).parent(parentKey)
                .textures(ModelTextures.builder().layers(ModelTexture.ofKey(KeyUtils.dropExtension(modelKey))).build())
                .build())
        );
    }
}

