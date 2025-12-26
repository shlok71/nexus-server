package com.nexus.skyblock.treasure;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hypixel-style Treasure system for NexusBlock SkyBlock
 * Handles treasure chest spawning, rewards, and tracking
 */
public class TreasureManager {

    private final NexusCore plugin;
    private final Map<UUID, TreasureChest> activeChests;
    private final Map<UUID, PlayerTreasureData> playerData;
    private final List<TreasureTemplate> treasureTemplates;
    private final Random random;

    public TreasureManager(NexusCore plugin) {
        this.plugin = plugin;
        this.activeChests = new ConcurrentHashMap<>();
        this.playerData = new ConcurrentHashMap<>();
        this.treasureTemplates = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * Initialize the treasure system
     */
    public void initialize() {
        // Register treasure templates
        registerTreasureTemplates();

        // Start treasure spawner
        startTreasureSpawner();

        // Start chest expiry checker
        startExpiryChecker();

        plugin.getNexusLogger().info("TreasureManager initialized with " + treasureTemplates.size() + " treasure types");
    }

    /**
     * Register treasure templates
     */
    private void registerTreasureTemplates() {
        // Common Treasure
        treasureTemplates.add(new TreasureTemplate(
            "common",
            "Wood Chest",
            Material.WOOD_CHEST,
            100, // Weight
            300, // Seconds until despawn
            createCommonRewards(),
            10, // Min items
            20, // Max items
            ChatColor.WHITE
        ));

        // Uncommon Treasure
        treasureTemplates.add(new TreasureTemplate(
            "uncommon",
            "Iron Chest",
            Material.IRON_CHEST,
            60,
            240,
            createUncommonRewards(),
            15,
            25,
            ChatColor.GREEN
        ));

        // Rare Treasure
        treasureTemplates.add(new TreasureTemplate(
            "rare",
            "Gold Chest",
            Material.GOLD_CHEST,
            30,
            180,
            createRareRewards(),
            20,
            35,
            ChatColor.AQUA
        ));

        // Epic Treasure
        treasureTemplates.add(new TreasureTemplate(
            "epic",
            "Diamond Chest",
            Material.DIAMOND_BLOCK,
            10,
            120,
            createEpicRewards(),
            25,
            40,
            ChatColor.DARK_PURPLE
        ));

        // Legendary Treasure
        treasureTemplates.add(new TreasureTemplate(
            "legendary",
            "Ender Chest",
            Material.ENDER_CHEST,
            3,
            60,
            createLegendaryRewards(),
            30,
            50,
            ChatColor.GOLD
        ));
    }

    /**
     * Create common rewards pool
     */
    private List<TreasureItem> createCommonRewards() {
        List<TreasureItem> rewards = new ArrayList<>();

        // Coins
        rewards.add(new TreasureItem(Material.GOLD_INGOT, "Coins", 50, 200, 100));
        rewards.add(new TreasureItem(Material.GOLD_NUGGET, "Gold Nuggets", 10, 30, 80));

        // Ores
        rewards.add(new TreasureItem(Material.COAL, "Coal", 5, 16, 70));
        rewards.add(new TreasureItem(Material.IRON_INGOT, "Iron Ingot", 2, 8, 50));
        rewards.add(new TreasureItem(Material.LAPIS_ORE, "Lapis", 2, 8, 40));

        // Materials
        rewards.add(new TreasureItem(Material.COBBLESTONE, "Cobblestone", 32, 64, 60));
        rewards.add(new TreasureItem(Material.DIRT, "Dirt", 16, 32, 50));
        rewards.add(new TreasureItem(Material.STONE, "Stone", 16, 32, 45));

        // Food
        rewards.add(new TreasureItem(Material.BREAD, "Bread", 4, 12, 40));
        rewards.add(new TreasureItem(Material.APPLE, "Apples", 4, 12, 35));

        // Utility
        rewards.add(new TreasureItem(Material.TORCH, "Torches", 16, 32, 40));
        rewards.add(new TreasureItem(Material.ARROW, "Arrows", 16, 32, 35));

        return rewards;
    }

    /**
     * Create uncommon rewards pool
     */
    private List<TreasureItem> createUncommonRewards() {
        List<TreasureItem> rewards = new ArrayList<>();

        // Coins
        rewards.add(new TreasureItem(Material.GOLD_INGOT, "Coins", 200, 500, 100));
        rewards.add(new TreasureItem(Material.GOLD_BLOCK, "Gold Block", 1, 2, 30));

        // Ores
        rewards.add(new TreasureItem(Material.IRON_INGOT, "Iron Ingot", 8, 16, 70));
        rewards.add(new TreasureItem(Material.GOLD_INGOT, "Gold Ingot", 4, 12, 60));
        rewards.add(new TreasureItem(Material.REDSTONE, "Redstone", 8, 16, 50));
        rewards.add(new TreasureItem(Material.LAPIS_BLOCK, "Lapis Block", 1, 4, 40));

        // Gems
        rewards.add(new TreasureItem(Material.DIAMOND, "Diamond", 1, 3, 30));

        // Materials
        rewards.add(new TreasureItem(Material.OBSIDIAN, "Obsidian", 4, 8, 40));
        rewards.add(new TreasureItem(Material.EMERALD, "Emerald", 1, 4, 35));

        // Tools
        rewards.add(new TreasureItem(Material.IRON_PICKAXE, "Iron Pickaxe", 1, 1, 20));
        rewards.add(new TreasureItem(Material.IRON_SWORD, "Iron Sword", 1, 1, 20));

        return rewards;
    }

    /**
     * Create rare rewards pool
     */
    private List<TreasureItem> createRareRewards() {
        List<TreasureItem> rewards = new ArrayList<>();

        // Coins
        rewards.add(new TreasureItem(Material.GOLD_BLOCK, "Gold Block", 2, 8, 100));
        rewards.add(new TreasureItem(Material.EMERALD, "Emerald", 4, 12, 80));

        // Diamonds
        rewards.add(new TreasureItem(Material.DIAMOND, "Diamond", 3, 8, 70));
        rewards.add(new TreasureItem(Material.DIAMOND_BLOCK, "Diamond Block", 1, 2, 40));

        // Enchanting
        rewards.add(new TreasureItem(Material.ENCHANTED_BOOK, "Enchanted Book", 1, 2, 50));
        rewards.add(new TreasureItem(Material.EXP_BOTTLE, "Experience", 16, 32, 60));

        // Rare items
        rewards.add(new TreasureItem(Material.GOLDEN_APPLE, "Golden Apple", 2, 6, 50));
        rewards.add(new TreasureItem(Material.NETHER_STAR, "Nether Star", 1, 1, 15));

        // Armor
        rewards.add(new TreasureItem(Material.DIAMOND_CHESTPLATE, "Diamond Chestplate", 1, 1, 25));
        rewards.add(new TreasureItem(Material.DIAMOND_LEGGINGS, "Diamond Leggings", 1, 1, 25));

        return rewards;
    }

    /**
     * Create epic rewards pool
     */
    private List<TreasureItem> createEpicRewards() {
        List<TreasureItem> rewards = new ArrayList<>();

        // Coins
        rewards.add(new TreasureItem(Material.DIAMOND_BLOCK, "Diamond Block", 2, 6, 100));
        rewards.add(new TreasureItem(Material.EMERALD_BLOCK, "Emerald Block", 1, 4, 80));

        // Diamonds
        rewards.add(new TreasureItem(Material.DIAMOND, "Diamond", 8, 16, 70));

        // Enchanting
        rewards.add(new TreasureItem(Material.ENCHANTED_BOOK, "Rare Enchanted Book", 2, 4, 60));

        // Rare items
        rewards.add(new TreasureItem(Material.GOLDEN_APPLE, "Golden Apple", 8, 16, 50));
        rewards.add(new TreasureItem(Material.NETHER_STAR, "Nether Star", 1, 3, 40));

        // Armor
        rewards.add(new TreasureItem(Material.DIAMOND_CHESTPLATE, "Diamond Chestplate", 1, 1, 35));
        rewards.add(new TreasureItem(Material.DIAMOND_LEGGINGS, "Diamond Leggings", 1, 1, 35));
        rewards.add(new TreasureItem(Material.DIAMOND_BOOTS, "Diamond Boots", 1, 1, 30));

        // Tools
        rewards.add(new TreasureItem(Material.DIAMOND_PICKAXE, "Diamond Pickaxe", 1, 1, 30));
        rewards.add(new TreasureItem(Material.DIAMOND_SWORD, "Diamond Sword", 1, 1, 30));

        return rewards;
    }

    /**
     * Create legendary rewards pool
     */
    private List<TreasureItem> createLegendaryRewards() {
        List<TreasureItem> rewards = new ArrayList<>();

        // Ultimate rewards
        rewards.add(new TreasureItem(Material.DIAMOND_BLOCK, "Diamond Block", 8, 16, 100));
        rewards.add(new TreasureItem(Material.EMERALD_BLOCK, "Emerald Block", 4, 8, 80));
        rewards.add(new TreasureItem(Material.NETHER_STAR, "Nether Star", 2, 6, 60));

        // Best gear
        rewards.add(new TreasureItem(Material.DIAMOND_CHESTPLATE, "Diamond Chestplate", 1, 1, 50));
        rewards.add(new TreasureItem(Material.DIAMOND_LEGGINGS, "Diamond Leggings", 1, 1, 50));
        rewards.add(new TreasureItem(Material.DIAMOND_BOOTS, "Diamond Boots", 1, 1, 45));
        rewards.add(new TreasureItem(Material.DIAMOND_HELMET, "Diamond Helmet", 1, 1, 45));

        // Ultimate tools
        rewards.add(new TreasureItem(Material.DIAMOND_PICKAXE, "Diamond Pickaxe", 1, 1, 40));
        rewards.add(new TreasureItem(Material.DIAMOND_SWORD, "Diamond Sword", 1, 1, 40));

        // Enchanting
        rewards.add(new TreasureItem(Material.ENCHANTED_BOOK, "Epic Enchanted Book", 3, 6, 60));
        rewards.add(new TreasureItem(Material.EXPERIENCE_BOTTLE, "Experience Bottle", 64, 64, 50));

        return rewards;
    }

    /**
     * Start treasure spawner task
     */
    private void startTreasureSpawner() {
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnRandomTreasure();
            }
        }.runTaskTimer(plugin, 6000L, 12000L); // Every 5-10 minutes
    }

    /**
     * Start expiry checker task
     */
    private void startExpiryChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkExpiringChests();
            }
        }.runTaskTimer(plugin, 200L, 1000L); // Every 50 seconds
    }

    /**
     * Spawn a random treasure chest
     */
    private void spawnRandomTreasure() {
        // Select random treasure type based on weight
        int totalWeight = treasureTemplates.stream()
            .mapToInt(TreasureTemplate::getWeight)
            .sum();

        int randomWeight = random.nextInt(totalWeight) + 1;
        int cumulativeWeight = 0;

        TreasureTemplate selectedTemplate = null;
        for (TreasureTemplate template : treasureTemplates) {
            cumulativeWeight += template.getWeight();
            if (randomWeight <= cumulativeWeight) {
                selectedTemplate = template;
                break;
            }
        }

        if (selectedTemplate == null) return;

        // Select random location in skyblock world
        Location spawnLocation = getRandomSpawnLocation();
        if (spawnLocation == null) return;

        // Create and spawn treasure chest
        createTreasureChest(selectedTemplate, spawnLocation);
    }

    /**
     * Get random spawn location
     */
    private Location getRandomSpawnLocation() {
        org.bukkit.World skyblockWorld = Bukkit.getWorld("skyblock");
        if (skyblockWorld == null) return null;

        // Spawn in random locations around the hub area
        int x = random.nextInt(400) - 200;
        int z = random.nextInt(400) - 200;
        int y = skyblockWorld.getHighestBlockYAt(x, z) + 2;

        return new Location(skyblockWorld, x, y, z);
    }

    /**
     * Create a treasure chest at location
     */
    private void createTreasureChest(TreasureTemplate template, Location location) {
        if (location.getWorld() == null) return;

        UUID chestId = UUID.randomUUID();

        // Create visual entity (armor stand with chest)
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(
            location,
            EntityType.ARMOR_STAND
        );

        // Configure armor stand
        armorStand.setVisible(false);
        armorStand.setSmall(false);
        armorStand.setBasePlate(true);
        armorStand.setGravity(false);
        armorStand.setCustomName(template.getColor() + template.getName());
        armorStand.setCustomNameVisible(true);
        armorStand.setRemoveWhenFarAway(false);

        // Set chest on head
        ItemStack chest = new ItemStack(template.getMaterial(), 1);
        armorStand.setHelmet(chest);

        // Create treasure data
        TreasureChest chestData = new TreasureChest(
            chestId,
            template,
            location,
            armorStand.getUniqueId(),
            System.currentTimeMillis()
        );

        // Generate rewards
        chestData.setRewards(generateRewards(template));

        // Store
        activeChests.put(chestId, chestData);

        // Play spawn sound
        location.getWorld().playSound(location, Sound.CHEST_OPEN, 1.0f, 1.0f);

        // Notify nearby players
        notifyNearbyPlayers(location, template);

        plugin.getNexusLogger().info("Spawned " + template.getName() + " at " +
            location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    /**
     * Generate random rewards for treasure
     */
    private List<ItemStack> generateRewards(TreasureTemplate template) {
        List<ItemStack> rewards = new ArrayList<>();
        int itemCount = random.nextInt(
            template.getMaxItems() - template.getMinItems() + 1
        ) + template.getMinItems();

        for (int i = 0; i < itemCount; i++) {
            TreasureItem treasureItem = selectRandomReward(template.getRewards());
            if (treasureItem != null) {
                ItemStack item = new ItemStack(
                    treasureItem.getMaterial(),
                    random.nextInt(treasureItem.getMaxAmount() - treasureItem.getMinAmount() + 1) +
                    treasureItem.getMinAmount()
                );
                rewards.add(item);
            }
        }

        // Always add some coins
        ItemStack coins = new ItemStack(Material.GOLD_INGOT, random.nextInt(100) + 50);
        rewards.add(coins);

        return rewards;
    }

    /**
     * Select random reward based on weight
     */
    private TreasureItem selectRandomReward(List<TreasureItem> rewards) {
        int totalWeight = rewards.stream().mapToInt(TreasureItem::getWeight).sum();
        int randomWeight = random.nextInt(totalWeight) + 1;

        int cumulativeWeight = 0;
        for (TreasureItem item : rewards) {
            cumulativeWeight += item.getWeight();
            if (randomWeight <= cumulativeWeight) {
                return item;
            }
        }

        return rewards.get(0);
    }

    /**
     * Notify nearby players of treasure spawn
     */
    private void notifyNearbyPlayers(Location location, TreasureTemplate template) {
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) < 50) {
                player.sendMessage("");
                player.sendMessage(template.getColor() + "★ A " + template.getName() + " appeared nearby!");
                player.sendMessage("");
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
            }
        }
    }

    /**
     * Player opens a treasure chest
     */
    public boolean openTreasure(Player player, UUID chestId) {
        TreasureChest chest = activeChests.get(chestId);
        if (chest == null) {
            player.sendMessage(ChatColor.RED + "This treasure chest is no longer available!");
            return false;
        }

        // Check distance
        if (player.getLocation().distance(chest.getLocation()) > 5) {
            player.sendMessage(ChatColor.RED + "Get closer to open this treasure!");
            return false;
        }

        // Give rewards
        List<ItemStack> rewards = chest.getRewards();
        for (ItemStack item : rewards) {
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            for (ItemStack dropped : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), dropped);
            }
        }

        // Update player stats
        PlayerTreasureData data = playerData.computeIfAbsent(
            player.getUniqueId(),
            k -> new PlayerTreasureData(player.getUniqueId())
        );
        data.addTreasureOpened();
        data.addTotalRewards(rewards.size());

        // Remove chest
        removeTreasureChest(chest);

        // Notify player
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "★ TREASURE OPENED! ★");
        player.sendMessage(ChatColor.GRAY + "You found:");
        for (ItemStack item : rewards.subList(0, Math.min(5, rewards.size()))) {
            player.sendMessage(ChatColor.WHITE + " - " + item.getAmount() + " " + item.getType().name());
        }
        if (rewards.size() > 5) {
            player.sendMessage(ChatColor.GRAY + "  ... and " + (rewards.size() - 5) + " more items!");
        }
        player.sendMessage("");

        // Play sound
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);

        return true;
    }

    /**
     * Remove a treasure chest
     */
    private void removeTreasureChest(TreasureChest chest) {
        // Remove entity
        if (chest.getEntityId() != null) {
            Entity entity = Bukkit.getEntity(chest.getEntityId());
            if (entity != null) {
                entity.remove();
            }
        }

        // Remove from map
        activeChests.remove(chest.getId());
    }

    /**
     * Check and remove expired chests
     */
    private void checkExpiringChests() {
        long currentTime = System.currentTimeMillis();

        Iterator<Map.Entry<UUID, TreasureChest>> iterator = activeChests.entrySet().iterator();
        while (iterator.hasNext()) {
            TreasureChest chest = iterator.next().getValue();
            long age = currentTime - chest.getSpawnTime();
            long maxAge = chest.getTemplate().getDespawnTime() * 1000;

            if (age >= maxAge) {
                // Remove chest
                removeTreasureChest(chest);

                // Play despawn sound
                chest.getLocation().getWorld().playSound(
                    chest.getLocation(),
                    Sound.CHEST_CLOSE,
                    1.0f,
                    0.8f
                );
            }
        }
    }

    /**
     * Get treasure by entity ID
     */
    public TreasureChest getTreasureByEntity(UUID entityId) {
        for (TreasureChest chest : activeChests.values()) {
            if (chest.getEntityId().equals(entityId)) {
                return chest;
            }
        }
        return null;
    }

    /**
     * Treasure template class
     */
    public static class TreasureTemplate {
        private final String id;
        private final String name;
        private final Material material;
        private final int weight;
        private final int despawnTime;
        private final List<TreasureItem> rewards;
        private final int minItems;
        private final int maxItems;
        private final ChatColor color;

        public TreasureTemplate(String id, String name, Material material, int weight,
                               int despawnTime, List<TreasureItem> rewards,
                               int minItems, int maxItems, ChatColor color) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.weight = weight;
            this.despawnTime = despawnTime;
            this.rewards = rewards;
            this.minItems = minItems;
            this.maxItems = maxItems;
            this.color = color;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Material getMaterial() { return material; }
        public int getWeight() { return weight; }
        public int getDespawnTime() { return despawnTime; }
        public List<TreasureItem> getRewards() { return rewards; }
        public int getMinItems() { return minItems; }
        public int getMaxItems() { return maxItems; }
        public ChatColor getColor() { return color; }
    }

    /**
     * Treasure item class
     */
    public static class TreasureItem {
        private final Material material;
        private final String name;
        private final int minAmount;
        private final int maxAmount;
        private final int weight;

        public TreasureItem(Material material, String name, int minAmount,
                           int maxAmount, int weight) {
            this.material = material;
            this.name = name;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.weight = weight;
        }

        public Material getMaterial() { return material; }
        public String getName() { return name; }
        public int getMinAmount() { return minAmount; }
        public int getMaxAmount() { return maxAmount; }
        public int getWeight() { return weight; }
    }

    /**
     * Treasure chest data class
     */
    public static class TreasureChest {
        private final UUID id;
        private final TreasureTemplate template;
        private final Location location;
        private final UUID entityId;
        private final long spawnTime;
        private List<ItemStack> rewards;

        public TreasureChest(UUID id, TreasureTemplate template, Location location,
                            UUID entityId, long spawnTime) {
            this.id = id;
            this.template = template;
            this.location = location;
            this.entityId = entityId;
            this.spawnTime = spawnTime;
        }

        public UUID getId() { return id; }
        public TreasureTemplate getTemplate() { return template; }
        public Location getLocation() { return location; }
        public UUID getEntityId() { return entityId; }
        public long getSpawnTime() { return spawnTime; }
        public List<ItemStack> getRewards() { return rewards; }
        public void setRewards(List<ItemStack> rewards) { this.rewards = rewards; }
    }

    /**
     * Player treasure data class
     */
    public static class PlayerTreasureData {
        private final UUID playerId;
        private int treasuresOpened;
        private int totalRewards;

        public PlayerTreasureData(UUID playerId) {
            this.playerId = playerId;
            this.treasuresOpened = 0;
            this.totalRewards = 0;
        }

        public void addTreasureOpened() { this.treasuresOpened++; }
        public void addTotalRewards(int amount) { this.totalRewards += amount; }

        public UUID getPlayerId() { return playerId; }
        public int getTreasuresOpened() { return treasuresOpened; }
        public int getTotalRewards() { return totalRewards; }
    }
}
