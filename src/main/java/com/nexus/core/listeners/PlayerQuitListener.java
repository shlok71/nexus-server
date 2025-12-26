package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player quit events - handles cleanup and logging
 */
public class PlayerQuitListener implements Listener {

    private final NexusCore plugin;

    public PlayerQuitListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Update player count
        plugin.setPlayerCount(Math.max(0, plugin.getPlayerCount() - 1));

        // Hide quit message in hub
        event.setQuitMessage(null);

        // Save player data
        savePlayerData(player);

        // Handle unauthenticated players
        if (plugin.getAuthSystem().isAuthEnabled()) {
            plugin.getAuthSystem().removeUnauthenticatedPlayer(player);
        }

        // Save economy data
        if (plugin.getEconomyManager() != null) {
            plugin.getEconomyManager().savePlayerBalance(player.getUniqueId());
        }

        // Remove from any minigame
        if (plugin.getMinigameManager() != null) {
            plugin.getMinigameManager().removePlayerFromGame(player);
        }
    }

    /**
     * Save player data on quit
     */
    private void savePlayerData(Player player) {
        // Save to database
        if (plugin.getDatabaseManager() != null) {
            plugin.getDatabaseManager().savePlayerData(player);
        }

        // Log quit for moderation
        plugin.getNexusLogger().info(player.getName() + " left the server");
    }
}
