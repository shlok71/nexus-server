package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for item drop events - prevents dropping hub items
 */
public class ItemDropListener implements Listener {

    private final NexusCore plugin;

    public ItemDropListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        // Only apply restrictions in hub
        if (!plugin.getHubManager().isInHub(player)) {
            return;
        }

        // Check for unauthenticated players
        if (plugin.getAuthSystem().isAuthEnabled() && !plugin.getAuthSystem().isAuthenticated(player)) {
            event.setCancelled(true);
            return;
        }

        // Check if item is a hub item
        if (isHubItem(droppedItem)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot drop this item!");
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
}
