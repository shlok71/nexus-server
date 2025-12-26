package com.nexus.core.commands;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Command to create and use warps
 */
public class WarpCommand implements CommandExecutor {

    private static final Map<String, Location> warps = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            listWarps(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (!player.hasPermission("nexus.warp.create")) {
                player.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /warp set <name>");
                return true;
            }
            setWarp(player, args[1]);
            return true;
        }

        if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
            if (!player.hasPermission("nexus.warp.create")) {
                player.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /warp del <name>");
                return true;
            }
            deleteWarp(player, args[1]);
            return true;
        }

        // Try to teleport to warp
        String warpName = args[0].toLowerCase();
        teleportToWarp(player, warpName);

        return true;
    }

    private void listWarps(Player player) {
        if (warps.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No warps have been set.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Warps" + ChatColor.GOLD + " ===");

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String warp : warps.keySet()) {
            if (count > 0) {
                sb.append(ChatColor.GRAY).append(", ");
            }
            sb.append(ChatColor.AQUA).append(warp);
            count++;
        }

        player.sendMessage(sb.toString());
        player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.AQUA + "/warp <name>" + ChatColor.GRAY + " to teleport.");
    }

    private void setWarp(Player player, String name) {
        String warpName = name.toLowerCase();

        if (warps.containsKey(warpName)) {
            player.sendMessage(ChatColor.RED + "Warp '" + warpName + "' already exists!");
            return;
        }

        warps.put(warpName, player.getLocation().clone());
        player.sendMessage(ChatColor.GREEN + "Warp '" + warpName + "' set at your current location!");

        // Save to config
        NexusCore.getInstance().getConfigManager().saveWarps(warps);
    }

    private void deleteWarp(Player player, String name) {
        String warpName = name.toLowerCase();

        if (!warps.containsKey(warpName)) {
            player.sendMessage(ChatColor.RED + "Warp '" + warpName + "' not found!");
            return;
        }

        warps.remove(warpName);
        player.sendMessage(ChatColor.GREEN + "Warp '" + warpName + "' deleted!");

        // Save to config
        NexusCore.getInstance().getConfigManager().saveWarps(warps);
    }

    private void teleportToWarp(Player player, String warpName) {
        if (!warps.containsKey(warpName)) {
            player.sendMessage(ChatColor.RED + "Warp '" + warpName + "' not found!");
            return;
        }

        Location warpLocation = warps.get(warpName);

        // Validate the location is still valid
        if (warpLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Warp '" + warpName + "' is broken! Please contact an admin.");
            return;
        }

        player.teleport(warpLocation);
        player.sendMessage(ChatColor.AQUA + "Warped to " + warpName + "!");
    }

    public static Map<String, Location> getWarps() {
        return warps;
    }

    public static void loadWarps(Map<String, Location> loadedWarps) {
        warps.clear();
        warps.putAll(loadedWarps);
    }
}
