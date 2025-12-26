package com.nexus.stats;

import com.nexus.core.NexusCore;
import com.nexus.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Manager class for handling custom tab list headers and footers.
 * Provides dynamic information display above/below the player list.
 */
public class TabListManager {
    
    private final NexusCore plugin;
    private final Map<UUID, TabListData> playerTabLists;
    
    public TabListManager(NexusCore plugin) {
        this.plugin = plugin;
        this.playerTabLists = new ConcurrentHashMap<>();
    }
    
    /**
     * Update a player's tab list header and footer
     */
    public void updateTabList(Player player) {
        try {
            TabListData data = playerTabLists.computeIfAbsent(player.getUniqueId(), 
                k -> new TabListData());
            
            String header = buildHeader(player);
            String footer = buildFooter(player);
            
            // Use reflection to send tab list packets (works on 1.8-1.21)
            sendTabList(player, header, footer);
            
            data.setLastUpdate(System.currentTimeMillis());
            
        } catch (Exception e) {
            plugin.getNexusLogger().warning("Failed to update tab list for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Build the header text
     */
    private String buildHeader(Player player) {
        StringBuilder header = new StringBuilder();
        
        // Server name with rank
        Rank rank = plugin.getRankManager().getPlayerRank(player.getUniqueId());
        header.append(ChatColor.GOLD).append(ChatColor.BOLD).append("❖ NexusBlock Network ❖\n");
        
        // Player info
        header.append(ChatColor.WHITE).append("Welcome, ")
              .append(rank.getPrefix()).append(ChatColor.WHITE).append(" ")
              .append(player.getName()).append("\n");
        
        // Decorative line
        header.append(ChatColor.GRAY).append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        return header.toString();
    }
    
    /**
     * Build the footer text
     */
    private String buildFooter(Player player) {
        StringBuilder footer = new StringBuilder();
        
        // Server stats
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        
        footer.append(ChatColor.GRAY).append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        // Online players
        footer.append(ChatColor.GREEN).append("Online: ").append(ChatColor.WHITE)
              .append(online).append("/").append(max).append("\n");
        
        // Skills info
        double skillAvg = plugin.getSkillsManager().getPlayerSkills(player.getUniqueId()).getAverageLevel();
        footer.append(ChatColor.AQUA).append("Skill Avg: ").append(ChatColor.WHITE)
              .append(String.format("%.1f", skillAvg)).append("\n");
        
        // Coins
        double coins = plugin.getEconomyManager().getBalance(player.getUniqueId());
        footer.append(ChatColor.GOLD).append("Coins: ").append(ChatColor.WHITE)
              .append(String.format("%.0f", coins)).append("\n");
        
        // Website
        footer.append(ChatColor.YELLOW).append("Website: www.nexusblock.net");
        
        return footer.toString();
    }
    
    /**
     * Send tab list update using ProtocolLib if available
     */
    private void sendTabList(Player player, String header, String footer) {
        // Try using ProtocolLib if available
        try {
            Class<?> packetClass = Class.forName("net.minecraft.server.VERSION.PacketPlayOutPlayerListHeaderFooter");
            Object packet = packetClass.getDeclaredConstructor().newInstance();
            
            // Set header
            setPacketField(packet, "a", createChatComponent(header));
            // Set footer
            setPacketField(packet, "b", createChatComponent(footer));
            
            // Send packet
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);
            
        } catch (ClassNotFoundException e) {
            // ProtocolLib not available, try NMS approach
            sendTabListNMS(player, header, footer);
        } catch (Exception e) {
            // Fallback: Use Bukkit's setPlayerListHeader/Footer (1.8+)
            try {
                player.setPlayerListHeaderFooter(
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', header),
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', footer)
                );
            } catch (Exception ex) {
                // Some versions don't support this method
            }
        }
    }
    
    /**
     * Send tab list using NMS (older approach)
     */
    private void sendTabListNMS(Player player, String header, String footer) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            
            // Create IChatBaseComponent
            Class<?> chatBaseComponentClass = Class.forName("net.minecraft.server." + getServerVersion() + ".IChatBaseComponent");
            Object headerComponent = chatBaseComponentClass.getDeclaredClasses()[0].getMethod("a", String.class)
                .invoke(null, "{\"text\":\"" + escapeJson(header) + "\"}");
            Object footerComponent = chatBaseComponentClass.getDeclaredClasses()[0].getMethod("a", String.class)
                .invoke(null, "{\"text\":\"" + escapeJson(footer) + "\"}");
            
            // Create packet
            Class<?> packetClass = Class.forName("net.minecraft.server." + getServerVersion() + ".PacketPlayOutPlayerListHeaderFooter");
            Object packet = packetClass.getDeclaredConstructor().newInstance();
            
            packetClass.getMethod("a", chatBaseComponentClass).invoke(packet, headerComponent);
            packetClass.getMethod("b", chatBaseComponentClass).invoke(packet, footerComponent);
            
            playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);
            
        } catch (Exception e) {
            // Fallback for unsupported versions
        }
    }
    
    /**
     * Create a Chat Component from text
     */
    private Object createChatComponent(String text) {
        try {
            Class<?> chatBaseComponentClass = Class.forName("net.minecraft.server." + getServerVersion() + ".IChatBaseComponent");
            Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + getServerVersion() + ".IChatBaseComponent$ChatSerializer");
            
            return chatSerializerClass.getMethod("a", String.class).invoke(null, 
                "{\"text\":\"" + escapeJson(text) + "\"}");
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Set a field on a packet object using reflection
     */
    private void setPacketField(Object packet, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = packet.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(packet, value);
        } catch (Exception e) {
            // Field not found or other error
        }
    }
    
    /**
     * Get the server NMS version
     */
    private String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }
    
    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * Update all online players' tab lists
     */
    public void updateAllTabLists() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabList(player);
        }
    }
    
    /**
     * Start automatic tab list update task
     */
    public void startAutoUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllTabLists, 200L, 200L); // Every 10 seconds
    }
    
    /**
     * Clear a player's tab list data
     */
    public void clearTabList(Player player) {
        playerTabLists.remove(player.getUniqueId());
    }
    
    /**
     * Shutdown the tab list manager
     */
    public void shutdown() {
        playerTabLists.clear();
    }
    
    /**
     * Helper class to store tab list update time
     */
    private static class TabListData {
        private long lastUpdate;
        
        public long getLastUpdate() {
            return lastUpdate;
        }
        
        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }
    
}
