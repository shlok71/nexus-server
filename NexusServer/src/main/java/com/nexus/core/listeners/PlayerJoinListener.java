package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listener for player join events - handles hub setup and player initialization
 */
public class PlayerJoinListener implements Listener {

    private final NexusCore plugin;

    public PlayerJoinListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Update player count
        plugin.setPlayerCount(plugin.getPlayerCount() + 1);

        // Hide join message in hub
        event.setJoinMessage(null);

        // Check if player is authenticated (for cracked servers)
        if (plugin.getAuthSystem().isAuthEnabled()) {
            if (!plugin.getAuthSystem().isAuthenticated(player)) {
                // Block movement and chat until authenticated
                plugin.getAuthSystem().queueUnauthenticatedPlayer(player);
                player.sendMessage(ChatColor.RED + "=================================");
                player.sendMessage(ChatColor.GOLD + "Welcome to " + ChatColor.AQUA + "NexusBlock Network" + ChatColor.GOLD + "!");
                player.sendMessage(ChatColor.RED + "=================================");
                player.sendMessage(ChatColor.YELLOW + "Please " + ChatColor.AQUA + "/register <password>" + ChatColor.YELLOW + " to create an account.");
                player.sendMessage(ChatColor.GRAY + "For help, ask our staff team!");
                return;
            }
        }

        // Initialize player in hub
        initializePlayerInHub(player);
    }

    /**
     * Initialize a player in the hub world
     */
    public void initializePlayerInHub(Player player) {
        // Teleport to hub spawn
        plugin.getHubManager().sendToHub(player);

        // Clear inventory and give hub items
        clearPlayerInventory(player);
        giveHubItems(player);

        // Set player settings
        player.setAllowFlight(false);
        player.setFlying(false);

        // Send welcome message
        sendWelcomeMessage(player);
    }

    /**
     * Clear player's inventory
     */
    private void clearPlayerInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
    }

    /**
     * Give hub items to player
     */
    private void giveHubItems(Player player) {
        // Game Selector (Compass)
        ItemStack selector = new ItemStack(Material.COMPASS, 1);
        ItemMeta selectorMeta = selector.getItemMeta();
        selectorMeta.setDisplayName(ChatColor.AQUA + "Game Selector");
        selectorMeta.setLore(java.util.Arrays.asList(
            ChatColor.GRAY + "Right-click to select a game",
            ChatColor.GRAY + "Current Hub: " + ChatColor.GREEN + "1"
        ));
        selector.setItemMeta(selectorMeta);
        player.getInventory().setItem(0, selector);

        // Cosmetics (Chest)
        ItemStack cosmetics = new ItemStack(Material.CHEST, 1);
        ItemMeta cosmeticsMeta = cosmetics.getItemMeta();
        cosmeticsMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Cosmetics");
        cosmeticsMeta.setLore(java.util.Arrays.asList(
            ChatColor.GRAY + "Click to open cosmetics menu",
            ChatColor.GRAY + "Particles, gadgets, and more!"
        ));
        cosmetics.setItemMeta(cosmeticsMeta);
        player.getInventory().setItem(4, cosmetics);

        // Profile (Player Head)
        ItemStack profile = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta profileMeta = profile.getItemMeta();
        profileMeta.setDisplayName(ChatColor.GREEN + "Profile");
        profileMeta.setLore(java.util.Arrays.asList(
            ChatColor.GRAY + "View your stats and settings",
            ChatColor.GRAY + "Click to open profile menu"
        ));
        profile.setItemMeta(profileMeta);
        player.getInventory().setItem(8, profile);

        player.updateInventory();
    }

    /**
     * Send welcome message to player
     */
    private void sendWelcomeMessage(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "Welcome to " + ChatColor.AQUA + "NexusBlock Network" + ChatColor.GOLD + "!");
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.WHITE + "/hub" + ChatColor.GRAY + " to return here anytime.");
        player.sendMessage(ChatColor.GRAY + "Click the " + ChatColor.AQUA + "compass" + ChatColor.GRAY + " to select a game!");
        player.sendMessage("");
    }
}
