package com.nexus.guilds.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener for guild-related events.
 * Handles guild chat and player join notifications.
 */
public class GuildListener implements Listener {
    
    private final NexusCore plugin;
    
    public GuildListener(NexusCore plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle player chat for guild chat
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if player wants to use guild chat
        String message = event.getMessage();
        if (message.startsWith("#") && message.length() > 1) {
            event.setCancelled(true);
            
            // Send to guild chat (without the #)
            plugin.getGuildManager().sendGuildChat(
                player.getUniqueId(), 
                message.substring(1).trim()
            );
            return;
        }
        
        if (message.startsWith("##") && message.length() > 2) {
            event.setCancelled(true);
            
            // Send to officer chat (without the ##)
            plugin.getGuildManager().sendOfficerChat(
                player.getUniqueId(),
                message.substring(2).trim()
            );
            return;
        }
    }
    
    /**
     * Handle player join for guild welcome
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has a guild
        var guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        if (guild != null) {
            // Notify other guild members
            String welcomeMessage = plugin.getConfigManager().getMessage("guild.memberJoined")
                .replace("%player%", player.getName());
            
            for (var memberId : guild.getMemberIds()) {
                if (memberId.equals(player.getUniqueId())) continue;
                
                Player member = plugin.getServer().getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage(welcomeMessage);
                }
            }
        }
    }
}
