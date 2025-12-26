package com.nexus.skyblock;

import com.nexus.core.NexusCore;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.Crops;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SkyBlock manager for NexusBlock Network
 * Handles island creation, management, and gameplay
 */
public class SkyBlockManager {

    private final NexusCore plugin;
    private final Map<UUID, SkyBlockIsland> islands;
    private final Map<UUID, Set<UUID>> islandMembers;
    private String worldName;
    private int defaultIslandSize;
    private final Random random;

    public SkyBlockManager(NexusCore plugin) {
        this.plugin = plugin;
        this.islands = new ConcurrentHashMap<>();
        this.islandMembers = new ConcurrentHashMap<>();
        this.random = new Random();
    }

    /**
     * Initialize SkyBlock system
     */
    public void initialize() {
        worldName = plugin.getConfigManager().getString("skyblock.world-name", "skyblock");
        defaultIslandSize = plugin.getConfigManager().getInt("skyblock.default-island-size", 100);

        setupSkyBlockWorld();
        loadIslands();

        plugin.getNexusLogger().info("SkyBlockManager initialized with world: " + worldName);
    }

    /**
     * Setup SkyBlock world
     */
    private void setupSkyBlockWorld() {
        World skyBlockWorld = Bukkit.getWorld(worldName);

        if (skyBlockWorld == null) {
            // Create new world
            plugin.getNexusLogger().info("Creating new SkyBlock world: " + worldName);
            createSkyBlockWorld();
        } else {
            configureWorld(skyBlockWorld);
        }
    }

    /**
     * Create a new SkyBlock world
     */
    private void createSkyBlockWorld() {
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);
        creator.type(WorldType.FLAT);
        creator.generator(new VoidChunkGenerator());

        World skyBlockWorld = creator.createWorld();
        if (skyBlockWorld != null) {
            configureWorld(skyBlockWorld);
        }
    }

    /**
     * Configure world settings
     */
    private void configureWorld(World world) {
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doDaylightCycle", "true");
        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("commandBlockOutput", "false");
        world.setSpawnLocation(0, 200, 0);
    }

    /**
     * Load existing islands from database
     */
    private void loadIslands() {
        // In a real implementation, load from database
        plugin.getNexusLogger().info("Loaded SkyBlock islands");
    }

    /**
     * Create a new island for a player
     */
    public boolean createIsland(Player player) {
        UUID playerId = player.getUniqueId();

        if (islands.containsKey(playerId)) {
            player.sendMessage(ChatColor.RED + "You already have an island!");
            return false;
        }

        // Generate island location
        Location islandCenter = generateIslandLocation(player);

        // Create island data
        SkyBlockIsland island = new SkyBlockIsland(playerId, islandCenter, defaultIslandSize);
        islands.put(playerId, island);

        // Create island members set
        Set<UUID> members = new HashSet<>();
        members.add(playerId);
        islandMembers.put(playerId, members);

        // Generate island terrain
        generateIslandTerrain(islandCenter);

        // Teleport player to island
        player.teleport(getIslandSpawn(islandCenter));

        // Give starter items
        giveStarterItems(player);

        player.sendMessage(ChatColor.GREEN + "Welcome to your new SkyBlock island!");
        player.sendMessage(ChatColor.GRAY + "Type /island help for commands");

        return true;
    }

    /**
     * Generate a unique island location
     */
    private Location generateIslandLocation(Player player) {
        // Generate coordinates based on player ID to ensure consistency
        int chunkX = Math.abs(player.getUniqueId().hashCode() % 100) + 1;
        int chunkZ = Math.abs((player.getUniqueId().hashCode() >> 16) % 100) + 1;

        return new Location(
            Bukkit.getWorld(worldName),
            chunkX * 16 + 8,
            100,
            chunkZ * 16 + 8
        );
    }

    /**
     * Generate island terrain
     */
    private void generateIslandTerrain(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        // Create basic island structure
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        // Center block
        setBlock(world, centerX, 99, centerZ, Material.DIRT);
        setBlock(world, centerX, 100, centerZ, Material.GRASS);

        // Surrounding blocks
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue;
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    // Edge blocks - 50% chance of grass
                    if (random.nextBoolean()) {
                        setBlock(world, centerX + x, 99, centerZ + z, Material.DIRT);
                        setBlock(world, centerX + x, 100, centerZ + z, Material.GRASS);
                    } else {
                        setBlock(world, centerX + x, 99, centerZ + z, Material.AIR);
                    }
                } else {
                    // Inner blocks - grass
                    setBlock(world, centerX + x, 99, centerZ + z, Material.DIRT);
                    setBlock(world, centerX + x, 100, centerZ + z, Material.GRASS);
                }
            }
        }

        // Create cobblestone generator
        setBlock(world, centerX + 3, 100, centerZ, Material.COBBLESTONE);

        // Create oak tree
        setBlock(world, centerX - 2, 101, centerZ - 1, Material.LOG);
        setBlock(world, centerX - 2, 102, centerZ - 1, Material.LOG);
        setBlock(world, centerX - 2, 103, centerZ - 1, Material.LOG);
        setBlock(world, centerX - 3, 103, centerZ - 1, Material.LEAVES);
        setBlock(world, centerX - 2, 104, centerZ - 1, Material.LEAVES);
        setBlock(world, centerX - 1, 104, centerZ - 1, Material.LEAVES);

        // Create chest with starter items
        setBlock(world, centerX, 101, centerZ + 2, Material.CHEST);

        // Create spawn platform
        setBlock(world, centerX, 101, centerZ, Material.WOOL, (byte) 0);
    }

    /**
     * Set a block with proper state update
     */
    private void setBlock(World world, int x, int y, int z, Material material) {
        setBlock(world, x, y, z, material, (byte) 0);
    }

    /**
     * Set a block with data value
     */
    private void setBlock(World world, int x, int y, int z, Material material, byte data) {
        Block block = world.getBlockAt(x, y, z);
        block.setTypeIdAndData(material.getId(), data, false);
    }

    /**
     * Get island spawn location
     */
    private Location getIslandSpawn(Location center) {
        return new Location(center.getWorld(), center.getX(), 105, center.getZ());
    }

    /**
     * Give starter items to player
     */
    private void giveStarterItems(Player player) {
        player.getInventory().clear();

        // Wooden pickaxe
        ItemStack pickaxe = new ItemStack(Material.WOOD_PICKAXE, 1);
        player.getInventory().addItem(pickaxe);

        // Wooden axe
        ItemStack axe = new ItemStack(Material.WOOD_AXE, 1);
        player.getInventory().addItem(axe);

        // Seeds
        ItemStack seeds = new ItemStack(Material.SEEDS, 16);
        player.getInventory().addItem(seeds);

        // Bread
        ItemStack bread = new ItemStack(Material.BREAD, 8);
        player.getInventory().addItem(bread);

        // Water bucket
        ItemStack water = new ItemStack(Material.WATER_BUCKET, 1);
        player.getInventory().addItem(water);

        // Lava bucket
        ItemStack lava = new ItemStack(Material.LAVA_BUCKET, 1);
        player.getInventory().addItem(lava);

        player.updateInventory();
    }

    /**
     * Handle block break in SkyBlock
     */
    public void handleBlockBreak(Player player, Block block) {
        UUID playerId = player.getUniqueId();
        SkyBlockIsland island = islands.get(playerId);

        if (island == null) {
            player.sendMessage(ChatColor.RED + "You don't have an island!");
            return;
        }

        // Check if block is within island bounds
        if (!isBlockInIsland(block.getLocation(), island)) {
            player.sendMessage(ChatColor.RED + "You can only break blocks on your island!");
            return;
        }

        // Check if block is protected
        if (isProtectedBlock(block)) {
            player.sendMessage(ChatColor.RED + "This block is protected!");
            return;
        }

        // Check if player has permission (island member or owner)
        if (!hasIslandAccess(player, island)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to break blocks here!");
            return;
        }
    }

    /**
     * Handle block place in SkyBlock
     */
    public void handleBlockPlace(Player player, Block block) {
        UUID playerId = player.getUniqueId();
        SkyBlockIsland island = islands.get(playerId);

        if (island == null) {
            player.sendMessage(ChatColor.RED + "You don't have an island!");
            return;
        }

        // Check if block is within island bounds
        if (!isBlockInIsland(block.getLocation(), island)) {
            player.sendMessage(ChatColor.RED + "You can only place blocks on your island!");
            return;
        }

        // Check if player has permission
        if (!hasIslandAccess(player, island)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to place blocks here!");
            return;
        }
    }

    /**
     * Check if block location is within island bounds
     */
    private boolean isBlockInIsland(Location location, SkyBlockIsland island) {
        Location center = island.getCenter();
        int size = island.getSize();

        double distance = Math.sqrt(
            Math.pow(location.getX() - center.getX(), 2) +
            Math.pow(location.getZ() - center.getZ(), 2)
        );

        return distance <= size;
    }

    /**
     * Check if block is protected
     */
    private boolean isProtectedBlock(Block block) {
        Material type = block.getType();
        return type == Material.BEDROCK ||
               type == Material.COMMAND ||
               type == Material.COMMAND_CHEST ||
               type == Material.MOB_SPAWNER;
    }

    /**
     * Check if player has access to island
     */
    private boolean hasIslandAccess(Player player, SkyBlockIsland island) {
        UUID playerId = player.getUniqueId();

        // Owner always has access
        if (island.getOwner().equals(playerId)) {
            return true;
        }

        // Check members
        Set<UUID> members = islandMembers.get(island.getOwner());
        if (members != null && members.contains(playerId)) {
            return true;
        }

        return false;
    }

    /**
     * Check if player is in SkyBlock world
     */
    public boolean isInSkyBlockWorld(Player player) {
        if (player.getWorld() == null) {
            return false;
        }
        return player.getWorld().getName().equals(worldName);
    }

    /**
     * Get player's island
     */
    public SkyBlockIsland getIsland(UUID player) {
        return islands.get(player);
    }

    /**
     * Teleport player to their island
     */
    public void teleportToIsland(Player player) {
        SkyBlockIsland island = islands.get(player.getUniqueId());

        if (island == null) {
            player.sendMessage(ChatColor.RED + "You don't have an island! Create one with /island create");
            return;
        }

        player.teleport(getIslandSpawn(island.getCenter()));
        player.sendMessage(ChatColor.GREEN + "Teleported to your island!");
    }

    /**
     * Auto-save all islands
     */
    public void autoSaveIslands() {
        // In a real implementation, save to database
        plugin.getNexusLogger().info("Auto-saved SkyBlock islands");
    }

    /**
     * Unload all islands (on shutdown)
     */
    public void unloadAllIslands() {
        autoSaveIslands();
        islands.clear();
        islandMembers.clear();
    }

    /**
     * SkyBlock island data class
     */
    public static class SkyBlockIsland {
        private final UUID owner;
        private final Location center;
        private final int size;
        private int level;
        private long createdAt;

        public SkyBlockIsland(UUID owner, Location center, int size) {
            this.owner = owner;
            this.center = center;
            this.size = size;
            this.level = 1;
            this.createdAt = System.currentTimeMillis();
        }

        public UUID getOwner() {
            return owner;
        }

        public Location getCenter() {
            return center;
        }

        public int getSize() {
            return size;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }

    /**
     * Void chunk generator for SkyBlock
     */
    public static class VoidChunkGenerator implements org.bukkit.generator.ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            ChunkData chunk = org.bukkit.generator.ChunkGenerator.createChunkData(world);
            return chunk;
        }

        @Override
        public boolean canSpawn(World world, int x, int z) {
            return false;
        }

        @Override
        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0, 100, 0);
        }
    }
}
