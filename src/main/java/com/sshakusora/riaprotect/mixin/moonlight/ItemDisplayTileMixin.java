package com.sshakusora.riaprotect.mixin.moonlight;

import com.sshakusora.riaprotect.log.LogEntry;
import com.sshakusora.riaprotect.log.LogQueue;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(ItemDisplayTile.class)
public abstract class ItemDisplayTileMixin {
    @Inject(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;I)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void beforeSetItemInHand(Player player, InteractionHand handIn, int slot, CallbackInfoReturnable<InteractionResult> cir, ItemStack handItem, ItemStack it) {
        if (player.level().isClientSide()) return;

        ItemDisplayTile self = (ItemDisplayTile) (Object) this;
        String blockFullId = Optional.ofNullable(ForgeRegistries.BLOCKS.getKey(self.getBlockState().getBlock()))
                .map(ResourceLocation::toString)
                .orElse("minecraft:air");
        String dimId = player.level().dimension().location().toString();

        if (!it.isEmpty()) {
            String itemFullId = Optional.ofNullable(ForgeRegistries.ITEMS.getKey(it.getItem()))
                    .map(ResourceLocation::toString)
                    .orElse("minecraft:air");

            LogQueue.push(new LogEntry(
                    player.getUUID(),
                    player.getName().getString(),
                    blockFullId,
                    dimId,
                    self.getBlockPos(),
                    LogEntry.Action.EXTRACT.getValue(),
                    itemFullId,
                    it.getCount(),
                    it.hasTag() ? it.getTag().getAsString() : "{}",
                    System.currentTimeMillis()
            ));
        }
    }

    @Inject(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;I)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/moonlight/api/block/ItemDisplayTile;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void beforeSetItem(Player player, InteractionHand handIn, int slot, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.level().isClientSide()) return;

        ItemDisplayTile self = (ItemDisplayTile) (Object) this;
        String blockFullId = Optional.ofNullable(ForgeRegistries.BLOCKS.getKey(self.getBlockState().getBlock()))
                .map(ResourceLocation::toString)
                .orElse("minecraft:air");
        String dimId = player.level().dimension().location().toString();
        ItemStack handItem = player.getItemInHand(handIn);

        if (!handItem.isEmpty()) {
            String itemFullId = Optional.ofNullable(ForgeRegistries.ITEMS.getKey(handItem.getItem()))
                    .map(ResourceLocation::toString)
                    .orElse("minecraft:air");

            LogQueue.push(new LogEntry(
                    player.getUUID(),
                    player.getName().getString(),
                    blockFullId,
                    dimId,
                    self.getBlockPos(),
                    LogEntry.Action.INSERT.getValue(),
                    itemFullId,
                    1,
                    handItem.hasTag() ? handItem.getTag().getAsString() : "{}",
                    System.currentTimeMillis()
            ));
        }
    }
}
