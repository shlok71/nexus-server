package com.nexus.skyblock.skills;

/**
 * Enum representing all available skills in the game.
 * Each skill has a display name, max level, and XP formula.
 */
public enum SkillType {
    
    MINING("Mining", 60, 0.5),
    FORAGING("Foraging", 50, 0.5),
    FARMING("Farming", 50, 0.5),
    FISHING("Fishing", 50, 0.5),
    COMBAT("Combat", 60, 0.5),
    ENCHANTING("Enchanting", 50, 0.5),
    ALCHEMY("Alchemy", 50, 0.5),
    CARPENTRY("Carpentry", 50, 0.5),
    RUNECRAFT("Runecraft", 50, 0.5),
    TAMING("Taming", 50, 0.5);
    
    private final String displayName;
    private final int maxLevel;
    private final double xpMultiplier;
    
    SkillType(String displayName, int maxLevel, double xpMultiplier) {
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.xpMultiplier = xpMultiplier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getMaxLevel() {
        return maxLevel;
    }
    
    public double getXpMultiplier() {
        return xpMultiplier;
    }
    
    /**
     * Calculate XP required for a specific level
     * Formula: base * level^exponent with scaling
     */
    public double getXpForLevel(int level) {
        if (level >= maxLevel) {
            return Double.MAX_VALUE;
        }
        // Exponential curve: more XP needed at higher levels
        return Math.pow(level, 1.5) * 100 * xpMultiplier;
    }
    
    /**
     * Calculate skill bonus based on level
     * Different skills have different bonus calculations
     */
    public double getSkillBonus(int level) {
        return switch (this) {
            case MINING -> level * 0.5; // +0.5% mining speed per level
            case FORAGING -> level * 0.5; // +0.5% foraging speed per level
            case FARMING -> level * 0.5; // +0.5% farming fortune per level
            case FISHING -> level * 0.5; // +0.5% fishing speed per level
            case COMBAT -> level * 0.5; // +0.5% damage per level
            case ENCHANTING -> level * 0.5; // +0.5% experience gain per level
            case ALCHEMY -> level * 0.5; // +0.5% potion effectiveness per level
            case CARPENTRY -> level * 0.5; // +0.5% crafting speed per level
            case RUNECRAFT -> level * 0.5; // +0.5% rune effectiveness per level
            case TAMING -> level * 0.5; // +0.5% pet bonus per level
        };
    }
}
