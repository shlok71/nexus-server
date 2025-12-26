package com.nexus.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * Command to request teleportation to another player
 */
public class TPACommand implements CommandExecutor {

    private static final HashMap<UUID, TPARequest> pendingRequests = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /tpa <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player '" + args[0] + "' not found!");
            return true;
        }

        if (target == player) {
            player.sendMessage(ChatColor.RED + "You cannot teleport to yourself!");
            return true;
        }

        // Check if there's already a pending request
        if (pendingRequests.containsKey(player.getUniqueId())) {
            TPARequest existing = pendingRequests.get(player.getUniqueId());
            if (existing.getTarget().equals(target)) {
                player.sendMessage(ChatColor.RED + "You already have a pending request to " + target.getName() + "!");
                return true;
            }
        }

        // Create the request
        TPARequest request = new TPARequest(player, target);
        pendingRequests.put(player.getUniqueId(), request);

        // Notify the target
        target.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.WHITE + " wants to teleport to you.");
        target.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.AQUA + "/tpaccept" + ChatColor.YELLOW + " or " + ChatColor.RED + "/tpdeny");
        target.sendMessage(ChatColor.GRAY + "Request expires in 60 seconds.");

        // Notify the sender
        player.sendMessage(ChatColor.AQUA + "Teleport request sent to " + target.getName() + "!");
        player.sendMessage(ChatColor.GRAY + "Request expires in 60 seconds.");

        // Schedule request expiration
        Bukkit.getScheduler().runTaskLaterAsynchronously(
            Bukkit.getPluginManager().getPlugin("NexusCore"),
            () -> {
                if (pendingRequests.remove(player.getUniqueId()) != null) {
                    // Only notify if the request is still the same
                    if (target.isOnline()) {
                        player.sendMessage(ChatColor.RED + "Your teleport request to " + target.getName() + " has expired.");
                    }
                }
            },
            1200L // 60 seconds * 20 ticks
        );

        return true;
    }

    public static TPARequest getRequest(UUID player) {
        return pendingRequests.get(player);
    }

    public static void removeRequest(UUID player) {
        pendingRequests.remove(player);
    }

    /**
     * Represents a teleport request
     */
    public static class TPARequest {
        private final Player requester;
        private final Player target;
        private final long timestamp;

        public TPARequest(Player requester, Player target) {
            this.requester = requester;
            this.target = target;
            this.timestamp = System.currentTimeMillis();
        }

        public Player getRequester() {
            return requester;
        }

        public Player getTarget() {
            return target;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 60000; // 60 seconds
        }
    }
}
