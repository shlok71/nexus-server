package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for inventory click events - prevents moving hub items
 */
public class InventoryClickListener implements Listener {

    private final NexusCore plugin;

    public InventoryClickListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Only apply restrictions in hub
        if (!plugin.getHubManager().isInHub(player)) {
            return;
        }

        // Check for unauthenticated players
        if (plugin.getAuthSystem().isAuthEnabled() && !plugin.getAuthSystem().isAuthenticated(player)) {
            event.setCancelled(true);
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }

        // Check if clicking on player inventory
        if (clickedInventory.equals(player.getInventory())) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) {
                return;
            }

            // Prevent moving hub items from protected slots
            if (isHubItem(clickedItem)) {
                if (shouldProtectSlot(event.getSlot())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot move this item!");
                }
            }
        }
    }

    /**
     * Check if item is a hub item
     */
    private boolean isHubItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        String name = item.getItemMeta().getDisplayName();
        if (name == null) {
            return false;
        }

        return name.equals(ChatColor.AQUA + "Game Selector") ||
               name.equals(ChatColor.LIGHT_PURPLE + "Cosmetics") ||
               name.equals(ChatColor.GREEN + "Profile");
    }

    /**
     * Check if slot should be protected
     */
    private boolean shouldProtectSlot(int slot) {
        // Protect slots 0, 4, 8 (hub item slots)
        return slot == 0 || slot == 4 || slot == 8;
    }
}
