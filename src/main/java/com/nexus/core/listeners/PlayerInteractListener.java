package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import com.nexus.core.utils.GUIUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for player interaction events - handles hub item clicks and game selectors
 */
public class PlayerInteractListener implements Listener {

    private final NexusCore plugin;

    public PlayerInteractListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if player is not in hub (skip if in minigame)
        if (!plugin.getHubManager().isInHub(player)) {
            return;
        }

        // Check for null item
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        // Game Selector (Compass) - Slot 0
        if (item.getType() == Material.COMPASS && item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(ChatColor.AQUA + "Game Selector")) {
                event.setCancelled(true);
                openGameSelector(player);
                return;
            }
        }

        // Cosmetics (Chest) - Slot 4
        if (item.getType() == Material.CHEST && item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Cosmetics")) {
                event.setCancelled(true);
                openCosmeticsMenu(player);
                return;
            }
        }

        // Profile (Player Head) - Slot 8
        if (item.getType() == Material.SKULL_ITEM && item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Profile")) {
                event.setCancelled(true);
                openProfileMenu(player);
                return;
            }
        }
    }

    /**
     * Open game selector GUI
     */
    private void openGameSelector(Player player) {
        GUIUtils.openGameSelector(player);
    }

    /**
     * Open cosmetics menu
     */
    private void openCosmeticsMenu(Player player) {
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Cosmetics menu coming soon!");
        // TODO: Implement cosmetics GUI
    }

    /**
     * Open profile menu
     */
    private void openProfileMenu(Player player) {
        GUIUtils.openProfileMenu(player);
    }
}
