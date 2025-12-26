package com.nexus.stats.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener for stats-related events.
 * Handles scoreboard and tab list updates on player join/quit.
 */
public class StatsListener implements Listener {
    
    private final NexusCore plugin;
    
    public StatsListener(NexusCore plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Initialize player stats
        plugin.getStatsManager().initializePlayer(player.getUniqueId(), player.getName());
        
        // Update scoreboard for new player
        plugin.getScoreboardManager().updateScoreboard(player);
        
        // Update tab list for new player
        plugin.getTabListManager().updateTabList(player);
    }
    
    /**
     * Handle player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Save player stats on quit
        plugin.getStatsManager().savePlayerStats(player.getUniqueId());
    }
}
