package com.nexus.skyblock.shops;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Hypixel-style NPC Shop system for NexusBlock SkyBlock
 * Handles NPC vendors, GUI shops, and trading
 */
public class ShopManager {

    private final NexusCore plugin;
    private final Map<String, NPCShop> npcShops;
    private final Map<String, PlayerShop> playerShops;
    private final Map<UUID, ShopData> playerShopData;
    private final List<ShopItem> catalogItems;

    public ShopManager(NexusCore plugin) {
        this.plugin = plugin;
        this.npcShops = new ConcurrentHashMap<>();
        this.playerShops = new ConcurrentHashMap<>();
        this.playerShopData = new ConcurrentHashMap<>();
        this.catalogItems = new ArrayList<>();
    }

    /**
     * Initialize the shop system
     */
    public void initialize() {
        // Register default NPC shops
        registerDefaultNPCShops();

        // Build catalog
        buildCatalog();

        plugin.getNexusLogger().info("ShopManager initialized with " + npcShops.size() + " NPC shops");
    }

    /**
     * Register default NPC shops
     */
    private void registerDefaultNPCShops() {
        // Coal Miner NPC
        NPCShop coalShop = new NPCShop(
            "coal_shop",
            "Coal Miner",
            new org.bukkit.Location(null, 0, 100, 0),
            Material.COAL
        );
        coalShop.addItem(new ShopItem(Material.COAL, "Coal", 16, 10, 0));
        coalShop.addItem(new ShopItem(Material.COAL_ORE, "Coal Ore", 4, 20, 0));
        coalShop.addItem(new ShopItem(Material.TORCH, "Torches", 32, 15, 0));
        npcShops.put("coal_shop", coalShop);

        // Iron Merchant NPC
        NPCShop ironShop = new NPCShop(
            "iron_shop",
            "Iron Merchant",
            new org.bukkit.Location(null, 5, 100, 0),
            Material.IRON_INGOT
        );
        ironShop.addItem(new ShopItem(Material.IRON_INGOT, "Iron Ingot", 8, 50, 0));
        ironShop.addItem(new ShopItem(Material.IRON_BLOCK, "Iron Block", 1, 450, 0));
        ironShop.addItem(new ShopItem(Material.ANVIL, "Anvil", 1, 500, 0));
        npcShops.put("iron_shop", ironShop);

        // Gold Merchant NPC
        NPCShop goldShop = new NPCShop(
            "gold_shop",
            "Gold Merchant",
            new org.bukkit.Location(null, 10, 100, 0),
            Material.GOLD_INGOT
        );
        goldShop.addItem(new ShopItem(Material.GOLD_INGOT, "Gold Ingot", 8, 100, 0));
        goldShop.addItem(new ShopItem(Material.GOLD_BLOCK, "Gold Block", 1, 900, 0));
        goldShop.addItem(new ShopItem(Material.GOLDEN_APPLE, "Golden Apple", 1, 250, 0));
        npcShops.put("gold_shop", goldShop);

        // Diamond Dealer NPC
        NPCShop diamondShop = new NPCShop(
            "diamond_shop",
            "Diamond Dealer",
            new org.bukkit.Location(null, 15, 100, 0),
            Material.DIAMOND
        );
        diamondShop.addItem(new ShopItem(Material.DIAMOND, "Diamond", 4, 200, 0));
        diamondShop.addItem(new ShopItem(Material.DIAMOND_BLOCK, "Diamond Block", 1, 1800, 0));
        diamondShop.addItem(new ShopItem(Material.DIAMOND_SWORD, "Diamond Sword", 1, 500, 0));
        diamondShop.addItem(new ShopItem(Material.DIAMOND_PICKAXE, "Diamond Pickaxe", 1, 750, 0));
        npcShops.put("diamond_shop", diamondShop);

        // Farm Merchant NPC
        NPCShop farmShop = new NPCShop(
            "farm_shop",
            "Farm Merchant",
            new org.bukkit.Location(null, 20, 100, 0),
            Material.WHEAT
        );
        farmShop.addItem(new ShopItem(Material.WHEAT_SEEDS, "Seeds", 16, 5, 0));
        farmShop.addItem(new ShopItem(Material.CARROT, "Carrots", 16, 10, 0));
        farmShop.addItem(new ShopItem(Material.POTATO, "Potatoes", 16, 10, 0));
        farmShop.addItem(new ShopItem(Material.MELON_SEEDS, "Melon Seeds", 8, 20, 0));
        farmShop.addItem(new ShopItem(Material.PUMPKIN_SEEDS, "Pumpkin Seeds", 8, 20, 0));
        npcShops.put("farm_shop", farmShop);

        // Lumberjack NPC
        NPCShop lumberShop = new NPCShop(
            "lumber_shop",
            "Lumberjack",
            new org.bukkit.Location(null, 25, 100, 0),
            Material.LOG
        );
        lumberShop.addItem(new ShopItem(Material.LOG, "Oak Wood", 16, 20, 0));
        lumberShop.addItem(new ShopItem(Material.LOG, "Spruce Wood", 16, 25, 0));
        lumberShop.addItem(new ShopItem(Material.LOG_2, "Birch Wood", 16, 25, 0));
        lumberShop.addItem(new ShopItem(Material.WOOD, "Planks", 32, 5, 0));
        lumberShop.addItem(new ShopItem(Material.STICK, "Sticks", 32, 3, 0));
        npcShops.put("lumber_shop", lumberShop);

        // Builder NPC
        NPCShop builderShop = new NPCShop(
            "builder_shop",
            "Builder",
            new org.bukkit.Location(null, 30, 100, 0),
            Material.BRICK
        );
        builderShop.addItem(new ShopItem(Material.COBBLESTONE, "Cobblestone", 64, 10, 0));
        builderShop.addItem(new ShopItem(Material.STONE, "Stone", 32, 20, 0));
        builderShop.addItem(new ShopItem(Material.BRICK, "Bricks", 16, 30, 0));
        builderShop.addItem(new ShopItem(Material.SANDSTONE, "Sandstone", 16, 25, 0));
        builderShop.addItem(new ShopItem(Material.GLASS, "Glass", 8, 40, 0));
        npcShops.put("builder_shop", builderShop);

        // Magic Shop NPC
        NPCShop magicShop = new NPCShop(
            "magic_shop",
            "Magic Merchant",
            new org.bukkit.Location(null, 35, 100, 0),
            Material.BOOK
        );
        magicShop.addItem(new ShopItem(Material.ENCHANTMENT_TABLE, "Enchantment Table", 1, 1000, 0));
        magicShop.addItem(new ShopItem(Material.EXP_BOTTLE, "Experience Bottles", 16, 25, 0));
        magicShop.addItem(new ShopItem(Material.LAPIS_BLOCK, "Lapis Lazuli", 16, 50, 0));
        magicShop.addItem(new ShopItem(Material.INK_SACK, "Lapis Dye", 16, 5, 0));
        npcShops.put("magic_shop", magicShop);
    }

    /**
     * Build the shop catalog
     */
    private void buildCatalog() {
        catalogItems.clear();
        for (NPCShop shop : npcShops.values()) {
            catalogItems.addAll(shop.getItems());
        }
    }

    /**
     * Open NPC shop GUI
     */
    public void openNPCShop(Player player, String shopId) {
        NPCShop shop = npcShops.get(shopId);
        if (shop == null) {
            player.sendMessage(ChatColor.RED + "Shop not found!");
            return;
        }

        Inventory gui = Bukkit.createInventory(
            player,
            27,
            ChatColor.GOLD + shop.getName()
        );

        // Add shop items
        int slot = 0;
        for (ShopItem item : shop.getItems()) {
            gui.setItem(slot, createShopItem(item));
            slot++;
        }

        // Fill empty slots
        for (int i = slot; i < 27; i++) {
            gui.setItem(i, createGlassPane());
        }

        player.openInventory(gui);
    }

    /**
     * Create shop item display
     */
    private ItemStack createShopItem(ShopItem shopItem) {
        ItemStack item = new ItemStack(shopItem.getMaterial(), shopItem.getAmount());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + shopItem.getName());
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Amount: " + shopItem.getAmount(),
            ChatColor.GOLD + "Price: " + shopItem.getPrice() + " Coins",
            "",
            ChatColor.YELLOW + "Click to purchase!"
        ));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create glass pane for empty slots
     */
    private ItemStack createGlassPane() {
        ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 7);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        return pane;
    }

    /**
     * Purchase item from shop
     */
    public boolean purchaseItem(Player player, String shopId, int slot) {
        NPCShop shop = npcShops.get(shopId);
        if (shop == null) {
            player.sendMessage(ChatColor.RED + "Shop not found!");
            return false;
        }

        List<ShopItem> items = shop.getItems();
        if (slot < 0 || slot >= items.size()) {
            player.sendMessage(ChatColor.RED + "Invalid item!");
            return false;
        }

        ShopItem item = items.get(slot);
        long cost = item.getPrice();

        // Check if player has enough coins
        if (!plugin.getEconomyManager().hasCoins(player.getUniqueId(), cost)) {
            player.sendMessage(ChatColor.RED + "Not enough coins! Need: " + cost);
            return false;
        }

        // Check inventory space
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Your inventory is full!");
            return false;
        }

        // Process purchase
        plugin.getEconomyManager().removeCoins(player.getUniqueId(), cost);

        ItemStack purchasedItem = new ItemStack(item.getMaterial(), item.getAmount());
        player.getInventory().addItem(purchasedItem);

        player.sendMessage(ChatColor.GREEN + "Purchased " + item.getName() + " for " + cost + " coins!");

        return true;
    }

    /**
     * Sell item to shop
     */
    public boolean sellItem(Player player, Material material, int amount) {
        // Check if player has the item
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }

        if (count < amount) {
            player.sendMessage(ChatColor.RED + "You don't have enough items!");
            return false;
        }

        // Calculate sell price (60% of buy price)
        int pricePerItem = getSellPrice(material);
        long totalPrice = pricePerItem * amount;

        // Remove items
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material && remaining > 0) {
                int toRemove = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - toRemove);
                remaining -= toRemove;

                if (item.getAmount() <= 0) {
                    player.getInventory().remove(item);
                }
            }
        }

        // Add coins
        plugin.getEconomyManager().addCoins(player.getUniqueId(), totalPrice);

        player.sendMessage(ChatColor.GREEN + "Sold " + amount + " items for " + totalPrice + " coins!");
        return true;
    }

    /**
     * Get sell price for material
     */
    private int getSellPrice(Material material) {
        // Base prices at 60% of buy price
        int basePrice = getBuyPrice(material);
        return (int) (basePrice * 0.6);
    }

    /**
     * Get buy price for material
     */
    private int getBuyPrice(Material material) {
        // Search through all shops
        for (NPCShop shop : npcShops.values()) {
            for (ShopItem item : shop.getItems()) {
                if (item.getMaterial() == material) {
                    return item.getPrice();
                }
            }
        }
        return 10; // Default price
    }

    /**
     * Spawn an NPC villager
     */
    public void spawnNPC(String shopId, org.bukkit.Location location) {
        NPCShop shop = npcShops.get(shopId);
        if (shop == null) return;

        if (location.getWorld() == null) return;

        Villager villager = (Villager) location.getWorld().spawnEntity(
            location,
            EntityType.VILLAGER
        );

        villager.setProfession(org.bukkit.entity.Villager.Profession.BLACKSMITH);
        villager.setCustomName(ChatColor.GOLD + shop.getName());
        villager.setCustomNameVisible(true);
        villager.setSilent(true);
        villager.setAgeLock(true);
        villager.setAdult();

        shop.setEntityId(villager.getUniqueId());
    }

    /**
     * Open the main shop catalog
     */
    public void openShopCatalog(Player player) {
        Inventory gui = Bukkit.createInventory(
            player,
            54,
            ChatColor.DARK_PURPLE + "Shop Catalog"
        );

        int slot = 0;
        for (NPCShop shop : npcShops.values()) {
            if (slot >= 54) break;

            ItemStack icon = new ItemStack(shop.getIcon(), 1);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + shop.getName());
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to open shop",
                ChatColor.GRAY + shop.getItems().size() + " items available"
            ));
            icon.setItemMeta(meta);

            gui.setItem(slot, icon);
            slot++;
        }

        player.openInventory(gui);
    }

    /**
     * Get shop by ID
     */
    public NPCShop getShop(String shopId) {
        return npcShops.get(shopId);
    }

    /**
     * Shop item class
     */
    public static class ShopItem {
        private final Material material;
        private final String name;
        private final int amount;
        private final int price;
        private final int gems;

        public ShopItem(Material material, String name, int amount, int price, int gems) {
            this.material = material;
            this.name = name;
            this.amount = amount;
            this.price = price;
            this.gems = gems;
        }

        public Material getMaterial() { return material; }
        public String getName() { return name; }
        public int getAmount() { return amount; }
        public int getPrice() { return price; }
        public int getGems() { return gems; }
    }

    /**
     * NPC Shop class
     */
    public static class NPCShop {
        private final String id;
        private final String name;
        private final org.bukkit.Location location;
        private final Material icon;
        private final List<ShopItem> items;
        private UUID entityId;

        public NPCShop(String id, String name, org.bukkit.Location location, Material icon) {
            this.id = id;
            this.name = name;
            this.location = location;
            this.icon = icon;
            this.items = new ArrayList<>();
        }

        public void addItem(ShopItem item) {
            items.add(item);
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public org.bukkit.Location getLocation() { return location; }
        public Material getIcon() { return icon; }
        public List<ShopItem> getItems() { return items; }
        public UUID getEntityId() { return entityId; }
        public void setEntityId(UUID entityId) { this.entityId = entityId; }
    }

    /**
     * Player shop data
     */
    public static class PlayerShop {
        private final UUID owner;
        private final String name;
        private final Map<Integer, ShopItem> inventory;
        private long balance;

        public PlayerShop(UUID owner, String name) {
            this.owner = owner;
            this.name = name;
            this.inventory = new HashMap<>();
            this.balance = 0;
        }

        public void addItem(int slot, ShopItem item) {
            inventory.put(slot, item);
        }

        public UUID getOwner() { return owner; }
        public String getName() { return name; }
        public Map<Integer, ShopItem> getInventory() { return inventory; }
        public long getBalance() { return balance; }
        public void setBalance(long balance) { this.balance = balance; }
    }

    /**
     * Shop data container
     */
    public static class ShopData {
        private long coins;
        private int gems;
        private Set<String> purchasedItems;

        public ShopData() {
            this.coins = 0;
            this.gems = 0;
            this.purchasedItems = new HashSet<>();
        }

        public long getCoins() { return coins; }
        public void setCoins(long coins) { this.coins = coins; }
        public int getGems() { return gems; }
        public void setGems(int gems) { this.gems = gems; }
        public Set<String> getPurchasedItems() { return purchasedItems; }
    }
}
