package com.sshakusora.riaprotect.mixin.vanilla;

import appeng.api.networking.security.IActionHost;
import appeng.menu.AEBaseMenu;
import appeng.parts.AEBasePart;
import com.sshakusora.riaprotect.log.LogEntry;
import com.sshakusora.riaprotect.log.LogQueue;
import com.sshakusora.riaprotect.mixin.ae2.accessor.AEBaseMenuAccessor;
import com.sshakusora.riaprotect.util.GetFullId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    //TODO 监听不到左键/右键拖曳操作
    @Unique private final ThreadLocal<ItemStack> snapshotCarried = new ThreadLocal<>();
    @Unique private final ThreadLocal<ItemStack> snapshotSlotItem = new ThreadLocal<>();

    @Inject(method = "doClick", at = @At("HEAD"))
    private void captureBefore(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (player.level().isClientSide()) return;

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        snapshotCarried.set(menu.getCarried().copy());

        if (slotId >= 0 && slotId < menu.slots.size()) {
            snapshotSlotItem.set(menu.getSlot(slotId).getItem().copy());
        } else {
            snapshotSlotItem.set(ItemStack.EMPTY);
        }
    }

    @Inject(method = "doClick", at = @At("RETURN"))
    private void captureAfter(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (player.level().isClientSide()) return;

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        ItemStack beforeCarried = snapshotCarried.get();
        ItemStack beforeSlot = snapshotSlotItem.get();
        ItemStack afterCarried = menu.getCarried();

        if (slotId < 0 || slotId >= menu.slots.size()) return;

        Slot slot = menu.getSlot(slotId);
        ItemStack afterSlot = slot.getItem();

        if (!isPlayerInventory(slot) && isExtracted(beforeSlot, afterSlot)) {
            int count = beforeSlot.getCount() - afterSlot.getCount();
            logAction(player, slot, beforeSlot, count, LogEntry.Action.EXTRACT);
        }

        if (!isPlayerInventory(slot) && isInserted(beforeSlot, afterSlot)) {
            int count = afterSlot.getCount() - beforeSlot.getCount();
            logAction(player, slot, afterSlot, count, LogEntry.Action.INSERT);
        }

        snapshotCarried.remove();
        snapshotSlotItem.remove();
    }

    @Unique
    private boolean isPlayerInventory(Slot slot) {
        return slot.container instanceof Inventory;
    }

    @Unique
    private boolean isExtracted(ItemStack before, ItemStack after) {
        return !before.isEmpty() && (after.isEmpty() || (ItemStack.isSameItemSameTags(before, after) && after.getCount() < before.getCount()));
    }

    @Unique
    private boolean isInserted(ItemStack before, ItemStack after) {
        return !after.isEmpty() && (before.isEmpty() || (ItemStack.isSameItemSameTags(before, after) && after.getCount() > before.getCount()));
    }

    @Unique
    private void logAction(Player player, Slot slot, ItemStack item, int count,  LogEntry.Action action) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        BlockPos pos = BlockPos.ZERO;
        String blockFullId = "minecraft:air";
        Level level = player.level();

        if (slot.container instanceof BlockEntity be) {
            pos = be.getBlockPos();
            blockFullId = GetFullId.GetBlockFullId(level.getBlockState(pos).getBlock());
        }

        if (menu instanceof AEBaseMenu aeMenu) {
            IActionHost host = ((AEBaseMenuAccessor) aeMenu).callGetActionHost();
            if (host instanceof AEBasePart part) {
                pos = part.getBlockEntity().getBlockPos();
                blockFullId = GetFullId.GetItemFullId(part.getPartItem().asItem());
            } else if (host instanceof BlockEntity be) {
                pos = be.getBlockPos();
                blockFullId = GetFullId.GetBlockFullId(be.getBlockState().getBlock());
            }
        }

        if (!item.isEmpty()) {
            String dimId = level.dimension().location().toString();
            String itemFullId = GetFullId.GetItemFullId(item.getItem());

            LogQueue.push(new LogEntry(
                    player.getUUID(),
                    player.getName().getString(),
                    blockFullId,
                    dimId,
                    pos,
                    action.getValue(),
                    itemFullId,
                    count,
                    item.hasTag() ? item.getTag().getAsString() : "{}",
                    System.currentTimeMillis()
            ));
        }
    }
}
