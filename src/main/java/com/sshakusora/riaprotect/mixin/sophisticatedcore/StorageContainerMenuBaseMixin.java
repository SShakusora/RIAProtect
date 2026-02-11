package com.sshakusora.riaprotect.mixin.sophisticatedcore;

import com.sshakusora.riaprotect.log.LogEntry;
import com.sshakusora.riaprotect.log.LogQueue;
import com.sshakusora.riaprotect.util.GetFullId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(StorageContainerMenuBase.class)
public abstract class StorageContainerMenuBaseMixin {
    //TODO 无法监听升级槽位
    @Shadow public abstract Optional<BlockPos> getBlockPosition();

    @Unique private final Map<Integer, ItemStack> slotSnapshots = new HashMap<>();

    @Inject(method = "clicked", at = @At("HEAD"))
    private void beforeClick(int slotId, int dragType, ClickType clickType, Player player, CallbackInfo ci) {
        if (player.level().isClientSide()) return;

        slotSnapshots.clear();
        StorageContainerMenuBase<?> menu = (StorageContainerMenuBase<?>) (Object) this;

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.getSlot(i);
            if (!isPlayerInventory(slot)) {
                slotSnapshots.put(i, slot.getItem().copy());
            }
        }
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void afterClick(int slotId, int dragType, ClickType clickType, Player player, CallbackInfo ci) {
        if (player.level().isClientSide() || this.getBlockPosition().isEmpty()) return;

        StorageContainerMenuBase<?> menu = (StorageContainerMenuBase<?>) (Object) this;

        slotSnapshots.forEach((index, stackBefore) -> {
            ItemStack stackAfter = menu.getSlot(index).getItem();

            if (!ItemStack.matches(stackBefore, stackAfter)) {
                processChange(player, menu, menu.getSlot(index), stackBefore, stackAfter);
            }
        });

        slotSnapshots.clear();
    }

    @Unique
    private void processChange(Player player, StorageContainerMenuBase<?> menu, Slot slot, ItemStack before, ItemStack after) {
        if (isIncreased(before, after)) {
            int amount = after.getCount() - (ItemStack.isSameItemSameTags(before, after) ? before.getCount() : 0);
            logContainerAction(player, menu, slot, after, amount, LogEntry.Action.INSERT);
        }
        else if (isIncreased(after, before)) {
            int amount = before.getCount() - (ItemStack.isSameItemSameTags(before, after) ? after.getCount() : 0);
            logContainerAction(player, menu, slot, before, amount, LogEntry.Action.EXTRACT);
        }
    }

    @Unique
    private boolean isIncreased(ItemStack before, ItemStack after) {
        if (after.isEmpty()) return false;
        if (before.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(before, after) && after.getCount() > before.getCount();
    }

    @Unique
    private boolean isPlayerInventory(Slot slot) {
        return slot.container instanceof Inventory;
    }

    @Unique
    private void logContainerAction(Player player, StorageContainerMenuBase<?> menu, Slot slot, ItemStack stack, int count, LogEntry.Action action) {
        BlockPos pos = this.getBlockPosition().orElse(BlockPos.ZERO);
        Level level = player.level();
        String blockFullId = GetFullId.GetBlockFullId(level.getBlockState(pos).getBlock());
        String dimId = level.dimension().location().toString();
        String itemFullId = GetFullId.GetItemFullId(stack.getItem());

        LogQueue.push(new LogEntry(
                player.getUUID(),
                player.getName().getString(),
                blockFullId,
                dimId,
                pos,
                action.getValue(),
                itemFullId,
                count,
                stack.hasTag() ? stack.getTag().getAsString() : "{}",
                System.currentTimeMillis()
        ));
    }
}
