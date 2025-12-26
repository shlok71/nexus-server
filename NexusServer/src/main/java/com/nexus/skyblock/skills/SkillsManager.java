package com.nexus.skyblock.skills;

import com.nexus.core.NexusCore;
import com.nexus.skyblock.skills.achievements.AchievementManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FishEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for handling all skill-related operations.
 * Tracks player skills, handles XP gain, and manages skill bonuses.
 */
public class SkillsManager {
    
    private final NexusCore plugin;
    private final Map<UUID, PlayerSkills> playerSkills;
    private final Map<Material, SkillType> blockSkills;
    private final Map<EntityType, SkillType> killSkills;
    private final Map<Material, Double> farmingSkills;
    
    public SkillsManager(NexusCore plugin) {
        this.plugin = plugin;
        this.playerSkills = new ConcurrentHashMap<>();
        this.blockSkills = new HashMap<>();
        this.killSkills = new HashMap<>();
        this.farmingSkills = new HashMap<>();
        registerSkillMappings();
    }
    
    /**
     * Register mappings between materials/actions and skills
     */
    private void registerSkillMappings() {
        // Mining skills
        blockSkills.put(Material.STONE, SkillType.MINING);
        blockSkills.put(Material.COAL_ORE, SkillType.MINING);
        blockSkills.put(Material.IRON_ORE, SkillType.MINING);
        blockSkills.put(Material.GOLD_ORE, SkillType.MINING);
        blockSkills.put(Material.DIAMOND_ORE, SkillType.MINING);
        blockSkills.put(Material.EMERALD_ORE, SkillType.MINING);
        blockSkills.put(Material.REDSTONE_ORE, SkillType.MINING);
        blockSkills.put(Material.LAPIS_ORE, SkillType.MINING);
        blockSkills.put(Material.NETHER_QUARTZ_ORE, SkillType.MINING);
        blockSkills.put(Material.OBSIDIAN, SkillType.MINING);
        blockSkills.put(Material.END_STONE, SkillType.MINING);
        blockSkills.put(Material.COBBLESTONE, SkillType.MINING);
        blockSkills.put(Material.MOSSY_COBBLESTONE, SkillType.MINING);
        blockSkills.put(Material.NETHERRACK, SkillType.MINING);
        
        // Foraging skills
        blockSkills.put(Material.LOG, SkillType.FORAGING);
        blockSkills.put(Material.LOG_2, SkillType.FORAGING);
        blockSkills.put(Material.LEAVES, SkillType.FORAGING);
        blockSkills.put(Material.LEAVES_2, SkillType.FORAGING);
        blockSkills.put(Material.SAPLING, SkillType.FORAGING);
        blockSkills.put(Material.VINE, SkillType.FORAGING);
        
        // Farming skills
        blockSkills.put(Material.WHEAT, SkillType.FARMING);
        blockSkills.put(Material.CARROTS, SkillType.FARMING);
        blockSkills.put(Material.POTATOES, SkillType.FARMING);
        blockSkills.put(Material.BEETROOTS, SkillType.FARMING);
        blockSkills.put(Material.PUMPKIN, SkillType.FARMING);
        blockSkills.put(Material.MELON, SkillType.FARMING);
        blockSkills.put(Material.SUGAR_CANE, SkillType.FARMING);
        blockSkills.put(Material.CACTUS, SkillType.FARMING);
        blockSkills.put(Material.NETHER_WART, SkillType.FARMING);
        
        // Farming XP from harvesting crops
        farmingSkills.put(Material.WHEAT, 15.0);
        farmingSkills.put(Material.CARROTS, 15.0);
        farmingSkills.put(Material.POTATOES, 15.0);
        farmingSkills.put(Material.BEETROOTS, 20.0);
        farmingSkills.put(Material.PUMPKIN, 30.0);
        farmingSkills.put(Material.MELON, 20.0);
        farmingSkills.put(Material.NETHER_WART, 25.0);
        
        // Combat skills (from mob kills)
        killSkills.put(EntityType.ZOMBIE, SkillType.COMBAT);
        killSkills.put(EntityType.SKELETON, SkillType.COMBAT);
        killSkills.put(EntityType.CREEPER, SkillType.COMBAT);
        killSkills.put(EntityType.SPIDER, SkillType.COMBAT);
        killSkills.put(EntityType.ENDERMAN, SkillType.COMBAT);
        killSkills.put(EntityType.BLAZE, SkillType.COMBAT);
        killSkills.put(EntityType.MAGMA_CUBE, SkillType.COMBAT);
        killSkills.put(EntityType.GHAST, SkillType.COMBAT);
        killSkills.put(EntityType.WITHER_SKELETON, SkillType.COMBAT);
        killSkills.put(EntityType.SHULKER, SkillType.COMBAT);
        killSkills.put(EntityType.VILLAGER, SkillType.COMBAT);
        killSkills.put(EntityType.PLAYER, SkillType.COMBAT);
    }
    
    /**
     * Initialize skills for a player
     */
    public void initializePlayer(UUID playerId) {
        playerSkills.putIfAbsent(playerId, new PlayerSkills());
    }
    
    /**
     * Get or create player skills
     */
    public PlayerSkills getPlayerSkills(UUID playerId) {
        return playerSkills.computeIfAbsent(playerId, k -> new PlayerSkills());
    }
    
    /**
     * Handle block break for mining/foraging XP
     */
    public void handleBlockBreak(UUID playerId, BlockBreakEvent event) {
        Material blockType = event.getBlock().getType();
        SkillType skillType = blockSkills.get(blockType);
        
        if (skillType != null) {
            double xpAmount = calculateXpGain(blockType, skillType);
            addSkillXp(playerId, skillType, xpAmount, event.getBlock().getLocation());
        }
    }
    
    /**
     * Handle crop harvest for farming XP
     */
    public void handleHarvest(UUID playerId, Material cropType) {
        SkillType skillType = SkillType.FARMING;
        Double xpAmount = farmingSkills.get(cropType);
        
        if (xpAmount != null) {
            addSkillXp(playerId, skillType, xpAmount, null);
        }
    }
    
    /**
     * Handle mob kill for combat XP
     */
    public void handleMobKill(UUID playerId, EntityDeathEvent event) {
        EntityType entityType = event.getEntity().getType();
        SkillType skillType = killSkills.get(entityType);
        
        if (skillType != null) {
            double xpAmount = calculateKillXp(entityType);
            addSkillXp(playerId, skillType, xpAmount, event.getEntity().getLocation());
        }
    }
    
    /**
     * Handle fishing for fishing XP
     */
    public void handleFishing(UUID playerId, PlayerFishEvent event) {
        if (event.getState() == FishEvent.State.CAUGHT_FISH || 
            event.getState() == FishEvent.State.CAUGHT_ENTITY ||
            event.getState() == FishEvent.State.CAUGHT_THING) {
            addSkillXp(playerId, SkillType.FISHING, 15.0, event.getHook().getLocation());
        }
    }
    
    /**
     * Handle enchanting for enchanting XP
     */
    public void handleEnchant(UUID playerId, EnchantItemEvent event) {
        addSkillXp(playerId, SkillType.ENCHANTING, 20.0, event.getEnchantBlock().getLocation());
    }
    
    /**
     * Calculate XP gain based on material and skill
     */
    private double calculateXpGain(Material material, SkillType skillType) {
        // Base XP varies by material
        return switch (material) {
            case COAL_ORE -> 10.0;
            case IRON_ORE -> 15.0;
            case GOLD_ORE -> 25.0;
            case DIAMOND_ORE -> 50.0;
            case EMERALD_ORE -> 75.0;
            case REDSTONE_ORE -> 12.0;
            case LAPIS_ORE -> 20.0;
            case NETHER_QUARTZ_ORE -> 18.0;
            case OBSIDIAN -> 40.0;
            case END_STONE -> 35.0;
            case COBBLESTONE, MOSSY_COBBLESTONE, NETHERRACK -> 5.0;
            case LOG, LOG_2 -> 15.0;
            case LEAVES, LEAVES_2 -> 2.0;
            case SAPLING -> 5.0;
            case VINE -> 3.0;
            default -> 10.0;
        };
    }
    
    /**
     * Calculate XP from killing an entity
     */
    private double calculateKillXp(EntityType entityType) {
        return switch (entityType) {
            case ZOMBIE, SKELETON, CREEPER, SPIDER -> 15.0;
            case ENDERMAN, BLAZE, MAGMA_CUBE -> 25.0;
            case GHAST -> 30.0;
            case WITHER_SKELETON, SHULKER -> 50.0;
            case VILLAGER -> 5.0;
            case PLAYER -> 100.0;
            default -> 10.0;
        };
    }
    
    /**
     * Add XP to a player's skill and handle level ups
     */
    private void addSkillXp(UUID playerId, SkillType skillType, double amount, org.bukkit.Location location) {
        PlayerSkills skills = getPlayerSkills(playerId);
        boolean leveledUp = skills.addXp(skillType, amount);
        
        if (leveledUp) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                int newLevel = skills.getSkill(skillType).getLevel();
                player.sendMessage(plugin.getConfigManager().getMessage("skills.levelUp")
                    .replace("%skill%", skillType.getDisplayName())
                    .replace("%level%", String.valueOf(newLevel)));
                
                // Play level up sound
                player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.0f);
                
                // Check for achievements
                AchievementManager achievementManager = plugin.getAchievementManager();
                if (achievementManager != null) {
                    achievementManager.checkSkillAchievements(player, skillType, newLevel);
                }
            }
        }
    }
    
    /**
     * Set skill level for a player (admin command)
     */
    public void setSkillLevel(UUID playerId, SkillType skillType, int level) {
        PlayerSkills skills = getPlayerSkills(playerId);
        skills.setLevel(skillType, level);
    }
    
    /**
     * Get skill bonus for a player and skill
     */
    public double getSkillBonus(UUID playerId, SkillType skillType) {
        PlayerSkills skills = getPlayerSkills(playerId);
        return skillType.getSkillBonus(skills.getSkill(skillType).getLevel());
    }
    
    /**
     * Save player skills data
     */
    public void savePlayerSkills(UUID playerId) {
        PlayerSkills skills = playerSkills.get(playerId);
        if (skills != null) {
            plugin.getDatabaseManager().savePlayerSkills(playerId, skills.serialize());
        }
    }
    
    /**
     * Load player skills data
     */
    public void loadPlayerSkills(UUID playerId, Map<String, Object> data) {
        PlayerSkills skills = new PlayerSkills();
        skills.deserialize(data);
        playerSkills.put(playerId, skills);
    }
    
    /**
     * Get leaderboard for a specific skill
     */
    public java.util.List<Map.Entry<UUID, Integer>> getSkillLeaderboard(SkillType skillType, int limit) {
        return playerSkills.entrySet().stream()
            .sorted((a, b) -> Integer.compare(
                b.getValue().getSkill(skillType).getLevel(),
                a.getValue().getSkill(skillType).getLevel()))
            .limit(limit)
            .toList();
    }
    
    /**
     * Shutdown the skills manager
     */
    public void shutdown() {
        // Save all player skills
        for (UUID playerId : playerSkills.keySet()) {
            savePlayerSkills(playerId);
        }
        playerSkills.clear();
    }
}
