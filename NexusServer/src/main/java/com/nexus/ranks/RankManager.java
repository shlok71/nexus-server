package com.nexus.ranks;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for handling player ranks.
 * Tracks player ranks, prefixes, and permissions.
 */
public class RankManager {
    
    private final NexusCore plugin;
    private final Map<String, Rank> ranks;
    private final Map<UUID, PlayerRankData> playerRanks;
    private final Map<String, Rank> rankByName;
    
    public RankManager(NexusCore plugin) {
        this.plugin = plugin;
        this.ranks = new HashMap<>();
        this.playerRanks = new ConcurrentHashMap<>();
        this.rankByName = new HashMap<>();
    }
    
    /**
     * Initialize ranks from configuration
     */
    public void initialize() {
        loadRanks();
        loadSpecialUsers();
        plugin.getNexusLogger().info("RankManager initialized with " + ranks.size() + " ranks");
    }
    
    /**
     * Load all ranks from configuration
     */
    @SuppressWarnings("unchecked")
    private void loadRanks() {
        org.bukkit.configuration.Configuration config = plugin.getConfigManager().getRanksConfig();
        if (config == null) {
            createDefaultRanks();
            return;
        }
        
        // Load default rank
        String defaultRankName = config.getString("default-rank", "DEFAULT");
        
        // Load rank definitions
        if (config.contains("ranks")) {
            org.bukkit.configuration.ConfigurationSection ranksSection = config.getConfigurationSection("ranks");
            if (ranksSection != null) {
                for (String rankId : ranksSection.getKeys(false)) {
                    org.bukkit.configuration.ConfigurationSection rankConfig = ranksSection.getConfigurationSection(rankId);
                    if (rankConfig != null) {
                        Rank rank = new Rank(rankId);
                        rank.setName(rankConfig.getString("name", rankId));
                        rank.setPrefix(ChatColor.translateAlternateColorCodes('&', rankConfig.getString("prefix", "")));
                        rank.setSuffix(ChatColor.translateAlternateColorCodes('&', rankConfig.getString("suffix", "")));
                        rank.setColor(rankConfig.getString("color", "WHITE"));
                        rank.setPriority(rankConfig.getInt("priority", 0));
                        rank.setDefault(rankConfig.getBoolean("default", false));
                        rank.setStaff(rankConfig.getBoolean("staff", false));
                        rank.setOwner(rankConfig.getBoolean("owner", false));
                        
                        // Load permissions
                        List<String> perms = rankConfig.getStringList("permissions");
                        rank.setPermissions(new HashSet<>(perms));
                        
                        // Load format settings
                        rank.setChatFormat(rankConfig.getString("chat-format", "%prefix% %player%: %message%"));
                        rank.setTabFormat(rankConfig.getString("tab-list-format", "%player%"));
                        
                        ranks.put(rankId.toUpperCase(), rank);
                        rankByName.put(rank.getName().toUpperCase(), rank);
                    }
                }
            }
        }
        
        // Set default rank if not loaded
        if (!ranks.containsKey(defaultRankName.toUpperCase())) {
            Rank defaultRank = ranks.get("DEFAULT");
            if (defaultRank == null) {
                createDefaultRanks();
            }
        }
    }
    
    /**
     * Create default ranks if config is missing
     */
    private void createDefaultRanks() {
        ranks.clear();
        rankByName.clear();
        
        // DEFAULT
        Rank defaultRank = new Rank("DEFAULT");
        defaultRank.setName("Member");
        defaultRank.setPrefix(ChatColor.GRAY + "[Member]");
        defaultRank.setColor("GRAY");
        defaultRank.setPriority(0);
        defaultRank.setDefault(true);
        defaultRank.setPermissions(new HashSet<>(Arrays.asList("nexus.basic", "nexus.skyblock.use")));
        defaultRank.setChatFormat("&7[Member] %player%: %message%");
        ranks.put("DEFAULT", defaultRank);
        
        // VIP
        Rank vipRank = new Rank("VIP");
        vipRank.setName("VIP");
        vipRank.setPrefix(ChatColor.YELLOW + "[VIP]");
        vipRank.setColor("YELLOW");
        vipRank.setPriority(10);
        vipRank.setPermissions(new HashSet<>(Arrays.asList("nexus.basic", "nexus.skyblock.use", "nexus.vip")));
        vipRank.setChatFormat("&e[VIP] %player%: %message%");
        ranks.put("VIP", vipRank);
        
        // MVP
        Rank mvpRank = new Rank("MVP");
        mvpRank.setName("MVP");
        mvpRank.setPrefix(ChatColor.AQUA + "[MVP]");
        mvpRank.setColor("AQUA");
        mvpRank.setPriority(30);
        mvpRank.setPermissions(new HashSet<>(Arrays.asList("nexus.basic", "nexus.skyblock.use", "nexus.vip", "nexus.mvp")));
        mvpRank.setChatFormat("&b[MVP] %player%: %message%");
        ranks.put("MVP", mvpRank);
        
        // ADMIN
        Rank adminRank = new Rank("ADMIN");
        adminRank.setName("Admin");
        adminRank.setPrefix(ChatColor.RED + "[Admin]");
        adminRank.setColor("RED");
        adminRank.setPriority(200);
        adminRank.setStaff(true);
        adminRank.setPermissions(new HashSet<>(Collections.singletonList("*")));
        adminRank.setChatFormat("&c[Admin] %player%: %message%");
        ranks.put("ADMIN", adminRank);
        
        // OWNER
        Rank ownerRank = new Rank("OWNER");
        ownerRank.setName("Owner");
        ownerRank.setPrefix(ChatColor.DARK_RED + "[Owner]");
        ownerRank.setColor("DARK_RED");
        ownerRank.setPriority(1000);
        ownerRank.setStaff(true);
        ownerRank.setOwner(true);
        ownerRank.setPermissions(new HashSet<>(Collections.singletonList("*")));
        ownerRank.setChatFormat("&4[Owner] %player%: %message%");
        ranks.put("OWNER", ownerRank);
        
        // Map by name
        for (Rank rank : ranks.values()) {
            rankByName.put(rank.getName().toUpperCase(), rank);
        }
    }
    
    /**
     * Load special users with custom ranks
     */
    @SuppressWarnings("unchecked")
    private void loadSpecialUsers() {
        org.bukkit.configuration.Configuration config = plugin.getConfigManager().getRanksConfig();
        if (config == null || !config.contains("special-users")) {
            return;
        }
        
        org.bukkit.configuration.ConfigurationSection specialSection = config.getConfigurationSection("special-users");
        if (specialSection != null) {
            for (String username : specialSection.getKeys(false)) {
                org.bukkit.configuration.ConfigurationSection userConfig = specialSection.getConfigurationSection(username);
                if (userConfig != null) {
                    String rankName = userConfig.getString("rank", "DEFAULT");
                    String customPrefix = ChatColor.translateAlternateColorCodes('&', 
                        userConfig.getString("prefix", ""));
                    List<String> extraPerms = userConfig.getStringList("extra-permissions");
                    
                    PlayerRankData data = new PlayerRankData(username, rankName);
                    data.setCustomPrefix(customPrefix);
                    data.setExtraPermissions(new HashSet<>(extraPerms));
                    
                    // Try to find player online
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().equalsIgnoreCase(username)) {
                            setPlayerRank(player.getUniqueId(), rankName);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Set a player's rank
     */
    public void setPlayerRank(UUID playerId, String rankId) {
        Rank rank = getRank(rankId);
        if (rank == null) {
            rank = ranks.get("DEFAULT");
        }
        
        PlayerRankData data = playerRanks.computeIfAbsent(playerId, k -> new PlayerRankData(null, rankId));
        data.setRankId(rankId);
        
        // Update permissions
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            updatePlayerPermissions(player);
        }
        
        // Save to database
        plugin.getDatabaseManager().savePlayerRank(playerId, rankId);
    }
    
    /**
     * Get a player's rank
     */
    public Rank getPlayerRank(UUID playerId) {
        PlayerRankData data = playerRanks.get(playerId);
        if (data != null) {
            Rank rank = ranks.get(data.getRankId().toUpperCase());
            if (rank != null) {
                return rank;
            }
        }
        
        // Try loading from database
        String rankId = plugin.getDatabaseManager().getPlayerRank(playerId);
        if (rankId != null) {
            Rank rank = ranks.get(rankId.toUpperCase());
            if (rank != null) {
                playerRanks.put(playerId, new PlayerRankData(null, rankId));
                return rank;
            }
        }
        
        return ranks.get("DEFAULT");
    }
    
    /**
     * Get rank by ID or name
     */
    public Rank getRank(String identifier) {
        // Try by ID first
        Rank rank = ranks.get(identifier.toUpperCase());
        if (rank != null) {
            return rank;
        }
        
        // Try by name
        return rankByName.get(identifier.toUpperCase());
    }
    
    /**
     * Update player permissions based on rank
     */
    public void updatePlayerPermissions(Player player) {
        Rank rank = getPlayerRank(player.getUniqueId());
        if (rank == null) {
            return;
        }
        
        // Clear old permissions
        player.recalculatePermissions();
        
        // Add rank permissions
        Set<String> permissions = rank.getPermissions();
        for (String perm : permissions) {
            player.addAttachment(plugin).setPermission(perm, true);
        }
        
        // Add extra permissions from player data
        PlayerRankData data = playerRanks.get(player.getUniqueId());
        if (data != null && data.getExtraPermissions() != null) {
            for (String perm : data.getExtraPermissions()) {
                player.addAttachment(plugin).setPermission(perm, true);
            }
        }
    }
    
    /**
     * Get player prefix including custom prefix
     */
    public String getPlayerPrefix(UUID playerId) {
        PlayerRankData data = playerRanks.get(playerId);
        if (data != null && data.getCustomPrefix() != null && !data.getCustomPrefix().isEmpty()) {
            return data.getCustomPrefix();
        }
        
        Rank rank = getPlayerRank(playerId);
        return rank != null ? rank.getPrefix() : "";
    }
    
    /**
     * Get player suffix
     */
    public String getPlayerSuffix(UUID playerId) {
        Rank rank = getPlayerRank(playerId);
        return rank != null ? rank.getSuffix() : "";
    }
    
    /**
     * Get player color
     */
    public ChatColor getPlayerColor(UUID playerId) {
        Rank rank = getPlayerRank(playerId);
        if (rank == null) {
            return ChatColor.WHITE;
        }
        
        try {
            return ChatColor.valueOf(rank.getColor().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChatColor.WHITE;
        }
    }
    
    /**
     * Get chat format for a player
     */
    public String getChatFormat(UUID playerId, String message) {
        Rank rank = getPlayerRank(playerId);
        if (rank == null) {
            rank = ranks.get("DEFAULT");
        }
        
        String format = rank.getChatFormat();
        Player player = Bukkit.getPlayer(playerId);
        String playerName = player != null ? player.getName() : "Unknown";
        
        format = format.replace("%prefix%", getPlayerPrefix(playerId));
        format = format.replace("%suffix%", getPlayerSuffix(playerId));
        format = format.replace("%player%", getPlayerColor(playerId) + playerName);
        format = format.replace("%message%", message);
        
        return format;
    }
    
    /**
     * Get tab list format for a player
     */
    public String getTabFormat(UUID playerId) {
        Rank rank = getPlayerRank(playerId);
        if (rank == null) {
            rank = ranks.get("DEFAULT");
        }
        
        String format = rank.getTabFormat();
        Player player = Bukkit.getPlayer(playerId);
        String playerName = player != null ? player.getName() : "Unknown";
        
        format = format.replace("%prefix%", getPlayerPrefix(playerId));
        format = format.replace("%player%", getPlayerColor(playerId) + playerName);
        
        return format;
    }
    
    /**
     * Check if player has a specific rank or higher
     */
    public boolean hasRank(UUID playerId, String rankId) {
        Rank playerRank = getPlayerRank(playerId);
        Rank targetRank = getRank(rankId);
        
        if (playerRank == null || targetRank == null) {
            return false;
        }
        
        return playerRank.getPriority() >= targetRank.getPriority();
    }
    
    /**
     * Check if player is staff
     */
    public boolean isStaff(UUID playerId) {
        Rank rank = getPlayerRank(playerId);
        return rank != null && rank.isStaff();
    }
    
    /**
     * Check if player is owner
     */
    public boolean isOwner(UUID playerId) {
        Rank rank = getPlayerRank(playerId);
        return rank != null && rank.isOwner();
    }
    
    /**
     * Get all available ranks
     */
    public Collection<Rank> getAllRanks() {
        return ranks.values();
    }
    
    /**
     * Shutdown the rank manager
     */
    public void shutdown() {
        // Save all player ranks
        for (UUID playerId : playerRanks.keySet()) {
            PlayerRankData data = playerRanks.get(playerId);
            if (data != null) {
                plugin.getDatabaseManager().savePlayerRank(playerId, data.getRankId());
            }
        }
        playerRanks.clear();
    }
}
