package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Listener for entity damage events - prevents damage in hub areas
 */
public class EntityDamageListener implements Listener {

    private final NexusCore plugin;

    public EntityDamageListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        // Handle player damage
        if (entity instanceof Player) {
            Player player = (Player) entity;

            // Only apply damage protection in hub
            if (plugin.getHubManager().isInHub(player)) {
                if (shouldCancelDamage(event)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Check if damage should be cancelled in hub
     */
    private boolean shouldCancelDamage(EntityDamageEvent event) {
        switch (event.getCause()) {
            case FALL:
                // No fall damage in hub
                return true;
            case VOID:
                // Void damage handled by movement listener
                return false;
            case LAVA:
                // Lava damage
                return plugin.getConfigManager().getBoolean("hub.protection.no-lava-damage", true);
            case FIRE:
            case FIRE_TICK:
                // Fire damage
                return plugin.getConfigManager().getBoolean("hub.protection.no-fire-damage", true);
            case DROWNING:
                // Drowning
                return plugin.getConfigManager().getBoolean("hub.protection.no-drowning", true);
            case STARVATION:
                // Starvation
                return plugin.getConfigManager().getBoolean("hub.protection.no-starvation", true);
            case PROJECTILE:
                // PvP or arrow damage
                return plugin.getConfigManager().getBoolean("hub.protection.no-pvp", true);
            case ENTITY_ATTACK:
                // Mob or player attack
                return plugin.getConfigManager().getBoolean("hub.protection.no-pvp", true);
            case CUSTOM:
            case MAGIC:
            case LIGHTNING:
                // Other damage types
                return true;
            default:
                // Default to cancel most damage
                return true;
        }
    }
}
