package com.nexus.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * GUI utilities for creating and managing player interfaces
 */
public class GUIUtils {

    /**
     * Create a new inventory with specified size and title
     */
    public static Inventory createInventory(Player player, String title, int size) {
        // Ensure size is multiple of 9
        if (size % 9 != 0) {
            size = ((size / 9) + 1) * 9;
        }
        size = Math.min(size, 54); // Max size

        String parsedTitle = ChatColor.translateAlternateColorCodes('&', title);
        return Bukkit.createInventory(player, size, parsedTitle);
    }

    /**
     * Open game selector menu
     */
    public static void openGameSelector(Player player) {
        Inventory gui = createInventory(player, "Select a Game", 27);

        // SkyBlock
        ItemStack skyblock = createItem(
            Material.GRASS,
            ChatColor.GREEN + "SkyBlock",
            Arrays.asList(
                ChatColor.GRAY + "Create your own island",
                ChatColor.GRAY + "farm resources, and level up!",
                "",
                ChatColor.YELLOW + "Click to join!"
            )
        );
        gui.setItem(10, skyblock);

        // BedWars
        ItemStack bedwars = createItem(
            Material.BED,
            ChatColor.RED + "BedWars",
            Arrays.asList(
                ChatColor.GRAY + "Protect your bed and",
                ChatColor.GRAY + "destroy enemy beds!",
                "",
                ChatColor.YELLOW + "Click to join!"
            )
        );
        gui.setItem(12, bedwars);

        // Duels
        ItemStack duels = createItem(
            Material.DIAMOND_SWORD,
            ChatColor.AQUA + "Duels",
            Arrays.asList(
                ChatColor.GRAY + "Challenge other players",
                ChatColor.GRAY + "to 1v1 combat!",
                "",
                ChatColor.YELLOW + "Click to join!"
            )
        );
        gui.setItem(14, duels);

        // Parkour
        ItemStack parkour = createItem(
            Material.STONE_BUTTON,
            ChatColor.GOLD + "Parkour",
            Arrays.asList(
                ChatColor.GRAY + "Test your jumping skills",
                ChatColor.GRAY + "through challenging courses!",
                "",
                ChatColor.YELLOW + "Click to join!"
            )
        );
        gui.setItem(16, duels);

        player.openInventory(gui);
    }

    /**
     * Open profile menu
     */
    public static void openProfileMenu(Player player) {
        Inventory gui = createInventory(player, "Your Profile", 27);

        // Player head
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwner(player.getName());
        headMeta.setDisplayName(ChatColor.GREEN + player.getName());
        headMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Your player profile",
            "",
            ChatColor.YELLOW + "Rank: " + ChatColor.WHITE + getPlayerRank(player)
        ));
        head.setItemMeta(headMeta);
        gui.setItem(13, head);

        player.openInventory(gui);
    }

    /**
     * Create a menu item with specified properties
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a menu item with specified properties
     */
    public static ItemStack createItem(Material material, String name, Iterable<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            java.util.ArrayList<String> loreList = new java.util.ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a glass pane item for filling empty slots
     */
    public static ItemStack createGlassPane(ChatColor color) {
        Material glassMaterial;
        try {
            glassMaterial = Material.valueOf(color.name() + "_STAINED_GLASS_PANE");
        } catch (IllegalArgumentException e) {
            glassMaterial = Material.THIN_GLASS;
        }

        ItemStack pane = new ItemStack(glassMaterial, 1);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        return pane;
    }

    /**
     * Fill empty slots with glass panes
     */
    public static void fillEmptySlots(Inventory inventory, ChatColor color) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createGlassPane(color));
            }
        }
    }

    /**
     * Get player's rank name
     */
    private static String getPlayerRank(Player player) {
        if (player.isOp()) {
            return ChatColor.RED + "Admin";
        } else if (player.hasPermission("nexus.mod")) {
            return ChatColor.DARK_PURPLE + "Mod";
        } else if (player.hasPermission("nexus.vip")) {
            return ChatColor.GOLD + "VIP";
        } else if (player.hasPermission("nexus.premium")) {
            return ChatColor.AQUA + "Premium";
        }
        return ChatColor.WHITE + "Default";
    }
}
