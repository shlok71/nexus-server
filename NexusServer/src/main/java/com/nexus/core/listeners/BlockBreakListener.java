package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Listener for block break events - prevents breaking blocks outside designated areas
 */
public class BlockBreakListener implements Listener {

    private final NexusCore plugin;

    public BlockBreakListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Check if player is authenticated
        if (plugin.getAuthSystem().isAuthEnabled() && !plugin.getAuthSystem().isAuthenticated(player)) {
            player.sendMessage(ChatColor.RED + "You must be authenticated to break blocks!");
            event.setCancelled(true);
            return;
        }

        // Check if player is in hub
        if (plugin.getHubManager().isInHub(player)) {
            // Check if player can break blocks in hub
            if (!player.hasPermission("nexus.hub.break-blocks")) {
                player.sendMessage(ChatColor.RED + "You cannot break blocks in the hub!");
                event.setCancelled(true);
                return;
            }

            // Check if block is in allowed area
            if (!isBlockAllowed(event.getBlock().getLocation())) {
                player.sendMessage(ChatColor.RED + "You cannot break blocks here!");
                event.setCancelled(true);
                return;
            }
        }

        // Check if player is in SkyBlock
        if (plugin.getSkyBlockManager().isInSkyBlockWorld(player)) {
            // SkyBlock handles its own block break logic
            plugin.getSkyBlockManager().handleBlockBreak(player, event.getBlock());
        }
    }

    /**
     * Check if block location is in allowed area
     */
    private boolean isBlockAllowed(org.bukkit.Location location) {
        // Get allowed region from config
        // For now, allow blocks in a specific area around spawn
        org.bukkit.Location spawn = plugin.getHubManager().getSpawnLocation();
        if (spawn == null) {
            return false;
        }

        double radius = plugin.getConfigManager().getDouble("hub.build-radius", 50.0);
        double distance = location.distance(spawn);

        return distance <= radius;
    }
}
