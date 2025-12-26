package com.nexus.stats;

import com.nexus.core.NexusCore;
import com.nexus.skyblock.skills.PlayerSkills;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for handling all player statistics.
 * Tracks stats, manages leaderboards, and handles persistence.
 */
public class StatsManager {
    
    private final NexusCore plugin;
    private final Map<UUID, PlayerStats> playerStats;
    private final Map<String, Map<UUID, Long>> leaderboardCache;
    
    public StatsManager(NexusCore plugin) {
        this.plugin = plugin;
        this.playerStats = new ConcurrentHashMap<>();
        this.leaderboardCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize a player's stats
     */
    public void initializePlayer(UUID playerId, String playerName) {
        playerStats.computeIfAbsent(playerId, k -> {
            PlayerStats stats = new PlayerStats(playerId);
            stats.setPlayerName(playerName);
            
            // Try loading from database
            Map<String, Object> data = plugin.getDatabaseManager().getPlayerStats(playerId);
            if (data != null && !data.isEmpty()) {
                stats.deserialize(data);
            }
            
            return stats;
        });
    }
    
    /**
     * Get a player's stats
     */
    public PlayerStats getPlayerStats(UUID playerId) {
        return playerStats.computeIfAbsent(playerId, k -> {
            PlayerStats stats = new PlayerStats(playerId);
            
            // Try loading from database
            Map<String, Object> data = plugin.getDatabaseManager().getPlayerStats(playerId);
            if (data != null && !data.isEmpty()) {
                stats.deserialize(data);
            }
            
            return stats;
        });
    }
    
    /**
     * Update player stats on kill
     */
    public void onPlayerKill(UUID killerId, UUID victimId) {
        PlayerStats killerStats = getPlayerStats(killerId);
        killerStats.addKill();
        
        PlayerStats victimStats = getPlayerStats(victimId);
        victimStats.addDeath();
        
        // Update skill average
        updateSkillAverage(killerId);
        updateSkillAverage(victimId);
        
        // Save to database
        savePlayerStats(killerId);
        savePlayerStats(victimId);
        
        // Invalidate leaderboard cache
        invalidateLeaderboard("kills");
        invalidateLeaderboard("deaths");
    }
    
    /**
     * Update player stats on block break
     */
    public void onBlockBreak(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addBlockBroken();
        savePlayerStats(playerId);
    }
    
    /**
     * Update player stats on block place
     */
    public void onBlockPlace(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addBlockPlaced();
        savePlayerStats(playerId);
    }
    
    /**
     * Update player stats on mob kill
     */
    public void onMobKill(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addMobKilled();
        savePlayerStats(playerId);
    }
    
    /**
     * Update player stats on fish caught
     */
    public void onFishCaught(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addFishCaught();
        savePlayerStats(playerId);
    }
    
    /**
     * Update player stats on item crafted
     */
    public void onItemCrafted(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addItemCrafted();
        savePlayerStats(playerId);
    }
    
    /**
     * Update player stats on game played
     */
    public void onGamePlayed(UUID playerId, boolean won) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addGamePlayed();
        if (won) {
            stats.addWin();
        } else {
            stats.addLoss();
        }
        savePlayerStats(playerId);
        invalidateLeaderboard("wins");
        invalidateLeaderboard("losses");
    }
    
    /**
     * Update player stats on island created
     */
    public void onIslandCreated(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addIslandCreated();
        savePlayerStats(playerId);
    }
    
    /**
     * Update skill average from skills system
     */
    public void updateSkillAverage(UUID playerId) {
        try {
            PlayerSkills skills = plugin.getSkillsManager().getPlayerSkills(playerId);
            PlayerStats stats = getPlayerStats(playerId);
            stats.setSkillAverage(skills.getAverageLevel());
            savePlayerStats(playerId);
        } catch (Exception e) {
            // Skills might not be initialized yet
        }
    }
    
    /**
     * Update play time for a player
     */
    public void updatePlayTime(UUID playerId, long seconds) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addPlayTime(seconds);
        savePlayerStats(playerId);
    }
    
    /**
     * Update coins earned/spent
     */
    public void onCoinsEarned(UUID playerId, long amount) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addCoinsEarned(amount);
        savePlayerStats(playerId);
        invalidateLeaderboard("coins");
    }
    
    public void onCoinsSpent(UUID playerId, long amount) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.addCoinsSpent(amount);
        savePlayerStats(playerId);
    }
    
    /**
     * Get leaderboard for a category
     */
    public List<StatsCommand.StatsLeaderboardEntry> getLeaderboard(String category, int limit) {
        // Check cache first
        Map<UUID, Long> cached = leaderboardCache.get(category);
        if (cached != null) {
            return buildLeaderboardFromCache(cached, limit);
        }
        
        // Build from player stats
        Map<UUID, Long> values = new HashMap<>();
        
        for (Map.Entry<UUID, PlayerStats> entry : playerStats.entrySet()) {
            PlayerStats stats = entry.getValue();
            long value = getStatValue(stats, category);
            values.put(entry.getKey(), value);
        }
        
        // Cache the result
        leaderboardCache.put(category, values);
        
        return buildLeaderboardFromCache(values, limit);
    }
    
    /**
     * Get a specific stat value from PlayerStats
     */
    private long getStatValue(PlayerStats stats, String category) {
        return switch (category.toLowerCase()) {
            case "kills" -> stats.getKills();
            case "deaths" -> stats.getDeaths();
            case "wins" -> stats.getWins();
            case "losses" -> stats.getLosses();
            case "games", "gamesplayed" -> stats.getGamesPlayed();
            case "coins" -> stats.getCoinsEarned();
            case "playtime" -> stats.getPlayTime();
            case "blocks_broken" -> stats.getBlocksBroken();
            case "blocks_placed" -> stats.getBlocksPlaced();
            case "items_crafted" -> stats.getItemsCrafted();
            case "mobs_killed" -> stats.getMobsKilled();
            case "fish_caught" -> stats.getFishCaught();
            case "islands" -> stats.getIslandsCreated();
            default -> 0;
        };
    }
    
    /**
     * Build leaderboard from cached values
     */
    private List<StatsCommand.StatsLeaderboardEntry> buildLeaderboardFromCache(Map<UUID, Long> values, int limit) {
        List<StatsCommand.StatsLeaderboardEntry> entries = new ArrayList<>();
        
        for (Map.Entry<UUID, Long> entry : values.entrySet()) {
            UUID playerId = entry.getKey();
            String playerName = getPlayerName(playerId);
            entries.add(new StatsCommand.StatsLeaderboardEntry(playerId, playerName, entry.getValue()));
        }
        
        // Sort by value descending
        entries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        
        return entries.subList(0, Math.min(limit, entries.size()));
    }
    
    /**
     * Get player name from cache or UUID
     */
    private String getPlayerName(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            return player.getName();
        }
        
        PlayerStats stats = playerStats.get(playerId);
        if (stats != null) {
            return stats.getPlayerName();
        }
        
        return "Unknown";
    }
    
    /**
     * Invalidate leaderboard cache for a category
     */
    public void invalidateLeaderboard(String category) {
        leaderboardCache.remove(category);
    }
    
    /**
     * Invalidate all leaderboard caches
     */
    public void invalidateAllLeaderboards() {
        leaderboardCache.clear();
    }
    
    /**
     * Save player stats to database
     */
    public void savePlayerStats(UUID playerId) {
        PlayerStats stats = playerStats.get(playerId);
        if (stats != null) {
            plugin.getDatabaseManager().savePlayerStats(playerId, stats.serialize());
        }
    }
    
    /**
     * Save all player stats
     */
    public void saveAllStats() {
        for (UUID playerId : playerStats.keySet()) {
            savePlayerStats(playerId);
        }
    }
    
    /**
     * Shutdown the stats manager
     */
    public void shutdown() {
        saveAllStats();
        playerStats.clear();
        leaderboardCache.clear();
    }
}
