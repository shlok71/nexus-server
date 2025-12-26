package com.nexus.staff;

import com.nexus.core.NexusCore;
import com.nexus.core.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Command handler for staff commands.
 * Handles kick, mute, unmute, and other moderation tools.
 */
public class StaffCommand extends SubCommand {
    
    private final NexusCore plugin;
    private final Map<UUID, MuteData> mutedPlayers;
    
    public StaffCommand(NexusCore plugin) {
        super(plugin, "staff", "nexus.staff.use", false);
        this.plugin = plugin;
        this.mutedPlayers = new HashMap<>();
    }
    
    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help", "?" -> showHelp(sender);
            case "kick" -> kickPlayer(sender, args);
            case "mute" -> mutePlayer(sender, args);
            case "unmute" -> unmutePlayer(sender, args);
            case "feed" -> feedPlayer(sender, args);
            case "heal" -> healPlayer(sender, args);
            case "fly" -> toggleFly(sender, args);
            case "god" -> toggleGodMode(sender, args);
            case "invsee" -> seeInventory(sender, args);
            case "vanish" -> toggleVanish(sender, args);
            default -> showHelp(sender);
        }
        
        return true;
    }
    
    /**
     * Kick a player from the server
     */
    private void kickPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.staff.kick")) {
            sender.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/kick <player> [reason]"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(getMessage("staff.playerNotFound")
                .replace("%player%", args[1]));
            return;
        }
        
        String reason = "Kicked by staff";
        if (args.length >= 3) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) reasonBuilder.append(" ");
                reasonBuilder.append(args[i]);
            }
            reason = reasonBuilder.toString();
        }
        
        // Kick the player
        target.kickPlayer(ChatColor.RED + "You have been kicked!\n\n" +
            ChatColor.GRAY + "Reason: " + ChatColor.WHITE + reason + "\n" +
            ChatColor.GRAY + "Staff: " + ChatColor.WHITE + sender.getName() + "\n" +
            ChatColor.GRAY + "Date: " + ChatColor.WHITE + new Date().toString());
        
        // Log the kick
        Bukkit.getLogger().info("[STAFF] " + sender.getName() + " kicked " + 
            target.getName() + " for: " + reason);
        
        // Notify staff
        notifyStaff(sender.getName() + " kicked " + target.getName() + 
            " (Reason: " + reason + ")");
        
        sender.sendMessage(getMessage("staff.kickSuccess")
            .replace("%player%", target.getName()));
    }
    
    /**
     * Mute a player
     */
    private void mutePlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.staff.mute")) {
            sender.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/mute <player> [time] [reason]"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(getMessage("staff.playerNotFound")
                .replace("%player%", args[1]));
            return;
        }
        
        // Parse duration
        long duration = 0; // 0 = permanent
        if (args.length >= 3) {
            String timeStr = args[2];
            try {
                if (timeStr.endsWith("s")) {
                    duration = Long.parseLong(timeStr.substring(0, timeStr.length() - 1)) * 1000;
                } else if (timeStr.endsWith("m")) {
                    duration = Long.parseLong(timeStr.substring(0, timeStr.length() - 1)) * 60 * 1000;
                } else if (timeStr.endsWith("h")) {
                    duration = Long.parseLong(timeStr.substring(0, timeStr.length() - 1)) * 60 * 60 * 1000;
                } else if (timeStr.endsWith("d")) {
                    duration = Long.parseLong(timeStr.substring(0, timeStr.length() - 1)) * 24 * 60 * 60 * 1000;
                } else {
                    duration = Long.parseLong(timeStr) * 60 * 1000; // Default to minutes
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(getMessage("staff.invalidTime"));
                return;
            }
        }
        
        String reason = "Muted by staff";
        if (args.length >= 4) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                if (i > 3) reasonBuilder.append(" ");
                reasonBuilder.append(args[i]);
            }
            reason = reasonBuilder.toString();
        }
        
        // Create mute data
        MuteData muteData = new MuteData(
            target.getUniqueId(),
            sender.getName(),
            reason,
            duration > 0 ? System.currentTimeMillis() + duration : -1
        );
        mutedPlayers.put(target.getUniqueId(), muteData);
        
        // Save to database
        plugin.getDatabaseManager().saveMute(target.getUniqueId(), muteData);
        
        // Notify
        String timeStr = duration > 0 ? " for " + formatDuration(duration) : " permanently";
        target.sendMessage(getMessage("staff.muted")
            .replace("%reason%", reason)
            .replace("%time%", timeStr));
        
        sender.sendMessage(getMessage("staff.muteSuccess")
            .replace("%player%", target.getName())
            .replace("%time%", timeStr));
        
        notifyStaff(sender.getName() + " muted " + target.getName() + timeStr + 
            " (Reason: " + reason + ")");
    }
    
    /**
     * Unmute a player
     */
    private void unmutePlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.staff.mute")) {
            sender.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/unmute <player>"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        UUID targetId = null;
        
        if (target != null) {
            targetId = target.getUniqueId();
        } else {
            // Try to find by name in muted players
            for (Map.Entry<UUID, MuteData> entry : mutedPlayers.entrySet()) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null && p.getName().equalsIgnoreCase(args[1])) {
                    targetId = entry.getKey();
                    break;
                }
            }
        }
        
        if (targetId == null || !mutedPlayers.containsKey(targetId)) {
            sender.sendMessage(getMessage("staff.notMuted")
                .replace("%player%", args[1]));
            return;
        }
        
        mutedPlayers.remove(targetId);
        plugin.getDatabaseManager().removeMute(targetId);
        
        if (target != null && target.isOnline()) {
            target.sendMessage(getMessage("staff.unmuted"));
        }
        
        sender.sendMessage(getMessage("staff.unmuteSuccess")
            .replace("%player%", args[1]));
    }
    
    /**
     * Check if a player is muted
     */
    public boolean isMuted(UUID playerId) {
        MuteData muteData = mutedPlayers.get(playerId);
        if (muteData == null) {
            return false;
        }
        
        // Check if mute has expired
        if (muteData.getExpiresAt() > 0 && System.currentTimeMillis() > muteData.getExpiresAt()) {
            mutedPlayers.remove(playerId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Feed a player
     */
    private void feedPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.staff.feed")) {
            sender.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(getMessage("staff.playerNotFound")
                    .replace("%player%", args[1]));
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/feed [player]"));
            return;
        }
        
        target.setFoodLevel(20);
        target.sendMessage(getMessage("staff.fed"));
        
        if (target != sender) {
            sender.sendMessage(getMessage("staff.fedOther")
                .replace("%player%", target.getName()));
        }
    }
    
    /**
     * Heal a player
     */
    private void healPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.staff.heal")) {
            sender.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(getMessage("staff.playerNotFound")
                    .replace("%player%", args[1]));
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/heal [player]"));
            return;
        }
        
        target.setHealth(target.getMaxHealth());
        target.setFireTicks(0);
        target.sendMessage(getMessage("staff.healed"));
        
        if (target != sender) {
            sender.sendMessage(getMessage("staff.healedOther")
                .replace("%player%", target.getName()));
        }
    }
    
    /**
     * Toggle fly mode
     */
    private void toggleFly(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.staff.fly")) {
            sender.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("commands.playerOnly"));
            return;
        }
        
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(getMessage("staff.playerNotFound")
                    .replace("%player%", args[1]));
                return;
            }
        } else {
            target = (Player) sender;
        }
        
        boolean flyEnabled = !target.getAllowFlight();
        target.setAllowFlight(flyEnabled);
        target.setFlying(flyEnabled);
        
        target.sendMessage(flyEnabled ? getMessage("staff.flyEnabled") : getMessage("staff.flyDisabled"));
        
        if (target != sender) {
            sender.sendMessage(getMessage(flyEnabled ? "staff.flyEnabledOther" : "staff.flyDisabledOther")
                .replace("%player%", target.getName()));
        }
    }
    
    /**
     * Toggle god mode
     */
    private void toggleGodMode(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.staff.god")) {
            sender.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("commands.playerOnly"));
            return;
        }
        
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(getMessage("staff.playerNotFound")
                    .replace("%player%", args[1]));
                return;
            }
        } else {
            target = (Player) sender;
        }
        
        // Toggle god mode via metadata or game rule
        boolean godMode = !target.hasMetadata("godMode");
        target.setMetadata("godMode", new org.bukkit.metadata.FixedMetadataValue(plugin, godMode));
        
        target.sendMessage(godMode ? getMessage("staff.godEnabled") : getMessage("staff.godDisabled"));
        
        if (target != sender) {
            sender.sendMessage(getMessage(godMode ? "staff.godEnabledOther" : "staff.godDisabledOther")
                .replace("%player%", target.getName()));
        }
    }
    
    /**
     * See player's inventory
     */
    private void seeInventory(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.staff.invsee")) {
            sender.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("commands.playerOnly"));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/invsee <player>"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(getMessage("staff.playerNotFound")
                .replace("%player%", args[1]));
            return;
        }
        
        Player viewer = (Player) sender;
        viewer.openInventory(target.getInventory());
        viewer.sendMessage(getMessage("staff.viewingInventory")
            .replace("%player%", target.getName()));
    }
    
    /**
     * Toggle vanish
     */
    private void toggleVanish(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexus.staff.vanish")) {
            sender.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("commands.playerOnly"));
            return;
        }
        
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(getMessage("staff.playerNotFound")
                    .replace("%player%", args[1]));
                return;
            }
        } else {
            target = (Player) sender;
        }
        
        boolean vanished = !target.hasMetadata("vanished");
        target.setMetadata("vanished", new org.bukkit.metadata.FixedMetadataValue(plugin, vanished));
        
        // Hide from other players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.hasPermission("nexus.staff.vanish")) {
                if (vanished) {
                    online.hidePlayer(target);
                } else {
                    online.showPlayer(target);
                }
            }
        }
        
        target.sendMessage(vanished ? getMessage("staff.vanishEnabled") : getMessage("staff.vanishDisabled"));
        
        if (target != sender) {
            sender.sendMessage(getMessage(vanished ? "staff.vanishEnabledOther" : "staff.vanishDisabledOther")
                .replace("%player%", target.getName()));
        }
    }
    
    /**
     * Notify staff members
     */
    private void notifyStaff(String message) {
        String formatted = ChatColor.RED + "[STAFF] " + ChatColor.WHITE + message;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("nexus.staff")) {
                player.sendMessage(formatted);
            }
        }
    }
    
    /**
     * Format duration in milliseconds to readable string
     */
    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
    /**
     * Show help message
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage("\n" + ChatColor.RED + ChatColor.BOLD.toString() + 
            "STAFF COMMANDS\n");
        
        if (sender.hasPermission("nexus.staff.kick")) {
            sender.sendMessage(ChatColor.YELLOW + "/kick <player> [reason]" + ChatColor.GRAY + " - Kick a player");
        }
        if (sender.hasPermission("nexus.staff.mute")) {
            sender.sendMessage(ChatColor.YELLOW + "/mute <player> [time] [reason]" + ChatColor.GRAY + " - Mute a player");
            sender.sendMessage(ChatColor.YELLOW + "/unmute <player>" + ChatColor.GRAY + " - Unmute a player");
        }
        if (sender.hasPermission("nexus.staff.feed")) {
            sender.sendMessage(ChatColor.YELLOW + "/feed [player]" + ChatColor.GRAY + " - Restore hunger");
        }
        if (sender.hasPermission("nexus.staff.heal")) {
            sender.sendMessage(ChatColor.YELLOW + "/heal [player]" + ChatColor.GRAY + " - Heal a player");
        }
        if (sender.hasPermission("nexus.staff.fly")) {
            sender.sendMessage(ChatColor.YELLOW + "/fly [player]" + ChatColor.GRAY + " - Toggle flight");
        }
        if (sender.hasPermission("nexus.staff.god")) {
            sender.sendMessage(ChatColor.YELLOW + "/god [player]" + ChatColor.GRAY + " - Toggle god mode");
        }
        if (sender.hasPermission("nexus.staff.invsee")) {
            sender.sendMessage(ChatColor.YELLOW + "/invsee <player>" + ChatColor.GRAY + " - View inventory");
        }
        if (sender.hasPermission("nexus.staff.vanish")) {
            sender.sendMessage(ChatColor.YELLOW + "/vanish [player]" + ChatColor.GRAY + " - Toggle vanish");
        }
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            java.util.List<String> options = new java.util.ArrayList<>();
            options.add("help");
            if (sender.hasPermission("nexus.staff.kick")) options.add("kick");
            if (sender.hasPermission("nexus.staff.mute")) {
                options.add("mute");
                options.add("unmute");
            }
            if (sender.hasPermission("nexus.staff.feed")) options.add("feed");
            if (sender.hasPermission("nexus.staff.heal")) options.add("heal");
            if (sender.hasPermission("nexus.staff.fly")) options.add("fly");
            if (sender.hasPermission("nexus.staff.god")) options.add("god");
            if (sender.hasPermission("nexus.staff.invsee")) options.add("invsee");
            if (sender.hasPermission("nexus.staff.vanish")) options.add("vanish");
            return options;
        }
        return null;
    }
    
    /**
     * Data class for mute information
     */
    public static class MuteData {
        private final UUID playerId;
        private final String mutedBy;
        private final String reason;
        private final long expiresAt;
        
        public MuteData(UUID playerId, String mutedBy, String reason, long expiresAt) {
            this.playerId = playerId;
            this.mutedBy = mutedBy;
            this.reason = reason;
            this.expiresAt = expiresAt;
        }
        
        public UUID getPlayerId() {
            return playerId;
        }
        
        public String getMutedBy() {
            return mutedBy;
        }
        
        public String getReason() {
            return reason;
        }
        
        public long getExpiresAt() {
            return expiresAt;
        }
    }
}
