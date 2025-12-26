package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 * Listener for food level change events - prevents hunger loss in hub
 */
public class FoodLevelChangeListener implements Listener {

    private final NexusCore plugin;

    public FoodLevelChangeListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Only apply hunger protection in hub
        if (plugin.getHubManager().isInHub(player)) {
            if (plugin.getConfigManager().getBoolean("hub.protection.no-hunger-loss", true)) {
                event.setCancelled(true);
            }
        }
    }
}
