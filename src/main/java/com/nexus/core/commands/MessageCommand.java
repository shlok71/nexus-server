package com.nexus.core.commands;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * Command for sending private messages between players
 */
public class MessageCommand implements CommandExecutor {

    private static final HashMap<UUID, UUID> lastMessages = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length >= 2) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    StringBuilder message = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        if (message.length() > 0) message.append(" ");
                        message.append(args[i]);
                    }
                    String formattedMsg = ChatColor.translateAlternateColorCodes('&', message.toString());

                    // Send to target
                    target.sendMessage(ChatColor.LIGHT_PURPLE + "[From Console] " + ChatColor.WHITE + formattedMsg);

                    // Send confirmation
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[To " + target.getName() + "] " + ChatColor.WHITE + formattedMsg);

                    lastMessages.put(target.getUniqueId(), sender instanceof Player ? ((Player) sender).getUniqueId() : null);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player '" + args[0] + "' not found!");
            return true;
        }

        if (target == player) {
            player.sendMessage(ChatColor.RED + "You cannot message yourself!");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (message.length() > 0) message.append(" ");
            message.append(args[i]);
        }

        String formattedMsg = ChatColor.translateAlternateColorCodes('&', message.toString());

        // Send to target
        target.sendMessage(ChatColor.LIGHT_PURPLE + "[From " + getPlayerPrefix(player) + player.getName() + ChatColor.LIGHT_PURPLE + "] " + ChatColor.WHITE + formattedMsg);

        // Send confirmation to sender
        player.sendMessage(ChatColor.LIGHT_PURPLE + "[To " + getPlayerPrefix(target) + target.getName() + ChatColor.LIGHT_PURPLE + "] " + ChatColor.WHITE + formattedMsg);

        // Store for reply system
        lastMessages.put(target.getUniqueId(), player.getUniqueId());

        return true;
    }

    private String getPlayerPrefix(Player player) {
        if (player.isOp()) {
            return ChatColor.RED + "";
        } else if (player.hasPermission("nexus.vip")) {
            return ChatColor.GOLD + "";
        }
        return ChatColor.WHITE + "";
    }

    public static UUID getLastMessaged(UUID player) {
        return lastMessages.get(player);
    }

    public static void clearLastMessage(UUID player) {
        lastMessages.remove(player);
    }
}
