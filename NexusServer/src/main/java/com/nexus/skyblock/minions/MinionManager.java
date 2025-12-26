package com.nexus.skyblock.minions;

import com.nexus.core.NexusCore;
import com.nexus.skyblock.island.SkyBlockIsland;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hypixel-style Minion system for NexusBlock SkyBlock
 * Handles minion spawning, automation, and resource gathering
 */
public class MinionManager {

    private final NexusCore plugin;
    private final Map<UUID, MinionData> playerMinions;
    private final Map<UUID, Long> lastUpdateTimes;
    private final BukkitScheduler scheduler;
    private static final long TICK_INTERVAL = 20L; // 1 second
    private static final long OFFLINE_CALCULATION_INTERVAL = 6000L; // 5 minutes

    public MinionManager(NexusCore plugin) {
        this.plugin = plugin;
        this.playerMinions = new ConcurrentHashMap<>();
        this.lastUpdateTimes = new ConcurrentHashMap<>();
        this.scheduler = Bukkit.getScheduler();
    }

    /**
     * Initialize the minion system
     */
    public void initialize() {
        // Start minion ticking task
        startMinionTicker();

        // Start offline calculation task
        startOfflineCalculator();

        plugin.getNexusLogger().info("MinionManager initialized");
    }

    /**
     * Start the main minion ticking task
     */
    private void startMinionTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tickAllMinions();
            }
        }.runTaskTimer(plugin, TICK_INTERVAL, TICK_INTERVAL);
    }

    /**
     * Start offline resource calculation task
     */
    private void startOfflineCalculator() {
        new BukkitRunnable() {
            @Override
            public void run() {
                calculateOfflineResources();
            }
        }.runTaskTimerAsynchronously(plugin, OFFLINE_CALCULATION_INTERVAL, OFFLINE_CALCULATION_INTERVAL);
    }

    /**
     * Tick all active minions
     */
    private void tickAllMinions() {
        for (MinionData minion : playerMinions.values()) {
            if (minion.isActive() && minion.getOwner().isOnline()) {
                tickMinion(minion);
            }
        }
    }

    /**
     * Process a single minion's tick
     */
    private void tickMinion(MinionData minion) {
        if (!minion.isActive()) return;

        long currentTime = System.currentTimeMillis();
        long actionsPerMinute = minion.getActionsPerMinute();
        long millisPerAction = (60000 / actionsPerMinute) * 1000;

        if (currentTime - minion.getLastAction() >= millisPerAction) {
            // Perform action
            performMinionAction(minion);
            minion.setLastAction(currentTime);
        }
    }

    /**
     * Perform a minion's gathering action
     */
    private void performMinionAction(MinionData minion) {
        Player owner = minion.getOwner();
        if (owner == null || !owner.isOnline()) return;

        // Calculate rewards
        ItemStack reward = calculateReward(minion);
        if (reward == null) return;

        // Add to minion storage or drop at feet
        if (hasStorageSpace(minion)) {
            addToStorage(minion, reward);
        } else {
            dropAtFeet(minion, reward);
        }

        // Play sound effect
        owner.playSound(owner.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.0f);

        // Visual feedback - particle effect
        Location minionLoc = minion.getLocation();
        if (minionLoc.getWorld() != null) {
            minionLoc.getWorld().spawnParticle(
                org.bukkit.Particle.VILLAGER_HAPPY,
                minionLoc.clone().add(0, 1, 0),
                3, 0.5, 0.5, 0.5
            );
        }
    }

    /**
     * Calculate the reward item for a minion
     */
    private ItemStack calculateReward(MinionData minion) {
        MinionType type = minion.getType();
        int tier = minion.getTier();
        double multiplier = 1.0 + (tier - 1) * 0.25; // 25% bonus per tier

        // Base resource
        Material resource = type.getResource();
        int amount = (int) (type.getBaseAmount() * multiplier);

        // Chance for bonus drops (based on luck stat)
        double luckBonus = minion.getLuckBonus() * 0.01;
        if (Math.random() < luckBonus) {
            amount *= 2; // Double drops!
        }

        ItemStack item = new ItemStack(resource, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type.getDisplayName());
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Check if minion storage has space
     */
    private boolean hasStorageSpace(MinionData minion) {
        // Simplified storage check
        return true; // Full implementation would check storage size
    }

    /**
     * Add item to minion storage
     */
    private void addToStorage(MinionData minion, ItemStack item) {
        minion.getStorage().add(item);
        saveMinionData(minion);
    }

    /**
     * Drop item at minion's feet
     */
    private void dropAtFeet(MinionData minion, ItemStack item) {
        Location loc = minion.getLocation();
        if (loc.getWorld() != null) {
            loc.getWorld().dropItemNaturally(loc, item);
        }
    }

    /**
     * Calculate offline resources
     */
    private void calculateOfflineResources() {
        long currentTime = System.currentTimeMillis();

        for (MinionData minion : playerMinions.values()) {
            if (!minion.isActive()) continue;

            Player owner = minion.getOwner();
            if (owner == null || !owner.isOnline()) {
                // Calculate offline time
                Long lastOnline = lastUpdateTimes.get(minion.getOwnerUUID());
                if (lastOnline == null) lastOnline = currentTime;

                long offlineTime = currentTime - lastOnline;
                long offlineMinutes = offlineTime / 60000;

                if (offlineMinutes > 0) {
                    // Calculate resources generated while offline
                    calculateOfflineGains(minion, offlineMinutes);
                }
            }

            lastUpdateTimes.put(minion.getOwnerUUID(), currentTime);
        }
    }

    /**
     * Calculate offline gains for a minion
     */
    private void calculateOfflineGains(MinionData minion, long offlineMinutes) {
        long actionsPerMinute = minion.getActionsPerMinute();
        long totalActions = actionsPerMinute * offlineMinutes;

        // Cap at 12 hours of offline gains
        totalActions = Math.min(totalActions, actionsPerMinute * 720);

        if (totalActions <= 0) return;

        // Calculate total rewards
        ItemStack reward = calculateReward(minion);
        if (reward == null) return;

        int totalAmount = reward.getAmount() * (int) totalActions;
        reward.setAmount(totalAmount);

        // Add to storage or notify player
        Player owner = minion.getOwner();
        if (owner != null && owner.isOnline()) {
            addToStorage(minion, reward);
            owner.sendMessage(
                org.bukkit.ChatColor.GREEN + "Your " + minion.getType().getDisplayName() +
                " collected " + totalAmount + " " + reward.getType().name().toLowerCase() + " while you were away!"
            );
        } else {
            minion.getStorage().add(reward);
            saveMinionData(minion);
        }
    }

    /**
     * Create a new minion
     */
    public MinionData createMinion(Player owner, MinionType type, Location location) {
        UUID minionId = UUID.randomUUID();
        MinionData minion = new MinionData(
            minionId,
            owner.getUniqueId(),
            type,
            location,
            1, // Tier 1
            new ArrayList<>()
        );

        // Spawn the minion entity
        spawnMinionEntity(minion);

        // Store in map
        playerMinions.put(minionId, minion);
        lastUpdateTimes.put(owner.getUniqueId(), System.currentTimeMillis());

        // Save to database
        saveMinionData(minion);

        owner.sendMessage(
            org.bukkit.ChatColor.GREEN + "You placed a " + type.getDisplayName() + " Minion!"
        );

        return minion;
    }

    /**
     * Spawn the visual minion entity (ArmorStand with player head)
     */
    private void spawnMinionEntity(MinionData minion) {
        Location loc = minion.getLocation();
        if (loc.getWorld() == null) return;

        // Create armor stand
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(
            loc.clone().add(0.5, 0, 0.5),
            EntityType.ARMOR_STAND
        );

        // Configure armor stand
        armorStand.setVisible(false);
        armorStand.setSmall(true);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);
        armorStand.setGravity(false);
        armorStand.setCustomName(minion.getType().getDisplayName() + " Minion");
        armorStand.setCustomNameVisible(true);
        armorStand.setRemoveWhenFarAway(false);

        // Set player head
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(minion.getType().getDisplayName());
            head.setItemMeta(meta);
        }
        armorStand.setHelmet(head);

        // Set metadata
        armorStand.setMetadata(
            "minionId",
            new FixedMetadataValue(plugin, minion.getId().toString())
        );

        minion.setEntityId(armorStand.getUniqueId());
    }

    /**
     * Upgrade a minion to next tier
     */
    public boolean upgradeMinion(MinionData minion, Player player) {
        int currentTier = minion.getTier();
        int maxTier = 11; // XI tier

        if (currentTier >= maxTier) {
            player.sendMessage(org.bukkit.ChatColor.RED + "This minion is already at maximum tier!");
            return false;
        }

        int upgradeCost = calculateUpgradeCost(currentTier);
        if (!plugin.getEconomyManager().hasCoins(player.getUniqueId(), upgradeCost)) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Not enough coins! Need: " + upgradeCost);
            return false;
        }

        // Deduct cost and upgrade
        plugin.getEconomyManager().removeCoins(player.getUniqueId(), upgradeCost);
        minion.setTier(currentTier + 1);

        // Update visual
        updateMinionVisuals(minion);

        saveMinionData(minion);

        player.sendMessage(
            org.bukkit.ChatColor.GREEN + "Upgraded " + minion.getType().getDisplayName() +
            " Minion to Tier " + minion.getTier() + "!"
        );

        return true;
    }

    /**
     * Calculate upgrade cost for a tier
     */
    private int calculateUpgradeCost(int currentTier) {
        // Base cost 50 coins, doubles each tier
        return (int) (50 * Math.pow(2, currentTier - 1));
    }

    /**
     * Update minion visual appearance
     */
    private void updateMinionVisuals(MinionData minion) {
        if (minion.getEntityId() == null) return;

        org.bukkit.entity.Entity entity = Bukkit.getEntity(minion.getEntityId());
        if (entity instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) entity;
            stand.setCustomName(
                minion.getType().getDisplayName() + " Minion " +
                getRomanNumeral(minion.getTier())
            );
        }
    }

    /**
     * Convert number to Roman numeral
     */
    private String getRomanNumeral(int num) {
        String[] roman = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI"};
        return roman[Math.min(num, 11)];
    }

    /**
     * Open minion storage GUI
     */
    public void openStorageGUI(Player player, MinionData minion) {
        org.bukkit.inventory.Inventory gui = Bukkit.createInventory(
            player,
            27,
            org.bukkit.ChatColor.GREEN + minion.getType().getDisplayName() + " Storage"
        );

        // Add items from storage
        int slot = 0;
        for (ItemStack item : minion.getStorage()) {
            if (slot < 27) {
                gui.setItem(slot, item);
                slot++;
            }
        }

        // Add collection button
        ItemStack collect = new ItemStack(Material.HOPPER, 1);
        ItemMeta collectMeta = collect.getItemMeta();
        collectMeta.setDisplayName(org.bukkit.ChatColor.GREEN + "Collect All");
        collect.setItemMeta(collectMeta);
        gui.setItem(26, collect);

        player.openInventory(gui);
    }

    /**
     * Collect all items from minion storage
     */
    public void collectStorage(Player player, MinionData minion) {
        for (ItemStack item : minion.getStorage()) {
            if (item != null) {
                // Add to player inventory or drop
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                for (ItemStack dropped : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), dropped);
                }
            }
        }

        minion.getStorage().clear();
        saveMinionData(minion);

        player.sendMessage(org.bukkit.ChatColor.GREEN + "Collected all items!");
    }

    /**
     * Remove a minion
     */
    public void removeMinion(MinionData minion) {
        // Remove entity
        if (minion.getEntityId() != null) {
            org.bukkit.entity.Entity entity = Bukkit.getEntity(minion.getEntityId());
            if (entity != null) {
                entity.remove();
            }
        }

        // Remove from map
        playerMinions.remove(minion.getId());

        // Clear from database
        clearMinionData(minion);
    }

    /**
     * Save minion data to database
     */
    private void saveMinionData(MinionData minion) {
        // In full implementation, save to database
    }

    /**
     * Clear minion data from database
     */
    private void clearMinionData(MinionData minion) {
        // In full implementation, remove from database
    }

    /**
     * Get minion by ID
     */
    public MinionData getMinion(UUID minionId) {
        return playerMinions.get(minionId);
    }

    /**
     * Get all minions for a player
     */
    public List<MinionData> getPlayerMinions(UUID playerId) {
        List<MinionData> result = new ArrayList<>();
        for (MinionData minion : playerMinions.values()) {
            if (minion.getOwnerUUID().equals(playerId)) {
                result.add(minion);
            }
        }
        return result;
    }

    /**
     * Shutdown minion system
     */
    public void shutdown() {
        // Save all minion data
        for (MinionData minion : playerMinions.values()) {
            saveMinionData(minion);
        }
        playerMinions.clear();
    }

    /**
     * Minion type enumeration
     */
    public enum MinionType {
        COBBLESTONE("Cobblestone", Material.COBBLESTONE, 1),
        STONE("Stone", Material.STONE, 1),
        COAL("Coal", Material.COAL_ORE, 2),
        IRON("Iron", Material.IRON_ORE, 3),
        GOLD("Gold", Material.GOLD_ORE, 4),
        DIAMOND("Diamond", Material.DIAMOND_ORE, 5),
        EMERALD("Emerald", Material.EMERALD_ORE, 6),
        REDSTONE("Redstone", Material.REDSTONE_ORE, 3),
        LAPIS("Lapis Lazuli", Material.LAPIS_ORE, 4),
        OBSIDIAN("Obsidian", Material.OBSIDIAN, 7),
        SAND("Sand", Material.SAND, 1),
        GRAVEL("Gravel", Material.GRAVEL, 1),
        ICE("Ice", Material.ICE, 2),
        NETHER_QUARTZ("Nether Quartz", Material.QUARTZ_ORE, 3),
        ENDERMAN("Enderman", Material.ENDER_PEARL, 8),
        BLAZE("Blaze", Material.BLAZE_POWDER, 9),
        SKELETON("Skeleton", Material.BONE, 5),
        ZOMBIE("Zombie", Material.ROTTEN_FLESH, 4),
        SPIDER("Spider", Material.STRING, 4),
        CACTUS("Cactus", Material.CACTUS, 1),
        SUGAR_CANE("Sugar Cane", Material.SUGAR_CANE, 1),
        WHEAT("Wheat", Material.WHEAT, 1),
        CARROT("Carrot", Material.CARROT, 1),
        POTATO("Potato", Material.POTATO, 1),
        MELON("Melon", Material.MELON, 1),
        PUMPKIN("Pumpkin", Material.PUMPKIN, 1),
        MUSHROOM("Mushroom", Material.RED_MUSHROOM, 2),
        COCOA("Cocoa", Material.INK_SACK, 2),
        CHICKEN("Chicken", Material.RAW_CHICKEN, 2),
        PIG("Pig", Material.PORK, 2),
        COW("Cow", Material.RAW_BEEF, 2),
        SHEEP("Sheep", Material.WOOL, 2),
        RABBIT("Rabbit", Material.RABBIT, 3),
        BAT("Bat", Material.ENDER_PEARL, 6),
        MAGMA_CUBE("Magma Cube", Material.MAGMA_CREAM, 7);

        private final String displayName;
        private final Material resource;
        private final int baseAmount;

        MinionType(String displayName, Material resource, int baseAmount) {
            this.displayName = displayName;
            this.resource = resource;
            this.baseAmount = baseAmount;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getResource() {
            return resource;
        }

        public int getBaseAmount() {
            return baseAmount;
        }
    }
}
