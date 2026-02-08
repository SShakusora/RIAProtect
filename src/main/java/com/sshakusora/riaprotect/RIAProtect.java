package com.sshakusora.riaprotect;

import com.mojang.logging.LogUtils;
import com.sshakusora.riaprotect.database.DatabaseHandler;
import com.sshakusora.riaprotect.log.LogQueue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Path;

@Mod(RIAProtect.MODID)
public class RIAProtect {
    public static final String MODID = "riaprotect";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RIAProtect() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        try {
            Path dbPath = FMLPaths.GAMEDIR.get().resolve("riaprotect.db");
            DatabaseHandler.init(dbPath.toString());
            LogQueue.startWorker();
        } catch (Exception e) {
            LOGGER.error("RIAProtect failed to initialize database!", e);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("RIAProtect: Closing database connections...");
        DatabaseHandler.shutdown();
    }
}
