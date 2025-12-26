package com.nexus.warp;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for handling player warps.
 * Allows players to set, delete, and teleport to warps.
 */
public class WarpManager {
    
    private final NexusCore plugin;
    private final Map<String, WarpData> warps;
    private final Map<UUID, Long> warpCooldowns;
    
    public WarpManager(NexusCore plugin) {
        this.plugin = plugin;
        this.warps = new ConcurrentHashMap<>();
        this.warpCooldowns = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize warps from configuration
     */
    public void initialize() {
        loadWarps();
        plugin.getNexusLogger().info("WarpManager initialized with " + warps.size() + " warps");
    }
    
    /**
     * Load warps from database/config
     */
    private void loadWarps() {
        Map<String, Location> loadedWarps = plugin.getConfigManager().loadWarps();
        for (Map.Entry<String, Location> entry : loadedWarps.entrySet()) {
            warps.put(entry.getKey().toLowerCase(), new WarpData(entry.getKey(), entry.getValue()));
        }
        
        // Load from database
        Map<String, Location> dbWarps = plugin.getDatabaseManager().loadWarps();
        for (Map.Entry<String, Location> entry : dbWarps.entrySet()) {
            String name = entry.getKey().toLowerCase();
            if (!warps.containsKey(name)) {
                warps.put(name, new WarpData(entry.getKey(), entry.getValue()));
            }
        }
    }
    
    /**
     * Create a new warp
     */
    public boolean createWarp(String name, Location location, UUID creatorId) {
        String lowerName = name.toLowerCase();
        
        if (warps.containsKey(lowerName)) {
            return false;
        }
        
        WarpData warp = new WarpData(name, location, creatorId);
        warp.setCreatedAt(System.currentTimeMillis());
        warps.put(lowerName, warp);
        
        saveWarp(warp);
        return true;
    }
    
    /**
     * Delete a warp
     */
    public boolean deleteWarp(String name) {
        String lowerName = name.toLowerCase();
        
        if (!warps.containsKey(lowerName)) {
            return false;
        }
        
        warps.remove(lowerName);
        deleteWarpFromDb(name);
        return true;
    }
    
    /**
     * Teleport a player to a warp
     */
    public boolean teleportToWarp(Player player, String name) {
        String lowerName = name.toLowerCase();
        
        WarpData warp = warps.get(lowerName);
        if (warp == null) {
            return false;
        }
        
        // Check cooldown
        Long lastWarp = warpCooldowns.get(player.getUniqueId());
        if (lastWarp != null && System.currentTimeMillis() - lastWarp < 1000) {
            player.sendMessage(plugin.getConfigManager().getMessage("warp.cooldown")
                .replace("%time%", "1 second"));
            return false;
        }
        
        Location location = warp.getLocation();
        
        // Check if world exists
        World world = location.getWorld();
        if (world == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("warp.notFound")
                .replace("%warp%", name));
            return false;
        }
        
        // Teleport player
        player.teleport(location);
        
        // Update cooldown
        warpCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        
        player.sendMessage(plugin.getConfigManager().getMessage("warp.success")
            .replace("%warp%", warp.getName()));
        
        return true;
    }
    
    /**
     * Get warp information
     */
    public WarpData getWarp(String name) {
        return warps.get(name.toLowerCase());
    }
    
    /**
     * Check if a warp exists
     */
    public boolean hasWarp(String name) {
        return warps.containsKey(name.toLowerCase());
    }
    
    /**
     * Get all warps
     */
    public Set<String> getAllWarps() {
        return warps.keySet();
    }
    
    /**
     * Get public warps (for /warp command completion)
     */
    public Set<String> getPublicWarps() {
        Set<String> publicWarps = new java.util.HashSet<>();
        for (WarpData warp : warps.values()) {
            if (warp.isPublic()) {
                publicWarps.add(warp.getName());
            }
        }
        return publicWarps;
    }
    
    /**
     * Get warp count
     */
    public int getWarpCount() {
        return warps.size();
    }
    
    /**
     * Save warp to database
     */
    private void saveWarp(WarpData warp) {
        plugin.getDatabaseManager().saveWarp(warp.getName(), warp.getLocation());
    }
    
    /**
     * Delete warp from database
     */
    private void deleteWarpFromDb(String name) {
        plugin.getDatabaseManager().deleteWarp(name);
    }
    
    /**
     * Save all warps
     */
    public void saveAllWarps() {
        for (WarpData warp : warps.values()) {
            saveWarp(warp);
        }
    }
    
    /**
     * Shutdown the warp manager
     */
    public void shutdown() {
        saveAllWarps();
        warps.clear();
        warpCooldowns.clear();
    }
    
    /**
     * Data class for warp information
     */
    public static class WarpData {
        private final String name;
        private final Location location;
        private UUID ownerId;
        private boolean isPublic;
        private String description;
        private long createdAt;
        private int uses;
        
        public WarpData(String name, Location location) {
            this(name, location, null);
        }
        
        public WarpData(String name, Location location, UUID ownerId) {
            this.name = name;
            this.location = location;
            this.ownerId = ownerId;
            this.isPublic = true;
            this.description = "";
            this.createdAt = System.currentTimeMillis();
            this.uses = 0;
        }
        
        public String getName() {
            return name;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public UUID getOwnerId() {
            return ownerId;
        }
        
        public void setOwnerId(UUID ownerId) {
            this.ownerId = ownerId;
        }
        
        public boolean isPublic() {
            return isPublic;
        }
        
        public void setPublic(boolean isPublic) {
            this.isPublic = isPublic;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public long getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }
        
        public int getUses() {
            return uses;
        }
        
        public void setUses(int uses) {
            this.uses = uses;
        }
        
        public void incrementUses() {
            this.uses++;
        }
        
        public String getOwnerName() {
            if (ownerId == null) {
                return "Server";
            }
            Player owner = Bukkit.getPlayer(ownerId);
            if (owner != null && owner.isOnline()) {
                return owner.getName();
            }
            return "Unknown";
        }
    }
}
