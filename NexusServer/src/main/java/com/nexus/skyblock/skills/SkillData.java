package com.nexus.skyblock.skills;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents data for a single skill (level and XP).
 */
public class SkillData {
    
    private int level;
    private double currentXp;
    
    public SkillData() {
        this(1, 0);
    }
    
    public SkillData(int level, double currentXp) {
        this.level = level;
        this.currentXp = currentXp;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public double getCurrentXp() {
        return currentXp;
    }
    
    public void setCurrentXp(double currentXp) {
        this.currentXp = currentXp;
    }
    
    /**
     * Add XP to the skill
     */
    public void addXp(double xpAmount) {
        this.currentXp += xpAmount;
    }
    
    /**
     * Level up the skill
     */
    public void levelUp(SkillType skillType) {
        if (this.level < skillType.getMaxLevel()) {
            this.level++;
        }
    }
    
    /**
     * Get XP progress towards next level
     */
    public double getXpTowardsNext(SkillType skillType) {
        if (this.level >= skillType.getMaxLevel()) {
            return 0;
        }
        
        double xpForCurrent = skillType.getXpForLevel(this.level);
        double xpForNext = skillType.getXpForLevel(this.level + 1);
        
        return this.currentXp - xpForCurrent;
    }
    
    /**
     * Get XP needed for next level
     */
    public double getXpNeededForNext(SkillType skillType) {
        if (this.level >= skillType.getMaxLevel()) {
            return 0;
        }
        
        double xpForCurrent = skillType.getXpForLevel(this.level);
        double xpForNext = skillType.getXpForLevel(this.level + 1);
        
        return xpForNext - xpForCurrent;
    }
    
    /**
     * Serialize skill data for storage
     */
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("level", level);
        data.put("xp", currentXp);
        return data;
    }
    
    /**
     * Deserialize skill data from storage
     */
    @SuppressWarnings("unchecked")
    public void deserialize(Map<String, Object> data) {
        if (data.containsKey("level")) {
            this.level = ((Number) data.get("level")).intValue();
        }
        if (data.containsKey("xp")) {
            this.currentXp = ((Number) data.get("xp")).doubleValue();
        }
    }
}
