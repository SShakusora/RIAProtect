package com.sshakusora.riaprotect.mixin.create;

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.SharedDepotBlockMethods;
import com.sshakusora.riaprotect.log.LogEntry;
import com.sshakusora.riaprotect.log.LogQueue;
import com.sshakusora.riaprotect.util.GetFullId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SharedDepotBlockMethods.class)
public class SharedDepotBlockMethodsMixin {
    @Inject(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void beforePlaceItemInInventoryA(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray, CallbackInfoReturnable<InteractionResult> cir, DepotBehaviour behaviour, ItemStack heldItem, boolean wasEmptyHanded, boolean shouldntPlaceItem, ItemStack mainItemStack) {
        if (world.isClientSide()) return;

        String blockFullId = GetFullId.GetBlockFullId(state.getBlock());
        String dimId = world.dimension().location().toString();

        if (!mainItemStack.isEmpty()) {
            String removeItemFullId = GetFullId.GetItemFullId(mainItemStack.getItem());

            LogQueue.push(new LogEntry(
                    player.getUUID(),
                    player.getName().getString(),
                    blockFullId,
                    dimId,
                    pos,
                    LogEntry.Action.EXTRACT.getValue(),
                    removeItemFullId,
                    mainItemStack.getCount(),
                    mainItemStack.hasTag() ? mainItemStack.getTag().getAsString() : "{}",
                    System.currentTimeMillis()
            ));
        }
    }

    @Inject(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void beforePlaceItemInInventoryB(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray, CallbackInfoReturnable<InteractionResult> cir, DepotBehaviour behaviour, ItemStack heldItem, boolean wasEmptyHanded, boolean shouldntPlaceItem, ItemStack mainItemStack, ItemStackHandler outputs, int i) {
        if (world.isClientSide()) return;

        ItemStack stack = outputs.extractItem(i, 64, true);
        if (stack.isEmpty()) return;

        String blockFullId = GetFullId.GetBlockFullId(state.getBlock());
        String removeItemFullId = GetFullId.GetItemFullId(stack.getItem());
        String dimId = world.dimension().location().toString();

        LogQueue.push(new LogEntry(
                player.getUUID(),
                player.getName().getString(),
                blockFullId,
                dimId,
                pos,
                LogEntry.Action.EXTRACT.getValue(),
                removeItemFullId,
                stack.getCount(),
                stack.hasTag() ? stack.getTag().getAsString() : "{}",
                System.currentTimeMillis()
        ));
    }

    @Inject(method = "onUse", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/depot/DepotBehaviour;setHeldItem(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;)V"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void beforeSetItem(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray, CallbackInfoReturnable<InteractionResult> cir, DepotBehaviour behaviour, ItemStack heldItem, boolean wasEmptyHanded, boolean shouldntPlaceItem, ItemStack mainItemStack, ItemStackHandler outputs, TransportedItemStack transported) {
        if (world.isClientSide()) return;

        ItemStack stack = transported.stack;

        String blockFullId = GetFullId.GetBlockFullId(state.getBlock());
        String dimId = world.dimension().location().toString();

        if (!stack.isEmpty()) {
            String addItemFullId = GetFullId.GetItemFullId(stack.getItem());

            LogQueue.push(new LogEntry(
                    player.getUUID(),
                    player.getName().getString(),
                    blockFullId,
                    dimId,
                    pos,
                    LogEntry.Action.INSERT.getValue(),
                    addItemFullId,
                    stack.getCount(),
                    stack.hasTag() ? stack.getTag().getAsString() : "{}",
                    System.currentTimeMillis()
            ));
        }
    }

}
