package com.sshakusora.riaprotect.mixin.ae2;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.me.storage.DelegatingMEInventory;
import appeng.parts.AEBasePart;
import com.sshakusora.riaprotect.log.LogEntry;
import com.sshakusora.riaprotect.log.LogQueue;
import com.sshakusora.riaprotect.mixin.ae2.accessor.WirelessTerminalMenuHostAccessor;
import com.sshakusora.riaprotect.util.GetFullId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DelegatingMEInventory.class)
public class DelegatingMEInventoryMixin {
    @Inject(method = "insert", at = @At("RETURN"), remap = false)
    private void onInsert(AEKey what, long amount, Actionable mode, IActionSource source, CallbackInfoReturnable<Long> cir) {
        long actualInserted = cir.getReturnValue();

        if (actualInserted > 0 && mode == Actionable.MODULATE && what instanceof AEItemKey itemKey) {
            Player player = source.player().orElse(null);
            IActionHost machine = source.machine().orElse(null);
            BlockPos pos = BlockPos.ZERO;
            String blockFullId = "minecraft:air";

            if (player != null) {
                String dimId = player.level().dimension().location().toString();
                ItemStack insertItem  = itemKey.toStack();
                String insetItemFullId = GetFullId.GetItemFullId(itemKey.getItem());

                if (machine instanceof BlockEntity be) {
                    pos = be.getBlockPos();
                    blockFullId = GetFullId.GetBlockFullId(be.getBlockState().getBlock());
                } else if (machine instanceof AEBasePart part) {
                    pos = part.getBlockEntity().getBlockPos();
                    blockFullId = GetFullId.GetItemFullId(part.getPartItem().asItem());
                } else if (machine instanceof WirelessTerminalMenuHost wirelessTerminalMenuHost) {
                    IWirelessAccessPoint wap = ((WirelessTerminalMenuHostAccessor) wirelessTerminalMenuHost).getMyWap();
                    if (wap != null) {
                        pos = wap.getLocation().getPos();
                        blockFullId = GetFullId.GetBlockFullId(wap.getLocation().getLevel().getBlockState(pos).getBlock());
                        dimId = wap.getLocation().getLevel().dimension().location().toString();
                    }
                }

                LogQueue.push(new LogEntry(
                        player.getUUID(),
                        player.getName().getString(),
                        blockFullId,
                        dimId,
                        pos,
                        LogEntry.Action.INSERT.getValue(),
                        insetItemFullId,
                        (int) actualInserted,
                        insertItem.hasTag() ? insertItem.getTag().getAsString() : "{}",
                        System.currentTimeMillis()
                ));
            }
        }
    }

    @Inject(method = "extract", at = @At("RETURN"), remap = false)
    private void onExtract(AEKey what, long amount, Actionable mode, IActionSource source, CallbackInfoReturnable<Long> cir) {
        long actualExtracted = cir.getReturnValue();

        if (actualExtracted > 0 && mode == Actionable.MODULATE && what instanceof AEItemKey itemKey) {
            Player player = source.player().orElse(null);
            IActionHost machine = source.machine().orElse(null);
            BlockPos pos = BlockPos.ZERO;
            String blockFullId = "minecraft:air";

            if (player != null) {
                if (machine instanceof BlockEntity be) {
                    pos = be.getBlockPos();
                    blockFullId = GetFullId.GetBlockFullId(be.getBlockState().getBlock());
                } else if (machine instanceof AEBasePart part) {
                    pos = part.getBlockEntity().getBlockPos();
                    blockFullId = GetFullId.GetItemFullId(part.getPartItem().asItem());
                } else if (machine instanceof WirelessTerminalMenuHost wirelessTerminalMenuHost) {
                    IWirelessAccessPoint wap = ((WirelessTerminalMenuHostAccessor) wirelessTerminalMenuHost).getMyWap();
                    if (wap != null) {
                        pos = wap.getLocation().getPos();
                        blockFullId = GetFullId.GetBlockFullId(wap.getLocation().getLevel().getBlockState(pos).getBlock());
                    }
                }

                String dimId = player.level().dimension().location().toString();
                ItemStack extractItem = itemKey.toStack();
                String insetItemFullId = GetFullId.GetItemFullId(itemKey.getItem());

                LogQueue.push(new LogEntry(
                        player.getUUID(),
                        player.getName().getString(),
                        blockFullId,
                        dimId,
                        pos,
                        LogEntry.Action.EXTRACT.getValue(),
                        insetItemFullId,
                        (int) actualExtracted,
                        extractItem.hasTag() ? extractItem.getTag().getAsString() : "{}",
                        System.currentTimeMillis()
                ));
            }
        }
    }
}
