package com.nexus.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Data class for storing player statistics.
 * Tracks various player metrics across the server.
 */
public class PlayerStats {
    
    private final UUID playerId;
    private String playerName;
    
    // General stats
    private long playTime; // in seconds
    private int kills;
    private int deaths;
    private int gamesPlayed;
    private int wins;
    private int losses;
    
    // SkyBlock stats
    private int islandLevel;
    private int islandsCreated;
    private long coinsEarned;
    private long coinsSpent;
    
    // Skill stats
    private double skillAverage;
    
    // Social stats
    private int partiesJoined;
    private int guildsCreated;
    
    // Misc stats
    private int blocksBroken;
    private int blocksPlaced;
    private int itemsCrafted;
    private int mobsKilled;
    private int fishCaught;
    
    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
        this.playerName = "";
        initializeDefaults();
    }
    
    /**
     * Initialize default values
     */
    private void initializeDefaults() {
        this.playTime = 0;
        this.kills = 0;
        this.deaths = 0;
        this.gamesPlayed = 0;
        this.wins = 0;
        this.losses = 0;
        this.islandLevel = 1;
        this.islandsCreated = 0;
        this.coinsEarned = 0;
        this.coinsSpent = 0;
        this.skillAverage = 0;
        this.partiesJoined = 0;
        this.guildsCreated = 0;
        this.blocksBroken = 0;
        this.blocksPlaced = 0;
        this.itemsCrafted = 0;
        this.mobsKilled = 0;
        this.fishCaught = 0;
    }
    
    // Getters and Setters
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public long getPlayTime() {
        return playTime;
    }
    
    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }
    
    public void addPlayTime(long seconds) {
        this.playTime += seconds;
    }
    
    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public void addKill() {
        this.kills++;
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public void addDeath() {
        this.deaths++;
    }
    
    public double getKDR() {
        if (deaths == 0) return kills;
        return (double) kills / deaths;
    }
    
    public int getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
    
    public void addGamePlayed() {
        this.gamesPlayed++;
    }
    
    public int getWins() {
        return wins;
    }
    
    public void setWins(int wins) {
        this.wins = wins;
    }
    
    public void addWin() {
        this.wins++;
    }
    
    public int getLosses() {
        return losses;
    }
    
    public void setLosses(int losses) {
        this.losses = losses;
    }
    
    public void addLoss() {
        this.losses++;
    }
    
    public int getIslandLevel() {
        return islandLevel;
    }
    
    public void setIslandLevel(int islandLevel) {
        this.islandLevel = Math.max(1, islandLevel);
    }
    
    public int getIslandsCreated() {
        return islandsCreated;
    }
    
    public void setIslandsCreated(int islandsCreated) {
        this.islandsCreated = islandsCreated;
    }
    
    public void addIslandCreated() {
        this.islandsCreated++;
    }
    
    public long getCoinsEarned() {
        return coinsEarned;
    }
    
    public void setCoinsEarned(long coinsEarned) {
        this.coinsEarned = coinsEarned;
    }
    
    public void addCoinsEarned(long amount) {
        this.coinsEarned += amount;
    }
    
    public long getCoinsSpent() {
        return coinsSpent;
    }
    
    public void setCoinsSpent(long coinsSpent) {
        this.coinsSpent = coinsSpent;
    }
    
    public void addCoinsSpent(long amount) {
        this.coinsSpent += amount;
    }
    
    public double getSkillAverage() {
        return skillAverage;
    }
    
    public void setSkillAverage(double skillAverage) {
        this.skillAverage = skillAverage;
    }
    
    public int getPartiesJoined() {
        return partiesJoined;
    }
    
    public void setPartiesJoined(int partiesJoined) {
        this.partiesJoined = partiesJoined;
    }
    
    public void addPartyJoined() {
        this.partiesJoined++;
    }
    
    public int getGuildsCreated() {
        return guildsCreated;
    }
    
    public void setGuildsCreated(int guildsCreated) {
        this.guildsCreated = guildsCreated;
    }
    
    public void addGuildCreated() {
        this.guildsCreated++;
    }
    
    public int getBlocksBroken() {
        return blocksBroken;
    }
    
    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }
    
    public void addBlockBroken() {
        this.blocksBroken++;
    }
    
    public int getBlocksPlaced() {
        return blocksPlaced;
    }
    
    public void setBlocksPlaced(int blocksPlaced) {
        this.blocksPlaced = blocksPlaced;
    }
    
    public void addBlockPlaced() {
        this.blocksPlaced++;
    }
    
    public int getItemsCrafted() {
        return itemsCrafted;
    }
    
    public void setItemsCrafted(int itemsCrafted) {
        this.itemsCrafted = itemsCrafted;
    }
    
    public void addItemCrafted() {
        this.itemsCrafted++;
    }
    
    public int getMobsKilled() {
        return mobsKilled;
    }
    
    public void setMobsKilled(int mobsKilled) {
        this.mobsKilled = mobsKilled;
    }
    
    public void addMobKilled() {
        this.mobsKilled++;
    }
    
    public int getFishCaught() {
        return fishCaught;
    }
    
    public void setFishCaught(int fishCaught) {
        this.fishCaught = fishCaught;
    }
    
    public void addFishCaught() {
        this.fishCaught++;
    }
    
    /**
     * Get formatted play time
     */
    public String getFormattedPlayTime() {
        long hours = playTime / 3600;
        long minutes = (playTime % 3600) / 60;
        long seconds = playTime % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Get overall level based on skill average
     */
    public int getOverallLevel() {
        return (int) (skillAverage / 5) + 1;
    }
    
    /**
     * Serialize stats to map for database storage
     */
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("playerName", playerName);
        data.put("playTime", playTime);
        data.put("kills", kills);
        data.put("deaths", deaths);
        data.put("gamesPlayed", gamesPlayed);
        data.put("wins", wins);
        data.put("losses", losses);
        data.put("islandLevel", islandLevel);
        data.put("islandsCreated", islandsCreated);
        data.put("coinsEarned", coinsEarned);
        data.put("coinsSpent", coinsSpent);
        data.put("skillAverage", skillAverage);
        data.put("partiesJoined", partiesJoined);
        data.put("guildsCreated", guildsCreated);
        data.put("blocksBroken", blocksBroken);
        data.put("blocksPlaced", blocksPlaced);
        data.put("itemsCrafted", itemsCrafted);
        data.put("mobsKilled", mobsKilled);
        data.put("fishCaught", fishCaught);
        return data;
    }
    
    /**
     * Deserialize stats from database map
     */
    @SuppressWarnings("unchecked")
    public void deserialize(Map<String, Object> data) {
        if (data.containsKey("playerName")) {
            this.playerName = (String) data.get("playerName");
        }
        if (data.containsKey("playTime")) {
            this.playTime = ((Number) data.get("playTime")).longValue();
        }
        if (data.containsKey("kills")) {
            this.kills = ((Number) data.get("kills")).intValue();
        }
        if (data.containsKey("deaths")) {
            this.deaths = ((Number) data.get("deaths")).intValue();
        }
        if (data.containsKey("gamesPlayed")) {
            this.gamesPlayed = ((Number) data.get("gamesPlayed")).intValue();
        }
        if (data.containsKey("wins")) {
            this.wins = ((Number) data.get("wins")).intValue();
        }
        if (data.containsKey("losses")) {
            this.losses = ((Number) data.get("losses")).intValue();
        }
        if (data.containsKey("islandLevel")) {
            this.islandLevel = ((Number) data.get("islandLevel")).intValue();
        }
        if (data.containsKey("islandsCreated")) {
            this.islandsCreated = ((Number) data.get("islandsCreated")).intValue();
        }
        if (data.containsKey("coinsEarned")) {
            this.coinsEarned = ((Number) data.get("coinsEarned")).longValue();
        }
        if (data.containsKey("coinsSpent")) {
            this.coinsSpent = ((Number) data.get("coinsSpent")).longValue();
        }
        if (data.containsKey("skillAverage")) {
            this.skillAverage = ((Number) data.get("skillAverage")).doubleValue();
        }
        if (data.containsKey("partiesJoined")) {
            this.partiesJoined = ((Number) data.get("partiesJoined")).intValue();
        }
        if (data.containsKey("guildsCreated")) {
            this.guildsCreated = ((Number) data.get("guildsCreated")).intValue();
        }
        if (data.containsKey("blocksBroken")) {
            this.blocksBroken = ((Number) data.get("blocksBroken")).intValue();
        }
        if (data.containsKey("blocksPlaced")) {
            this.blocksPlaced = ((Number) data.get("blocksPlaced")).intValue();
        }
        if (data.containsKey("itemsCrafted")) {
            this.itemsCrafted = ((Number) data.get("itemsCrafted")).intValue();
        }
        if (data.containsKey("mobsKilled")) {
            this.mobsKilled = ((Number) data.get("mobsKilled")).intValue();
        }
        if (data.containsKey("fishCaught")) {
            this.fishCaught = ((Number) data.get("fishCaught")).intValue();
        }
    }
}
