package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Listener for block place events - prevents placing blocks outside designated areas
 */
public class BlockPlaceListener implements Listener {

    private final NexusCore plugin;

    public BlockPlaceListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location blockLocation = event.getBlockPlaced().getLocation();

        // Check if player is authenticated
        if (plugin.getAuthSystem().isAuthEnabled() && !plugin.getAuthSystem().isAuthenticated(player)) {
            player.sendMessage(ChatColor.RED + "You must be authenticated to place blocks!");
            event.setCancelled(true);
            return;
        }

        // Check if player is in hub
        if (plugin.getHubManager().isInHub(player)) {
            // Check if player can place blocks in hub
            if (!player.hasPermission("nexus.hub.place-blocks")) {
                player.sendMessage(ChatColor.RED + "You cannot place blocks in the hub!");
                event.setCancelled(true);
                return;
            }

            // Check if block location is allowed
            if (!isLocationAllowed(blockLocation)) {
                player.sendMessage(ChatColor.RED + "You cannot place blocks here!");
                event.setCancelled(true);
                return;
            }
        }

        // Check if player is in SkyBlock
        if (plugin.getSkyBlockManager().isInSkyBlockWorld(player)) {
            // SkyBlock handles its own block place logic
            plugin.getSkyBlockManager().handleBlockPlace(player, event.getBlockPlaced());
        }
    }

    /**
     * Check if location is allowed for block placement
     */
    private boolean isLocationAllowed(Location location) {
        // Get allowed region from config
        Location spawn = plugin.getHubManager().getSpawnLocation();
        if (spawn == null) {
            return false;
        }

        double radius = plugin.getConfigManager().getDouble("hub.build-radius", 50.0);
        double distance = location.distance(spawn);

        return distance <= radius;
    }
}
