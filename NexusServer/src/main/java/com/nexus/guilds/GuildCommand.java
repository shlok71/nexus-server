package com.nexus.guilds;

import com.nexus.core.NexusCore;
import com.nexus.core.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Command handler for /guild command.
 * Handles all guild-related commands including create, invite, kick, etc.
 */
public class GuildCommand extends SubCommand {
    
    private final GuildManager guildManager;
    
    public GuildCommand(NexusCore plugin) {
        super(plugin, "guild", "nexus.guild.use", false);
        this.guildManager = plugin.getGuildManager();
    }
    
    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("commands.playerOnly"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Show player's guild info
            showGuildInfo(player, player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help", "?" -> showHelp(player);
            case "create", "new" -> createGuild(player, args);
            case "disband", "delete" -> disbandGuild(player);
            case "invite" -> invitePlayer(player, args);
            case "accept" -> acceptInvite(player);
            case "leave" -> leaveGuild(player);
            case "kick" -> kickPlayer(player, args);
            case "promote" -> promotePlayer(player, args);
            case "demote" -> demotePlayer(player, args);
            case "leader", "transfer" -> transferLeader(player, args);
            case "setdesc", "description" -> setDescription(player, args);
            case "public", "private" -> togglePublic(player);
            case "list" -> listGuilds(player, args);
            case "top", "leaderboard" -> showLeaderboard(player, args);
            case "info" -> showGuildInfo(player, args);
            default -> {
                // Check if it's a guild name/tag lookup
                Guild guild = guildManager.getGuildByName(subCommand);
                if (guild == null) {
                    guild = guildManager.getGuildByTag(subCommand);
                }
                if (guild != null) {
                    showGuildInfo(player, guild);
                } else {
                    showHelp(player);
                }
            }
        }
        
        return true;
    }
    
    /**
     * Create a new guild
     */
    private void createGuild(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/guild create <name> <tag>"));
            return;
        }
        
        String name = args[1];
        String tag = args[2];
        
        guildManager.createGuild(player, name, tag);
    }
    
    /**
     * Disband the guild
     */
    private void disbandGuild(Player player) {
        if (guildManager.disbandGuild(player.getUniqueId())) {
            // Success
        } else {
            player.sendMessage(getMessage("guild.noPermission"));
        }
    }
    
    /**
     * Invite a player to the guild
     */
    private void invitePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/guild invite <player>"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(getMessage("guild.playerNotFound")
                .replace("%player%", args[1]));
            return;
        }
        
        if (guildManager.invitePlayer(player.getUniqueId(), target)) {
            // Success - message sent by manager
        } else {
            player.sendMessage(getMessage("guild.cantInvite"));
        }
    }
    
    /**
     * Accept guild invitation
     */
    private void acceptInvite(Player player) {
        guildManager.acceptInvite(player);
    }
    
    /**
     * Leave the guild
     */
    private void leaveGuild(Player player) {
        if (guildManager.leaveGuild(player.getUniqueId())) {
            // Success - message sent by manager
        } else {
            player.sendMessage(getMessage("guild.notInGuild"));
        }
    }
    
    /**
     * Kick a player from the guild
     */
    private void kickPlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/guild kick <player>"));
            return;
        }
        
        if (guildManager.kickPlayer(player.getUniqueId(), args[1])) {
            // Success - message sent by manager
        } else {
            player.sendMessage(getMessage("guild.cantKick"));
        }
    }
    
    /**
     * Promote a member to officer
     */
    private void promotePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/guild promote <player>"));
            return;
        }
        
        guildManager.promoteToOfficer(player.getUniqueId(), args[1]);
    }
    
    /**
     * Demote an officer to member
     */
    private void demotePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/guild demote <player>"));
            return;
        }
        
        guildManager.demoteToMember(player.getUniqueId(), args[1]);
    }
    
    /**
     * Transfer guild leadership
     */
    private void transferLeader(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/guild leader <player>"));
            return;
        }
        
        guildManager.transferLeadership(player.getUniqueId(), args[1]);
    }
    
    /**
     * Set guild description
     */
    private void setDescription(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/guild setdesc <description>"));
            return;
        }
        
        StringBuilder desc = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) desc.append(" ");
            desc.append(args[i]);
        }
        
        guildManager.setDescription(player.getUniqueId(), desc.toString());
        player.sendMessage(getMessage("guild.descriptionSet"));
    }
    
    /**
     * Toggle guild public/private status
     */
    private void togglePublic(Player player) {
        guildManager.togglePublic(player.getUniqueId());
    }
    
    /**
     * List all guilds
     */
    private void listGuilds(Player player, int page) {
        player.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            "All Guilds (" + guildManager.getGuildCount() + ")\n");
        
        int perPage = 10;
        List<Guild> allGuilds = Arrays.asList(
            guildManager.getAllGuilds().toArray(new Guild[0])
        );
        
        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, allGuilds.size());
        
        if (start >= allGuilds.size()) {
            player.sendMessage(ChatColor.GRAY + "No guilds found.");
            return;
        }
        
        for (int i = start; i < end; i++) {
            Guild guild = allGuilds.get(i);
            String tagColor = guildManager.getGuildTagColor(guild);
            player.sendMessage(tagColor + "[" + guild.getTag() +] " + 
                ChatColor.WHITE + guild.getName() + 
                ChatColor.GRAY + " (Lv." + guild.getLevel() + ")");
        }
        
        if (end < allGuilds.size()) {
            player.sendMessage(ChatColor.GRAY + "Use /guild list " + (page + 1) + 
                " for more...");
        }
    }
    
    /**
     * Show guild leaderboard
     */
    private void showLeaderboard(Player player, String[] args) {
        String type = "level";
        if (args.length >= 2) {
            type = args[1].toLowerCase();
        }
        
        player.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            "Guild Leaderboard - " + type.toUpperCase() + "\n");
        
        List<Guild> leaderboard;
        switch (type) {
            case "exp", "experience" -> leaderboard = guildManager.getLeaderboardByExperience(10);
            case "wins" -> leaderboard = guildManager.getLeaderboardByWins(10);
            default -> leaderboard = guildManager.getLeaderboardByLevel(10);
        }
        
        int rank = 1;
        for (Guild guild : leaderboard) {
            String tagColor = guildManager.getGuildTagColor(guild);
            ChatColor rankColor;
            if (rank == 1) rankColor = ChatColor.GOLD;
            else if (rank == 2) rankColor = ChatColor.GRAY;
            else if (rank == 3) rankColor = ChatColor.DARK_RED;
            else rankColor = ChatColor.WHITE;
            
            String value = switch (type) {
                case "exp", "experience" -> String.format("%,d XP", guild.getExperience());
                case "wins" -> guild.getWins() + " wins";
                default -> "Lv." + guild.getLevel();
            };
            
            player.sendMessage(rankColor + "#" + rank + " " + 
                tagColor + "[" + guild.getTag() + "] " + 
                ChatColor.WHITE + guild.getName() + 
                ChatColor.GRAY + " - " + value);
            rank++;
        }
    }
    
    /**
     * Show guild information
     */
    private void showGuildInfo(Player viewer, Guild guild) {
        String tagColor = guildManager.getGuildTagColor(guild);
        
        StringBuilder info = new StringBuilder();
        info.append("\n");
        info.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╔════════════════════════════════════════╗\n");
        info.append(ChatColor.GOLD).append(ChatColor.BOLD).append("║  ");
        info.append(tagColor).append("[").append(guild.getTag()).append("] ");
        info.append(ChatColor.WHITE).append(guild.getName());
        int padding = 32 - guild.getTag().length() - guild.getName().length() - 3;
        if (padding > 0) {
            info.append(" ".repeat(padding));
        }
        info.append(ChatColor.GOLD).append(ChatColor.BOLD).append("║\n");
        info.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╠════════════════════════════════════════╣\n");
        
        // Description
        if (!guild.getDescription().isEmpty()) {
            info.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY)
                .append(guild.getDescription()).append("\n");
            info.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╠════════════════════════════════════════╣\n");
        }
        
        // Stats
        info.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY)
            .append("Level: ").append(ChatColor.WHITE).append(guild.getLevel())
            .append(ChatColor.GRAY).append(" | XP: ").append(ChatColor.WHITE)
            .append(guild.getExperienceProgressPercent()).append("%\n");
        
        info.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY)
            .append("Members: ").append(ChatColor.WHITE).append(guild.getMemberCount())
            .append(ChatColor.GRAY).append(" | Wins: ").append(ChatColor.WHITE)
            .append(guild.getWins()).append("\n");
        
        info.append(ChatColor.YELLOW).append("║ ").append(ChatColor.GRAY)
            .append("Kills: ").append(ChatColor.WHITE).append(guild.getKills())
            .append(ChatColor.GRAY).append(" | Status: ")
            .append(guild.isPublic() ? ChatColor.GREEN + "Public" : ChatColor.RED + "Private")
            .append("\n");
        
        info.append(ChatColor.GOLD).append(ChatColor.BOLD).append("╚════════════════════════════════════════╝\n");
        
        Player viewerPlayer = (Player) viewer;
        viewerPlayer.sendMessage(info.toString());
    }
    
    private void showGuildInfo(Player viewer, String[] args) {
        if (args.length < 2) {
            showGuildInfo(viewer, viewer);
            return;
        }
        
        Guild guild = guildManager.getGuildByName(args[1]);
        if (guild == null) {
            guild = guildManager.getGuildByTag(args[1]);
        }
        
        if (guild != null) {
            showGuildInfo(viewer, guild);
        } else {
            viewer.sendMessage(getMessage("guild.notFound")
                .replace("%guild%", args[1]));
        }
    }
    
    /**
     * Show help message
     */
    private void showHelp(Player player) {
        player.sendMessage("\n" + ChatColor.RED + ChatColor.BOLD.toString() + 
            "GUILD COMMANDS\n");
        
        player.sendMessage(ChatColor.YELLOW + "/guild" + ChatColor.GRAY + 
            " - View your guild information");
        player.sendMessage(ChatColor.YELLOW + "/guild <name/tag>" + ChatColor.GRAY + 
            " - View guild information");
        player.sendMessage(ChatColor.YELLOW + "/guild create <name> <tag>" + ChatColor.GRAY + 
            " - Create a new guild");
        player.sendMessage(ChatColor.YELLOW + "/guild disband" + ChatColor.GRAY + 
            " - Disband your guild");
        player.sendMessage(ChatColor.YELLOW + "/guild invite <player>" + ChatColor.GRAY + 
            " - Invite a player to your guild");
        player.sendMessage(ChatColor.YELLOW + "/guild accept" + ChatColor.GRAY + 
            " - Accept a guild invitation");
        player.sendMessage(ChatColor.YELLOW + "/guild leave" + ChatColor.GRAY + 
            " - Leave your current guild");
        player.sendMessage(ChatColor.YELLOW + "/guild kick <player>" + ChatColor.GRAY + 
            " - Kick a player from your guild");
        player.sendMessage(ChatColor.YELLOW + "/guild promote <player>" + ChatColor.GRAY + 
            " - Promote a member to officer");
        player.sendMessage(ChatColor.YELLOW + "/guild demote <player>" + ChatColor.GRAY + 
            " - Demote an officer to member");
        player.sendMessage(ChatColor.YELLOW + "/guild leader <player>" + ChatColor.GRAY + 
            " - Transfer guild leadership");
        player.sendMessage(ChatColor.YELLOW + "/guild setdesc <text>" + ChatColor.GRAY + 
            " - Set guild description");
        player.sendMessage(ChatColor.YELLOW + "/guild public/private" + ChatColor.GRAY + 
            " - Toggle guild privacy");
        player.sendMessage(ChatColor.YELLOW + "/guild list [page]" + ChatColor.GRAY + 
            " - List all guilds");
        player.sendMessage(ChatColor.YELLOW + "/guild top [level/wins/exp]" + ChatColor.GRAY + 
            " - View guild leaderboard");
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "create", "disband", "invite", "accept", 
                "leave", "kick", "promote", "demote", "leader", "setdesc", 
                "public", "list", "top", "info");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                return Arrays.asList("1", "2", "3");
            }
            if (args[0].equalsIgnoreCase("top")) {
                return Arrays.asList("level", "wins", "exp", "experience");
            }
        }
        return null;
    }
}
