package com.nexus.ranks;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a player rank with all its properties.
 */
public class Rank {
    
    private final String id;
    private String name;
    private String prefix;
    private String suffix;
    private String color;
    private int priority;
    private boolean isDefault;
    private boolean isStaff;
    private boolean isOwner;
    private boolean isCustom;
    private double price;
    private Set<String> permissions;
    private String chatFormat;
    private String tabFormat;
    
    public Rank(String id) {
        this.id = id;
        this.name = id;
        this.prefix = "";
        this.suffix = "";
        this.color = "WHITE";
        this.priority = 0;
        this.isDefault = false;
        this.isStaff = false;
        this.isOwner = false;
        this.isCustom = false;
        this.price = 0;
        this.permissions = new HashSet<>();
        this.chatFormat = "%prefix% %player%: %message%";
        this.tabFormat = "%player%";
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public String getSuffix() {
        return suffix;
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public boolean isStaff() {
        return isStaff;
    }
    
    public void setStaff(boolean staff) {
        isStaff = staff;
    }
    
    public boolean isOwner() {
        return isOwner;
    }
    
    public void setOwner(boolean owner) {
        isOwner = owner;
    }
    
    public boolean isCustom() {
        return isCustom;
    }
    
    public void setCustom(boolean custom) {
        isCustom = custom;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public Set<String> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
    
    public String getChatFormat() {
        return chatFormat;
    }
    
    public void setChatFormat(String chatFormat) {
        this.chatFormat = chatFormat;
    }
    
    public String getTabFormat() {
        return tabFormat;
    }
    
    public void setTabFormat(String tabFormat) {
        this.tabFormat = tabFormat;
    }
    
    /**
     * Check if this rank has a specific permission
     */
    public boolean hasPermission(String permission) {
        if (permissions.contains("*")) {
            return true;
        }
        return permissions.contains(permission);
    }
    
    /**
     * Check if this rank is higher than another rank
     */
    public boolean isHigherThan(Rank other) {
        return this.priority > other.priority;
    }
}
