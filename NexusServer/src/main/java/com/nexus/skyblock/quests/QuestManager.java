package com.nexus.skyblock.quests;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuestAdvanceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hypixel-style Quest system for NexusBlock SkyBlock
 * Handles quest tracking, progression, and rewards
 */
public class QuestManager implements Listener {

    private final NexusCore plugin;
    private final Map<String, Quest> quests;
    private final Map<UUID, PlayerQuestData> playerQuests;
    private final Map<UUID, Set<String>> completedQuests;
    private final Random random;

    public QuestManager(NexusCore plugin) {
        this.plugin = plugin;
        this.quests = new ConcurrentHashMap<>();
        this.playerQuests = new ConcurrentHashMap<>();
        this.completedQuests = new ConcurrentHashMap<>();
        this.random = new Random();
    }

    /**
     * Initialize the quest system
     */
    public void initialize() {
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Register default quests
        registerDefaultQuests();

        plugin.getNexusLogger().info("QuestManager initialized with " + quests.size() + " quests");
    }

    /**
     * Register default SkyBlock quests
     */
    private void registerDefaultQuests() {
        // Starter quests
        registerQuest(new Quest(
            "welcome_to_skyblock",
            "Welcome to SkyBlock!",
            QuestType.STORY,
            1,
            "Talk to the SkyBlock Guide",
            "Speak to the Guide NPC in the Hub to get started.",
            createQuestItem(Material.BOOK, "Guide's Handbook"),
            0,
            createReward(100, 0, 5)
        ));

        registerQuest(new Quest(
            "first_steps",
            "First Steps",
            QuestType.STORY,
            2,
            "Create your SkyBlock Island",
            "Use /island create to start your own island adventure.",
            createQuestItem(Material.GRASS, "Island Creation"),
            0,
            createReward(250, 0, 10)
        ));

        // Mining quests
        registerQuest(new Quest(
            "cobble_master",
            "Cobble Master",
            QuestType.MINE,
            3,
            "Mine 100 Cobblestone",
            "Gather resources by mining cobblestone from your generator.",
            createQuestItem(Material.COBBLESTONE, "Cobblestone Miner"),
            100,
            createReward(500, 0, 15)
        ));

        registerQuest(new Quest(
            "stone_age",
            "Stone Age",
            QuestType.MINE,
            5,
            "Mine 500 Stone",
            "Upgrade your generator and mine some stone.",
            createQuestItem(Material.STONE, "Stone Gatherer"),
            500,
            createReward(1000, 0, 20)
        ));

        registerQuest(new Quest(
            "coal_rush",
            "Coal Rush",
            QuestType.MINE,
            7,
            "Mine 200 Coal Ore",
            "Find coal ore and start your journey to the mines.",
            createQuestItem(Material.COAL, "Coal Miner"),
            200,
            createReward(2000, 0, 25)
        ));

        registerQuest(new Quest(
            "iron_man",
            "Iron Man",
            QuestType.MINE,
            10,
            "Mine 300 Iron Ore",
            "Gather iron for armor and tools.",
            createQuestItem(Material.IRON_INGOT, "Iron Hunter"),
            300,
            createReward(4000, 0, 30)
        ));

        // Farming quests
        registerQuest(new Quest(
            "green_thumb",
            "Green Thumb",
            QuestType.FARM,
            3,
            "Harvest 64 Wheat",
            "Start farming on your island.",
            createQuestItem(Material.WHEAT, "Wheat Farmer"),
            64,
            createReward(500, 0, 15)
        ));

        registerQuest(new Quest(
            "harvest_moon",
            "Harvest Moon",
            QuestType.FARM,
            7,
            "Harvest 256 Wheat",
            "Expand your farm and become a master farmer.",
            createQuestItem(Material.HAY_BLOCK, "Master Farmer"),
            256,
            createReward(2000, 0, 25)
        ));

        // Combat quests
        registerQuest(new Quest(
            "first_blood",
            "First Blood",
            QuestType.COMBAT,
            5,
            "Kill 10 Zombies",
            "Defend your island from the undead!",
            createQuestItem(Material.ROTTEN_FLESH, "Zombie Slayer"),
            10,
            createReward(750, 0, 20)
        ));

        registerQuest(new Quest(
            "monster_hunter",
            "Monster Hunter",
            QuestType.COMBAT,
            10,
            "Kill 50 Mobs",
            "Clear your island of all threats.",
            createQuestItem(Material.SKULL_ITEM, "Monster Hunter"),
            50,
            createReward(3000, 0, 35)
        ));

        // Collection quests
        registerQuest(new Quest(
            "collector",
            "The Collector",
            QuestType.COLLECTION,
            5,
            "Collect 10 Different Materials",
            "Gather various materials for your collection.",
            createQuestItem(Material.CHEST, "Material Collector"),
            10,
            createReward(1000, 0, 20)
        ));

        registerQuest(new Quest(
            "treasure_hunter",
            "Treasure Hunter",
            QuestType.EXPLORATION,
            10,
            "Find 5 Treasure Chests",
            "Search for treasure chests around the islands.",
            createQuestItem(Material.ENDER_CHEST, "Treasure Seeker"),
            5,
            createReward(5000, 0, 40)
        ));
    }

    /**
     * Register a quest
     */
    public void registerQuest(Quest quest) {
        quests.put(quest.getId(), quest);
    }

    /**
     * Create a quest item for tracking
     */
    private ItemStack createQuestItem(Material material, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name);
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Quest Item"));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a quest reward
     */
    private QuestReward createReward(int coins, int gems, int xp) {
        return new QuestReward(coins, gems, xp);
    }

    /**
     * Start a quest for a player
     */
    public boolean startQuest(Player player, String questId) {
        UUID playerId = player.getUniqueId();

        // Check if quest exists
        Quest quest = quests.get(questId);
        if (quest == null) {
            player.sendMessage(ChatColor.RED + "Quest not found!");
            return false;
        }

        // Check if already completed
        Set<String> completed = completedQuests.get(playerId);
        if (completed != null && completed.contains(questId)) {
            player.sendMessage(ChatColor.RED + "You've already completed this quest!");
            return false;
        }

        // Check if already in progress
        PlayerQuestData data = playerQuests.get(playerId);
        if (data != null && data.hasQuest(questId)) {
            player.sendMessage(ChatColor.RED + "You're already on this quest!");
            return false;
        }

        // Initialize quest data if needed
        if (data == null) {
            data = new PlayerQuestData(playerId);
            playerQuests.put(playerId, data);
        }

        // Start quest
        data.startQuest(quest);
        player.sendMessage(ChatColor.GREEN + "Quest Started: " + ChatColor.WHITE + quest.getName());
        player.sendMessage(ChatColor.GRAY + quest.getDescription());

        return true;
    }

    /**
     * Progress a quest for a player
     */
    public void progressQuest(Player player, String questId, int amount) {
        UUID playerId = player.getUniqueId();
        PlayerQuestData data = playerQuests.get(playerId);

        if (data == null || !data.hasQuest(questId)) return;

        QuestProgress progress = data.getProgress(questId);
        Quest quest = quests.get(questId);

        if (progress.isComplete()) return;

        progress.addProgress(amount);

        // Check if complete
        if (progress.isComplete()) {
            completeQuest(player, quest);
        } else {
            // Update progress display
            updateQuestProgress(player, quest, progress);
        }

        savePlayerQuestData(data);
    }

    /**
     * Complete a quest
     */
    private void completeQuest(Player player, Quest quest) {
        UUID playerId = player.getUniqueId();
        PlayerQuestData data = playerQuests.get(playerId);

        if (data == null) return;

        data.completeQuest(quest.getId());

        // Track completion
        Set<String> completed = completedQuests.computeIfAbsent(playerId, k -> new HashSet<>());
        completed.add(quest.getId());

        // Award rewards
        QuestReward reward = quest.getReward();
        if (reward.getCoins() > 0) {
            plugin.getEconomyManager().addCoins(playerId, reward.getCoins());
        }
        if (reward.getGems() > 0) {
            plugin.getEconomyManager().addGems(playerId, reward.getGems());
        }

        // Give XP (could be skill XP in full implementation)
        // AwardBits(player, reward.getBits());

        // Notify player
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + " Quest Completed: " + ChatColor.GOLD + quest.getName());
        player.sendMessage(ChatColor.GRAY + " Rewards: " +
            ChatColor.GOLD + reward.getCoins() + " Coins" +
            ChatColor.GRAY + ", " + ChatColor.LIGHT_PURPLE + reward.getGems() + " Gems");
        player.sendMessage("");

        // Play sound
        player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.0f);

        // Check for quest chain
        checkQuestChain(player, quest);
    }

    /**
     * Update player with quest progress
     */
    private void updateQuestProgress(Player player, Quest quest, QuestProgress progress) {
        int current = progress.getCurrentProgress();
        int target = quest.getTargetAmount();
        int percent = (int) ((current / (double) target) * 100);

        player.sendMessage(ChatColor.YELLOW + quest.getName() + ": " +
            ChatColor.GREEN + current + "/" + target +
            ChatColor.GRAY + " (" + percent + "%)");
    }

    /**
     * Check for quest chain completion
     */
    private void checkQuestChain(Player player, Quest quest) {
        // In full implementation, unlock next quest in chain
    }

    /**
     * Open quest GUI for player
     */
    public void openQuestGUI(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerQuestData data = playerQuests.get(playerId);
        Set<String> completed = completedQuests.getOrDefault(playerId, new HashSet<>());

        org.bukkit.inventory.Inventory gui = Bukkit.createInventory(
            player,
            54,
            ChatColor.DARK_PURPLE + "Quest Log"
        );

        int slot = 0;

        for (Quest quest : quests.values()) {
            if (slot >= 54) break;

            // Skip completed quests
            if (completed.contains(quest.getId())) continue;

            ItemStack icon = quest.getIcon().clone();
            ItemMeta meta = icon.getItemMeta();

            // Check if quest is in progress
            boolean inProgress = data != null && data.hasQuest(quest.getId());
            QuestProgress progress = inProgress ? data.getProgress(quest.getId()) : null;

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + quest.getDescription());
            lore.add("");

            if (inProgress && progress != null) {
                lore.add(ChatColor.YELLOW + "Progress: " +
                    ChatColor.GREEN + progress.getCurrentProgress() + "/" + quest.getTargetAmount());
            } else {
                lore.add(ChatColor.YELLOW + "Target: " + quest.getTargetAmount() + " " + quest.getTask());
            }

            lore.add("");
            lore.add(ChatColor.GREEN + "Reward: " + quest.getReward().getCoins() + " Coins");

            meta.setLore(lore);
            icon.setItemMeta(meta);

            gui.setItem(slot, icon);
            slot++;
        }

        player.openInventory(gui);
    }

    /**
     * Get active quests for a player
     */
    public List<Quest> getActiveQuests(Player player) {
        PlayerQuestData data = playerQuests.get(player.getUniqueId());
        if (data == null) return Collections.emptyList();

        List<Quest> activeQuests = new ArrayList<>();
        for (String questId : data.getActiveQuests()) {
            Quest quest = quests.get(questId);
            if (quest != null) {
                activeQuests.add(quest);
            }
        }
        return activeQuests;
    }

    /**
     * Save player quest data
     */
    private void savePlayerQuestData(PlayerQuestData data) {
        // In full implementation, save to database
    }

    /**
     * Load player quest data
     */
    public void loadPlayerQuestData(UUID playerId) {
        // In full implementation, load from database
    }

    /**
     * Quest class
     */
    public static class Quest {
        private final String id;
        private final String name;
        private final QuestType type;
        private final int difficulty;
        private final String task;
        private final String description;
        private final ItemStack icon;
        private final int targetAmount;
        private final QuestReward reward;

        public Quest(String id, String name, QuestType type, int difficulty,
                     String task, String description, ItemStack icon,
                     int targetAmount, QuestReward reward) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.difficulty = difficulty;
            this.task = task;
            this.description = description;
            this.icon = icon;
            this.targetAmount = targetAmount;
            this.reward = reward;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public QuestType getType() { return type; }
        public int getDifficulty() { return difficulty; }
        public String getTask() { return task; }
        public String getDescription() { return description; }
        public ItemStack getIcon() { return icon; }
        public int getTargetAmount() { return targetAmount; }
        public QuestReward getReward() { return reward; }
    }

    /**
     * Quest type enumeration
     */
    public enum QuestType {
        STORY,
        MINE,
        FARM,
        COMBAT,
        EXPLORATION,
        COLLECTION
    }

    /**
     * Quest reward class
     */
    public static class QuestReward {
        private final int coins;
        private final int gems;
        private final int bits;

        public QuestReward(int coins, int gems, int bits) {
            this.coins = coins;
            this.gems = gems;
            this.bits = bits;
        }

        public int getCoins() { return coins; }
        public int getGems() { return gems; }
        public int getBits() { return bits; }
    }

    /**
     * Player quest progress data
     */
    public static class PlayerQuestData {
        private final UUID playerId;
        private final Map<String, QuestProgress> activeQuests;
        private final Set<String> completedQuests;

        public PlayerQuestData(UUID playerId) {
            this.playerId = playerId;
            this.activeQuests = new ConcurrentHashMap<>();
            this.completedQuests = ConcurrentHashMap.newKeySet();
        }

        public void startQuest(Quest quest) {
            activeQuests.put(quest.getId(), new QuestProgress(quest));
        }

        public void completeQuest(String questId) {
            activeQuests.remove(questId);
            completedQuests.add(questId);
        }

        public boolean hasQuest(String questId) {
            return activeQuests.containsKey(questId);
        }

        public QuestProgress getProgress(String questId) {
            return activeQuests.get(questId);
        }

        public Set<String> getActiveQuests() {
            return activeQuests.keySet();
        }

        public Set<String> getCompletedQuests() {
            return completedQuests;
        }
    }

    /**
     * Quest progress tracking
     */
    public static class QuestProgress {
        private final Quest quest;
        private int currentProgress;

        public QuestProgress(Quest quest) {
            this.quest = quest;
            this.currentProgress = 0;
        }

        public void addProgress(int amount) {
            currentProgress = Math.min(currentProgress + amount, quest.getTargetAmount());
        }

        public boolean isComplete() {
            return currentProgress >= quest.getTargetAmount();
        }

        public int getCurrentProgress() {
            return currentProgress;
        }

        public Quest getQuest() {
            return quest;
        }
    }
}
