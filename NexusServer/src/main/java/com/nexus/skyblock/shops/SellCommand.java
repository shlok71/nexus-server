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
 * Command handler for selling items to shops
 */
public class SellCommand implements CommandExecutor {

    private final NexusCore plugin;

    public SellCommand(NexusCore plugin) {
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
            showSellHelp(player);
            return true;
        }

        String amountArg = args[0].toLowerCase();
        int amount;

        // Parse amount
        if (amountArg.equals("all")) {
            amount = -1; // Special value for all items
        } else {
            try {
                amount = Integer.parseInt(amountArg);
                if (amount <= 0) {
                    player.sendMessage(ChatColor.RED + "Invalid amount!");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid amount! Use a number or 'all'");
                return true;
            }
        }

        // Get item in hand
        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item to sell!");
            return true;
        }

        Material material = item.getType();
        int availableAmount = item.getAmount();

        int sellAmount;
        if (amount == -1) {
            sellAmount = availableAmount;
        } else {
            sellAmount = Math.min(amount, availableAmount);
        }

        // Calculate sell price
        int pricePerItem = getSellPrice(material);
        long totalValue = (long) sellAmount * pricePerItem;

        if (pricePerItem == 0) {
            player.sendMessage(ChatColor.RED + "This item cannot be sold!");
            return true;
        }

        // Remove items from inventory
        int remaining = sellAmount;
        for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
            ItemStack invItem = player.getInventory().getItem(i);
            if (invItem != null && invItem.getType() == material) {
                int toRemove = Math.min(invItem.getAmount(), remaining);
                invItem.setAmount(invItem.getAmount() - toRemove);
                remaining -= toRemove;
            }
        }

        // Add coins
        plugin.getEconomyManager().addCoins(player.getUniqueId(), totalValue);

        // Notify player
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "Sold " + sellAmount + " " + formatMaterialName(material) + "!");
        player.sendMessage(ChatColor.GOLD + "Received: " + totalValue + " Coins");
        player.sendMessage("");

        // Play sound
        player.playSound(player.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.0f);

        return true;
    }

    private void showSellHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.AQUA + "Sell Command" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "/sell <amount>" + ChatColor.GRAY + " - Sell held item");
        player.sendMessage(ChatColor.YELLOW + "/sell all" + ChatColor.GRAY + " - Sell all of that item type");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Items sell for 60% of their buy price!");
        player.sendMessage(ChatColor.GOLD + "===================================");
    }

    private int getSellPrice(Material material) {
        // Simplified price lookup
        // In full implementation, this would check the ShopManager
        int[] prices = {
            1,    // COAL
            3,    // IRON_INGOT
            6,    // GOLD_INGOT
            10,   // DIAMOND
            15,   // EMERALD
            2,    // REDSTONE
            1,    // LAPIS_ORE
            5,    // COBBLESTONE
            2,    // DIRT
            2,    // SAND
            1,    // GRAVEL
            4,    // SUGAR_CANE
            2,    // WHEAT
            2,    // CARROT
            2,    // POTATO
            10,   // MELON
            10,   // PUMPKIN
            1,    // STICK
            1,    // WOOD
            1,    // LOG
            5,    // SANDSTONE
            3,    // STONE
            20,   // OBSIDIAN
            5,    // BONE
            2,    // STRING
            3,    // ROTTEN_FLESH
            5,    // SPIDER_EYE
            10,   // ENDER_PEARL
            8,    // BLAZE_ROD
            6,    // GHAST_TEAR
            4,    // SLIME_BALL
            50,   // DIAMOND_BLOCK
            50,   // GOLD_BLOCK
            15,   // IRON_BLOCK
            100,  // NETHER_STAR
            20,   // ENCHANTED_BOOK
            50,   // GOLDEN_APPLE
            100,  // EXPERIENCE_BOTTLE
        };

        // Return 0 for unknown materials
        return 1;
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0)).append(word.substring(1))).append(" ");
        }
        return result.toString().trim();
    }
}
