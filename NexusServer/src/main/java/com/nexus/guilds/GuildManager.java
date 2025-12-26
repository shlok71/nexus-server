package com.nexus.guilds;

import com.nexus.core.NexusCore;
import com.nexus.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for handling all guild-related operations.
 * Manages guild creation, membership, chat, and persistence.
 */
public class GuildManager {
    
    private final NexusCore plugin;
    private final Map<UUID, Guild> guilds;
    private final Map<UUID, UUID> playerGuildMap;
    private final Map<String, Guild> guildNameMap;
    private final Map<String, Guild> guildTagMap;
    
    public GuildManager(NexusCore plugin) {
        this.plugin = plugin;
        this.guilds = new ConcurrentHashMap<>();
        this.playerGuildMap = new ConcurrentHashMap<>();
        this.guildNameMap = new ConcurrentHashMap<>();
        this.guildTagMap = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize the guild system
     */
    public void initialize() {
        loadGuilds();
        plugin.getNexusLogger().info("GuildManager initialized with " + guilds.size() + " guilds");
    }
    
    /**
     * Load all guilds from database
     */
    private void loadGuilds() {
        try {
            List<Map<String, Object>> guildData = plugin.getDatabaseManager().getAllGuilds();
            for (Map<String, Object> data : guildData) {
                Guild guild = Guild.deserialize(data);
                registerGuild(guild);
            }
        } catch (Exception e) {
            plugin.getNexusLogger().warning("Failed to load guilds: " + e.getMessage());
        }
    }
    
    /**
     * Register a guild in all internal maps
     */
    private void registerGuild(Guild guild) {
        guilds.put(guild.getId(), guild);
        guildNameMap.put(guild.getName().toLowerCase(), guild);
        guildTagMap.put(guild.getTag().toLowerCase(), guild);
        
        // Map all members to this guild
        for (UUID memberId : guild.getMemberIds()) {
            playerGuildMap.put(memberId, guild.getId());
        }
    }
    
    /**
     * Create a new guild
     */
    public boolean createGuild(Player leader, String name, String tag) {
        String lowerName = name.toLowerCase();
        String lowerTag = tag.toLowerCase();
        
        // Validate name and tag
        if (!isValidGuildName(name)) {
            leader.sendMessage(plugin.getConfigManager().getMessage("guild.invalidName"));
            return false;
        }
        
        if (!isValidGuildTag(tag)) {
            leader.sendMessage(plugin.getConfigManager().getMessage("guild.invalidTag"));
            return false;
        }
        
        // Check if player is already in a guild
        if (getPlayerGuild(leader.getUniqueId()) != null) {
            leader.sendMessage(plugin.getConfigManager().getMessage("guild.alreadyInGuild"));
            return false;
        }
        
        // Check if name or tag is taken
        if (guildNameMap.containsKey(lowerName)) {
            leader.sendMessage(plugin.getConfigManager().getMessage("guild.nameTaken"));
            return false;
        }
        
        if (guildTagMap.containsKey(lowerTag)) {
            leader.sendMessage(plugin.getConfigManager().getMessage("guild.tagTaken"));
            return false;
        }
        
        // Create the guild
        UUID guildId = UUID.randomUUID();
        Guild guild = new Guild(guildId, name, tag, leader.getUniqueId());
        
        registerGuild(guild);
        saveGuild(guild);
        
        leader.sendMessage(plugin.getConfigManager().getMessage("guild.created")
            .replace("%guild%", name)
            .replace("%tag%", tag));
        
        // Broadcast to server
        Bukkit.broadcastMessage(ChatColor.GOLD + leader.getName() + 
            ChatColor.WHITE + " created guild " + 
            getGuildTagColor(guild) + "[" + tag + "]" + 
            ChatColor.WHITE + "!");
        
        return true;
    }
    
    /**
     * Delete a guild
     */
    public boolean deleteGuild(UUID guildId) {
        Guild guild = guilds.get(guildId);
        if (guild == null) {
            return false;
        }
        
        // Notify all members
        for (UUID memberId : guild.getMemberIds()) {
            playerGuildMap.remove(memberId);
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(plugin.getConfigManager().getMessage("guild.deleted")
                    .replace("%guild%", guild.getName()));
            }
        }
        
        // Remove from maps
        guilds.remove(guildId);
        guildNameMap.remove(guild.getName().toLowerCase());
        guildTagMap.remove(guild.getTag().toLowerCase());
        
        // Delete from database
        plugin.getDatabaseManager().deleteGuild(guildId);
        
        return true;
    }
    
    /**
     * Disband guild by leader
     */
    public boolean disbandGuild(UUID leaderId) {
        Guild guild = getPlayerGuild(leaderId);
        if (guild == null) {
            return false;
        }
        
        if (!guild.isLeader(leaderId)) {
            return false;
        }
        
        return deleteGuild(guild.getId());
    }
    
    /**
     * Invite a player to the guild
     */
    public boolean invitePlayer(UUID officerId, Player target) {
        Guild guild = getPlayerGuild(officerId);
        if (guild == null) {
            return false;
        }
        
        if (!guild.canManage(officerId)) {
            return false;
        }
        
        if (guild.isMember(target.getUniqueId())) {
            return false;
        }
        
        if (guild.hasInvite(target.getUniqueId())) {
            return false;
        }
        
        guild.addInvite(target.getUniqueId());
        saveGuild(guild);
        
        target.sendMessage(plugin.getConfigManager().getMessage("guild.invited")
            .replace("%guild%", guild.getName())
            .replace("%tag%", getGuildTagColor(guild) + "[" + guild.getTag() + "]"));
        
        return true;
    }
    
    /**
     * Accept a guild invitation
     */
    public boolean acceptInvite(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Find guild with pending invite
        for (Guild guild : guilds.values()) {
            if (guild.hasInvite(playerId)) {
                guild.removeInvite(playerId);
                guild.addMember(playerId);
                playerGuildMap.put(playerId, guild.getId());
                
                player.sendMessage(plugin.getConfigManager().getMessage("guild.joined")
                    .replace("%guild%", guild.getName()));
                
                // Notify guild members
                notifyGuildMembers(guild, plugin.getConfigManager().getMessage("guild.memberJoined")
                    .replace("%player%", player.getName()), playerId);
                
                saveGuild(guild);
                return true;
            }
        }
        
        player.sendMessage(plugin.getConfigManager().getMessage("guild.noInvite"));
        return false;
    }
    
    /**
     * Leave the guild
     */
    public boolean leaveGuild(UUID playerId) {
        Guild guild = getPlayerGuild(playerId);
        if (guild == null) {
            return false;
        }
        
        if (guild.isLeader(playerId)) {
            player.sendMessage(plugin.getConfigManager().getMessage("guild.cantLeaveAsLeader"));
            return false;
        }
        
        guild.removeMember(playerId);
        playerGuildMap.remove(playerId);
        
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.sendMessage(plugin.getConfigManager().getMessage("guild.left")
                .replace("%guild%", guild.getName()));
        }
        
        notifyGuildMembers(guild, plugin.getConfigManager().getMessage("guild.memberLeft")
            .replace("%player%", player != null ? player.getName() : "A player"), playerId);
        
        saveGuild(guild);
        return true;
    }
    
    /**
     * Kick a player from the guild
     */
    public boolean kickPlayer(UUID officerId, String targetName) {
        Guild guild = getPlayerGuild(officerId);
        if (guild == null) {
            return false;
        }
        
        if (!guild.canManage(officerId)) {
            return false;
        }
        
        // Find target player
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            return false;
        }
        
        UUID targetId = target.getUniqueId();
        if (!guild.isMember(targetId)) {
            return false;
        }
        
        if (guild.isLeader(targetId)) {
            return false;
        }
        
        guild.removeMember(targetId);
        playerGuildMap.remove(targetId);
        
        target.sendMessage(plugin.getConfigManager().getMessage("guild.kicked")
            .replace("%guild%", guild.getName()));
        
        notifyGuildMembers(guild, plugin.getConfigManager().getMessage("guild.memberKicked")
            .replace("%player%", targetName), officerId);
        
        saveGuild(guild);
        return true;
    }
    
    /**
     * Promote a member to officer
     */
    public boolean promoteToOfficer(UUID leaderId, String targetName) {
        Guild guild = getPlayerGuild(leaderId);
        if (guild == null) {
            return false;
        }
        
        if (!guild.isLeader(leaderId)) {
            return false;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            return false;
        }
        
        UUID targetId = target.getUniqueId();
        if (!guild.isMember(targetId)) {
            return false;
        }
        
        if (guild.isOfficer(targetId)) {
            return false;
        }
        
        guild.addOfficer(targetId);
        
        target.sendMessage(plugin.getConfigManager().getMessage("guild.promoted")
            .replace("%guild%", guild.getName()));
        
        notifyGuildMembers(guild, plugin.getConfigManager().getMessage("guild.memberPromoted")
            .replace("%player%", targetName), leaderId);
        
        saveGuild(guild);
        return true;
    }
    
    /**
     * Demote an officer to member
     */
    public boolean demoteToMember(UUID leaderId, String targetName) {
        Guild guild = getPlayerGuild(leaderId);
        if (guild == null) {
            return false;
        }
        
        if (!guild.isLeader(leaderId)) {
            return false;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            return false;
        }
        
        UUID targetId = target.getUniqueId();
        if (!guild.isOfficer(targetId)) {
            return false;
        }
        
        if (guild.isLeader(targetId)) {
            return false;
        }
        
        guild.removeOfficer(targetId);
        
        target.sendMessage(plugin.getConfigManager().getMessage("guild.demoted")
            .replace("%guild%", guild.getName()));
        
        notifyGuildMembers(guild, plugin.getConfigManager().getMessage("guild.memberDemoted")
            .replace("%player%", targetName), leaderId);
        
        saveGuild(guild);
        return true;
    }
    
    /**
     * Transfer guild leadership
     */
    public boolean transferLeadership(UUID currentLeaderId, String newLeaderName) {
        Guild guild = getPlayerGuild(currentLeaderId);
        if (guild == null) {
            return false;
        }
        
        if (!guild.isLeader(currentLeaderId)) {
            return false;
        }
        
        Player newLeader = Bukkit.getPlayer(newLeaderName);
        if (newLeader == null) {
            return false;
        }
        
        UUID newLeaderId = newLeader.getUniqueId();
        if (!guild.isMember(newLeaderId)) {
            return false;
        }
        
        guild.setLeaderId(newLeaderId);
        guild.removeOfficer(newLeaderId);
        
        newLeader.sendMessage(plugin.getConfigManager().getMessage("guild.leadershipTransferred")
            .replace("%guild%", guild.getName()));
        
        notifyGuildMembers(guild, plugin.getConfigManager().getMessage("guild.newLeader")
            .replace("%player%", newLeaderName), currentLeaderId);
        
        saveGuild(guild);
        return true;
    }
    
    /**
     * Set guild description
     */
    public boolean setDescription(UUID officerId, String description) {
        Guild guild = getPlayerGuild(officerId);
        if (guild == null) {
            return false;
        }
        
        if (!guild.canManage(officerId)) {
            return false;
        }
        
        guild.setDescription(description.substring(0, Math.min(description.length(), 100)));
        saveGuild(guild);
        
        return true;
    }
    
    /**
     * Toggle guild public/private status
     */
    public boolean togglePublic(UUID officerId) {
        Guild guild = getPlayerGuild(officerId);
        if (guild == null) {
            return false;
        }
        
        if (!guild.canManage(officerId)) {
            return false;
        }
        
        guild.setPublic(!guild.isPublic());
        saveGuild(guild);
        
        Player player = Bukkit.getPlayer(officerId);
        if (player != null) {
            player.sendMessage(plugin.getConfigManager().getMessage(guild.isPublic() ? 
                "guild.nowPublic" : "guild.nowPrivate")
                .replace("%guild%", guild.getName()));
        }
        
        return true;
    }
    
    // Guild Chat
    
    /**
     * Send a message to guild chat
     */
    public void sendGuildChat(UUID playerId, String message) {
        Guild guild = getPlayerGuild(playerId);
        if (guild == null) {
            return;
        }
        
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        
        String rankPrefix = guild.isLeader(playerId) ? "★" : 
                          guild.isOfficer(playerId) ? "♦" : "";
        
        String chatMessage = plugin.getConfigManager().getMessage("guild.chatFormat")
            .replace("%rank%", rankPrefix)
            .replace("%player%", player.getName())
            .replace("%guild%", getGuildTagColor(guild) + "[" + guild.getTag() + "]")
            .replace("%message%", message);
        
        for (UUID memberId : guild.getMemberIds()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(chatMessage);
            }
        }
    }
    
    /**
     * Send a message to guild officer chat
     */
    public void sendOfficerChat(UUID playerId, String message) {
        Guild guild = getPlayerGuild(playerId);
        if (guild == null) {
            return;
        }
        
        if (!guild.isOfficer(playerId)) {
            return;
        }
        
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        
        String chatMessage = ChatColor.DARK_PURPLE + "[Officer] " + 
            getGuildTagColor(guild) + "[" + guild.getTag() + "] " + 
            player.getName() + ": " + ChatColor.RESET + message;
        
        for (UUID officerId : guild.getOfficerIds()) {
            if (guild.isLeader(officerId) || guild.getOfficerIds().contains(officerId)) {
                Player officer = Bukkit.getPlayer(officerId);
                if (officer != null && officer.isOnline()) {
                    officer.sendMessage(chatMessage);
                }
            }
        }
    }
    
    // Getters
    
    /**
     * Get a player's guild
     */
    public Guild getPlayerGuild(UUID playerId) {
        UUID guildId = playerGuildMap.get(playerId);
        if (guildId == null) {
            return null;
        }
        return guilds.get(guildId);
    }
    
    /**
     * Get guild by name
     */
    public Guild getGuildByName(String name) {
        return guildNameMap.get(name.toLowerCase());
    }
    
    /**
     * Get guild by tag
     */
    public Guild getGuildByTag(String tag) {
        return guildTagMap.get(tag.toLowerCase());
    }
    
    /**
     * Get guild by ID
     */
    public Guild getGuildById(UUID id) {
        return guilds.get(id);
    }
    
    /**
     * Get all guilds
     */
    public Collection<Guild> getAllGuilds() {
        return guilds.values();
    }
    
    /**
     * Get guild count
     */
    public int getGuildCount() {
        return guilds.size();
    }
    
    // Leaderboards
    
    /**
     * Get guild leaderboard by level
     */
    public List<Guild> getLeaderboardByLevel(int limit) {
        List<Guild> sorted = new ArrayList<>(guilds.values());
        sorted.sort((a, b) -> Integer.compare(b.getLevel(), a.getLevel()));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }
    
    /**
     * Get guild leaderboard by experience
     */
    public List<Guild> getLeaderboardByExperience(int limit) {
        List<Guild> sorted = new ArrayList<>(guilds.values());
        sorted.sort((a, b) -> Long.compare(b.getExperience(), a.getExperience()));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }
    
    /**
     * Get guild leaderboard by wins
     */
    public List<Guild> getLeaderboardByWins(int limit) {
        List<Guild> sorted = new ArrayList<>(guilds.values());
        sorted.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }
    
    // Validation
    
    /**
     * Validate guild name
     */
    private boolean isValidGuildName(String name) {
        return name.length() >= 2 && name.length() <= 16 && 
               name.matches("^[a-zA-Z0-9]+$");
    }
    
    /**
     * Validate guild tag
     */
    private boolean isValidGuildTag(String tag) {
        return tag.length() >= 2 && tag.length() <= 4 && 
               tag.matches("^[a-zA-Z0-9]+$");
    }
    
    /**
     * Get colored guild tag
     */
    public String getGuildTagColor(Guild guild) {
        return switch (guild.getColor().toUpperCase()) {
            case "DARK_RED" -> ChatColor.DARK_RED.toString();
            case "RED" -> ChatColor.RED.toString();
            case "GOLD" -> ChatColor.GOLD.toString();
            case "YELLOW" -> ChatColor.YELLOW.toString();
            case "GREEN" -> ChatColor.GREEN.toString();
            case "DARK_GREEN" -> ChatColor.DARK_GREEN.toString();
            case "AQUA" -> ChatColor.AQUA.toString();
            case "DARK_AQUA" -> ChatColor.DARK_AQUA.toString();
            case "BLUE" -> ChatColor.BLUE.toString();
            case "DARK_BLUE" -> ChatColor.DARK_BLUE.toString();
            case "DARK_PURPLE" -> ChatColor.DARK_PURPLE.toString();
            case "LIGHT_PURPLE" -> ChatColor.LIGHT_PURPLE.toString();
            case "WHITE" -> ChatColor.WHITE.toString();
            case "GRAY" -> ChatColor.GRAY.toString();
            case "DARK_GRAY" -> ChatColor.DARK_GRAY.toString();
            default -> ChatColor.GRAY.toString();
        };
    }
    
    /**
     * Notify all guild members
     */
    private void notifyGuildMembers(Guild guild, String message, UUID excludeId) {
        for (UUID memberId : guild.getMemberIds()) {
            if (memberId.equals(excludeId)) {
                continue;
            }
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(message);
            }
        }
    }
    
    /**
     * Save guild to database
     */
    private void saveGuild(Guild guild) {
        plugin.getDatabaseManager().saveGuild(guild);
    }
    
    /**
     * Add experience to guild
     */
    public void addGuildExperience(UUID playerId, long amount) {
        Guild guild = getPlayerGuild(playerId);
        if (guild != null) {
            int oldLevel = guild.getLevel();
            guild.addExperience(amount);
            
            // Check for level up
            if (guild.getLevel() > oldLevel) {
                notifyGuildMembers(guild, 
                    plugin.getConfigManager().getMessage("guild.levelUp")
                        .replace("%guild%", guild.getName())
                        .replace("%level%", String.valueOf(guild.getLevel())), 
                    null);
            }
            
            saveGuild(guild);
        }
    }
    
    /**
     * Shutdown the guild manager
     */
    public void shutdown() {
        // Save all guilds
        for (Guild guild : guilds.values()) {
            saveGuild(guild);
        }
        guilds.clear();
        playerGuildMap.clear();
        guildNameMap.clear();
        guildTagMap.clear();
    }
}
