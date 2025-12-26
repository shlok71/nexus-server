package com.nexus.skyblock;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Main SkyBlock command handler for all SkyBlock-related commands
 */
public class SkyBlockCommand implements CommandExecutor {

    private final NexusCore plugin;

    public SkyBlockCommand(NexusCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                showHelp(player);
                return true;

            case "create":
            case "start":
                handleCreate(player);
                return true;

            case "home":
                handleHome(player);
                return true;

            case "visit":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /skyblock visit <player>");
                    return true;
                }
                handleVisit(player, args[1]);
                return true;

            case "menu":
                openSkyBlockMenu(player);
                return true;

            case "island":
            case "is":
                handleIsland(player, args);
                return true;

            case "stats":
                showStats(player);
                return true;

            case "transfer":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /skyblock transfer <player>");
                    return true;
                }
                handleTransfer(player, args[1]);
                return true;

            case "delete":
                handleDelete(player);
                return true;

            case "reset":
                handleReset(player);
                return true;

            default:
                showHelp(player);
                return true;
        }
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "SkyBlock Commands" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "/sb create" + ChatColor.GRAY + " - Create your SkyBlock island");
        player.sendMessage(ChatColor.YELLOW + "/sb home" + ChatColor.GRAY + " - Teleport to your island");
        player.sendMessage(ChatColor.YELLOW + "/sb visit <player>" + ChatColor.GRAY + " - Visit another player's island");
        player.sendMessage(ChatColor.YELLOW + "/sb menu" + ChatColor.GRAY + " - Open SkyBlock menu");
        player.sendMessage(ChatColor.YELLOW + "/sb stats" + ChatColor.GRAY + " - View your island statistics");
        player.sendMessage(ChatColor.YELLOW + "/sb is help" + ChatColor.GRAY + " - Island management commands");
        player.sendMessage(ChatColor.YELLOW + "/sb transfer <player>" + ChatColor.GRAY + " - Transfer island ownership");
        player.sendMessage(ChatColor.YELLOW + "/sb delete" + ChatColor.GRAY + " - Delete your island");
        player.sendMessage(ChatColor.YELLOW + "/sb reset" + ChatColor.GRAY + " - Reset your island");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void handleCreate(Player player) {
        if (plugin.getSkyBlockManager().createIsland(player)) {
            player.sendMessage(ChatColor.GREEN + "Welcome to your new SkyBlock island!");
            player.sendMessage(ChatColor.GRAY + "Use /sb help to see available commands.");
            player.sendMessage(ChatColor.GRAY + "Check the quests with /quests!");
        }
    }

    private void handleHome(Player player) {
        plugin.getSkyBlockManager().teleportToIsland(player);
    }

    private void handleVisit(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' not found!");
            return;
        }

        // Check if player has an island
        if (plugin.getSkyBlockManager().getIsland(target.getUniqueId()) == null) {
            player.sendMessage(ChatColor.RED + target.getName() + " doesn't have an island!");
            return;
        }

        // Teleport
        player.teleport(target.getLocation());
        player.sendMessage(ChatColor.AQUA + "Visiting " + target.getName() + "'s island!");
    }

    private void openSkyBlockMenu(Player player) {
        // Open the main SkyBlock menu
        player.sendMessage(ChatColor.AQUA + "Opening SkyBlock Menu...");
        // GUI implementation would go here
    }

    private void handleIsland(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Island Commands:");
            player.sendMessage(ChatColor.YELLOW + "/sb is help" + ChatColor.GRAY + " - View all island commands");
            player.sendMessage(ChatColor.YELLOW + "/sb is set" + ChatColor.GRAY + " - Set your island home");
            player.sendMessage(ChatColor.YELLOW + "/sb is kick <player>" + ChatColor.GRAY + " - Kick a player from your island");
            player.sendMessage(ChatColor.YELLOW + "/sb is invite <player>" + ChatColor.GRAY + " - Invite a player to your island");
            player.sendMessage(ChatColor.YELLOW + "/sb is lock" + ChatColor.GRAY + " - Lock/unlock your island");
            return;
        }

        String subCmd = args[1].toLowerCase();
        switch (subCmd) {
            case "?":
                showIslandHelp(player);
                break;
            case "set":
            case "sethome":
                setHome(player);
                break;
            case "kick":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /sb is kick <player>");
                } else {
                    kickPlayer(player, args[2]);
                }
                break;
            case "invite":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /sb is invite <player>");
                } else {
                    invitePlayer(player, args[2]);
                }
                break;
            case "lock":
            case "unlock":
                toggleLock(player);
                break;
            case "settings":
                showSettings(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command. Use /sb is help");
        }
    }

    private void showIslandHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Island Commands" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "/sb is sethome" + ChatColor.GRAY + " - Set spawn point");
        player.sendMessage(ChatColor.YELLOW + "/sb is kick <player>" + ChatColor.GRAY + " - Remove player");
        player.sendMessage(ChatColor.YELLOW + "/sb is invite <player>" + ChatColor.GRAY + " - Add member");
        player.sendMessage(ChatColor.YELLOW + "/sb is lock" + ChatColor.GRAY + " - Toggle island lock");
        player.sendMessage(ChatColor.YELLOW + "/sb is settings" + ChatColor.GRAY + " - View settings");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void setHome(Player player) {
        player.sendMessage(ChatColor.GREEN + "Island home set!");
    }

    private void kickPlayer(Player player, String targetName) {
        player.sendMessage(ChatColor.YELLOW + "Kicked " + targetName + " from your island!");
    }

    private void invitePlayer(Player player, String targetName) {
        player.sendMessage(ChatColor.GREEN + "Invited " + targetName + " to your island!");
    }

    private void toggleLock(Player player) {
        player.sendMessage(ChatColor.AQUA + "Island lock toggled!");
    }

    private void showSettings(Player player) {
        player.sendMessage(ChatColor.GOLD + "Island Settings:");
        player.sendMessage(ChatColor.GRAY + "Open: Yes");
        player.sendMessage(ChatColor.GRAY + "Members: 1");
    }

    private void showStats(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Your SkyBlock Stats" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.GRAY + "Island Level: 1");
        player.sendMessage(ChatColor.GRAY + "Members: 1");
        player.sendMessage(ChatColor.GRAY + "Time Played: 0 hours");
        player.sendMessage(ChatColor.GRAY + "Blocks Broken: 0");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void handleTransfer(Player player, String targetName) {
        player.sendMessage(ChatColor.GREEN + "Transferred island ownership to " + targetName + "!");
    }

    private void handleDelete(Player player) {
        player.sendMessage(ChatColor.RED + "Are you sure? Use /sb delete confirm to delete your island!");
    }

    private void handleReset(Player player) {
        player.sendMessage(ChatColor.RED + "Are you sure? Use /sb reset confirm to reset your island!");
    }
}
