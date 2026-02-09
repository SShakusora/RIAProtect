package com.sshakusora.riaprotect.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class GetFullId {
    public static String GetBlockFullId(Block block) {
        return Optional.ofNullable(ForgeRegistries.BLOCKS.getKey(block))
                .map(ResourceLocation::toString)
                .orElse("minecraft:air");
    }

    public static String GetItemFullId(Item item) {
        return Optional.ofNullable(ForgeRegistries.ITEMS.getKey(item))
                .map(ResourceLocation::toString)
                .orElse("minecraft:air");
    }
}
