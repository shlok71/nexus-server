package com.nexus.stats;

import com.nexus.core.NexusCore;
import com.nexus.core.commands.SubCommand;
import com.nexus.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Command handler for /stats and /profile commands.
 * Displays player statistics and profile information.
 */
public class StatsCommand extends SubCommand {
    
    private final StatsManager statsManager;
    
    public StatsCommand(NexusCore plugin) {
        super(plugin, "stats", "nexus.commands.stats", false);
        this.statsManager = plugin.getStatsManager();
    }
    
    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        String commandName = cmd.getName().toLowerCase();
        
        if (args.length == 0) {
            // Show own stats or help
            if (sender instanceof Player) {
                showPlayerStats((Player) sender, (Player) sender);
            } else {
                sender.sendMessage(getMessage("commands.playerOnly"));
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help", "?" -> showHelp(sender);
            case "top", "leaderboard", "lb" -> showLeaderboard(sender, args);
            default -> {
                // Try to show another player's stats
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    if (sender instanceof Player) {
                        showPlayerStats((Player) sender, target);
                    } else {
                        showPlayerStats(null, target);
                    }
                } else {
                    sender.sendMessage(getMessage("stats.playerNotFound")
                        .replace("%player%", args[0]));
                }
            }
        }
        
        return true;
    }
    
    /**
     * Display a player's stats
     */
    private void showPlayerStats(Player viewer, Player target) {
        UUID targetId = target.getUniqueId();
        PlayerStats stats = statsManager.getPlayerStats(targetId);
        Rank rank = plugin.getRankManager().getPlayerRank(targetId);
        
        // Build stats display
        StringBuilder display = new StringBuilder();
        
        // Header
        display.append("\n");
        display.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╔════════════════════════════════════════╗\n");
        display.append(ChatColor.GOLD).append(ChatColor.BOLD).append("║      ");
        display.append(rank.getPrefix()).append(ChatColor.GOLD).append(ChatColor.BOLD).append("      ║\n");
        display.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╠════════════════════════════════════════╣\n");
        
        // Player name and rank
        display.append(ChatColor.YELLOW).append("║ ").append(ChatColor.WHITE).append("Player: ")
            .append(rank.getPrefix()).append(ChatColor.WHITE).append(" ").append(target.getName())
            .append(" ".repeat(Math.max(0, 24 - target.getName().length() - rank.getPrefix().length() - 9)))
            .append(ChatColor.YELLOW).append("║\n");
        
        // Basic stats section
        display.append(ChatColor.YELLOW).append("╠═ ").append(ChatColor.GOLD).append(ChatColor.BOLD)
            .append("GENERAL STATS").append(ChatColor.RESET).append(ChatColor.YELLOW).append(" ═╣\n");
        
        display.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY).append("Kills: ").append(ChatColor.WHITE)
            .append(stats.getKills()).append(ChatColor.GRAY).append(" | Deaths: ").append(ChatColor.WHITE)
            .append(stats.getDeaths()).append(ChatColor.GRAY).append(" | KDR: ").append(ChatColor.WHITE)
            .append(String.format("%.2f", stats.getKDR())).append("\n");
        
        display.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY).append("Wins: ").append(ChatColor.WHITE)
            .append(stats.getWins()).append(ChatColor.GRAY).append(" | Losses: ").append(ChatColor.WHITE)
            .append(stats.getLosses()).append(ChatColor.GRAY).append(" | Games: ").append(ChatColor.WHITE)
            .append(stats.getGamesPlayed()).append("\n");
        
        display.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY).append("Play Time: ").append(ChatColor.WHITE)
            .append(stats.getFormattedPlayTime()).append("\n");
        
        // SkyBlock stats section
        display.append(ChatColor.YELLOW).append("╠═ ").append(ChatColor.GOLD).append(ChatColor.BOLD)
            .append("SKYBLOCK STATS").append(ChatColor.RESET).append(ChatColor.YELLOW).append(" ═╣\n");
        
        display.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY).append("Island Level: ").append(ChatColor.WHITE)
            .append(stats.getIslandLevel()).append(ChatColor.GRAY).append(" | Islands: ").append(ChatColor.WHITE)
            .append(stats.getIslandsCreated()).append("\n");
        
        display.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY).append("Coins Earned: ").append(ChatColor.GOLD)
            .append(String.format("%,d", stats.getCoinsEarned())).append(ChatColor.GRAY).append(" | Spent: ")
            .append(ChatColor.GOLD).append(String.format("%,d", stats.getCoinsSpent())).append("\n");
        
        display.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY).append("Skill Average: ").append(ChatColor.AQUA)
            .append(String.format("%.1f", stats.getSkillAverage())).append(ChatColor.GRAY).append(" | Level: ")
            .append(ChatColor.AQUA).append(stats.getOverallLevel()).append("\n");
        
        // Activity stats section
        display.append(ChatColor.YELLOW).append("╠═ ").append(ChatColor.GOLD).append(ChatColor.BOLD)
            .append("ACTIVITY STATS").append(ChatColor.RESET).append(ChatColor.YELLOW).append(" ═╣\n");
        
        display.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY).append("Blocks Broken: ").append(ChatColor.WHITE)
            .append(String.format("%,d", stats.getBlocksBroken())).append(ChatColor.GRAY).append(" | Placed: ")
            .append(ChatColor.WHITE).append(String.format("%,d", stats.getBlocksPlaced())).append("\n");
        
        display.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY).append("Mobs Killed: ").append(ChatColor.WHITE)
            .append(String.format("%,d", stats.getMobsKilled())).append(ChatColor.GRAY).append(" | Fish Caught: ")
            .append(ChatColor.WHITE).append(String.format("%,d", stats.getFishCaught())).append("\n");
        
        display.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY).append("Items Crafted: ").append(ChatColor.WHITE)
            .append(String.format("%,d", stats.getItemsCrafted())).append("\n");
        
        // Footer
        display.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╚════════════════════════════════════════╝\n");
        display.append(ChatColor.GRAY).append("  Use ").append(ChatColor.YELLOW).append("/stats top kills")
            .append(ChatColor.GRAY).append(" to view leaderboards\n");
        
        if (viewer != null) {
            viewer.sendMessage(display.toString());
        } else {
            sender.sendMessage(display.toString());
        }
    }
    
    /**
     * Show leaderboard
     */
    private void showLeaderboard(CommandSender sender, String[] args) {
        String category = "kills";
        if (args.length >= 2) {
            category = args[1].toLowerCase();
        }
        
        sender.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            "TOP 10 PLAYERS - " + category.toUpperCase() + "\n");
        
        List<StatsLeaderboardEntry> leaderboard = statsManager.getLeaderboard(category, 10);
        
        int rank = 1;
        for (StatsLeaderboardEntry entry : leaderboard) {
            String playerName = entry.getPlayerName();
            long value = entry.getValue();
            
            ChatColor color;
            if (rank == 1) color = ChatColor.GOLD;
            else if (rank == 2) color = ChatColor.GRAY;
            else if (rank == 3) color = ChatColor.DARK_RED;
            else color = ChatColor.WHITE;
            
            String displayValue;
            switch (category) {
                case "kills":
                case "deaths":
                case "wins":
                case "losses":
                case "games":
                case "blocks_broken":
                case "blocks_placed":
                case "items_crafted":
                case "mobs_killed":
                case "fish_caught":
                case "islands":
                    displayValue = String.format("%,d", value);
                    break;
                case "playtime":
                    displayValue = formatTime(value);
                    break;
                case "coins":
                    displayValue = ChatColor.GOLD + String.format("%,d", value) + ChatColor.GRAY;
                    break;
                default:
                    displayValue = String.valueOf(value);
            }
            
            sender.sendMessage(color + "#" + rank + " " + ChatColor.WHITE + playerName + 
                ChatColor.GRAY + " - " + displayValue);
            rank++;
        }
    }
    
    /**
     * Format time in seconds to readable format
     */
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return String.format("%dh %dm", hours, minutes);
    }
    
    /**
     * Show help message
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            "STATS COMMAND\n");
        sender.sendMessage(ChatColor.YELLOW + "/stats" + ChatColor.GRAY + " - View your own statistics");
        sender.sendMessage(ChatColor.YELLOW + "/stats <player>" + ChatColor.GRAY + " - View another player's stats");
        sender.sendMessage(ChatColor.YELLOW + "/stats top kills" + ChatColor.GRAY + " - View kills leaderboard");
        sender.sendMessage(ChatColor.YELLOW + "/stats top coins" + ChatColor.GRAY + " - View coins leaderboard");
        sender.sendMessage(ChatColor.YELLOW + "/stats top playtime" + ChatColor.GRAY + " - View playtime leaderboard");
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "top", "leaderboard");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("top") || args[0].equalsIgnoreCase("leaderboard"))) {
            return Arrays.asList("kills", "deaths", "wins", "coins", "playtime", "blocks_broken", "mobs_killed");
        }
        return null;
    }
    
    /**
     * Helper class for leaderboard entries
     */
    public static class StatsLeaderboardEntry {
        private final UUID playerId;
        private final String playerName;
        private final long value;
        
        public StatsLeaderboardEntry(UUID playerId, String playerName, long value) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.value = value;
        }
        
        public UUID getPlayerId() {
            return playerId;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public long getValue() {
            return value;
        }
    }
}
