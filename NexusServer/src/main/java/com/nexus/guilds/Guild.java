package com.nexus.guilds;

import java.util.*;

/**
 * Represents a guild in the NexusBlock Network.
 * Guilds allow players to group together, compete on leaderboards,
 * and unlock exclusive perks.
 */
public class Guild {
    
    private final UUID id;
    private String name;
    private String tag;
    private UUID leaderId;
    private Set<UUID> officerIds;
    private Set<UUID> memberIds;
    private Set<UUID> invitePending;
    private String description;
    private String color;
    private int level;
    private long experience;
    private long createdAt;
    private int wins;
    private int kills;
    private boolean isPublic;
    
    public Guild(UUID id, String name, String tag, UUID leaderId) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.leaderId = leaderId;
        this.officerIds = new HashSet<>();
        this.memberIds = new HashSet<>();
        this.invitePending = new HashSet<>();
        this.description = "";
        this.color = "GRAY";
        this.level = 1;
        this.experience = 0;
        this.createdAt = System.currentTimeMillis();
        this.wins = 0;
        this.kills = 0;
        this.isPublic = true;
        
        // Add leader as member
        memberIds.add(leaderId);
    }
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public UUID getLeaderId() {
        return leaderId;
    }
    
    public void setLeaderId(UUID leaderId) {
        this.leaderId = leaderId;
    }
    
    public Set<UUID> getOfficerIds() {
        return officerIds;
    }
    
    public Set<UUID> getMemberIds() {
        return memberIds;
    }
    
    public Set<UUID> getInvitePending() {
        return invitePending;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public long getExperience() {
        return experience;
    }
    
    public void setExperience(long experience) {
        this.experience = experience;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public int getWins() {
        return wins;
    }
    
    public void setWins(int wins) {
        this.wins = wins;
    }
    
    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public int getMemberCount() {
        return memberIds.size();
    }
    
    // Member management
    
    public boolean addMember(UUID playerId) {
        if (memberIds.contains(playerId)) {
            return false;
        }
        memberIds.add(playerId);
        invitePending.remove(playerId);
        return true;
    }
    
    public boolean removeMember(UUID playerId) {
        if (memberIds.remove(playerId)) {
            officerIds.remove(playerId);
            if (leaderId.equals(playerId)) {
                // Transfer leadership to oldest officer, or oldest member
                UUID newLeader = null;
                for (UUID officer : officerIds) {
                    if (memberIds.contains(officer)) {
                        newLeader = officer;
                        break;
                    }
                }
                if (newLeader == null) {
                    for (UUID member : memberIds) {
                        newLeader = member;
                        break;
                    }
                }
                if (newLeader != null) {
                    leaderId = newLeader;
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean addOfficer(UUID playerId) {
        if (memberIds.contains(playerId) && officerIds.add(playerId)) {
            return true;
        }
        return false;
    }
    
    public boolean removeOfficer(UUID playerId) {
        return officerIds.remove(playerId);
    }
    
    public boolean isOfficer(UUID playerId) {
        return officerIds.contains(playerId) || leaderId.equals(playerId);
    }
    
    public boolean isMember(UUID playerId) {
        return memberIds.contains(playerId);
    }
    
    public boolean isLeader(UUID playerId) {
        return leaderId.equals(playerId);
    }
    
    public boolean canManage(UUID playerId) {
        return isLeader(playerId) || isOfficer(playerId);
    }
    
    public boolean addInvite(UUID playerId) {
        if (isMember(playerId)) {
            return false;
        }
        invitePending.add(playerId);
        return true;
    }
    
    public boolean hasInvite(UUID playerId) {
        return invitePending.contains(playerId);
    }
    
    public boolean removeInvite(UUID playerId) {
        return invitePending.remove(playerId);
    }
    
    // Experience and leveling
    
    public void addExperience(long amount) {
        this.experience += amount;
        checkLevelUp();
    }
    
    private void checkLevelUp() {
        long requiredExp = getExperienceRequired(level + 1);
        while (experience >= requiredExp) {
            experience -= requiredExp;
            level++;
            requiredExp = getExperienceRequired(level + 1);
        }
    }
    
    public static long getExperienceRequired(int level) {
        // Formula: 100 * level^2 + 100
        return 100L * level * level + 100;
    }
    
    public long getExperienceProgress() {
        if (level == 1) {
            return experience;
        }
        long required = getExperienceRequired(level);
        return Math.min(experience, required);
    }
    
    public int getExperienceProgressPercent() {
        if (level == 1) {
            return (int) Math.min(experience, 100);
        }
        long required = getExperienceRequired(level);
        return (int) ((experience * 100) / required);
    }
    
    // Stats
    
    public void addKill() {
        this.kills++;
    }
    
    public void addWin() {
        this.wins++;
    }
    
    // Serialization
    
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id.toString());
        data.put("name", name);
        data.put("tag", tag);
        data.put("leaderId", leaderId.toString());
        data.put("officerIds", serializeUUIDs(officerIds));
        data.put("memberIds", serializeUUIDs(memberIds));
        data.put("invitePending", serializeUUIDs(invitePending));
        data.put("description", description);
        data.put("color", color);
        data.put("level", level);
        data.put("experience", experience);
        data.put("createdAt", createdAt);
        data.put("wins", wins);
        data.put("kills", kills);
        data.put("isPublic", isPublic);
        return data;
    }
    
    private List<String> serializeUUIDs(Set<UUID> uuids) {
        List<String> list = new ArrayList<>();
        for (UUID uuid : uuids) {
            list.add(uuid.toString());
        }
        return list;
    }
    
    public static Guild deserialize(Map<String, Object> data) {
        UUID id = UUID.fromString((String) data.get("id"));
        String name = (String) data.get("name");
        String tag = (String) data.get("tag");
        UUID leaderId = UUID.fromString((String) data.get("leaderId"));
        
        Guild guild = new Guild(id, name, tag, leaderId);
        
        if (data.containsKey("officerIds")) {
            guild.officerIds = deserializeUUIDs((List<String>) data.get("officerIds"));
        }
        if (data.containsKey("memberIds")) {
            guild.memberIds = deserializeUUIDs((List<String>) data.get("memberIds"));
        }
        if (data.containsKey("invitePending")) {
            guild.invitePending = deserializeUUIDs((List<String>) data.get("invitePending"));
        }
        if (data.containsKey("description")) {
            guild.description = (String) data.get("description");
        }
        if (data.containsKey("color")) {
            guild.color = (String) data.get("color");
        }
        if (data.containsKey("level")) {
            guild.level = (Integer) data.get("level");
        }
        if (data.containsKey("experience")) {
            guild.experience = ((Number) data.get("experience")).longValue();
        }
        if (data.containsKey("createdAt")) {
            guild.createdAt = ((Number) data.get("createdAt")).longValue();
        }
        if (data.containsKey("wins")) {
            guild.wins = (Integer) data.get("wins");
        }
        if (data.containsKey("kills")) {
            guild.kills = (Integer) data.get("kills");
        }
        if (data.containsKey("isPublic")) {
            guild.isPublic = (Boolean) data.get("isPublic");
        }
        
        return guild;
    }
    
    private static Set<UUID> deserializeUUIDs(List<String> list) {
        Set<UUID> set = new HashSet<>();
        for (String str : list) {
            set.add(UUID.fromString(str));
        }
        return set;
    }
}
