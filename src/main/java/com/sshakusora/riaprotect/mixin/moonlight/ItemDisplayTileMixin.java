package com.sshakusora.riaprotect.mixin.moonlight;

import com.sshakusora.riaprotect.log.LogEntry;
import com.sshakusora.riaprotect.log.LogQueue;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemDisplayTile.class)
public abstract class ItemDisplayTileMixin {
    @Shadow protected abstract NonNullList<ItemStack> getItems();

    @Inject(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;I)Lnet/minecraft/world/InteractionResult;", at = @At("RETURN"), remap = false)
    private void onInteractReturn(Player player, InteractionHand hand, int slot, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue().consumesAction() && !player.level().isClientSide) {
            ItemDisplayTile self = (ItemDisplayTile) (Object) this;

            ItemStack containerStack = this.getItems().get(slot);
            ItemStack handStack = player.getItemInHand(hand);
            String dimId = player.level().dimension().location().toString();

            String action;
            ItemStack loggedStack;

            if (containerStack.isEmpty()) {
                action = "EXTRACT";
                loggedStack = handStack;
            } else {
                action = "INSERT";
                loggedStack = containerStack;
            }

            if (!loggedStack.isEmpty()) {
                String itemFullId = Optional.ofNullable(ForgeRegistries.ITEMS.getKey(loggedStack.getItem()))
                        .map(ResourceLocation::toString)
                        .orElse("minecraft:air");

                String blockFullId = Optional.ofNullable(ForgeRegistries.BLOCKS.getKey(self.getBlockState().getBlock()))
                        .map(ResourceLocation::toString)
                        .orElse("minecraft:air");

                LogQueue.push(new LogEntry(
                        player.getUUID(),
                        player.getName().getString(),
                        blockFullId,
                        dimId,
                        self.getBlockPos(),
                        action,
                        itemFullId,
                        loggedStack.getCount(),
                        loggedStack.hasTag() ? loggedStack.getTag().getAsString() : "{}",
                        System.currentTimeMillis()
                ));
            }
        }
    }
}
