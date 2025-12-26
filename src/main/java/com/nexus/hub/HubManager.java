package com.nexus.hub;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Manager class for hub functionality - handles hub world, spawn points, and hub features
 */
public class HubManager {

    private final NexusCore plugin;
    private Location spawnLocation;
    private String hubWorldName;
    private final Set<Player> hubPlayers;

    public HubManager(NexusCore plugin) {
        this.plugin = plugin;
        this.hubPlayers = new HashSet<>();
    }

    /**
     * Initialize the hub manager
     */
    public void initialize() {
        // Load configuration
        loadConfiguration();

        // Setup hub world
        setupHubWorld();

        // Setup spawn point
        setupSpawnPoint();

        plugin.getNexusLogger().info("HubManager initialized successfully");
    }

    /**
     * Load hub configuration
     */
    private void loadConfiguration() {
        hubWorldName = plugin.getConfigManager().getString("hub.world-name", "hub");

        // Create spawn location from config
        double x = plugin.getConfigManager().getDouble("hub.spawn.x", 0);
        double y = plugin.getConfigManager().getDouble("hub.spawn.y", 100);
        double z = plugin.getConfigManager().getDouble("hub.spawn.z", 0);
        float yaw = (float) plugin.getConfigManager().getDouble("hub.spawn.yaw", 0);
        float pitch = (float) plugin.getConfigManager().getDouble("hub.spawn.pitch", 0);

        World world = Bukkit.getWorld(hubWorldName);
        if (world != null) {
            spawnLocation = new Location(world, x, y, z, yaw, pitch);
        }
    }

    /**
     * Setup hub world
     */
    private void setupHubWorld() {
        World hubWorld = Bukkit.getWorld(hubWorldName);

        if (hubWorld == null) {
            // Create new hub world
            plugin.getNexusLogger().info("Creating new hub world: " + hubWorldName);
            createHubWorld();
        } else {
            // Configure existing world
            configureWorld(hubWorld);
        }
    }

    /**
     * Create a new hub world
     */
    private void createHubWorld() {
        // World creation would be handled by Minecraft's built-in world generation
        // For now, we assume the world exists or will be generated on server start
        plugin.getNexusLogger().info("Hub world '" + hubWorldName + "' ready");
    }

    /**
     * Configure world settings
     */
    private void configureWorld(World world) {
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("commandBlockOutput", "false");
        world.setTime(1000); // Day time
        world.setStorm(false);
        world.setThundering(false);
    }

    /**
     * Setup spawn point
     */
    private void setupSpawnPoint() {
        if (spawnLocation == null) {
            // Use default location
            World hubWorld = Bukkit.getWorld(hubWorldName);
            if (hubWorld != null) {
                spawnLocation = new Location(hubWorld, 0, 100, 0, 0, 0);
            }
            plugin.getNexusLogger().warning("Using default spawn location!");
        }

        plugin.getNexusLogger().info("Spawn location: " + formatLocation(spawnLocation));
    }

    /**
     * Send player to hub
     */
    public void sendToHub(Player player) {
        if (spawnLocation == null) {
            player.sendMessage(ChatColor.RED + "Hub not configured properly!");
            return;
        }

        // Add to hub players
        hubPlayers.add(player);

        // Teleport to spawn
        player.teleport(spawnLocation);

        // Set game mode to adventure
        player.setGameMode(GameMode.ADVENTURE);

        // Clear inventory and give hub items
        clearInventory(player);
        giveHubItems(player);

        // Reset player state
        resetPlayerState(player);

        player.sendMessage(ChatColor.AQUA + "Welcome to the Hub!");
    }

    /**
     * Remove player from hub
     */
    public void removeFromHub(Player player) {
        hubPlayers.remove(player);
    }

    /**
     * Clear player's inventory
     */
    private void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
    }

    /**
     * Give hub items to player
     */
    private void giveHubItems(Player player) {
        // Game Selector (Compass)
        org.bukkit.ItemStack selector = new org.bukkit.ItemStack(Material.COMPASS, 1);
        org.bukkit.ItemMeta selectorMeta = selector.getItemMeta();
        selectorMeta.setDisplayName(ChatColor.AQUA + "Game Selector");
        selectorMeta.setLore(java.util.Arrays.asList(
            ChatColor.GRAY + "Right-click to select a game"
        ));
        selector.setItemMeta(selectorMeta);
        player.getInventory().setItem(0, selector);

        // Cosmetics (Chest)
        org.bukkit.ItemStack cosmetics = new org.bukkit.ItemStack(Material.CHEST, 1);
        org.bukkit.ItemMeta cosmeticsMeta = cosmetics.getItemMeta();
        cosmeticsMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Cosmetics");
        cosmeticsMeta.setLore(java.util.Arrays.asList(
            ChatColor.GRAY + "Click to open cosmetics menu"
        ));
        cosmetics.setItemMeta(cosmeticsMeta);
        player.getInventory().setItem(4, cosmetics);

        // Profile (Player Head)
        org.bukkit.ItemStack profile = new org.bukkit.ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        org.bukkit.ItemMeta profileMeta = profile.getItemMeta();
        profileMeta.setDisplayName(ChatColor.GREEN + "Profile");
        profileMeta.setLore(java.util.Arrays.asList(
            ChatColor.GRAY + "View your stats and settings"
        ));
        profile.setItemMeta(profileMeta);
        player.getInventory().setItem(8, profile);

        player.updateInventory();
    }

    /**
     * Reset player state
     */
    private void resetPlayerState(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setLevel(0);
        player.setExp(0);
    }

    /**
     * Play spawn effect for player
     */
    public void playSpawnEffect(Player player) {
        // Play sound
        player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.0f);

        // Spawn particles
        player.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, player.getLocation(), 10, 0.5, 0.5, 0.5);
    }

    /**
     * Check if player is in hub
     */
    public boolean isInHub(Player player) {
        if (player.getWorld() == null) {
            return false;
        }
        return player.getWorld().getName().equals(hubWorldName);
    }

    /**
     * Get hub world name
     */
    public String getHubWorldName() {
        return hubWorldName;
    }

    /**
     * Get spawn location
     */
    public Location getSpawnLocation() {
        return spawnLocation;
    }

    /**
     * Set spawn location
     */
    public void setSpawnLocation(Location location) {
        this.spawnLocation = location;

        // Save to config
        plugin.getConfigManager().set("hub.spawn.x", location.getX());
        plugin.getConfigManager().set("hub.spawn.y", location.getY());
        plugin.getConfigManager().set("hub.spawn.z", location.getZ());
        plugin.getConfigManager().set("hub.spawn.yaw", location.getYaw());
        plugin.getConfigManager().set("hub.spawn.pitch", location.getPitch());
    }

    /**
     * Get hub players count
     */
    public int getHubPlayerCount() {
        return hubPlayers.size();
    }

    /**
     * Format location for logging
     */
    private String formatLocation(Location loc) {
        if (loc == null) {
            return "null";
        }
        return String.format("(%.1f, %.1f, %.1f) in %s", loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }
}
