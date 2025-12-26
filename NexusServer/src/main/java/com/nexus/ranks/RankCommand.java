package com.nexus.ranks;

import com.nexus.core.NexusCore;
import com.nexus.core.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Command handler for /rank command.
 * Allows players to view ranks and admins to manage ranks.
 */
public class RankCommand extends SubCommand {
    
    private final RankManager rankManager;
    
    public RankCommand(NexusCore plugin) {
        super(plugin, "rank", "nexus.ranks.use", false);
        this.rankManager = plugin.getRankManager();
    }
    
    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            // Show help or own rank
            if (sender instanceof Player) {
                showPlayerRank((Player) sender);
            } else {
                sender.sendMessage(getMessage("commands.playerOnly"));
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help", "?" -> showHelp(sender);
            case "list" -> showAllRanks(sender);
            case "info" -> showRankInfo(sender, args);
            case "set" -> setPlayerRank(sender, args);
            case "buy" -> buyRank(sender, args);
            default -> {
                // Try to show player rank
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    showPlayerRank(sender, target);
                } else {
                    sender.sendMessage(getMessage("commands.invalidSyntax")
                        .replace("%command%", "/rank"));
                    showHelp(sender);
                }
            }
        }
        
        return true;
    }
    
    /**
     * Show the player's own rank
     */
    private void showPlayerRank(Player player) {
        showPlayerRank(player, player);
    }
    
    /**
     * Show a player's rank to another player
     */
    private void showPlayerRank(CommandSender viewer, Player target) {
        Rank rank = rankManager.getPlayerRank(target.getUniqueId());
        
        viewer.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            target.getName() + "'s Rank\n");
        viewer.sendMessage(ChatColor.GRAY + "Rank: " + ChatColor.WHITE + rank.getName());
        viewer.sendMessage(ChatColor.GRAY + "Prefix: " + rankManager.getPlayerPrefix(target.getUniqueId()));
        
        boolean isStaff = rankManager.isStaff(target.getUniqueId());
        boolean isOwner = rankManager.isOwner(target.getUniqueId());
        
        if (isStaff) {
            viewer.sendMessage(ChatColor.RED + "✦ STAFF MEMBER ✦");
        }
        if (isOwner) {
            viewer.sendMessage(ChatColor.DARK_RED + "♦ SERVER OWNER ♦");
        }
    }
    
    /**
     * Show all available ranks
     */
    private void showAllRanks(CommandSender sender) {
        sender.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            "Available Ranks\n");
        
        List<Rank> sortedRanks = Arrays.asList(rankManager.getAllRanks().toArray(new Rank[0]));
        sortedRanks.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
        
        for (Rank rank : sortedRanks) {
            StringBuilder line = new StringBuilder();
            
            if (rank.isOwner()) {
                line.append(ChatColor.DARK_RED).append("♦ ");
            } else if (rank.isStaff()) {
                line.append(ChatColor.RED).append("✦ ");
            } else if (rank.isDefault()) {
                line.append(ChatColor.GRAY).append("  ");
            } else {
                line.append(ChatColor.GREEN).append("+ ");
            }
            
            line.append(rank.getPrefix()).append(ChatColor.RESET)
                .append(" ").append(rank.getName());
            
            if (rank.getPrice() > 0) {
                line.append(ChatColor.GOLD).append(" ($").append((long) rank.getPrice()).append(")");
            }
            
            sender.sendMessage(line.toString());
        }
    }
    
    /**
     * Show detailed info about a rank
     */
    private void showRankInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/rank info <rank>"));
            return;
        }
        
        Rank rank = rankManager.getRank(args[1]);
        if (rank == null) {
            sender.sendMessage(getMessage("ranks.notFound")
                .replace("%rank%", args[1]));
            return;
        }
        
        sender.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            rank.getName() + " Rank\n");
        sender.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.WHITE + rank.getId());
        sender.sendMessage(ChatColor.GRAY + "Prefix: " + rank.getPrefix());
        sender.sendMessage(ChatColor.GRAY + "Priority: " + ChatColor.WHITE + rank.getPriority());
        
        if (rank.getPrice() > 0) {
            sender.sendMessage(ChatColor.GOLD + "Price: $" + (long) rank.getPrice());
        }
        
        if (rank.isDefault()) {
            sender.sendMessage(ChatColor.GREEN + "✓ Default Rank");
        }
        if (rank.isStaff()) {
            sender.sendMessage(ChatColor.RED + "✦ Staff Rank");
        }
        if (rank.isOwner()) {
            sender.sendMessage(ChatColor.DARK_RED + "♦ Owner Rank");
        }
        
        // Show permissions if sender has permission
        if (sender.hasPermission("nexus.ranks.admin")) {
            sender.sendMessage(ChatColor.GRAY + "\nPermissions:");
            for (String perm : rank.getPermissions()) {
                sender.sendMessage(ChatColor.WHITE + "  - " + perm);
            }
        }
    }
    
    /**
     * Set a player's rank (admin command)
     */
    private void setPlayerRank(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.ranks.admin")) {
            sender.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/rank set <player> <rank>"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(getMessage("ranks.playerNotFound")
                .replace("%player%", args[1]));
            return;
        }
        
        Rank newRank = rankManager.getRank(args[2]);
        if (newRank == null) {
            sender.sendMessage(getMessage("ranks.notFound")
                .replace("%rank%", args[2]));
            return;
        }
        
        rankManager.setPlayerRank(target.getUniqueId(), newRank.getId());
        
        sender.sendMessage(getMessage("ranks.setSuccess")
            .replace("%player%", target.getName())
            .replace("%rank%", newRank.getName()));
        
        target.sendMessage(getMessage("ranks.promoted")
            .replace("%rank%", newRank.getName())
            .replace("%prefix%", newRank.getPrefix()));
    }
    
    /**
     * Buy a rank (if available for purchase)
     */
    private void buyRank(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("commands.playerOnly"));
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            sender.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/rank buy <rank>"));
            return;
        }
        
        Rank rank = rankManager.getRank(args[1]);
        if (rank == null) {
            sender.sendMessage(getMessage("ranks.notFound")
                .replace("%rank%", args[1]));
            return;
        }
        
        if (rank.getPrice() <= 0) {
            sender.sendMessage(getMessage("ranks.notForSale"));
            return;
        }
        
        // Check if player already has this rank or higher
        if (rankManager.hasRank(player.getUniqueId(), rank.getId())) {
            sender.sendMessage(getMessage("ranks.alreadyHave"));
            return;
        }
        
        // Check balance
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        if (balance < rank.getPrice()) {
            sender.sendMessage(getMessage("ranks.notEnoughMoney")
                .replace("%price%", String.valueOf((long) rank.getPrice()))
                .replace("%balance%", String.valueOf((long) balance)));
            return;
        }
        
        // Purchase the rank
        plugin.getEconomyManager().withdrawCoins(player.getUniqueId(), (long) rank.getPrice());
        rankManager.setPlayerRank(player.getUniqueId(), rank.getId());
        
        player.sendMessage(getMessage("ranks.purchaseSuccess")
            .replace("%rank%", rank.getName())
            .replace("%price%", String.valueOf((long) rank.getPrice())));
        
        // Broadcast if it's a high-tier rank
        if (rank.getPriority() >= 30) {
            Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + 
                ChatColor.WHITE + " has purchased " + rank.getPrefix() + 
                ChatColor.WHITE + "!");
        }
    }
    
    /**
     * Show help for rank command
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            "RANK COMMAND\n");
        sender.sendMessage(ChatColor.YELLOW + "/rank" + ChatColor.GRAY + " - View your current rank");
        sender.sendMessage(ChatColor.YELLOW + "/rank <player>" + ChatColor.GRAY + " - View another player's rank");
        sender.sendMessage(ChatColor.YELLOW + "/rank list" + ChatColor.GRAY + " - View all available ranks");
        sender.sendMessage(ChatColor.YELLOW + "/rank info <rank>" + ChatColor.GRAY + " - View rank information");
        sender.sendMessage(ChatColor.YELLOW + "/rank buy <rank>" + ChatColor.GRAY + " - Purchase a rank");
        
        if (sender.hasPermission("nexus.ranks.admin")) {
            sender.sendMessage(ChatColor.RED + "/rank set <player> <rank>" + ChatColor.GRAY + " - Set player's rank");
        }
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.asList("help", "list", "info", "buy");
            if (sender.hasPermission("nexus.ranks.admin")) {
                options = Arrays.asList("help", "list", "info", "buy", "set");
            }
            return options;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("buy")) {
                java.util.List<String> ranks = new java.util.ArrayList<>();
                for (Rank rank : rankManager.getAllRanks()) {
                    ranks.add(rank.getId().toLowerCase());
                }
                return ranks;
            }
            if (args[0].equalsIgnoreCase("set") && sender.hasPermission("nexus.ranks.admin")) {
                java.util.List<String> players = new java.util.ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    players.add(player.getName());
                }
                return players;
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            java.util.List<String> ranks = new java.util.ArrayList<>();
            for (Rank rank : rankManager.getAllRanks()) {
                ranks.add(rank.getId().toLowerCase());
            }
            return ranks;
        }
        return null;
    }
}
