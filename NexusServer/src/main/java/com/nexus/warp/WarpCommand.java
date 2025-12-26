package com.nexus.warp;

import com.nexus.core.NexusCore;
import com.nexus.core.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Command handler for /warp command.
 * Allows players to use, create, and manage warps.
 */
public class WarpCommand extends SubCommand {
    
    private final WarpManager warpManager;
    
    public WarpCommand(NexusCore plugin) {
        super(plugin, "warp", "nexus.warp.use", false);
        this.warpManager = plugin.getWarpManager();
    }
    
    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("commands.playerOnly"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Show available warps
            showWarps(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help", "?" -> showHelp(player);
            case "list" -> showWarps(player);
            case "create", "set" -> createWarp(player, args);
            case "delete", "remove", "del" -> deleteWarp(player, args);
            case "info" -> showWarpInfo(player, args);
            case "private", "hidden" -> togglePrivate(player, args);
            default -> {
                // Try to teleport to warp
                if (warpManager.teleportToWarp(player, args[0])) {
                    // Success - message handled by manager
                } else {
                    player.sendMessage(getMessage("warp.notFound")
                        .replace("%warp%", args[0]));
                    showHelp(player);
                }
            }
        }
        
        return true;
    }
    
    /**
     * Display all available warps
     */
    private void showWarps(Player player) {
        Set<String> warps = warpManager.getAllWarps();
        
        if (warps.isEmpty()) {
            player.sendMessage(getMessage("warp.noWarps"));
            return;
        }
        
        player.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            "Available Warps (" + warps.size() + ")\n");
        
        StringBuilder warpList = new StringBuilder();
        int count = 0;
        for (String warpName : warps) {
            WarpManager.WarpData warp = warpManager.getWarp(warpName);
            if (warp != null && (warp.isPublic() || warp.getOwnerId().equals(player.getUniqueId()))) {
                if (count > 0) {
                    warpList.append(ChatColor.GRAY).append(", ");
                }
                warpList.append(ChatColor.AQUA).append(warp.getName());
                count++;
            }
        }
        
        if (count == 0) {
            player.sendMessage(ChatColor.GRAY + "No public warps available.");
        } else {
            player.sendMessage(warpList.toString());
        }
        
        player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/warp <name>" + 
            ChatColor.GRAY + " to teleport");
    }
    
    /**
     * Create a new warp
     */
    private void createWarp(Player player, String[] args) {
        if (!player.hasPermission("nexus.warp.create")) {
            player.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/warp create <name>"));
            return;
        }
        
        String name = args[1];
        
        // Validate name
        if (!name.matches("^[a-zA-Z0-9_]+$")) {
            player.sendMessage(getMessage("warp.invalidName"));
            return;
        }
        
        Location location = player.getLocation();
        
        if (warpManager.createWarp(name, location, player.getUniqueId())) {
            player.sendMessage(getMessage("warp.created")
                .replace("%warp%", name));
            
            // Broadcast if it's a special warp
            if (player.hasPermission("nexus.admin")) {
                Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + 
                    ChatColor.WHITE + " created warp: " + ChatColor.AQUA + name);
            }
        } else {
            player.sendMessage(getMessage("warp.alreadyExists")
                .replace("%warp%", name));
        }
    }
    
    /**
     * Delete a warp
     */
    private void deleteWarp(Player player, String[] args) {
        if (!player.hasPermission("nexus.warp.delete")) {
            player.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/warp delete <name>"));
            return;
        }
        
        String name = args[1];
        WarpManager.WarpData warp = warpManager.getWarp(name);
        
        if (warp == null) {
            player.sendMessage(getMessage("warp.notFound")
                .replace("%warp%", name));
            return;
        }
        
        // Check ownership for non-admins
        if (!player.hasPermission("nexus.admin") && 
            warp.getOwnerId() != null && 
            !warp.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage(getMessage("warp.notOwner"));
            return;
        }
        
        if (warpManager.deleteWarp(name)) {
            player.sendMessage(getMessage("warp.deleted")
                .replace("%warp%", name));
        } else {
            player.sendMessage(getMessage("warp.notFound")
                .replace("%warp%", name));
        }
    }
    
    /**
     * Show warp information
     */
    private void showWarpInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/warp info <name>"));
            return;
        }
        
        String name = args[1];
        WarpManager.WarpData warp = warpManager.getWarp(name);
        
        if (warp == null) {
            player.sendMessage(getMessage("warp.notFound")
                .replace("%warp%", name));
            return;
        }
        
        player.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            warp.getName() + " Warp\n");
        player.sendMessage(ChatColor.GRAY + "Owner: " + ChatColor.WHITE + warp.getOwnerName());
        player.sendMessage(ChatColor.GRAY + "Uses: " + ChatColor.WHITE + warp.getUses());
        player.sendMessage(ChatColor.GRAY + "Public: " + (warp.isPublic() ? 
            ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        
        Location loc = warp.getLocation();
        player.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.WHITE + 
            loc.getWorld().getName() + " (" + loc.getBlockX() + ", " + 
            loc.getBlockY() + ", " + loc.getBlockZ() + ")");
    }
    
    /**
     * Toggle warp privacy
     */
    private void togglePrivate(Player player, String[] args) {
        if (!player.hasPermission("nexus.warp.create")) {
            player.sendMessage(getMessage("commands.noPermission"));
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(getMessage("commands.usage")
                .replace("%command%", "/warp private <name>"));
            return;
        }
        
        String name = args[1];
        WarpManager.WarpData warp = warpManager.getWarp(name);
        
        if (warp == null) {
            player.sendMessage(getMessage("warp.notFound")
                .replace("%warp%", name));
            return;
        }
        
        warp.setPublic(!warp.isPublic());
        player.sendMessage(getMessage("warp.toggled")
            .replace("%warp%", name)
            .replace("%state%", warp.isPublic() ? "public" : "private"));
    }
    
    /**
     * Show help message
     */
    private void showHelp(Player player) {
        player.sendMessage("\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + 
            "WARP COMMAND\n");
        player.sendMessage(ChatColor.YELLOW + "/warp" + ChatColor.GRAY + " - List all warps");
        player.sendMessage(ChatColor.YELLOW + "/warp <name>" + ChatColor.GRAY + " - Teleport to warp");
        player.sendMessage(ChatColor.YELLOW + "/warp list" + ChatColor.GRAY + " - List all warps");
        player.sendMessage(ChatColor.YELLOW + "/warp info <name>" + ChatColor.GRAY + " - View warp info");
        
        if (player.hasPermission("nexus.warp.create")) {
            player.sendMessage(ChatColor.YELLOW + "/warp create <name>" + ChatColor.GRAY + " - Create a warp");
            player.sendMessage(ChatColor.YELLOW + "/warp private <name>" + ChatColor.GRAY + " - Toggle warp privacy");
        }
        
        if (player.hasPermission("nexus.warp.delete")) {
            player.sendMessage(ChatColor.YELLOW + "/warp delete <name>" + ChatColor.GRAY + " - Delete a warp");
        }
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("nexus.warp.create")) {
                return Arrays.asList("help", "list", "create", "delete", "info");
            }
            return Arrays.asList("help", "list");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create") && sender.hasPermission("nexus.warp.create")) {
                return Arrays.asList("warpname");
            }
            if ((args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("info") || 
                 args[0].equalsIgnoreCase("private")) && sender.hasPermission("nexus.warp.delete")) {
                java.util.List<String> warps = new java.util.ArrayList<>();
                for (String warp : warpManager.getAllWarps()) {
                    warps.add(warp);
                }
                return warps;
            }
            if (warpManager.hasWarp(args[0]) || args[0].isEmpty()) {
                java.util.List<String> warps = new java.util.ArrayList<>();
                for (String warp : warpManager.getAllWarps()) {
                    warps.add(warp);
                }
                return warps;
            }
        }
        return null;
    }
}
