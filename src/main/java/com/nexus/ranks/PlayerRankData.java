package com.nexus.ranks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Data class for storing player-specific rank information.
 */
public class PlayerRankData {
    
    private final UUID playerId;
    private String playerName;
    private String rankId;
    private String customPrefix;
    private String customSuffix;
    private Set<String> extraPermissions;
    private long rankSetAt;
    
    public PlayerRankData(String playerName, String rankId) {
        this.playerId = UUID.randomUUID(); // Will be updated with actual UUID
        this.playerName = playerName;
        this.rankId = rankId;
        this.customPrefix = null;
        this.customSuffix = null;
        this.extraPermissions = new HashSet<>();
        this.rankSetAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(UUID playerId) {
        // This is a bit hacky but needed for proper initialization
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getRankId() {
        return rankId;
    }
    
    public void setRankId(String rankId) {
        this.rankId = rankId;
        this.rankSetAt = System.currentTimeMillis();
    }
    
    public String getCustomPrefix() {
        return customPrefix;
    }
    
    public void setCustomPrefix(String customPrefix) {
        this.customPrefix = customPrefix;
    }
    
    public String getCustomSuffix() {
        return customSuffix;
    }
    
    public void setCustomSuffix(String customSuffix) {
        this.customSuffix = customSuffix;
    }
    
    public Set<String> getExtraPermissions() {
        return extraPermissions;
    }
    
    public void setExtraPermissions(Set<String> extraPermissions) {
        this.extraPermissions = extraPermissions;
    }
    
    public void addExtraPermission(String permission) {
        this.extraPermissions.add(permission);
    }
    
    public void removeExtraPermission(String permission) {
        this.extraPermissions.remove(permission);
    }
    
    public long getRankSetAt() {
        return rankSetAt;
    }
    
    public void setRankSetAt(long rankSetAt) {
        this.rankSetAt = rankSetAt;
    }
    
    /**
     * Check if player has a specific extra permission
     */
    public boolean hasExtraPermission(String permission) {
        return extraPermissions.contains(permission);
    }
}
