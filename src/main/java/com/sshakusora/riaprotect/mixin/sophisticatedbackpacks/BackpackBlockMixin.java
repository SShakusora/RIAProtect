package com.sshakusora.riaprotect.mixin.sophisticatedbackpacks;

import com.sshakusora.riaprotect.log.LogEntry;
import com.sshakusora.riaprotect.log.LogQueue;
import com.sshakusora.riaprotect.util.GetFullId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackpackBlock.class)
public class BackpackBlockMixin {
    @Inject(method = "putInPlayersHandAndRemove", at = @At("HEAD"), remap = false)
    private static void putInPlayersHandAndRemove(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, CallbackInfo ci) {
        if (world.isClientSide()) return;

        ItemStack backpack = (ItemStack) WorldHelper.getBlockEntity(world, pos, BackpackBlockEntity.class).map((te) -> te.getBackpackWrapper().getBackpack()).orElse(ItemStack.EMPTY);
        String dimId = world.dimension().location().toString();

        if (!backpack.isEmpty()) {
            String blockFullId = GetFullId.GetBlockFullId(state.getBlock());
            String backpackFullId = GetFullId.GetItemFullId(backpack.getItem());

            LogQueue.push(new LogEntry(
                    player.getUUID(),
                    player.getName().getString(),
                    blockFullId,
                    dimId,
                    pos,
                    LogEntry.Action.BREAK.getValue(),
                    backpackFullId,
                    backpack.getCount(),
                    backpack.hasTag() ? backpack.getTag().getAsString() : "{}",
                    System.currentTimeMillis()
            ));
        }
    }
}
