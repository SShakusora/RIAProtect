package com.sshakusora.riaprotect.log;

import net.minecraft.core.BlockPos;

import java.util.UUID;

public record LogEntry(
        UUID playerUUID,
        String playerName,
        String blockId,
        String level,
        BlockPos pos,
        String action,
        String itemId,
        int count,
        String nbtData,
        long timestamp
) {
    public String getFormattedPos() {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public enum Action {
        INSERT("INSERT"),
        EXTRACT("EXTRACT");

        private final String value;

        Action(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Action fromString(String text) {
            for (Action a : Action.values()) {
                if (a.value.equalsIgnoreCase(text)) {
                    return a;
                }
            }
            return null;
        }
    }
}
