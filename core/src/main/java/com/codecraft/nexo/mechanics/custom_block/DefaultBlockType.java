package com.codecraft.nexo.mechanics.custom_block;

import com.codecraft.nexo.mechanics.MechanicFactory;

public record DefaultBlockType(String name, MechanicFactory factory) implements CustomBlockType {
}
