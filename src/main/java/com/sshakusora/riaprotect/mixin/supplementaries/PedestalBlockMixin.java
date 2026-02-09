package com.sshakusora.riaprotect.mixin.supplementaries;

import com.sshakusora.riaprotect.log.LogEntry;
import com.sshakusora.riaprotect.log.LogQueue;
import com.sshakusora.riaprotect.util.GetFullId;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PedestalBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PedestalBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PedestalBlock.class)
public class PedestalBlockMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/supplementaries/common/block/tiles/PedestalBlockTile;setDisplayedItem(Lnet/minecraft/world/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onUseSackItem(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir, InteractionResult resultType, PedestalBlockTile tile, ItemStack handItem, ItemStack it, ItemStack removed) {
        if (level.isClientSide()) return;

        String dimId = level.dimension().location().toString();

        String blockFullId = GetFullId.GetBlockFullId(state.getBlock());

        if (!removed.isEmpty()) {
            String removeItemFullId = GetFullId.GetItemFullId(removed.getItem());

            LogQueue.push(new LogEntry(
                    player.getUUID(),
                    player.getName().getString(),
                    blockFullId,
                    dimId,
                    pos,
                    LogEntry.Action.EXTRACT.getValue(),
                    removeItemFullId,
                    removed.getCount(),
                    removed.hasTag() ? removed.getTag().getAsString() : "{}",
                    System.currentTimeMillis()
            ));
        }

        if (!it.isEmpty()) {
            String addItemFullId = GetFullId.GetItemFullId(it.getItem());

            LogQueue.push(new LogEntry(
                    player.getUUID(),
                    player.getName().getString(),
                    blockFullId,
                    dimId,
                    pos,
                    LogEntry.Action.INSERT.getValue(),
                    addItemFullId,
                    it.getCount(),
                    it.hasTag() ? handItem.getTag().getAsString() : "{}",
                    System.currentTimeMillis()
            ));
        }
    }
}
