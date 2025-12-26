package com.nexus.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command to view server information and statistics
 */
public class ServerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendServerInfo(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
            case "players":
                if (!sender.hasPermission("nexus.server.list")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                sendPlayerList(sender);
                break;
            case "motd":
                if (!sender.hasPermission("nexus.server.motd")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "Server MOTD: " + ChatColor.WHITE + org.bukkit.Bukkit.getServer().getMotd());
                break;
            default:
                sendServerInfo(sender);
                break;
        }

        return true;
    }

    private void sendServerInfo(CommandSender sender) {
        org.bukkit.Server server = org.bukkit.Bukkit.getServer();

        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.AQUA + "Server Info" + ChatColor.GOLD + " ==========");
        sender.sendMessage(ChatColor.YELLOW + "Server Name: " + ChatColor.WHITE + "NexusBlock Network");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + server.getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Bukkit Version: " + ChatColor.WHITE + server.getBukkitVersion());
        sender.sendMessage(ChatColor.YELLOW + "Online: " + ChatColor.WHITE + server.getOnlinePlayers().size() + "/" + server.getMaxPlayers());
        sender.sendMessage(ChatColor.YELLOW + "Worlds: " + ChatColor.WHITE + server.getWorlds().size());
        sender.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void sendPlayerList(CommandSender sender) {
        List<Player> players = (List<Player>) org.bukkit.Bukkit.getOnlinePlayers();

        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.AQUA + "Players (" + players.size() + ")" + ChatColor.GOLD + " ==========");

        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No players online");
        } else {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Player p : players) {
                if (count > 0) {
                    sb.append(ChatColor.GRAY).append(", ");
                }
                if (p.isOp()) {
                    sb.append(ChatColor.RED);
                } else if (p.hasPermission("nexus.vip")) {
                    sb.append(ChatColor.GOLD);
                } else {
                    sb.append(ChatColor.WHITE);
                }
                sb.append(p.getName());
                count++;
            }
            sender.sendMessage(sb.toString());
        }
        sender.sendMessage(ChatColor.GOLD + "===================================");
    }
}
