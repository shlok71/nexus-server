package com.nexus.skyblock.skills;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a player's skills data.
 * Contains level, current XP, and progress for all skill types.
 */
public class PlayerSkills {
    
    private final Map<SkillType, SkillData> skills;
    
    public PlayerSkills() {
        this.skills = new HashMap<>();
        initializeSkills();
    }
    
    /**
     * Initialize all skills with default values (level 1, 0 XP)
     */
    private void initializeSkills() {
        for (SkillType skill : SkillType.values()) {
            skills.put(skill, new SkillData(1, 0));
        }
    }
    
    /**
     * Get skill data for a specific skill type
     */
    public SkillData getSkill(SkillType skillType) {
        return skills.getOrDefault(skillType, new SkillData(1, 0));
    }
    
    /**
     * Add XP to a specific skill and handle level ups
     * @return true if a level up occurred
     */
    public boolean addXp(SkillType skillType, double xpAmount) {
        SkillData data = skills.get(skillType);
        if (data == null || data.getLevel() >= skillType.getMaxLevel()) {
            return false;
        }
        
        data.addXp(xpAmount);
        
        // Check for level up
        boolean leveledUp = false;
        while (data.getLevel() < skillType.getMaxLevel() && 
               data.getCurrentXp() >= skillType.getXpForLevel(data.getLevel())) {
            data.levelUp(skillType);
            leveledUp = true;
        }
        
        return leveledUp;
    }
    
    /**
     * Set skill level directly (for admin commands)
     */
    public void setLevel(SkillType skillType, int level) {
        SkillData data = skills.get(skillType);
        if (data != null) {
            int clampedLevel = Math.max(1, Math.min(level, skillType.getMaxLevel()));
            double requiredXp = 0;
            for (int i = 1; i < clampedLevel; i++) {
                requiredXp += skillType.getXpForLevel(i);
            }
            data.setLevel(clampedLevel);
            data.setCurrentXp(requiredXp);
        }
    }
    
    /**
     * Get total level of all skills (average skill level)
     */
    public int getTotalLevel() {
        return skills.values().stream()
                .mapToInt(SkillData::getLevel)
                .sum();
    }
    
    /**
     * Get average skill level
     */
    public double getAverageLevel() {
        return getTotalLevel() / (double) SkillType.values().length;
    }
    
    /**
     * Get skill progress percentage (0.0 to 1.0) for a specific level
     */
    public double getProgressForLevel(SkillType skillType) {
        SkillData data = skills.get(skillType);
        if (data == null || data.getLevel() >= skillType.getMaxLevel()) {
            return 1.0;
        }
        
        double currentXp = data.getCurrentXp();
        double xpForCurrent = skillType.getXpForLevel(data.getLevel());
        double xpForNext = skillType.getXpForLevel(data.getLevel() + 1);
        
        double xpInLevel = currentXp - xpForCurrent;
        double xpNeeded = xpForNext - xpForCurrent;
        
        return Math.min(1.0, Math.max(0, xpInLevel / xpNeeded));
    }
    
    /**
     * Get skill data as a map for serialization
     */
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        for (Map.Entry<SkillType, SkillData> entry : skills.entrySet()) {
            serialized.put(entry.getKey().name(), entry.getValue().serialize());
        }
        return serialized;
    }
    
    /**
     * Load skills from serialized data
     */
    @SuppressWarnings("unchecked")
    public void deserialize(Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            try {
                SkillType skillType = SkillType.valueOf(entry.getKey());
                SkillData skillData = new SkillData(1, 0);
                skillData.deserialize((Map<String, Object>) entry.getValue());
                skills.put(skillType, skillData);
            } catch (IllegalArgumentException e) {
                // Unknown skill type, skip
            }
        }
    }
}
