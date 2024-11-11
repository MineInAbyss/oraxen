package com.codecraft.nexo.mechanics.custom_block;

import com.codecraft.nexo.mechanics.MechanicFactory;
import org.jetbrains.annotations.Nullable;

public interface CustomBlockType {
    String name();
    @Nullable MechanicFactory factory();
}


