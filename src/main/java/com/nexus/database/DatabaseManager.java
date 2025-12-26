package com.nexus.database;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Database manager for NexusCore - handles all database operations using SQLite
 */
public class DatabaseManager {

    private final NexusCore plugin;
    private Connection connection;
    private String databasePath;

    // Table names
    private static final String PLAYERS_TABLE = "players";
    private static final String ECONOMY_TABLE = "economy";
    private static final String SKYBLOCK_TABLE = "skyblock_islands";
    private static final String WARP_TABLE = "warps";

    public DatabaseManager(NexusCore plugin) {
        this.plugin = plugin;
        this.databasePath = plugin.getDataFolder().getAbsolutePath() + "/nexus.db";
    }

    /**
     * Initialize database connection and create tables
     */
    public void initialize() {
        try {
            // Ensure data folder exists
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            // Load SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Create connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

            plugin.getNexusLogger().info("Database connection established");

        } catch (ClassNotFoundException | SQLException e) {
            plugin.getNexusLogger().log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Create all required tables
     */
    public void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Players table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS " + PLAYERS_TABLE + " (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "username VARCHAR(16) NOT NULL," +
                "first_join BIGINT NOT NULL," +
                "last_join BIGINT NOT NULL," +
                "play_time BIGINT DEFAULT 0," +
                "kills INT DEFAULT 0," +
                "deaths INT DEFAULT 0," +
                "games_played INT DEFAULT 0," +
                "wins INT DEFAULT 0" +
                ")"
            );

            // Economy table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS " + ECONOMY_TABLE + " (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "coins BIGINT DEFAULT 0," +
                "gems INT DEFAULT 0," +
                "last_claim BIGINT DEFAULT 0" +
                ")"
            );

            // SkyBlock islands table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS " + SKYBLOCK_TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "owner_uuid VARCHAR(36) NOT NULL," +
                "world_name VARCHAR(64) NOT NULL," +
                "center_x INT NOT NULL," +
                "center_z INT NOT NULL," +
                "created_at BIGINT NOT NULL," +
                "level INT DEFAULT 1," +
                "members TEXT DEFAULT '[]'," +
                "settings TEXT DEFAULT '{}'" +
                ")"
            );

            // Warps table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS " + WARP_TABLE + " (" +
                "name VARCHAR(32) PRIMARY KEY," +
                "world_name VARCHAR(64) NOT NULL," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "yaw FLOAT NOT NULL," +
                "pitch FLOAT NOT NULL," +
                "created_by VARCHAR(36) NOT NULL," +
                "created_at BIGINT NOT NULL," +
                "uses INT DEFAULT 0" +
                ")"
            );

            plugin.getNexusLogger().info("All database tables created/verified");

        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.SEVERE, "Failed to create tables", e);
        }
    }

    /**
     * Save player data
     */
    public void savePlayerData(Player player) {
        String sql = "INSERT OR REPLACE INTO " + PLAYERS_TABLE +
                    " (uuid, username, first_join, last_join, play_time, kills, deaths, games_played, wins) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Get existing data first
            Map<String, Object> existingData = getPlayerData(player.getUniqueId());

            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setLong(3, (Long) existingData.getOrDefault("first_join", System.currentTimeMillis()));
            stmt.setLong(4, System.currentTimeMillis());
            stmt.setLong(5, (Long) existingData.getOrDefault("play_time", 0L) +
                    (System.currentTimeMillis() - (Long) existingData.getOrDefault("last_join", System.currentTimeMillis())) / 1000);
            stmt.setInt(6, (Integer) existingData.getOrDefault("kills", 0));
            stmt.setInt(7, (Integer) existingData.getOrDefault("deaths", 0));
            stmt.setInt(8, (Integer) existingData.getOrDefault("games_played", 0));
            stmt.setInt(9, (Integer) existingData.getOrDefault("wins", 0));

            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to save player data for " + player.getName(), e);
        }
    }

    /**
     * Get player data from database
     */
    public Map<String, Object> getPlayerData(UUID uuid) {
        Map<String, Object> data = new HashMap<>();
        String sql = "SELECT * FROM " + PLAYERS_TABLE + " WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    data.put("uuid", rs.getString("uuid"));
                    data.put("username", rs.getString("username"));
                    data.put("first_join", rs.getLong("first_join"));
                    data.put("last_join", rs.getLong("last_join"));
                    data.put("play_time", rs.getLong("play_time"));
                    data.put("kills", rs.getInt("kills"));
                    data.put("deaths", rs.getInt("deaths"));
                    data.put("games_played", rs.getInt("games_played"));
                    data.put("wins", rs.getInt("wins"));
                } else {
                    // Return defaults for new player
                    data.put("first_join", System.currentTimeMillis());
                    data.put("last_join", System.currentTimeMillis());
                    data.put("play_time", 0L);
                    data.put("kills", 0);
                    data.put("deaths", 0);
                    data.put("games_played", 0);
                    data.put("wins", 0);
                }
            }

        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to get player data", e);
        }

        return data;
    }

    /**
     * Save all data (called periodically and on shutdown)
     */
    public void saveAllData() {
        try {
            // Save all online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                savePlayerData(player);
            }

            // Save economy data
            if (plugin.getEconomyManager() != null) {
                plugin.getEconomyManager().saveAllBalances();
            }

            plugin.getNexusLogger().info("All data saved successfully");

        } catch (Exception e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to save all data", e);
        }
    }

    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getNexusLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Error closing database connection", e);
        }
    }

    /**
     * Get connection
     */
    public Connection getConnection() {
        return connection;
    }

    // Table name getters
    public static String getPlayersTable() {
        return PLAYERS_TABLE;
    }

    public static String getEconomyTable() {
        return ECONOMY_TABLE;
    }

    public static String getSkyblockTable() {
        return SKYBLOCK_TABLE;
    }

    public static String getWarpTable() {
        return WARP_TABLE;
    }
}
