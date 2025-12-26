package com.nexus.skyblock.shops;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Command handler for shop-related commands
 */
public class ShopCommand implements CommandExecutor {

    private final NexusCore plugin;

    public ShopCommand(NexusCore plugin) {
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
            openShopCatalog(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                showHelp(player);
                return true;

            case "catalog":
            case "menu":
            case "gui":
                openShopCatalog(player);
                return true;

            case "list":
                listShops(player);
                return true;

            case "buy":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /shop buy <item>");
                    return true;
                }
                openBuyMenu(player, args[1]);
                return true;

            case "sell":
                handleSellPrompt(player);
                return true;

            case "npc":
                openNearbyNPCs(player);
                return true;

            default:
                openShopCatalog(player);
                return true;
        }
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Shop Commands" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "/shop" + ChatColor.GRAY + " - Open shop catalog");
        player.sendMessage(ChatColor.YELLOW + "/shop list" + ChatColor.GRAY + " - List all shops");
        player.sendMessage(ChatColor.YELLOW + "/shop buy <item>" + ChatColor.GRAY + " - Buy items");
        player.sendMessage(ChatColor.YELLOW + "/shop sell" + ChatColor.GRAY + - Sell items from inventory");
        player.sendMessage(ChatColor.YELLOW + "/shop npc" + ChatColor.GRAY + " - Find nearby NPC shops");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void openShopCatalog(Player player) {
        player.sendMessage(ChatColor.AQUA + "Opening Shop Catalog...");
        plugin.getShopManager().openShopCatalog(player);
    }

    private void listShops(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Available Shops" + ChatColor.GOLD + " ===");

        // List all shops
        player.sendMessage(ChatColor.YELLOW + "1. " + ChatColor.WHITE + "Coal Miner");
        player.sendMessage(ChatColor.GRAY + "   Coal, torches, and mining supplies");
        player.sendMessage(ChatColor.YELLOW + "2. " + ChatColor.WHITE + "Iron Merchant");
        player.sendMessage(ChatColor.GRAY + "   Iron ingots, blocks, and anvils");
        player.sendMessage(ChatColor.YELLOW + "3. " + ChatColor.WHITE + "Gold Merchant");
        player.sendMessage(ChatColor.GRAY + "   Gold ingots, blocks, and golden apples");
        player.sendMessage(ChatColor.YELLOW + "4. " + ChatColor.WHITE + "Diamond Dealer");
        player.sendMessage(ChatColor.GRAY + "   Diamonds and diamond equipment");
        player.sendMessage(ChatColor.YELLOW + "5. " + ChatColor.WHITE + "Farm Merchant");
        player.sendMessage(ChatColor.GRAY + "   Seeds, crops, and farming supplies");
        player.sendMessage(ChatColor.YELLOW + "6. " + ChatColor.WHITE + "Lumberjack");
        player.sendMessage(ChatColor.GRAY + "   Wood, planks, and sticks");
        player.sendMessage(ChatColor.YELLOW + "7. " + ChatColor.WHITE + "Builder");
        player.sendMessage(ChatColor.GRAY + "   Building blocks and materials");
        player.sendMessage(ChatColor.YELLOW + "8. " + ChatColor.WHITE + "Magic Merchant");
        player.sendMessage(ChatColor.GRAY + "   Enchanting supplies");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Use /shop to browse all items!");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void openBuyMenu(Player player, String itemType) {
        player.sendMessage(ChatColor.AQUA + "Looking for: " + itemType);
        player.sendMessage(ChatColor.GRAY + "Use /shop to browse all available items!");
    }

    private void handleSellPrompt(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Sell Items" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.GRAY + "To sell items:");
        player.sendMessage(ChatColor.YELLOW + "1. Hold the item you want to sell");
        player.sendMessage(ChatColor.YELLOW + "2. Type: /sell <amount> or /sell all");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Items sell for 60% of their shop price!");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private void openNearbyNPCs(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Nearby NPCs" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.GRAY + "Right-click NPC villagers to open their shops!");
        player.sendMessage(ChatColor.GRAY + "NPCs are located in the Hub area.");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }
}
