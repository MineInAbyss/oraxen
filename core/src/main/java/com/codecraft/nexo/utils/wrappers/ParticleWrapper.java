package com.codecraft.nexo.utils.wrappers;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ParticleWrapper {

    @NotNull public static final Particle DUST = Objects.requireNonNull(Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft("dust")));
    @NotNull public static final Particle SPLASH = Objects.requireNonNull(Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft("splash")));
}