package com.nexus.skyblock.quests;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command handler for quest-related commands
 */
public class QuestCommand implements CommandExecutor {

    private final NexusCore plugin;

    public QuestCommand(NexusCore plugin) {
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
            openQuestLog(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                showHelp(player);
                return true;

            case "log":
            case "menu":
            case "gui":
                openQuestLog(player);
                return true;

            case "list":
                listQuests(player);
                return true;

            case "active":
            case "current":
                showActiveQuests(player);
                return true;

            case "completed":
            case "done":
                showCompletedQuests(player);
                return true;

            case "start":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /quest start <questid>");
                    return true;
                }
                startQuest(player, args[1]);
                return true;

            default:
                openQuestLog(player);
                return true;
        }
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Quest Commands" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "/quests" + ChatColor.GRAY + " - Open quest log");
        player.sendMessage(ChatColor.YELLOW + "/quest list" + ChatColor.GRAY + " - List available quests");
        player.sendMessage(ChatColor.YELLOW + "/quest active" + ChatColor.GRAY + " - View active quests");
        player.sendMessage(ChatColor.YELLOW + "/quest completed" + ChatColor.GRAY + " - View completed quests");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void openQuestLog(Player player) {
        player.sendMessage(ChatColor.AQUA + "Opening Quest Log...");
        plugin.getQuestManager().openQuestGUI(player);
    }

    private void listQuests(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Available Quests" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.GRAY + "Use /quests to open the quest GUI!");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Story Quests:");
        player.sendMessage(ChatColor.GRAY + "  - Welcome to SkyBlock");
        player.sendMessage(ChatColor.GRAY + "  - First Steps");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Mining Quests:");
        player.sendMessage(ChatColor.GRAY + "  - Cobble Master");
        player.sendMessage(ChatColor.GRAY + "  - Stone Age");
        player.sendMessage(ChatColor.GRAY + "  - Coal Rush");
        player.sendMessage(ChatColor.GRAY + "  - Iron Man");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Farming Quests:");
        player.sendMessage(ChatColor.GRAY + "  - Green Thumb");
        player.sendMessage(ChatColor.GRAY + "  - Harvest Moon");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Combat Quests:");
        player.sendMessage(ChatColor.GRAY + "  - First Blood");
        player.sendMessage(ChatColor.GRAY + "  - Monster Hunter");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void showActiveQuests(Player player) {
        List<QuestManager.Quest> activeQuests = plugin.getQuestManager().getActiveQuests(player);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Active Quests" + ChatColor.GOLD + " ===");

        if (activeQuests.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No active quests!");
            player.sendMessage(ChatColor.GRAY + "Use /quests to pick up new quests.");
        } else {
            for (QuestManager.Quest quest : activeQuests) {
                QuestManager.PlayerQuestData data = plugin.getQuestManager().getPlayerData(player.getUniqueId());
                QuestManager.QuestProgress progress = data.getProgress(quest.getId());
                int current = progress.getCurrentProgress();
                int target = quest.getTargetAmount();
                int percent = (int) ((current / (double) target) * 100);

                player.sendMessage(ChatColor.YELLOW + quest.getName() + ": " +
                    ChatColor.GREEN + current + "/" + target +
                    ChatColor.GRAY + " (" + percent + "%)");
            }
        }

        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void showCompletedQuests(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Completed Quests" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.GRAY + "Quest completion tracking coming soon!");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void startQuest(Player player, String questId) {
        if (plugin.getQuestManager().startQuest(player, questId)) {
            player.sendMessage(ChatColor.GREEN + "Quest started! Check your progress with /quest active");
        }
    }

    private QuestManager.PlayerQuestData getPlayerData(Player player) {
        return plugin.getQuestManager().getPlayerData(player.getUniqueId());
    }
}
