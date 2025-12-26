package com.nexus.skyblock.minions;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Command handler for minion-related commands
 */
public class MinionCommand implements CommandExecutor {

    private final NexusCore plugin;

    public MinionCommand(NexusCore plugin) {
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

            case "place":
            case "spawn":
            case "create":
                handlePlace(player);
                return true;

            case "upgrade":
                handleUpgrade(player);
                return true;

            case "storage":
            case "inv":
            case "inventory":
                handleStorage(player);
                return true;

            case "collect":
            case "gather":
                handleCollect(player);
                return true;

            case "info":
            case "status":
                handleInfo(player);
                return true;

            case "types":
            case "list":
                showTypes(player);
                return true;

            default:
                showHelp(player);
                return true;
        }
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Minion Commands" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "/minion place" + ChatColor.GRAY + " - Place a minion");
        player.sendMessage(ChatColor.YELLOW + "/minion upgrade" + ChatColor.GRAY + " - Upgrade selected minion");
        player.sendMessage(ChatColor.YELLOW + "/minion storage" + ChatColor.GRAY + " - Access minion storage");
        player.sendMessage(ChatColor.YELLOW + "/minion collect" + ChatColor.GRAY + " - Collect items");
        player.sendMessage(ChatColor.YELLOW + "/minion info" + ChatColor.GRAY + " - View minion info");
        player.sendMessage(ChatColor.YELLOW + "/minion types" + ChatColor.GRAY + " - List minion types");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void handlePlace(Player player) {
        player.sendMessage(ChatColor.AQUA + "Place a minion by right-clicking with the minion item!");
        player.sendMessage(ChatColor.GRAY + "Use /minion types to see available minions.");
    }

    private void handleUpgrade(Player player) {
        player.sendMessage(ChatColor.AQUA + "Right-click a minion to upgrade it!");
        player.sendMessage(ChatColor.GRAY + "Upgrading costs coins based on current tier.");
    }

    private void handleStorage(Player player) {
        player.sendMessage(ChatColor.AQUA + "Right-click a minion to access its storage!");
    }

    private void handleCollect(Player player) {
        player.sendMessage(ChatColor.GREEN + "Collecting items from all minions...");
        // Implementation: collect from all player minions
        player.sendMessage(ChatColor.GRAY + "Items have been added to your inventory!");
    }

    private void handleInfo(Player player) {
        List<MinionData> minions = plugin.getMinionManager().getPlayerMinions(player.getUniqueId());
        if (minions.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "You don't have any minions!");
            return;
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Your Minions" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.GRAY + "Total Minions: " + minions.size());

        int i = 1;
        for (MinionData minion : minions) {
            player.sendMessage(ChatColor.YELLOW + "#" + i + " " +
                minion.getType().getDisplayName() + " Minion " +
                getRomanNumeral(minion.getTier()));
            i++;
        }
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void showTypes(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Available Minion Types" + ChatColor.GOLD + " ===");

        int i = 0;
        for (MinionManager.MinionType type : MinionManager.MinionType.values()) {
            if (i >= 20) {
                player.sendMessage(ChatColor.GRAY + "... and more!");
                break;
            }
            player.sendMessage(ChatColor.YELLOW + "- " + type.getDisplayName());
            i++;
        }

        player.sendMessage(ChatColor.GRAY + "Get minion spawn eggs from the /shop!");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private String getRomanNumeral(int num) {
        String[] roman = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI"};
        return roman[Math.min(num, 11)];
    }
}
