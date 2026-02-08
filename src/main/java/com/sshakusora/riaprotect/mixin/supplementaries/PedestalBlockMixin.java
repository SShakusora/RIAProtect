package com.sshakusora.riaprotect.mixin.supplementaries;

import com.sshakusora.riaprotect.log.LogEntry;
import com.sshakusora.riaprotect.log.LogQueue;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PedestalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PedestalBlock.class)
public class PedestalBlockMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"))
    private void onUseSackItem(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide) return;

        ItemStack handItem = player.getItemInHand(handIn);
        String dimId = level.dimension().location().toString();

        String itemFullId = Optional.ofNullable(ForgeRegistries.ITEMS.getKey(handItem.getItem()))
                .map(ResourceLocation::toString)
                .orElse("minecraft:air");

        String blockFullId = Optional.ofNullable(ForgeRegistries.BLOCKS.getKey(state.getBlock()))
                .map(ResourceLocation::toString)
                .orElse("minecraft:air");

        LogQueue.push(new LogEntry(
                player.getUUID(),
                player.getName().getString(),
                blockFullId,
                dimId,
                pos,
                "INSERT",
                itemFullId,
                handItem.getCount(),
                handItem.hasTag() ? handItem.getTag().getAsString() : "{}",
                System.currentTimeMillis()
        ));
    }
}
