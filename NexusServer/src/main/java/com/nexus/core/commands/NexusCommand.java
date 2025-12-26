package com.nexus.core.commands;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.management.ManagementPermission;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * Main administrative command for NexusBlock Network
 */
public class NexusCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof CommandSender)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "info":
            case "status":
                return handleInfo(sender);
            case "players":
                return handlePlayers(sender);
            case "tps":
                return handleTPS(sender);
            case "memory":
                return handleMemory(sender);
            case "version":
                return handleVersion(sender);
            case "broadcast":
                return handleBroadcast(sender, args);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "NexusCore Help" + ChatColor.GOLD + " ===");
        sender.sendMessage(ChatColor.YELLOW + "/nexus info" + ChatColor.GRAY + " - Server information");
        sender.sendMessage(ChatColor.YELLOW + "/nexus tps" + ChatColor.GRAY + " - View server TPS");
        sender.sendMessage(ChatColor.YELLOW + "/nexus memory" + ChatColor.GRAY + " - View memory usage");
        sender.sendMessage(ChatColor.YELLOW + "/nexus players" + ChatColor.GRAY + - View online players");
        sender.sendMessage(ChatColor.YELLOW + "/nexus broadcast <msg>" + ChatColor.GRAY + " - Broadcast message");
        if (sender.hasPermission("nexus.admin")) {
            sender.sendMessage(ChatColor.RED + "/nexus reload" + ChatColor.GRAY + " - Reload configuration");
        }
        sender.sendMessage(ChatColor.GOLD + "==============================");
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("nexus.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }

        sender.sendMessage(ChatColor.AQUA + "Reloading NexusCore configuration...");
        NexusCore.getInstance().getConfigManager().reloadConfigs();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        RuntimeMXBean runtimeBean = ManagementPermission.getRuntimeMXBean();
        long uptime = runtimeBean.getUptime();

        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Server Information" + ChatColor.GOLD + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Server Name: " + ChatColor.WHITE + "NexusBlock Network");
        sender.sendMessage(ChatColor.YELLOW + "Minecraft Version: " + ChatColor.WHITE + Bukkit.getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Bukkit Version: " + ChatColor.WHITE + Bukkit.getBukkitVersion());
        sender.sendMessage(ChatColor.YELLOW + "Online Players: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
        sender.sendMessage(ChatColor.YELLOW + "Uptime: " + ChatColor.WHITE + formatUptime(uptime));
        sender.sendMessage(ChatColor.GOLD + "==============================");
        return true;
    }

    private boolean handleTPS(CommandSender sender) {
        double tps = Bukkit.getServer().getTPS()[0];
        String tpsColor = tps > 17 ? ChatColor.GREEN : (tps > 14 ? ChatColor.YELLOW : ChatColor.RED);

        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Server TPS" + ChatColor.GOLD + " ===");
        sender.sendMessage(ChatColor.YELLOW + "TPS (1m): " + tpsColor + String.format("%.2f", tps));
        sender.sendMessage(ChatColor.YELLOW + "Loaded Chunks: " + ChatColor.WHITE + Bukkit.getWorlds().stream()
                .mapToInt(w -> w.getLoadedChunks().length).sum());
        sender.sendMessage(ChatColor.YELLOW + "Entities: " + ChatColor.WHITE + Bukkit.getWorlds().stream()
                .mapToInt(w -> w.getEntities().size()).sum());
        sender.sendMessage(ChatColor.GOLD + "==============================");
        return true;
    }

    private boolean handleMemory(CommandSender sender) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Memory Usage" + ChatColor.GOLD + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Used: " + ChatColor.RED + usedMemory + " MB" + ChatColor.GRAY + " / " + ChatColor.WHITE + maxMemory + " MB");
        sender.sendMessage(ChatColor.YELLOW + "Allocated: " + ChatColor.WHITE + totalMemory + " MB");
        sender.sendMessage(ChatColor.GOLD + "==============================");
        return true;
    }

    private boolean handlePlayers(CommandSender sender) {
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();

        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Online Players (" + players.size() + ")" + ChatColor.GOLD + " ===");

        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No players online");
        } else {
            StringBuilder playerList = new StringBuilder();
            for (Player p : players) {
                if (playerList.length() > 0) {
                    playerList.append(ChatColor.GRAY).append(", ");
                }
                playerList.append(ChatColor.WHITE).append(p.getName());
            }
            sender.sendMessage(playerList.toString());
        }
        sender.sendMessage(ChatColor.GOLD + "==============================");
        return true;
    }

    private boolean handleBroadcast(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /nexus broadcast <message>");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (message.length() > 0) {
                message.append(" ");
            }
            message.append(args[i]);
        }

        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message.toString());
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Broadcast] " + formattedMessage);
        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Version Info" + ChatColor.GOLD + " ===");
        sender.sendMessage(ChatColor.YELLOW + "NexusCore: " + ChatColor.WHITE + "v1.0.0");
        sender.sendMessage(ChatColor.YELLOW + "Bukkit: " + ChatColor.WHITE + Bukkit.getBukkitVersion());
        sender.sendMessage(ChatColor.YELLOW + "NMS Version: " + ChatColor.WHITE + NexusCore.getInstance().getNmsUtils().getVersion());
        sender.sendMessage(ChatColor.GOLD + "==============================");
        return true;
    }

    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
    }
}
