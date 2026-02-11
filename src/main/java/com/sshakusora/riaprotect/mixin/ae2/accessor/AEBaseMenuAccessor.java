package com.sshakusora.riaprotect.mixin.ae2.accessor;

import appeng.api.networking.security.IActionHost;
import appeng.menu.AEBaseMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AEBaseMenu.class)
public interface AEBaseMenuAccessor {
    @Invoker("getActionHost")
    IActionHost callGetActionHost();
}
