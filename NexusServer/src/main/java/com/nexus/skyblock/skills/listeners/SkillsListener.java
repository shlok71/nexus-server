package com.nexus.skyblock.skills.listeners;

import com.nexus.core.NexusCore;
import com.nexus.skyblock.skills.SkillsManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

/**
 * Listener for skill-related events.
 * Handles XP gain from various in-game activities.
 */
public class SkillsListener implements Listener {
    
    private final NexusCore plugin;
    private final SkillsManager skillsManager;
    
    public SkillsListener(NexusCore plugin) {
        this.plugin = plugin;
        this.skillsManager = plugin.getSkillsManager();
    }
    
    /**
     * Handle block break for mining and foraging XP
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has skills initialized
        if (!hasSkillsInitialized(player)) {
            return;
        }
        
        // Don't give XP if the block was placed by a player (not natural)
        Block block = event.getBlock();
        if (block.hasMetadata("placedByPlayer")) {
            return;
        }
        
        skillsManager.handleBlockBreak(player.getUniqueId(), event);
    }
    
    /**
     * Handle block place - mark as player placed
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Mark the block as player-placed
        block.setMetadata("placedByPlayer", new org.bukkit.metadata.FixedMetadataValue(plugin, player.getUniqueId().toString()));
    }
    
    /**
     * Handle harvesting crops for farming XP
     */
    @EventHandler
    public void onHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        
        if (!hasSkillsInitialized(player)) {
            return;
        }
        
        Material harvestedCrop = event.getHarvestedMaterial();
        skillsManager.handleHarvest(player.getUniqueId(), harvestedCrop);
    }
    
    /**
     * Handle mob kills for combat XP
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }
        
        Player player = event.getEntity().getKiller();
        
        if (!hasSkillsInitialized(player)) {
            return;
        }
        
        skillsManager.handleMobKill(player.getUniqueId(), event);
    }
    
    /**
     * Handle fishing for fishing XP
     */
    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        
        if (!hasSkillsInitialized(player)) {
            return;
        }
        
        skillsManager.handleFishing(player.getUniqueId(), event);
    }
    
    /**
     * Handle enchanting for enchanting XP
     */
    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        
        if (!hasSkillsInitialized(player)) {
            return;
        }
        
        skillsManager.handleEnchant(player.getUniqueId(), event);
    }
    
    /**
     * Check if player has skills initialized
     */
    private boolean hasSkillsInitialized(Player player) {
        return player != null && 
               player.isOnline() && 
               plugin.getSkillsManager().getPlayerSkills(player.getUniqueId()) != null;
    }
}
