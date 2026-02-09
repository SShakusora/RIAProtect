package com.sshakusora.riaprotect.mixin.ae2.accessor;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.helpers.WirelessTerminalMenuHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WirelessTerminalMenuHost.class)
public interface WirelessTerminalMenuHostAccessor {
    @Accessor("myWap")
    IWirelessAccessPoint getMyWap();
}
