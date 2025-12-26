package com.nexus.stats;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for handling player scoreboards.
 * Creates and updates dynamic scoreboards for each player.
 */
public class ScoreboardManager {
    
    private final NexusCore plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private final Map<UUID, Objective> playerObjectives;
    private final Map<UUID, Map<String, Integer>> cachedScores;
    
    public ScoreboardManager(NexusCore plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new ConcurrentHashMap<>();
        this.playerObjectives = new ConcurrentHashMap<>();
        this.cachedScores = new ConcurrentHashMap<>();
    }
    
    /**
     * Create or update a player's scoreboard
     */
    public void updateScoreboard(Player player) {
        try {
            Scoreboard scoreboard = playerScoreboards.computeIfAbsent(player.getUniqueId(), 
                k -> Bukkit.getScoreboardManager().getNewScoreboard());
            
            // Create objective if doesn't exist or display name changed
            String objectiveName = "nexus_stats";
            Objective objective = scoreboard.getObjective(objectiveName);
            
            if (objective == null) {
                objective = scoreboard.registerNewObjective(objectiveName, "dummy",
                    getScoreboardTitle(player));
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else {
                objective.setDisplayName(getScoreboardTitle(player));
            }
            
            playerObjectives.put(player.getUniqueId(), objective);
            
            // Update scores
            updateScores(player, scoreboard, objective);
            
            // Set the scoreboard
            player.setScoreboard(scoreboard);
            
        } catch (Exception e) {
            plugin.getNexusLogger().warning("Failed to update scoreboard for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Get the scoreboard title based on player rank
     */
    private String getScoreboardTitle(Player player) {
        String rankPrefix = plugin.getRankManager().getPlayerPrefix(player.getUniqueId());
        if (rankPrefix.isEmpty()) {
            rankPrefix = ChatColor.GRAY + "[Member]";
        }
        return rankPrefix + " " + ChatColor.WHITE + "NexusBlock";
    }
    
    /**
     * Update scoreboard scores
     */
    private void updateScores(Player player, Scoreboard scoreboard, Objective objective) {
        Map<String, Integer> newScores = new HashMap<>();
        Map<String, Integer> oldScores = cachedScores.getOrDefault(player.getUniqueId(), new HashMap<>());
        
        // Get player data
        String rank = plugin.getRankManager().getPlayerRank(player.getUniqueId()).getName();
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        double skillAvg = plugin.getSkillsManager().getPlayerSkills(player.getUniqueId()).getAverageLevel();
        int islandLevel = 1;
        
        // Build scoreboard lines (top to bottom, reverse order for display)
        int line = 0;
        
        // Line 1: Empty separator
        newScores.put(ChatColor.GRAY + "─────────────────", line++);
        
        // Line 2: Rank
        newScores.put(ChatColor.WHITE + "Rank: " + getRankColor(rank) + rank, line++);
        
        // Line 3: Coins
        newScores.put(ChatColor.WHITE + "Coins: " + ChatColor.GOLD + String.format("%.0f", balance), line++);
        
        // Line 4: Skill Average
        newScores.put(ChatColor.WHITE + "Skill Avg: " + getSkillColor(skillAvg) + String.format("%.1f", skillAvg), line++);
        
        // Line 5: Island Level
        newScores.put(ChatColor.WHITE + "Island: " + ChatColor.AQUA + "Lv." + islandLevel, line++);
        
        // Line 6: Empty separator
        newScores.put(ChatColor.GRAY + "─────────────────", line++);
        
        // Line 7: Online players
        newScores.put(ChatColor.WHITE + "Online: " + ChatColor.GREEN + Bukkit.getOnlinePlayers().size(), line++);
        
        // Line 8: Website
        newScores.put(ChatColor.YELLOW + "www.nexusblock.net", line++);
        
        // Remove old scores that are no longer needed
        for (Map.Entry<String, Integer> entry : oldScores.entrySet()) {
            String objectiveName = entry.getKey();
            if (!newScores.containsKey(objectiveName)) {
                Team team = scoreboard.getTeam("score_" + objectiveName.hashCode());
                if (team != null) {
                    team.unregister();
                }
            }
        }
        
        // Update scores
        for (Map.Entry<String, Integer> entry : newScores.entrySet()) {
            String objectiveName = entry.getKey();
            int score = entry.getValue();
            
            // Create or update team for color handling
            String teamName = "score_" + Math.abs(objectiveName.hashCode());
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }
            
            // Remove all entries first
            for (String existingEntry : team.getEntries()) {
                team.removeEntry(existingEntry);
            }
            
            // Add entry to team and objective
            if (!scoreboard.getEntries().contains(objectiveName)) {
                team.addEntry(objectiveName);
            }
            objective.getScore(objectiveName).setScore(score);
        }
        
        cachedScores.put(player.getUniqueId(), newScores);
    }
    
    /**
     * Get color based on rank name
     */
    private ChatColor getRankColor(String rank) {
        return switch (rank.toUpperCase()) {
            case "OWNER" -> ChatColor.DARK_RED;
            case "ADMIN" -> ChatColor.RED;
            case "MODERATOR" -> ChatColor.DARK_GREEN;
            case "LEGEND" -> ChatColor.DARK_PURPLE;
            case "MVP+" -> ChatColor.DARK_AQUA;
            case "MVP" -> ChatColor.AQUA;
            case "VIP+" -> ChatColor.GOLD;
            case "VIP" -> ChatColor.YELLOW;
            default -> ChatColor.GRAY;
        };
    }
    
    /**
     * Get color based on skill average
     */
    private ChatColor getSkillColor(double skillAvg) {
        if (skillAvg >= 50) return ChatColor.DARK_PURPLE;
        if (skillAvg >= 40) return ChatColor.RED;
        if (skillAvg >= 30) ChatColor.GOLD;
        if (skillAvg >= 20) ChatColor.GREEN;
        if (skillAvg >= 10) ChatColor.AQUA;
        return ChatColor.WHITE;
    }
    
    /**
     * Clear a player's scoreboard
     */
    public void clearScoreboard(Player player) {
        UUID playerId = player.getUniqueId();
        
        Scoreboard scoreboard = playerScoreboards.get(playerId);
        if (scoreboard != null) {
            for (String entry : scoreboard.getEntries()) {
                Team team = scoreboard.getTeam(entry);
                if (team != null) {
                    team.unregister();
                }
            }
            
            Objective objective = scoreboard.getObjective("nexus_stats");
            if (objective != null) {
                objective.unregister();
            }
        }
        
        playerScoreboards.remove(playerId);
        playerObjectives.remove(playerId);
        cachedScores.remove(playerId);
        
        // Reset to main scoreboard
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
    
    /**
     * Update all online players' scoreboards
     */
    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }
    
    /**
     * Start automatic scoreboard update task
     */
    public void startAutoUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllScoreboards, 100L, 100L); // Every 5 seconds
    }
    
    /**
     * Shutdown the scoreboard manager
     */
    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            clearScoreboard(player);
        }
        playerScoreboards.clear();
        playerObjectives.clear();
        cachedScores.clear();
    }
}
