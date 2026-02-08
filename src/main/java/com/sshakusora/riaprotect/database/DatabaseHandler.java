package com.sshakusora.riaprotect.database;

import com.sshakusora.riaprotect.log.LogEntry;
import org.sqlite.JDBC;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class DatabaseHandler {
    private static Connection connection;

    public static void init(String path) throws SQLException {
        File dbFile = new File(path);
        if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        try {
            Driver driver = new JDBC();
            connection = driver.connect("jdbc:sqlite:" + path, new Properties());
        } catch (Exception e) {
            throw new SQLException("Failed to manually register SQLite driver", e);
        }

        if (connection == null) {
            throw new SQLException("Could not establish database connection to: " + path);
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS container_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid TEXT,
                    player_name TEXT,
                    block TEXT,
                    level TEXT,
                    pos TEXT,
                    action TEXT,
                    item TEXT,
                    count INTEGER,
                    nbt TEXT,
                    time DATETIME
                )
            """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_pos ON container_logs(pos)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_time ON container_logs(time)");
        }
    }

    public static void saveBatch(List<LogEntry> entries) {
        String sql = "INSERT INTO container_logs(uuid, player_name, block, level, pos, action, item, count, nbt, time) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            for (LogEntry entry : entries) {
                pstmt.setString(1, entry.playerUUID().toString());
                pstmt.setString(2, entry.playerName());
                pstmt.setString(3, entry.blockId());
                pstmt.setString(4, entry.level());
                pstmt.setString(5, entry.getFormattedPos());
                pstmt.setString(6, entry.action());
                pstmt.setString(7, entry.itemId());
                pstmt.setInt(8, entry.count());
                pstmt.setString(9, entry.nbtData());
                pstmt.setLong(10, entry.timestamp());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}
