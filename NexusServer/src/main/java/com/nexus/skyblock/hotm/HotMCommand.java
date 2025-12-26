package com.nexus.skyblock.hotm;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for Heart of the Mountain (HotM) commands
 */
public class HotMCommand implements CommandExecutor {

    private final NexusCore plugin;

    public HotMCommand(NexusCore plugin) {
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
            openHotMInterface(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                showHelp(player);
                return true;

            case "menu":
            case "gui":
            case "tree":
                openHotMInterface(player);
                return true;

            case "stats":
            case "status":
                showStats(player);
                return true;

            case "powder":
                showPowder(player);
                return true;

            case "xp":
            case "mining":
                showMiningXp(player);
                return true;

            case "perks":
                showPerks(player);
                return true;

            default:
                openHotMInterface(player);
                return true;
        }
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Heart of the Mountain Commands" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "/hotm" + ChatColor.GRAY + " - Open HotM interface");
        player.sendMessage(ChatColor.YELLOW + "/hotm stats" + ChatColor.GRAY + " - View your stats");
        player.sendMessage(ChatColor.YELLOW + "/hotm powder" + ChatColor.GRAY + " - View powder amounts");
        player.sendMessage(ChatColor.YELLOW + "/hotm xp" + ChatColor.GRAY + " - View mining XP");
        player.sendMessage(ChatColor.YELLOW + "/hotm perks" + ChatColor.GRAY + " - List unlocked perks");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Unlock perks with Mithril Powder in the HotM menu!");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void openHotMInterface(Player player) {
        player.sendMessage(ChatColor.AQUA + "Opening Heart of the Mountain...");
        plugin.getHotmManager().openHotMInterface(player);
    }

    private void showStats(Player player) {
        HotMManager.PlayerHotMData data = plugin.getHotmManager().getPlayerData(player.getUniqueId());

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Your HotM Stats" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "Tier: " + ChatColor.WHITE + data.getCurrentTier());
        player.sendMessage(ChatColor.YELLOW + "Mining XP: " + ChatColor.WHITE + formatNumber(data.getMiningXp()));
        player.sendMessage(ChatColor.YELLOW + "Mithril Powder: " + ChatColor.WHITE + formatNumber(data.getMithrilPowder()));
        player.sendMessage(ChatColor.YELLOW + "Gemstone Powder: " + ChatColor.WHITE + formatNumber(data.getGemstonePowder()));
        player.sendMessage(ChatColor.YELLOW + "Unlocked Perks: " + ChatColor.WHITE + data.getUnlockedPerks().size());
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void showPowder(Player player) {
        HotMManager.PlayerHotMData data = plugin.getHotmManager().getPlayerData(player.getUniqueId());

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Your Powder" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.DARK_PURPLE + "Mithril Powder: " + ChatColor.WHITE + formatNumber(data.getMithrilPowder()));
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Gemstone Powder: " + ChatColor.WHITE + formatNumber(data.getGemstonePowder()));
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Use Mithril Powder to unlock perks in /hotm!");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void showMiningXp(Player player) {
        HotMManager.PlayerHotMData data = plugin.getHotmManager().getPlayerData(player.getUniqueId());

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Mining XP" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "Total XP: " + ChatColor.WHITE + formatNumber(data.getMiningXp()));
        player.sendMessage(ChatColor.YELLOW + "Current Tier: " + ChatColor.WHITE + data.getCurrentTier());
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Earn Mining XP by breaking blocks in the mines!");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void showPerks(Player player) {
        HotMManager.PlayerHotMData data = plugin.getHotmManager().getPlayerData(player.getUniqueId());

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Unlocked Perks" + ChatColor.GOLD + " ===");

        if (data.getUnlockedPerks().isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No perks unlocked yet!");
            player.sendMessage(ChatColor.GRAY + "Use /hotm to unlock perks.");
        } else {
            for (String perkId : data.getUnlockedPerks()) {
                HotMManager.HotMPerk perk = getPerkById(perkId);
                if (perk != null) {
                    player.sendMessage(ChatColor.GREEN + "✓ " + ChatColor.WHITE + perk.getName());
                }
            }
        }

        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private HotMManager.HotMPerk getPerkById(String id) {
        // In a full implementation, this would look up from the HotMManager
        return null;
    }

    private String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }
}
