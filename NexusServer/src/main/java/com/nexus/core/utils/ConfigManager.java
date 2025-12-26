package com.nexus.core.utils;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration manager for NexusCore - handles all configuration file operations
 */
public class ConfigManager {

    private final NexusCore plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration warpsConfig;
    private File warpsFile;

    public ConfigManager(NexusCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Load all configuration files
     */
    public void loadConfigs() {
        loadMainConfig();
        loadWarpsConfig();
    }

    /**
     * Load main configuration file
     */
    private void loadMainConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Set default values if not present
        setDefaults();

        saveMainConfig();
    }

    /**
     * Set default configuration values
     */
    private void setDefaults() {
        // Hub settings
        if (!config.contains("hub.world-name")) {
            config.set("hub.world-name", "hub");
        }
        if (!config.contains("hub.spawn.x")) {
            config.set("hub.spawn.x", 0);
        }
        if (!config.contains("hub.spawn.y")) {
            config.set("hub.spawn.y", 100);
        }
        if (!config.contains("hub.spawn.z")) {
            config.set("hub.spawn.z", 0);
        }
        if (!config.contains("hub.spawn.yaw")) {
            config.set("hub.spawn.yaw", 0);
        }
        if (!config.contains("hub.spawn.pitch")) {
            config.set("hub.spawn.pitch", 0);
        }
        if (!config.contains("hub.build-radius")) {
            config.set("hub.build-radius", 50.0);
        }

        // Hub protection
        if (!config.contains("hub.protection.no-fall-damage")) {
            config.set("hub.protection.no-fall-damage", true);
        }
        if (!config.contains("hub.protection.no-pvp")) {
            config.set("hub.protection.no-pvp", true);
        }
        if (!config.contains("hub.protection.no-hunger-loss")) {
            config.set("hub.protection.no-hunger-loss", true);
        }

        // Double jump
        if (!config.contains("hub.double-jump.enabled")) {
            config.set("hub.double-jump.enabled", true);
        }
        if (!config.contains("hub.double-jump.max-jumps")) {
            config.set("hub.double-jump.max-jumps", 1);
        }

        // Authentication
        if (!config.contains("auth.enabled")) {
            config.set("auth.enabled", false);
        }
        if (!config.contains("auth.session-timeout")) {
            config.set("auth.session-timeout", 300);
        }

        // Economy
        if (!config.contains("economy.starting-coins")) {
            config.set("economy.starting-coins", 1000);
        }
        if (!config.contains("economy.starting-gems")) {
            config.set("economy.starting-gems", 0);
        }

        // Chat
        if (!config.contains("chat.enabled")) {
            config.set("chat.enabled", true);
        }
        if (!config.contains("chat.cooldown-seconds")) {
            config.set("chat.cooldown-seconds", 2);
        }

        // SkyBlock
        if (!config.contains("skyblock.world-name")) {
            config.set("skyblock.world-name", "skyblock");
        }
        if (!config.contains("skyblock.default-island-size")) {
            config.set("skyblock.default-island-size", 100);
        }

        // Server info
        if (!config.contains("server.name")) {
            config.set("server.name", "NexusBlock Network");
        }
        if (!config.contains("server.max-players")) {
            config.set("server.max-players", 100);
        }
    }

    /**
     * Load warps configuration
     */
    private void loadWarpsConfig() {
        warpsFile = new File(plugin.getDataFolder(), "warps.yml");

        if (!warpsFile.exists()) {
            try {
                warpsFile.createNewFile();
            } catch (IOException e) {
                plugin.getNexusLogger().warning("Failed to create warps.yml");
            }
        }

        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
    }

    /**
     * Save main configuration
     */
    public void saveMainConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getNexusLogger().warning("Failed to save config.yml");
        }
    }

    /**
     * Reload all configurations
     */
    public void reloadConfigs() {
        loadMainConfig();
        loadWarpsConfig();
        plugin.getNexusLogger().info("All configurations reloaded");
    }

    /**
     * Save warps to file
     */
    public void saveWarps(Map<String, Location> warps) {
        warpsConfig.set("warps", null);

        for (Map.Entry<String, Location> entry : warps.entrySet()) {
            String name = entry.getKey();
            Location loc = entry.getValue();

            warpsConfig.set("warps." + name + ".world", loc.getWorld().getName());
            warpsConfig.set("warps." + name + ".x", loc.getX());
            warpsConfig.set("warps." + name + ".y", loc.getY());
            warpsConfig.set("warps." + name + ".z", loc.getZ());
            warpsConfig.set("warps." + name + ".yaw", loc.getYaw());
            warpsConfig.set("warps." + name + ".pitch", loc.getPitch());
        }

        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            plugin.getNexusLogger().warning("Failed to save warps.yml");
        }
    }

    /**
     * Load warps from file
     */
    public Map<String, Location> loadWarps() {
        Map<String, Location> warps = new HashMap<>();

        ConfigurationSection warpsSection = warpsConfig.getConfigurationSection("warps");
        if (warpsSection == null) {
            return warps;
        }

        for (String name : warpsSection.getKeys(false)) {
            String worldName = warpsSection.getString(name + ".world");
            double x = warpsSection.getDouble(name + ".x");
            double y = warpsSection.getDouble(name + ".y");
            double z = warpsSection.getDouble(name + ".z");
            float yaw = (float) warpsSection.getDouble(name + ".yaw");
            float pitch = (float) warpsSection.getDouble(name + ".pitch");

            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world != null) {
                warps.put(name, new Location(world, x, y, z, yaw, pitch));
            }
        }

        return warps;
    }

    /**
     * Get string configuration value
     */
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    /**
     * Get int configuration value
     */
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    /**
     * Get double configuration value
     */
    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }

    /**
     * Get boolean configuration value
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    /**
     * Get long configuration value
     */
    public long getLong(String path, long defaultValue) {
        return config.getLong(path, defaultValue);
    }

    /**
     * Get list configuration value
     */
    public List<?> getList(String path, List<?> defaultValue) {
        return config.getList(path, defaultValue);
    }

    /**
     * Set configuration value
     */
    public void set(String path, Object value) {
        config.set(path, value);
        saveMainConfig();
    }

    /**
     * Get main configuration
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Get warps configuration
     */
    public FileConfiguration getWarpsConfig() {
        return warpsConfig;
    }
}
