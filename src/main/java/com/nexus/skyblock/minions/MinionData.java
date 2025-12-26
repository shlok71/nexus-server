package com.nexus.skyblock.minions;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data class representing a minion's state and configuration
 */
public class MinionData {

    private final UUID id;
    private final UUID ownerUUID;
    private MinionManager.MinionType type;
    private Location location;
    private int tier;
    private List<ItemStack> storage;
    private long lastAction;
    private boolean active;
    private UUID entityId;
    private int luckBonus;

    public MinionData(UUID id, UUID ownerUUID, MinionManager.MinionType type,
                      Location location, int tier, List<ItemStack> storage) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.type = type;
        this.location = location;
        this.tier = tier;
        this.storage = storage != null ? storage : new ArrayList<>();
        this.lastAction = System.currentTimeMillis();
        this.active = true;
        this.luckBonus = 0;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public MinionManager.MinionType getType() {
        return type;
    }

    public void setType(MinionManager.MinionType type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public List<ItemStack> getStorage() {
        return storage;
    }

    public void setStorage(List<ItemStack> storage) {
        this.storage = storage;
    }

    public long getLastAction() {
        return lastAction;
    }

    public void setLastAction(long lastAction) {
        this.lastAction = lastAction;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public int getLuckBonus() {
        return luckBonus;
    }

    public void setLuckBonus(int luckBonus) {
        this.luckBonus = luckBonus;
    }

    /**
     * Get actions per minute based on minion tier
     */
    public long getActionsPerMinute() {
        // Base: 10 actions per minute, increases with tier
        int baseAPM = 10;
        return baseAPM + (tier - 1) * 2;
    }

    /**
     * Get max storage slots based on tier
     */
    public int getMaxStorage() {
        return 9 + (tier - 1) * 3;
    }

    /**
     * Get upgrade cost for next tier
     */
    public int getUpgradeCost() {
        return (int) (50 * Math.pow(2, tier - 1));
    }
}
