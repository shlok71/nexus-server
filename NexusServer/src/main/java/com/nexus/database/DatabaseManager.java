package com.nexus.database;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final String SKILLS_TABLE = "player_skills";
    private static final String ACHIEVEMENTS_TABLE = "player_achievements";

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

            // Skills table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS " + SKILLS_TABLE + " (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "skills_data TEXT NOT NULL" +
                ")"
            );

            // Achievements table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS " + ACHIEVEMENTS_TABLE + " (" +
                "id VARCHAR(64) NOT NULL," +
                "uuid VARCHAR(36) NOT NULL," +
                "name VARCHAR(64) NOT NULL," +
                "description TEXT," +
                "coins_reward INT DEFAULT 0," +
                "unlocked_at BIGINT NOT NULL," +
                "PRIMARY KEY (id, uuid)" +
                ")"
            );

            // Player mutes table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS player_mutes (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "muted_by VARCHAR(36) NOT NULL," +
                "reason TEXT," +
                "expires_at BIGINT DEFAULT -1" +
                ")"
            );

            // Guilds table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS guilds (" +
                "id VARCHAR(36) PRIMARY KEY," +
                "name VARCHAR(16) NOT NULL UNIQUE," +
                "tag VARCHAR(4) NOT NULL UNIQUE," +
                "data TEXT NOT NULL" +
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

    // Skills methods

    /**
     * Save player skills data
     */
    public void savePlayerSkills(UUID uuid, Map<String, Object> skillsData) {
        String sql = "INSERT OR REPLACE INTO " + SKILLS_TABLE + " (uuid, skills_data) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, new com.google.gson.Gson().toJson(skillsData));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to save player skills", e);
        }
    }

    /**
     * Get player skills data
     */
    public Map<String, Object> getPlayerSkills(UUID uuid) {
        Map<String, Object> data = new HashMap<>();
        String sql = "SELECT skills_data FROM " + SKILLS_TABLE + " WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("skills_data");
                    data = new com.google.gson.Gson().fromJson(json, new com.google.gson.TypeToken<Map<String, Object>>(){}.getType());
                }
            }
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to get player skills", e);
        }

        return data;
    }

    // Achievements methods

    /**
     * Save player achievement
     */
    public void saveAchievement(UUID uuid, com.nexus.skyblock.skills.achievements.AchievementManager.Achievement achievement) {
        String sql = "INSERT OR REPLACE INTO " + ACHIEVEMENTS_TABLE + " (id, uuid, name, description, coins_reward, unlocked_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, achievement.getId());
            stmt.setString(2, uuid.toString());
            stmt.setString(3, achievement.getName());
            stmt.setString(4, achievement.getDescription());
            stmt.setInt(5, achievement.getCoinsReward());
            stmt.setLong(6, achievement.getUnlockedAt());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to save achievement", e);
        }
    }

    /**
     * Get player achievements
     */
    public List<Map<String, Object>> getPlayerAchievements(UUID uuid) {
        List<Map<String, Object>> achievements = new ArrayList<>();
        String sql = "SELECT * FROM " + ACHIEVEMENTS_TABLE + " WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> achievement = new HashMap<>();
                    achievement.put("id", rs.getString("id"));
                    achievement.put("name", rs.getString("name"));
                    achievement.put("description", rs.getString("description"));
                    achievement.put("coinsReward", rs.getInt("coins_reward"));
                    achievement.put("unlockedAt", rs.getLong("unlocked_at"));
                    achievements.add(achievement);
                }
            }
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to get player achievements", e);
        }

        return achievements;
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

    public static String getSkillsTable() {
        return SKILLS_TABLE;
    }

    public static String getAchievementsTable() {
        return ACHIEVEMENTS_TABLE;
    }

    // Player Rank methods

    /**
     * Save player rank to database
     */
    public void savePlayerRank(UUID uuid, String rankId) {
        String sql = "INSERT OR REPLACE INTO " + PLAYERS_TABLE + 
                    " (uuid, rank_id) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, rankId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to save player rank", e);
        }
    }

    /**
     * Get player rank from database
     */
    public String getPlayerRank(UUID uuid) {
        String rankId = null;
        String sql = "SELECT rank_id FROM " + PLAYERS_TABLE + " WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rankId = rs.getString("rank_id");
                }
            }
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to get player rank", e);
        }

        return rankId;
    }

    // Player Stats methods

    /**
     * Save player stats to database
     */
    public void savePlayerStats(UUID uuid, Map<String, Object> statsData) {
        String sql = "INSERT OR REPLACE INTO " + PLAYERS_TABLE + 
                    " (uuid, stats_data) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, new com.google.gson.Gson().toJson(statsData));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to save player stats", e);
        }
    }

    /**
     * Get player stats from database
     */
    public Map<String, Object> getPlayerStats(UUID uuid) {
        Map<String, Object> data = new HashMap<>();
        String sql = "SELECT stats_data FROM " + PLAYERS_TABLE + " WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("stats_data");
                    data = new com.google.gson.Gson().fromJson(json, new com.google.gson.TypeToken<Map<String, Object>>(){}.getType());
                }
            }
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to get player stats", e);
        }

        return data;
    }

    // Mute system methods

    /**
     * Save mute data to database
     */
    public void saveMute(UUID playerId, com.nexus.staff.StaffCommand.MuteData muteData) {
        String sql = "INSERT OR REPLACE INTO player_mutes (uuid, muted_by, reason, expires_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, muteData.getMutedBy());
            stmt.setString(3, muteData.getReason());
            stmt.setLong(4, muteData.getExpiresAt());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to save mute data", e);
        }
    }

    /**
     * Get mute data from database
     */
    public com.nexus.staff.StaffCommand.MuteData getMute(UUID playerId) {
        String sql = "SELECT * FROM player_mutes WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new com.nexus.staff.StaffCommand.MuteData(
                        playerId,
                        rs.getString("muted_by"),
                        rs.getString("reason"),
                        rs.getLong("expires_at")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to get mute data", e);
        }

        return null;
    }

    /**
     * Remove mute from database
     */
    public void removeMute(UUID playerId) {
        String sql = "DELETE FROM player_mutes WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to remove mute data", e);
        }
    }

    // Guild system methods

    /**
     * Save guild to database
     */
    public void saveGuild(com.nexus.guilds.Guild guild) {
        String sql = "INSERT OR REPLACE INTO guilds (id, name, tag, data) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, guild.getId().toString());
            stmt.setString(2, guild.getName());
            stmt.setString(3, guild.getTag());
            stmt.setString(4, new com.google.gson.Gson().toJson(guild.serialize()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to save guild", e);
        }
    }

    /**
     * Get all guilds from database
     */
    public List<Map<String, Object>> getAllGuilds() {
        List<Map<String, Object>> guilds = new ArrayList<>();
        String sql = "SELECT data FROM guilds";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String json = rs.getString("data");
                Map<String, Object> data = new com.google.gson.Gson().fromJson(
                    json, new com.google.gson.TypeToken<Map<String, Object>>(){}.getType());
                guilds.add(data);
            }
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to get guilds", e);
        }

        return guilds;
    }

    /**
     * Delete guild from database
     */
    public void deleteGuild(UUID guildId) {
        String sql = "DELETE FROM guilds WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, guildId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to delete guild", e);
        }
    }

    /**
     * Get guild by name
     */
    public Map<String, Object> getGuildByName(String name) {
        String sql = "SELECT data FROM guilds WHERE name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("data");
                    return new com.google.gson.Gson().fromJson(
                        json, new com.google.gson.TypeToken<Map<String, Object>>(){}.getType());
                }
            }
        } catch (SQLException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to get guild by name", e);
        }

        return null;
    }
}
