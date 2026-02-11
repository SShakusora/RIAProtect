package com.sshakusora.riaprotect.mixin.sophisticatedbackpacks;

import com.sshakusora.riaprotect.log.LogEntry;
import com.sshakusora.riaprotect.log.LogQueue;
import com.sshakusora.riaprotect.util.GetFullId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BackpackItem.class)
public class BackpackItemMixin {
    @Inject(method = "tryPlace", at = @At(value = "INVOKE", target = "Lnet/p3pp3rf1y/sophisticatedcore/util/WorldHelper;getBlockEntity(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Ljava/lang/Class;)Ljava/util/Optional;"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private void beforeTryPlace(Player player, Direction direction, BlockPlaceContext blockItemUseContext, CallbackInfoReturnable<InteractionResult> cir, Level world, BlockPos pos, FluidState fluidstate, BlockState placementState, ItemStack backpack) {
        if (world.isClientSide()) return;

        String dimId = world.dimension().location().toString();
        Block block = world.getBlockState(pos).getBlock();

        if (!backpack.isEmpty()) {
            String blockFullId = GetFullId.GetBlockFullId(block);
            String backpackFullId = GetFullId.GetItemFullId(backpack.getItem());

            LogQueue.push(new LogEntry(
                    player.getUUID(),
                    player.getName().getString(),
                    blockFullId,
                    dimId,
                    pos,
                    LogEntry.Action.PLACE.getValue(),
                    backpackFullId,
                    backpack.getCount(),
                    backpack.hasTag() ? backpack.getTag().getAsString() : "{}",
                    System.currentTimeMillis()
            ));
        }
    }
}
