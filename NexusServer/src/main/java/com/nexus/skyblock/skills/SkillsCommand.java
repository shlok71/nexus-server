package com.nexus.skyblock.skills;

import com.nexus.core.NexusCore;
import com.nexus.core.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Command handler for /skills command.
 * Allows players to view their skills and skill leaderboards.
 */
public class SkillsCommand extends SubCommand {
    
    private final SkillsManager skillsManager;
    
    public SkillsCommand(NexusCore plugin) {
        super(plugin, "skills", "nexus.skyblock.skills", false);
        this.skillsManager = plugin.getSkillsManager();
    }
    
    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("commands.playerOnly"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Show all skills for the player
            showPlayerSkills(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help", "?" -> showHelp(player);
            case "leaderboard", "top", "lb" -> showLeaderboard(player, args);
            case "set" -> setSkillLevel(player, args);
            case "info" -> showSkillInfo(player, args);
            default -> {
                // Try to show specific skill
                try {
                    SkillType skillType = SkillType.valueOf(subCommand.toUpperCase());
                    showSpecificSkill(player, skillType);
                } catch (IllegalArgumentException) {
                    player.sendMessage(getMessage("commands.invalidSyntax")
                        .replace("%command%", "/skills"));
                    showHelp(player);
                }
            }
        }
        
        return true;
    }
    
    /**
     * Display all skills for the player
     */
    private void showPlayerSkills(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerSkills skills = skillsManager.getPlayerSkills(playerId);
        
        StringBuilder message = new StringBuilder();
        message.append("\n");
        message.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╔════════════════════════════════════╗\n");
        message.append(ChatColor.GOLD).append(ChatColor.BOLD).append("║         ").append(ChatColor.WHITE).append("YOUR SKILLS").append(ChatColor.GOLD).append(ChatColor.BOLD).append("           ║\n");
        message.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╠════════════════════════════════════╣\n");
        
        int totalLevel = skills.getTotalLevel();
        double avgLevel = skills.getAverageLevel();
        
        message.append(ChatColor.YELLOW).append("║ ").append(ChatColor.WHITE)
            .append(String.format("Total Level: %d  |  Avg: %.1f", totalLevel, avgLevel))
            .append(" ".repeat(Math.max(0, 19 - String.format("Total Level: %d  |  Avg: %.1f", totalLevel, avgLevel).length())))
            .append(ChatColor.YELLOW).append("║\n");
        message.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╠════════════════════════════════════╣\n");
        
        for (SkillType skill : SkillType.values()) {
            SkillData data = skills.getSkill(skill);
            int level = data.getLevel();
            double progress = skills.getProgressForLevel(skill);
            
            // Create progress bar
            int barLength = 15;
            int filledBars = (int) (progress * barLength);
            StringBuilder bar = new StringBuilder();
            bar.append(ChatColor.GREEN);
            for (int i = 0; i < filledBars; i++) {
                bar.append("█");
            }
            bar.append(ChatColor.GRAY);
            for (int i = filledBars; i < barLength; i++) {
                bar.append("░");
            }
            
            message.append(ChatColor.YELLOW).append("║ ").append(ChatColor.WHITE)
                .append(String.format("%-12s", skill.getDisplayName()))
                .append(ChatColor.GRAY).append(" [")
                .append(ChatColor.GREEN).append(level)
                .append(ChatColor.GRAY).append("/").append(skill.getMaxLevel())
                .append("] ").append(bar)
                .append(ChatColor.YELLOW).append("║\n");
        }
        
        message.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╚════════════════════════════════════╝\n");
        
        player.sendMessage(message.toString());
        
        // Send hint about leaderboard
        player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/skills leaderboard [skill]" + 
            ChatColor.GRAY + " to view top players");
    }
    
    /**
     * Display a specific skill in detail
     */
    private void showSpecificSkill(Player player, SkillType skillType) {
        UUID playerId = player.getUniqueId();
        PlayerSkills skills = skillsManager.getPlayerSkills(playerId);
        SkillData data = skills.getSkill(skillType);
        
        int level = data.getLevel();
        double progress = skills.getProgressForLevel(skillType);
        double bonus = skillType.getSkillBonus(level);
        
        player.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD + skillType.getDisplayName() + " Skill");
        player.sendMessage(ChatColor.GRAY + "Level: " + ChatColor.GREEN + level + 
            ChatColor.GRAY + "/" + skillType.getMaxLevel());
        
        // Progress bar
        int barLength = 25;
        int filledBars = (int) (progress * barLength);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                bar.append(ChatColor.GREEN).append("█");
            } else {
                bar.append(ChatColor.GRAY).append("░");
            }
        }
        player.sendMessage(ChatColor.GRAY + "Progress: " + bar + 
            String.format(" %.1f%%", progress * 100));
        
        // XP to next level
        if (level < skillType.getMaxLevel()) {
            double xpNeeded = data.getXpNeededForNext(skillType);
            double xpCurrent = data.getXpTowardsNext(skillType);
            player.sendMessage(ChatColor.GRAY + "XP: " + ChatColor.YELLOW + 
                String.format("%.0f", xpCurrent) + "/" + String.format("%.0f", xpNeeded));
        } else {
            player.sendMessage(ChatColor.GOLD + "MAX LEVEL REACHED!");
        }
        
        // Skill bonus
        player.sendMessage(ChatColor.GRAY + "Current Bonus: " + ChatColor.AQUA + 
            String.format("+%.1f%%", bonus));
        
        // XP to max level
        if (level < skillType.getMaxLevel()) {
            double totalXpNeeded = skillType.getXpForLevel(skillType.getMaxLevel()) - data.getCurrentXp();
            player.sendMessage(ChatColor.GRAY + "XP to Max: " + ChatColor.RED + 
                String.format("%.0f", totalXpNeeded));
        }
    }
    
    /**
     * Display skill leaderboard
     */
    private void showLeaderboard(Player player, String[] args) {
        SkillType skillType = null;
        
        if (args.length >= 2) {
            try {
                skillType = SkillType.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException) {
                player.sendMessage(getMessage("skills.invalidSkill"));
                return;
            }
        }
        
        if (skillType == null) {
            // Show overall leaderboard (by total level)
            player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + 
                "=== SKILL LEADERBOARD ===\n");
            
            java.util.List<Map.Entry<UUID, Integer>> leaderboard = skillsManager
                .getSkillLeaderboard(SkillType.COMBAT, 10);
            
            int rank = 1;
            for (Map.Entry<UUID, Integer> entry : leaderboard) {
                Player target = Bukkit.getPlayer(entry.getKey());
                String name = target != null ? target.getName() : "Unknown";
                int totalLevel = entry.getValue();
                
                String color = rank == 1 ? ChatColor.GOLD : 
                    rank == 2 ? ChatColor.GRAY : 
                    rank == 3 ? ChatColor.DARK_RED : ChatColor.WHITE;
                
                player.sendMessage(color + "#" + rank + " " + ChatColor.WHITE + name + 
                    ChatColor.GRAY + " - Total Level: " + ChatColor.YELLOW + totalLevel);
                rank++;
            }
        } else {
            // Show specific skill leaderboard
            player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + 
                skillType.getDisplayName() + " LEADERBOARD\n");
            
            java.util.List<Map.Entry<UUID, Integer>> leaderboard = skillsManager
                .getSkillLeaderboard(skillType, 10);
            
            int rank = 1;
            for (Map.Entry<UUID, Integer> entry : leaderboard) {
                Player target = Bukkit.getPlayer(entry.getKey());
                String name = target != null ? target.getName() : "Unknown";
                int level = entry.getValue();
                
                String color = rank == 1 ? ChatColor.GOLD : 
                    rank == 2 ? ChatColor.GRAY : 
                    rank == 3 ? ChatColor.DARK_RED : ChatColor.WHITE;
                
                player.sendMessage(color + "#" + rank + " " + ChatColor.WHITE + name + 
                    ChatColor.GRAY + " - Level: " + ChatColor.YELLOW + level);
                rank++;
            }
        }
    }
    
    /**
     * Set skill level for a player (admin)
     */
    private void setSkillLevel(Player player, String[] args) {
        if (!player.hasPermission("nexus.admin")) {
            player.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (args.length < 3) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/skills set <player> <skill> <level>"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(getMessage("skills.playerNotFound"));
            return;
        }
        
        try {
            SkillType skillType = SkillType.valueOf(args[2].toUpperCase());
            
            int level;
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage(getMessage("skills.invalidLevel"));
                return;
            }
            
            skillsManager.setSkillLevel(target.getUniqueId(), skillType, level);
            player.sendMessage(getMessage("skills.levelSet")
                .replace("%player%", target.getName())
                .replace("%skill%", skillType.getDisplayName())
                .replace("%level%", String.valueOf(level)));
                
        } catch (IllegalArgumentException e) {
            player.sendMessage(getMessage("skills.invalidSkill"));
        }
    }
    
    /**
     * Show detailed info about a skill
     */
    private void showSkillInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/skills info <skill>"));
            return;
        }
        
        try {
            SkillType skillType = SkillType.valueOf(args[1].toUpperCase());
            
            player.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
                skillType.getDisplayName() + " INFO\n");
            player.sendMessage(ChatColor.GRAY + "Max Level: " + ChatColor.YELLOW + 
                skillType.getMaxLevel());
            player.sendMessage(ChatColor.GRAY + "XP Multiplier: " + ChatColor.YELLOW + 
                skillType.getXpMultiplier() + "x");
            
            player.sendMessage(ChatColor.GRAY + "\nHow to gain XP:");
            String[] methods = getXpMethods(skillType);
            for (String method : methods) {
                player.sendMessage(ChatColor.WHITE + " • " + method);
            }
            
            player.sendMessage(ChatColor.GRAY + "\nSkill Bonuses:");
            String[] bonuses = getSkillBonuses(skillType);
            for (String bonus : bonuses) {
                player.sendMessage(ChatColor.AQUA + " • " + bonus);
            }
            
        } catch (IllegalArgumentException e) {
            player.sendMessage(getMessage("skills.invalidSkill"));
        }
    }
    
    /**
     * Get XP gain methods for a skill
     */
    private String[] getXpMethods(SkillType skillType) {
        return switch (skillType) {
            case MINING -> new String[]{"Mine ores and stone", "Mine nether materials", "Mine end stone"};
            case FORAGING -> new String[]{"Chop wood", "Gather leaves", "Collect vines"};
            case FARMING -> new String[]{"Harvest crops", "Harvest pumpkins/melons", "Harvest nether wart"};
            case FISHING -> new String[]{"Catch fish", "Catch treasure", "Catch junk"};
            case COMBAT -> new String[]{"Kill mobs", "Kill players", "Kill bosses"};
            case ENCHANTING -> new String[]{"Enchant items", "Disenchant items"};
            case ALCHEMY -> new String[]{"Brew potions", "Brew splash potions"};
            case CARPENTING -> new String[]{"Craft items", "Use crafting table"};
            case RUNECRAFT -> new String[]{"Craft runes", "Use runestones"};
            case TAMING -> new String[]{"Tame pets", "Feed pets"};
        };
    }
    
    /**
     * Get skill bonuses for a skill
     */
    private String[] getSkillBonuses(SkillType skillType) {
        return switch (skillType) {
            case MINING -> new String[]{"+1% Mining Speed per level", "+1% Fortune per 50 levels"};
            case FORAGING -> new String[]{"+1% Foraging Speed per level", "+1% Fortune per 50 levels"};
            case FARMING -> new String[]{"+1% Farming Fortune per level", "+1% Drops per 50 levels"};
            case FISHING -> new String[]{"+1% Fishing Speed per level", "+1% Treasure chance per 50 levels"};
            case COMBAT -> new String[]{"+1% Damage per level", "+1% Crit Chance per 50 levels"};
            case ENCHANTING -> new String[]{"+1% XP gain per level", "+1% Enchanting table speed per 50 levels"};
            case ALCHEMY -> new String[]{"+1% Potion duration per level", "+1% Potion strength per 50 levels"};
            case CARPENTING -> new String[]{"+1% Crafting speed per level", "+1% Recipe success per 50 levels"};
            case RUNECRAFT -> new String[]{"+1% Rune effectiveness per level", "+1% Runestone charge per 50 levels"};
            case TAMING -> new String[]{"+1% Pet bonus per level", "+1% Pet XP per 50 levels"};
        };
    }
    
    /**
     * Show help for skills command
     */
    private void showHelp(Player player) {
        player.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + "SKILLS COMMAND\n");
        player.sendMessage(ChatColor.YELLOW + "/skills" + ChatColor.GRAY + " - View all your skills");
        player.sendMessage(ChatColor.YELLOW + "/skills <skill>" + ChatColor.GRAY + " - View specific skill details");
        player.sendMessage(ChatColor.YELLOW + "/skills leaderboard" + ChatColor.GRAY + " - View top players");
        player.sendMessage(ChatColor.YELLOW + "/skills leaderboard <skill>" + ChatColor.GRAY + " - View skill leaderboard");
        player.sendMessage(ChatColor.YELLOW + "/skills info <skill>" + ChatColor.GRAY + - View skill information");
        player.sendMessage(ChatColor.YELLOW + "/skills set <player> <skill> <level>" + ChatColor.GRAY + " - Set skill level (admin)");
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "leaderboard", "info");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("leaderboard") || args[0].equalsIgnoreCase("info")) {
                java.util.List<String> skills = new java.util.ArrayList<>();
                for (SkillType skill : SkillType.values()) {
                    skills.add(skill.name().toLowerCase());
                }
                return skills;
            }
        }
        return null;
    }
}
