package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listener for player movement events - handles double jump and movement restrictions
 */
public class PlayerMoveListener implements Listener {

    private final NexusCore plugin;

    // Track double jump usage
    private final java.util.concurrent.ConcurrentHashMap<Player, Integer> jumpCount = new java.util.concurrent.ConcurrentHashMap<>();

    public PlayerMoveListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only apply restrictions in hub world
        if (!plugin.getHubManager().isInHub(player)) {
            return;
        }

        // Check if player is authenticated
        if (plugin.getAuthSystem().isAuthEnabled() && !plugin.getAuthSystem().isAuthenticated(player)) {
            // Stop movement for unauthenticated players
            event.setTo(event.getFrom());
            return;
        }

        // Check for falling into void - respawn at hub
        if (player.getLocation().getY() < 0) {
            plugin.getHubManager().sendToHub(player);
            player.sendMessage(ChatColor.RED + "You fell into the void! Returning to hub...");
        }
    }

    /**
     * Handle double jump - call this when player jumps
     */
    public boolean handleDoubleJump(Player player) {
        if (!plugin.getHubManager().isInHub(player)) {
            return false;
        }

        // Check permissions
        if (!player.hasPermission("nexus.hub.doublejump")) {
            return false;
        }

        // Check jump count
        int jumps = jumpCount.getOrDefault(player, 0);
        int maxJumps = plugin.getConfigManager().getInt("hub.double-jump.max-jumps", 1);

        if (jumps >= maxJumps) {
            // Reset jumps if on ground
            if (player.isOnGround()) {
                jumpCount.put(player, 0);
                return false;
            }
            return false;
        }

        // Apply velocity
        org.bukkit.util.Vector velocity = player.getLocation().getDirection().multiply(0.5);
        velocity.setY(0.8);
        player.setVelocity(velocity);

        // Increment jump count
        jumpCount.put(player, jumps + 1);

        // Play sound
        player.playSound(player.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.0f);

        return true;
    }

    /**
     * Reset jump count when player lands
     */
    public void resetJumpCount(Player player) {
        jumpCount.remove(player);
    }

    /**
     * Check if player is in void and needs respawn
     */
    public boolean checkVoidRespawn(Player player) {
        if (player.getLocation().getY() < -10) {
            plugin.getHubManager().sendToHub(player);
            player.sendMessage(ChatColor.RED + "Returning to hub...");
            return true;
        }
        return false;
    }
}
