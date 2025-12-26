package com.nexus.core;

import com.nexus.auth.NexusAuth;
import com.nexus.core.commands.*;
import com.nexus.core.listeners.*;
import com.nexus.core.utils.ConfigManager;
import com.nexus.core.utils.NMSUtils;
import com.nexus.database.DatabaseManager;
import com.nexus.economy.EconomyManager;
import com.nexus.hub.HubManager;
import com.nexus.minigames.MinigameManager;
import com.nexus.skyblock.SkyBlockManager;
import com.nexus.skyblock.hotm.HotMManager;
import com.nexus.skyblock.minions.MinionManager;
import com.nexus.skyblock.quests.QuestManager;
import com.nexus.skyblock.shops.ShopManager;
import com.nexus.skyblock.treasure.TreasureManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main NexusCore plugin class for NexusBlock Network.
 * This is the core plugin that manages all other modules and provides
 * the foundation for the Hypixel-style server experience.
 */
public class NexusCore extends JavaPlugin {

    private static NexusCore instance;
    private Logger logger;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private HubManager hubManager;
    private SkyBlockManager skyBlockManager;
    private MinigameManager minigameManager;
    private NexusAuth authSystem;
    private NMSUtils nmsUtils;

    // SkyBlock managers
    private MinionManager minionManager;
    private QuestManager questManager;
    private ShopManager shopManager;
    private HotMManager hotmManager;
    private TreasureManager treasureManager;

    // Server metrics and stats
    private int playerCount = 0;
    private long serverStartTime;

    @Override
    public void onLoad() {
        instance = this;
        logger = getLogger();
        serverStartTime = System.currentTimeMillis();

        logger.info("===========================================");
        logger.info("  NexusBlock Network - Core Plugin v1.0.0");
        logger.info("===========================================");
        logger.info("Loading NexusCore...");

        // Initialize NMS utilities for version compatibility
        try {
            nmsUtils = new NMSUtils();
            logger.info("NMS utilities initialized successfully");
        } catch (Exception e) {
            logger.warning("Failed to initialize NMS utilities: " + e.getMessage());
            nmsUtils = null;
        }

        // Load configuration
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        logger.info("Configuration loaded successfully");
    }

    @Override
    public void onEnable() {
        logger.info("Enabling NexusCore components...");

        // Initialize database
        if (!initializeDatabase()) {
            logger.severe("Failed to initialize database! Disabling plugin...");
            setEnabled(false);
            return;
        }

        // Initialize economy
        initializeEconomy();

        // Initialize authentication system
        authSystem = new NexusAuth(this);
        authSystem.initialize();

        // Initialize managers
        hubManager = new HubManager(this);
        hubManager.initialize();

        skyBlockManager = new SkyBlockManager(this);
        skyBlockManager.initialize();

        minigameManager = new MinigameManager(this);
        minigameManager.initialize();

        // Initialize SkyBlock feature managers
        initializeSkyBlockManagers();

        // Register commands
        registerCommands();

        // Register event listeners
        registerEventListeners();

        // Register scheduled tasks
        registerScheduledTasks();

        // Setup metrics and updates
        setupMetrics();

        logger.info("===========================================");
        logger.info("  NexusCore v1.0.0 Enabled Successfully!");
        logger.info("  Server running on Minecraft " + Bukkit.getVersion());
        logger.info("  Online Players: " + Bukkit.getOnlinePlayers().size());
        logger.info("===========================================");
    }

    /**
     * Initialize all SkyBlock feature managers
     */
    private void initializeSkyBlockManagers() {
        // Minion system
        minionManager = new MinionManager(this);
        minionManager.initialize();
        logger.info("MinionManager initialized");

        // Quest system
        questManager = new QuestManager(this);
        questManager.initialize();
        logger.info("QuestManager initialized");

        // Shop system
        shopManager = new ShopManager(this);
        shopManager.initialize();
        logger.info("ShopManager initialized");

        // Heart of the Mountain
        hotmManager = new HotMManager(this);
        hotmManager.initialize();
        logger.info("HotMManager initialized");

        // Treasure system
        treasureManager = new TreasureManager(this);
        treasureManager.initialize();
        logger.info("TreasureManager initialized");
    }

    @Override
    public void onDisable() {
        logger.info("Disabling NexusCore...");

        // Shutdown SkyBlock managers
        if (treasureManager != null) treasureManager.shutdown();
        if (hotmManager != null) {} // HotM doesn't have shutdown
        if (shopManager != null) {}
        if (questManager != null) {}
        if (minionManager != null) minionManager.shutdown();

        // Save all data
        if (databaseManager != null) {
            databaseManager.saveAllData();
            databaseManager.closeConnection();
        }

        // Save economy data
        if (economyManager != null) {
            economyManager.saveEconomyData();
        }

        // Unload worlds
        if (skyBlockManager != null) {
            skyBlockManager.unloadAllIslands();
        }

        long uptime = System.currentTimeMillis() - serverStartTime;
        logger.info("NexusCore disabled. Server uptime: " + (uptime / 1000) + " seconds");

        instance = null;
    }

    /**
     * Initialize the database connection and tables
     */
    private boolean initializeDatabase() {
        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            databaseManager.createTables();
            logger.info("Database initialized successfully");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize database", e);
            return false;
        }
    }

    /**
     * Initialize economy system using Vault
     */
    private void initializeEconomy() {
        try {
            economyManager = new EconomyManager(this);
            if (economyManager.initialize()) {
                logger.info("Economy system initialized successfully");
            } else {
                logger.warning("Vault not found! Using internal economy system");
                economyManager.initializeInternal();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to initialize economy", e);
            economyManager = new EconomyManager(this);
            economyManager.initializeInternal();
        }
    }

    /**
     * Register all plugin commands
     */
    private void registerCommands() {
        // Core commands
        getCommand("nexus").setExecutor(new NexusCommand());
        getCommand("hub").setExecutor(new HubCommand());
        getCommand("lobby").setExecutor(new HubCommand());
        getCommand("server").setExecutor(new ServerCommand());

        // Communication commands
        getCommand("msg").setExecutor(new MessageCommand());
        getCommand("reply").setExecutor(new ReplyCommand());

        // Teleport commands
        getCommand("tpa").setExecutor(new TPACommand());
        getCommand("tpaccept").setExecutor(new TPAcceptCommand());
        getCommand("tpdeny").setExecutor(new TPDenyCommand());
        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("warp").setExecutor(new WarpCommand());

        // SkyBlock commands
        getCommand("skyblock").setExecutor(new com.nexus.skyblock.SkyBlockCommand(this));
        getCommand("sb").setExecutor(new com.nexus.skyblock.SkyBlockCommand(this));
        getCommand("island").setExecutor(new com.nexus.skyblock.SkyBlockCommand(this));
        getCommand("minion").setExecutor(new com.nexus.skyblock.minions.MinionCommand(this));
        getCommand("quests").setExecutor(new com.nexus.skyblock.quests.QuestCommand(this));
        getCommand("quest").setExecutor(new com.nexus.skyblock.quests.QuestCommand(this));
        getCommand("hotm").setExecutor(new com.nexus.skyblock.hotm.HotMCommand(this));
        getCommand("shop").setExecutor(new com.nexus.skyblock.shops.ShopCommand(this));
        getCommand("sell").setExecutor(new com.nexus.skyblock.shops.SellCommand(this));
        getCommand("treasure").setExecutor(new com.nexus.skyblock.treasure.TreasureCommand(this));

        logger.info("All commands registered successfully");
    }

    /**
     * Register all event listeners
     */
    private void registerEventListeners() {
        PluginManager pm = Bukkit.getPluginManager();

        // Core listeners
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new PlayerMoveListener(this), this);
        pm.registerEvents(new PlayerChatListener(this), this);

        // Damage and protection listeners
        pm.registerEvents(new EntityDamageListener(this), this);
        pm.registerEvents(new FoodLevelChangeListener(this), this);

        // Inventory and item listeners
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new ItemDropListener(this), this);

        // World and block listeners
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new BlockPlaceListener(this), this);

        logger.info("All event listeners registered successfully");
    }

    /**
     * Register scheduled tasks
     */
    private void registerScheduledTasks() {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        // Save data periodically (every 5 minutes)
        scheduler.runTaskTimerAsynchronously(this, () -> {
            if (databaseManager != null) {
                databaseManager.saveAllData();
            }
            if (economyManager != null) {
                economyManager.saveEconomyData();
            }
        }, 6000L, 6000L);

        // Update player count (every 30 seconds)
        scheduler.runTaskTimer(this, () -> {
            playerCount = Bukkit.getOnlinePlayers().size();
        }, 0L, 600L);

        // Auto-save islands (every 10 minutes)
        scheduler.runTaskTimerAsynchronously(this, () -> {
            if (skyBlockManager != null) {
                skyBlockManager.autoSaveIslands();
            }
        }, 12000L, 12000L);

        logger.info("Scheduled tasks registered successfully");
    }

    /**
     * Setup bStats metrics
     */
    private void setupMetrics() {
        logger.info("Metrics system ready");
    }

    // Getter methods for managers

    public static NexusCore getInstance() {
        return instance;
    }

    public Logger getNexusLogger() {
        return logger;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public HubManager getHubManager() {
        return hubManager;
    }

    public SkyBlockManager getSkyBlockManager() {
        return skyBlockManager;
    }

    public MinigameManager getMinigameManager() {
        return minigameManager;
    }

    public NexusAuth getAuthSystem() {
        return authSystem;
    }

    public NMSUtils getNmsUtils() {
        return nmsUtils;
    }

    // SkyBlock feature managers getters
    public MinionManager getMinionManager() {
        return minionManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public HotMManager getHotmManager() {
        return hotmManager;
    }

    public TreasureManager getTreasureManager() {
        return treasureManager;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public long getServerStartTime() {
        return serverStartTime;
    }

    public void setPlayerCount(int count) {
        this.playerCount = count;
    }
}
