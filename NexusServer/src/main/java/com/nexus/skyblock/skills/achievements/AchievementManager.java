package com.nexus.skyblock.skills.achievements;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager class for handling player achievements.
 * Tracks completed achievements and grants rewards.
 */
public class AchievementManager {
    
    private final NexusCore plugin;
    private final Map<UUID, Map<String, Achievement>> playerAchievements;
    
    public AchievementManager(NexusCore plugin) {
        this.plugin = plugin;
        this.playerAchievements = new HashMap<>();
    }
    
    /**
     * Check and grant skill-related achievements
     */
    public void checkSkillAchievements(Player player, com.nexus.skyblock.skills.SkillType skillType, int newLevel) {
        UUID playerId = player.getUniqueId();
        Map<String, Achievement> achievements = getPlayerAchievements(playerId);
        
        // Check for milestone achievements
        if (newLevel == 10) {
            grantAchievement(player, "SKILL_" + skillType.name() + "_10", 
                skillType.getDisplayName() + " Expert", 
                "Reach level 10 in " + skillType.getDisplayName(),
                1000);
        }
        if (newLevel == 25) {
            grantAchievement(player, "SKILL_" + skillType.name() + "_25",
                skillType.getDisplayName() + " Master",
                "Reach level 25 in " + skillType.getDisplayName(),
                5000);
        }
        if (newLevel == 50 && skillType.getMaxLevel() >= 50) {
            grantAchievement(player, "SKILL_" + skillType.name() + "_50",
                skillType.getDisplayName() + " Grandmaster",
                "Reach level 50 in " + skillType.getDisplayName(),
                25000);
        }
    }
    
    /**
     * Check and grant general achievements
     */
    public void checkGeneralAchievements(Player player, String eventType, Object data) {
        UUID playerId = player.getUniqueId();
        
        switch (eventType) {
            case "FIRST_JOIN" -> {
                grantAchievement(player, "FIRST_JOIN", "Welcome!",
                    "Join the server for the first time", 100);
            }
            case "FIRST_ISLAND" -> {
                grantAchievement(player, "FIRST_ISLAND", "Home Sweet Home",
                    "Create your first island", 500);
            }
            case "FIRST_MINION" -> {
                grantAchievement(player, "FIRST_MINION", "Helper",
                    "Place your first minion", 250);
            }
            case "FIRST_QUEST" -> {
                grantAchievement(player, "FIRST_QUEST", "Adventurer",
                    "Complete your first quest", 300);
            }
            case "FIRST_SHOP" -> {
                grantAchievement(player, "FIRST_SHOP", "Merchant",
                    "Buy something from a shop", 200);
            }
            case "FIRST_SELL" -> {
                grantAchievement(player, "FIRST_SELL", "Entrepreneur",
                    "Sell your first item", 150);
            }
            case "FIRST_DOLLAR" -> {
                if ((double) data >= 1000) {
                    grantAchievement(player, "FIRST_DOLLAR", "Getting Rich",
                        "Earn your first 1,000 coins", 500);
                }
            }
            case "FIRST_MILLION" -> {
                if ((double) data >= 1000000) {
                    grantAchievement(player, "FIRST_MILLION", "Millionaire",
                        "Earn 1,000,000 coins", 50000);
                }
            }
        }
    }
    
    /**
     * Grant an achievement to a player
     */
    private void grantAchievement(Player player, String id, String name, String description, int coinsReward) {
        UUID playerId = player.getUniqueId();
        Map<String, Achievement> achievements = getPlayerAchievements(playerId);
        
        if (achievements.containsKey(id)) {
            return; // Already have this achievement
        }
        
        Achievement achievement = new Achievement(id, name, description, coinsReward);
        achievements.put(id, achievement);
        
        // Save to database
        plugin.getDatabaseManager().saveAchievement(playerId, achievement);
        
        // Give reward
        if (coinsReward > 0 && plugin.getEconomyManager() != null) {
            plugin.getEconomyManager().depositCoins(playerId, coinsReward);
        }
        
        // Notify player
        player.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + "★ ACHIEVEMENT UNLOCKED! ★\n");
        player.sendMessage(ChatColor.GOLD + "» " + ChatColor.WHITE + name);
        player.sendMessage(ChatColor.GRAY + "  " + description);
        if (coinsReward > 0) {
            player.sendMessage(ChatColor.GREEN + "  +" + coinsReward + " coins");
        }
        player.sendMessage("");
        
        // Play sound
        player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.0f);
    }
    
    /**
     * Get player's achievements
     */
    private Map<String, Achievement> getPlayerAchievements(UUID playerId) {
        return playerAchievements.computeIfAbsent(playerId, k -> new HashMap<>());
    }
    
    /**
     * Load player achievements from database
     */
    public void loadPlayerAchievements(UUID playerId, java.util.List<Map<String, Object>> data) {
        Map<String, Achievement> achievements = new HashMap<>();
        for (Map<String, Object> achievementData : data) {
            Achievement achievement = new Achievement();
            achievement.deserialize(achievementData);
            achievements.put(achievement.getId(), achievement);
        }
        playerAchievements.put(playerId, achievements);
    }
    
    /**
     * Get count of player's achievements
     */
    public int getAchievementCount(UUID playerId) {
        Map<String, Achievement> achievements = playerAchievements.get(playerId);
        return achievements != null ? achievements.size() : 0;
    }
    
    /**
     * Get total coins earned from achievements
     */
    public int getTotalAchievementCoins(UUID playerId) {
        Map<String, Achievement> achievements = playerAchievements.get(playerId);
        if (achievements == null) {
            return 0;
        }
        return achievements.values().stream()
            .mapToInt(Achievement::getCoinsReward)
            .sum();
    }
    
    /**
     * Achievement class
     */
    public static class Achievement {
        private String id;
        private String name;
        private String description;
        private int coinsReward;
        private long unlockedAt;
        
        public Achievement() {
            this.unlockedAt = System.currentTimeMillis();
        }
        
        public Achievement(String id, String name, String description, int coinsReward) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.coinsReward = coinsReward;
            this.unlockedAt = System.currentTimeMillis();
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getCoinsReward() {
            return coinsReward;
        }
        
        public long getUnlockedAt() {
            return unlockedAt;
        }
        
        public Map<String, Object> serialize() {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("name", name);
            data.put("description", description);
            data.put("coinsReward", coinsReward);
            data.put("unlockedAt", unlockedAt);
            return data;
        }
        
        @SuppressWarnings("unchecked")
        public void deserialize(Map<String, Object> data) {
            this.id = (String) data.get("id");
            this.name = (String) data.get("name");
            this.description = (String) data.get("description");
            this.coinsReward = ((Number) data.get("coinsReward")).intValue();
            this.unlockedAt = ((Number) data.get("unlockedAt")).longValue();
        }
    }
}
