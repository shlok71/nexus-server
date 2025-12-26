package com.nexus.skyblock.hotm;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Hypixel-style Heart of the Mountain tree for NexusBlock SkyBlock
 * Manages the mining skill tree with perks and upgrades
 */
public class HotMManager {

    private final NexusCore plugin;
    private final Map<String, HotMPerk> perks;
    private final Map<UUID, PlayerHotMData> playerData;
    private final List<HotMTier> tiers;

    public HotMManager(NexusCore plugin) {
        this.plugin = plugin;
        this.perks = new ConcurrentHashMap<>();
        this.playerData = new ConcurrentHashMap<>();
        this.tiers = new ArrayList<>();
    }

    /**
     * Initialize the HotM system
     */
    public void initialize() {
        // Register perks
        registerPerks();

        // Define tiers
        defineTiers();

        plugin.getNexusLogger().info("HotMManager initialized with " + perks.size() + " perks");
    }

    /**
     * Register all HotM perks
     */
    private void registerPerks() {
        // Tier 1 Perks
        registerPerk(new HotMPerk(
            "mining_speed_1",
            "Mining Speed I",
            HotMTier.TIER_1,
            "Increases mining speed by 10%",
            new HashMap<>(),
            createDisplayItem(Material.GOLD_PICKAXE, "Mining Speed I")
        ));

        registerPerk(new HotMPerk(
            "efficient_miner",
            "Efficient Miner",
            HotMTier.TIER_1,
            "40% chance to get double ore drops",
            new HashMap<>(),
            createDisplayItem(Material.DIAMOND_PICKAXE, "Efficient Miner")
        ));

        registerPerk(new HotMPerk(
            "lucky_chunks",
            "Lucky Chunk",
            HotMTier.TIER_1,
            "20% chance for a vein to drop 2x items",
            new HashMap<>(),
            createDisplayItem(Material.COAL, "Lucky Chunk")
        ));

        // Tier 2 Perks
        registerPerk(new HotMPerk(
            "mining_speed_2",
            "Mining Speed II",
            HotMTier.TIER_2,
            "Increases mining speed by 15%",
            new HashMap<>(Map.of("mining_speed_1", 1)),
            createDisplayItem(Material.GOLD_PICKAXE, "Mining Speed II")
        ));

        registerPerk(new HotMPerk(
            "mining_fortune",
            "Mining Fortune",
            HotMTier.TIER_2,
            "+15% chance to get extra ore drops",
            new HashMap<>(),
            createDisplayItem(Material.GOLD_INGOT, "Mining Fortune")
        ));

        registerPerk(new HotMPerk(
            "spawner_drop",
            "Miner's Greed",
            HotMTier.TIER_2,
            "Spawners drop 3x more items",
            new HashMap<>(),
            createDisplayItem(Material.MOB_SPAWNER, "Miner's Greed")
        ));

        // Tier 3 Perks
        registerPerk(new HotMPerk(
            "mining_speed_3",
            "Mining Speed III",
            HotMTier.TIER_3,
            "Increases mining speed by 20%",
            new HashMap<>(Map.of("mining_speed_2", 1)),
            createDisplayItem(Material.GOLD_PICKAXE, "Mining Speed III")
        ));

        registerPerk(new HotMPerk(
            "crystal_infusion",
            "Crystal Infusion",
            HotMTier.TIER_3,
            "Gain a chance for crystals to drop from any block",
            new HashMap<>(Map.of("efficient_miner", 1)),
            createDisplayItem(Material.EMERALD, "Crystal Infusion")
        ));

        registerPerk(new HotMPerk(
            "giant_killer",
            "Titanium Perseverance",
            HotMTier.TIER_3,
            "Titanium plates have a chance to drop 2x",
            new HashMap<>(Map.of("lucky_chunks", 1)),
            createDisplayItem(Material.IRON_BLOCK, "Titanium Perseverance")
        ));

        // Tier 4 Perks
        registerPerk(new HotMPerk(
            "mining_speed_4",
            "Mining Speed IV",
            HotMTier.TIER_4,
            "Increases mining speed by 25%",
            new HashMap<>(Map.of("mining_speed_3", 1)),
            createDisplayItem(Material.GOLD_PICKAXE, "Mining Speed IV")
        ));

        registerPerk(new HotMPerk(
            "front_loaded",
            "Front Loaded",
            HotMTier.TIER_4,
            "Gain bonus Mining XP from every block",
            new HashMap<>(),
            createDisplayItem(Material.EXP_BOTTLE, "Front Loaded")
        ));

        registerPerk(new HotMPerk(
            "special_0",
            "Rare Metal",
            HotMTier.TIER_4,
            "Diamond and Obsidian drop rates increased",
            new HashMap<>(Map.of("mining_fortune", 1)),
            createDisplayItem(Material.DIAMOND, "Rare Metal")
        ));

        // Tier 5 Perks
        registerPerk(new HotMPerk(
            "mining_speed_5",
            "Mining Speed V",
            HotMTier.TIER_5,
            "Increases mining speed by 30%",
            new HashMap<>(Map.of("mining_speed_4", 1)),
            createDisplayItem(Material.GOLD_PICKAXE, "Mining Speed V")
        ));

        registerPerk(new HotMPerk(
            "professional",
            "Professional",
            HotMTier.TIER_5,
            "50% chance to not consume pickaxe durability",
            new HashMap<>(Map.of("mining_speed_3", 1)),
            createDisplayItem(Material.DIAMOND_PICKAXE, "Professional")
        ));

        registerPerk(new HotMPerk(
            "chimera",
            "Chimera",
            HotMTier.TIER_5,
            "Nearby players gain 10% of your Mining Fortune",
            new HashMap<>(Map.of("crystal_infusion", 1)),
            createDisplayItem(Material.QUARTZ, "Chimera")
        ));
    }

    /**
     * Define HotM tiers
     */
    private void defineTiers() {
        tiers.add(new HotMTier(1, 0, 0, 0, "Novice"));
        tiers.add(new HotMTier(2, 0, 0, 50, "Apprentice"));
        tiers.add(new HotMTier(3, 1000, 10, 150, "Journeyman"));
        tiers.add(new HotMTier(4, 2500, 25, 400, "Expert"));
        tiers.add(new HotMTier(5, 5000, 50, 750, "Master"));
        tiers.add(new HotMTier(6, 10000, 100, 1500, "Grand Master"));
        tiers.add(new HotMTier(7, 25000, 250, 3000, "Legendary"));
    }

    /**
     * Register a perk
     */
    private void registerPerk(HotMPerk perk) {
        perks.put(perk.getId(), perk);
    }

    /**
     * Create display item for a perk
     */
    private ItemStack createDisplayItem(Material material, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Open HotM GUI for player
     */
    public void openHotMInterface(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerHotMData data = playerData.computeIfAbsent(playerId, k -> new PlayerHotMData(playerId));

        Inventory gui = Bukkit.createInventory(
            player,
            54,
            ChatColor.DARK_PURPLE + "Heart of the Mountain"
        );

        // Tier display
        ItemStack tierItem = createTierDisplay(data);
        gui.setItem(4, tierItem);

        // Powder display
        ItemStack powderItem = createPowderDisplay(data);
        gui.setItem(13, powderItem);

        // Spinel Gem display
        ItemStack spinelItem = createSpinelDisplay(data);
        gui.setItem(22, spinelItem);

        // Perk slots (organized by tier)
        int perkSlot = 29;
        for (HotMTier tier : tiers) {
            if (tier.getTierNumber() > data.getCurrentTier()) continue;

            for (HotMPerk perk : getPerksByTier(tier)) {
                if (perkSlot >= 54) break;

                gui.setItem(perkSlot, createPerkItem(player, perk, data));
                perkSlot++;
            }
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW, 1);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(49, back);

        player.openInventory(gui);
    }

    /**
     * Create tier display item
     */
    private ItemStack createTierDisplay(PlayerHotMData data) {
        HotMTier tier = getTier(data.getCurrentTier());
        ItemStack item = new ItemStack(Material.DIAMOND, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Tier " + data.getCurrentTier());
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Rank: " + tier.getRankName(),
            ChatColor.GRAY + "Mining XP: " + formatNumber(data.getMiningXp()),
            "",
            ChatColor.YELLOW + "Next tier at: " + formatNumber(tier.getRequiredXp()) + " XP"
        ));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create powder display item
     */
    private ItemStack createPowderDisplay(PlayerHotMData data) {
        ItemStack item = new ItemStack(Material.PURPLE_WOOL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Mithril Powder");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Available: " + formatNumber(data.getMithrilPowder()),
            ChatColor.GRAY + "Spent: " + formatNumber(data.getMithrilPowderSpent()),
            "",
            ChatColor.YELLOW + "Use powder to unlock perks!"
        ));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create spinel display item
     */
    private ItemStack createSpinelDisplay(PlayerHotMData data) {
        ItemStack item = new ItemStack(Material.PINK_DYE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Gemstone Powder");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Available: " + formatNumber(data.getGemstonePowder()),
            "",
            ChatColor.GRAY + "Used for higher tier perks"
        ));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create perk item for GUI
     */
    private ItemStack createPerkItem(Player player, HotMPerk perk, PlayerHotMData data) {
        boolean unlocked = data.hasPerk(perk.getId());
        boolean available = isPerkAvailable(player, perk, data);

        ItemStack item = perk.getDisplayItem().clone();
        ItemMeta meta = item.getItemMeta();

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + perk.getDescription());
        lore.add("");

        if (unlocked) {
            lore.add(ChatColor.GREEN + "UNLOCKED");
        } else if (available) {
            int cost = getPerkCost(perk, data);
            lore.add(ChatColor.YELLOW + "Cost: " + cost + " Mithril Powder");
            lore.add(ChatColor.GREEN + "Click to unlock!");
        } else {
            lore.add(ChatColor.RED + "Requirements not met");
            if (!perk.getRequirements().isEmpty()) {
                lore.add(ChatColor.GRAY + "Requires:");
                for (Map.Entry<String, Integer> req : perk.getRequirements().entrySet()) {
                    HotMPerk reqPerk = perks.get(req.getKey());
                    if (reqPerk != null) {
                        boolean reqUnlocked = data.hasPerk(req.getKey());
                        String status = reqUnlocked ? ChatColor.GREEN : ChatColor.RED;
                        lore.add(status + "  - " + reqPerk.getName());
                    }
                }
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        // Apply glow effect if unlocked
        if (unlocked) {
            item = addGlow(item);
        }

        return item;
    }

    /**
     * Check if perk is available for unlock
     */
    private boolean isPerkAvailable(Player player, HotMPerk perk, PlayerHotMData data) {
        // Check requirements
        for (Map.Entry<String, Integer> req : perk.getRequirements().entrySet()) {
            if (!data.hasPerk(req.getKey())) {
                return false;
            }
        }

        // Check tier
        if (perk.getTier().getTierNumber() > data.getCurrentTier()) {
            return false;
        }

        return true;
    }

    /**
     * Get perk unlock cost
     */
    private int getPerkCost(HotMPerk perk, PlayerHotMData data) {
        int baseCost = perk.getTier().getTierNumber() * 50;
        int previousPerks = data.getUnlockedPerks().size();
        return baseCost + (previousPerks * 10);
    }

    /**
     * Unlock a perk for player
     */
    public boolean unlockPerk(Player player, String perkId) {
        UUID playerId = player.getUniqueId();
        PlayerHotMData data = playerData.computeIfAbsent(playerId, k -> new PlayerHotMData(playerId));

        HotMPerk perk = perks.get(perkId);
        if (perk == null) {
            player.sendMessage(ChatColor.RED + "Perk not found!");
            return false;
        }

        if (data.hasPerk(perkId)) {
            player.sendMessage(ChatColor.RED + "You already have this perk!");
            return false;
        }

        if (!isPerkAvailable(player, perk, data)) {
            player.sendMessage(ChatColor.RED + "You can't unlock this perk yet!");
            return false;
        }

        int cost = getPerkCost(perk, data);
        if (data.getMithrilPowder() < cost) {
            player.sendMessage(ChatColor.RED + "Not enough Mithril Powder! Need: " + cost);
            return false;
        }

        // Unlock perk
        data.setMithrilPowder(data.getMithrilPowder() - cost);
        data.addUnlockedPerk(perkId);

        player.sendMessage(ChatColor.GREEN + "Unlocked: " + perk.getName() + "!");
        player.sendMessage(ChatColor.GRAY + "Cost: " + cost + " Mithril Powder");

        // Check for bonus perks
        checkBonusPerks(player, data);

        return true;
    }

    /**
     * Check and award bonus perks
     */
    private void checkBonusPerks(Player player, PlayerHotMData data) {
        // Compact Mining - unlock at 5 perks
        if (data.getUnlockedPerks().size() >= 5 && !data.hasPerk("compact_mining")) {
            data.addUnlockedPerk("compact_mining");
            player.sendMessage(ChatColor.GOLD + "BONUS PERK: " + ChatColor.GREEN + "Compact Mining!");
        }
    }

    /**
     * Get perks by tier
     */
    private List<HotMPerk> getPerksByTier(HotMTier tier) {
        List<HotMPerk> result = new ArrayList<>();
        for (HotMPerk perk : perks.values()) {
            if (perk.getTier().equals(tier)) {
                result.add(perk);
            }
        }
        return result;
    }

    /**
     * Get tier by number
     */
    private HotMTier getTier(int tierNumber) {
        for (HotMTier tier : tiers) {
            if (tier.getTierNumber() == tierNumber) {
                return tier;
            }
        }
        return tiers.get(0);
    }

    /**
     * Format large numbers
     */
    private String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }

    /**
     * Add glow effect to item
     */
    private ItemStack addGlow(ItemStack item) {
        item.addUnsafeEnchantment(org.bukkit.Enchantment.DURABILITY, 1);
        return item;
    }

    /**
     * Add mining XP to player
     */
    public void addMiningXp(Player player, int amount) {
        UUID playerId = player.getUniqueId();
        PlayerHotMData data = playerData.computeIfAbsent(playerId, k -> new PlayerHotMData(playerId));

        // Apply multipliers from perks
        double multiplier = 1.0;
        if (data.hasPerk("front_loaded")) {
            multiplier += 0.5;
        }

        int adjustedAmount = (int) (amount * multiplier);
        data.addMiningXp(adjustedAmount);

        // Check for tier upgrade
        checkTierUpgrade(player, data);
    }

    /**
     * Check and process tier upgrade
     */
    private void checkTierUpgrade(Player player, PlayerHotMData data) {
        for (HotMTier tier : tiers) {
            if (tier.getTierNumber() > data.getCurrentTier() &&
                data.getMiningXp() >= tier.getRequiredXp()) {
                data.setCurrentTier(tier.getTierNumber());
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + " HOTM UPGRADED TO TIER " + tier.getTierNumber() + "!");
                player.sendMessage(ChatColor.GRAY + "Rank: " + tier.getRankName());
                player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.0f);
            }
        }
    }

    /**
     * Add mithril powder
     */
    public void addMithrilPowder(Player player, int amount) {
        UUID playerId = player.getUniqueId();
        PlayerHotMData data = playerData.computeIfAbsent(playerId, k -> new PlayerHotMData(playerId));
        data.setMithrilPowder(data.getMithrilPowder() + amount);
    }

    /**
     * Get player HotM data
     */
    public PlayerHotMData getPlayerData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> new PlayerHotMData(playerId));
    }

    /**
     * Calculate mining speed bonus
     */
    public double getMiningSpeedBonus(UUID playerId) {
        PlayerHotMData data = playerData.get(playerId);
        if (data == null) return 0;

        double bonus = 0;
        if (data.hasPerk("mining_speed_1")) bonus += 0.10;
        if (data.hasPerk("mining_speed_2")) bonus += 0.15;
        if (data.hasPerk("mining_speed_3")) bonus += 0.20;
        if (data.hasPerk("mining_speed_4")) bonus += 0.25;
        if (data.hasPerk("mining_speed_5")) bonus += 0.30;

        return bonus;
    }

    /**
     * Calculate mining fortune
     */
    public int getMiningFortune(UUID playerId) {
        PlayerHotMData data = playerData.get(playerId);
        if (data == null) return 0;

        int fortune = 0;
        if (data.hasPerk("efficient_miner")) fortune += 40;
        if (data.hasPerk("mining_fortune")) fortune += 15;
        if (data.hasPerk("rare_metal")) fortune += 10;

        return fortune;
    }

    /**
     * HotM Perk class
     */
    public static class HotMPerk {
        private final String id;
        private final String name;
        private final HotMTier tier;
        private final String description;
        private final Map<String, Integer> requirements;
        private final ItemStack displayItem;

        public HotMPerk(String id, String name, HotMTier tier, String description,
                        Map<String, Integer> requirements, ItemStack displayItem) {
            this.id = id;
            this.name = name;
            this.tier = tier;
            this.description = description;
            this.requirements = requirements;
            this.displayItem = displayItem;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public HotMTier getTier() { return tier; }
        public String getDescription() { return description; }
        public Map<String, Integer> getRequirements() { return requirements; }
        public ItemStack getDisplayItem() { return displayItem; }
    }

    /**
     * HotM Tier class
     */
    public static class HotMTier {
        public static final HotMTier TIER_1 = new HotMTier(1, 0, 0, 0, "Novice");
        public static final HotMTier TIER_2 = new HotMTier(2, 0, 0, 50, "Apprentice");
        public static final HotMTier TIER_3 = new HotMTier(3, 1000, 10, 150, "Journeyman");
        public static final HotMTier TIER_4 = new HotMTier(4, 2500, 25, 400, "Expert");
        public static final HotMTier TIER_5 = new HotMTier(5, 5000, 50, 750, "Master");

        private final int tierNumber;
        private final long requiredXp;
        private final int mithrilPowderReward;
        private final int gemstonePowderReward;
        private final String rankName;

        public HotMTier(int tierNumber, long requiredXp, int mithrilPowderReward,
                        int gemstonePowderReward, String rankName) {
            this.tierNumber = tierNumber;
            this.requiredXp = requiredXp;
            this.mithrilPowderReward = mithrilPowderReward;
            this.gemstonePowderReward = gemstonePowderReward;
            this.rankName = rankName;
        }

        public int getTierNumber() { return tierNumber; }
        public long getRequiredXp() { return requiredXp; }
        public int getMithrilPowderReward() { return mithrilPowderReward; }
        public int getGemstonePowderReward() { return gemstonePowderReward; }
        public String getRankName() { return rankName; }
    }

    /**
     * Player HotM data class
     */
    public static class PlayerHotMData {
        private final UUID playerId;
        private int currentTier;
        private long miningXp;
        private long mithrilPowder;
        private long mithrilPowderSpent;
        private long gemstonePowder;
        private Set<String> unlockedPerks;

        public PlayerHotMData(UUID playerId) {
            this.playerId = playerId;
            this.currentTier = 1;
            this.miningXp = 0;
            this.mithrilPowder = 0;
            this.mithrilPowderSpent = 0;
            this.gemstonePowder = 0;
            this.unlockedPerks = ConcurrentHashMap.newKeySet();
        }

        public void addMiningXp(long amount) { this.miningXp += amount; }
        public void setMiningXp(long miningXp) { this.miningXp = miningXp; }
        public void setMithrilPowder(long mithrilPowder) { this.mithrilPowder = mithrilPowder; }
        public void setGemstonePowder(long gemstonePowder) { this.gemstonePowder = gemstonePowder; }
        public void setCurrentTier(int currentTier) { this.currentTier = currentTier; }
        public void setMithrilPowderSpent(long spent) { this.mithrilPowderSpent = spent; }

        public void addUnlockedPerk(String perkId) {
            unlockedPerks.add(perkId);
            mithrilPowderSpent += 50; // Simplified cost tracking
        }

        public boolean hasPerk(String perkId) { return unlockedPerks.contains(perkId); }

        public UUID getPlayerId() { return playerId; }
        public int getCurrentTier() { return currentTier; }
        public long getMiningXp() { return miningXp; }
        public long getMithrilPowder() { return mithrilPowder; }
        public long getMithrilPowderSpent() { return mithrilPowderSpent; }
        public long getGemstonePowder() { return gemstonePowder; }
        public Set<String> getUnlockedPerks() { return unlockedPerks; }
    }
}
