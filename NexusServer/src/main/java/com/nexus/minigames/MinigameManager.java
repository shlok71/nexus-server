package com.nexus.minigames;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minigame manager for NexusBlock Network
 * Provides framework for creating and managing minigames
 */
public class MinigameManager {

    private final NexusCore plugin;
    private final Map<String, Minigame> registeredGames;
    private final Map<UUID, String> playerGames;
    private final Map<String, Set<Player>> activePlayers;
    private final Random random;

    public MinigameManager(NexusCore plugin) {
        this.plugin = plugin;
        this.registeredGames = new ConcurrentHashMap<>();
        this.playerGames = new ConcurrentHashMap<>();
        this.activePlayers = new ConcurrentHashMap<>();
        this.random = new Random();
    }

    /**
     * Initialize minigame system
     */
    public void initialize() {
        // Register default minigames
        registerDefaultMinigames();

        plugin.getNexusLogger().info("MinigameManager initialized with " + registeredGames.size() + " games");
    }

    /**
     * Register default minigames
     */
    private void registerDefaultMinigames() {
        // These would be registered in a full implementation
        // registerMinigame(new BedWars(this));
        // registerMinigame(new Duels(this));
        // registerMinigame(new Parkour(this));
    }

    /**
     * Register a new minigame
     */
    public void registerMinigame(Minigame minigame) {
        registeredGames.put(minigame.getId().toLowerCase(), minigame);
        plugin.getNexusLogger().info("Registered minigame: " + minigame.getName());
    }

    /**
     * Unregister a minigame
     */
    public void unregisterMinigame(String id) {
        registeredGames.remove(id.toLowerCase());
    }

    /**
     * Get a minigame by ID
     */
    public Minigame getMinigame(String id) {
        return registeredGames.get(id.toLowerCase());
    }

    /**
     * Get all registered minigames
     */
    public Collection<Minigame> getAllMinigames() {
        return registeredGames.values();
    }

    /**
     * Join a minigame
     */
    public boolean joinMinigame(Player player, String gameId) {
        Minigame minigame = registeredGames.get(gameId.toLowerCase());

        if (minigame == null) {
            player.sendMessage(ChatColor.RED + "Minigame not found: " + gameId);
            return false;
        }

        // Check if player is already in a game
        if (playerGames.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in a game! Leave first with /game leave");
            return false;
        }

        // Check if game is full
        if (minigame.isFull()) {
            player.sendMessage(ChatColor.RED + "This game is full!");
            return false;
        }

        // Save player inventory
        savePlayerInventory(player);

        // Add player to game
        boolean success = minigame.addPlayer(player);

        if (success) {
            playerGames.put(player.getUniqueId(), gameId.toLowerCase());

            Set<Player> players = activePlayers.computeIfAbsent(gameId.toLowerCase(), k -> new HashSet<>());
            players.add(player);

            player.sendMessage(ChatColor.GREEN + "Joined " + minigame.getName() + "!");
            minigame.onPlayerJoin(player);
        }

        return success;
    }

    /**
     * Leave current minigame
     */
    public boolean leaveMinigame(Player player) {
        UUID playerId = player.getUniqueId();
        String gameId = playerGames.get(playerId);

        if (gameId == null) {
            player.sendMessage(ChatColor.RED + "You are not in a game!");
            return false;
        }

        Minigame minigame = registeredGames.get(gameId);
        if (minigame != null) {
            minigame.removePlayer(player);
            minigame.onPlayerLeave(player);
        }

        // Remove from tracking
        playerGames.remove(playerId);

        Set<Player> players = activePlayers.get(gameId);
        if (players != null) {
            players.remove(player);
        }

        // Restore player inventory
        restorePlayerInventory(player);

        // Send back to hub
        plugin.getHubManager().sendToHub(player);

        player.sendMessage(ChatColor.YELLOW + "You left the game.");
        return true;
    }

    /**
     * Remove player from any game
     */
    public void removePlayerFromGame(Player player) {
        leaveMinigame(player);
    }

    /**
     * Check if player is in a minigame
     */
    public boolean isInMinigame(Player player) {
        return playerGames.containsKey(player.getUniqueId());
    }

    /**
     * Get the minigame a player is in
     */
    public Minigame getPlayerMinigame(Player player) {
        String gameId = playerGames.get(player.getUniqueId());
        if (gameId == null) {
            return null;
        }
        return registeredGames.get(gameId);
    }

    /**
     * Save player inventory
     */
    private void savePlayerInventory(Player player) {
        // In a real implementation, save to player data
        player.getInventory().clear();
    }

    /**
     * Restore player inventory
     */
    private void restorePlayerInventory(Player player) {
        // In a real implementation, restore from player data
        plugin.getHubManager().sendToHub(player);
    }

    /**
     * Get player count for a game
     */
    public int getPlayerCount(String gameId) {
        Set<Player> players = activePlayers.get(gameId.toLowerCase());
        return players != null ? players.size() : 0;
    }

    /**
     * Get total player count across all minigames
     */
    public int getTotalPlayerCount() {
        return playerGames.size();
    }

    /**
     * Minigame abstract class
     */
    public static abstract class Minigame {
        protected final MinigameManager manager;
        protected final String id;
        protected final String name;
        protected int maxPlayers;
        protected Location lobbyLocation;
        protected Location[] spawnLocations;
        protected boolean active;
        protected GameState state;

        public Minigame(MinigameManager manager, String id, String name, int maxPlayers) {
            this.manager = manager;
            this.id = id;
            this.name = name;
            this.maxPlayers = maxPlayers;
            this.active = false;
            this.state = GameState.WAITING;
        }

        public abstract boolean addPlayer(Player player);
        public abstract void removePlayer(Player player);
        public abstract void onPlayerJoin(Player player);
        public abstract void onPlayerLeave(Player player);
        public abstract void startGame();
        public abstract void endGame();

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public int getCurrentPlayers() {
            Set<Player> players = manager.activePlayers.get(id.toLowerCase());
            return players != null ? players.size() : 0;
        }

        public boolean isFull() {
            return getCurrentPlayers() >= maxPlayers;
        }

        public boolean isActive() {
            return active;
        }

        public GameState getState() {
            return state;
        }

        public void setLobbyLocation(Location location) {
            this.lobbyLocation = location;
        }

        public void setSpawnLocations(Location... locations) {
            this.spawnLocations = locations;
        }

        public Location getNextSpawn() {
            if (spawnLocations == null || spawnLocations.length == 0) {
                return lobbyLocation;
            }
            return spawnLocations[random.nextInt(spawnLocations.length)];
        }

        public void broadcast(String message) {
            Set<Player> players = manager.activePlayers.get(id.toLowerCase());
            if (players != null) {
                for (Player p : players) {
                    p.sendMessage(message);
                }
            }
        }

        public void broadcast(ChatColor color, String message) {
            broadcast(color + message);
        }

        protected final Random getRandom() {
            return manager.random;
        }

        protected final NexusCore getPlugin() {
            return manager.plugin;
        }
    }

    /**
     * Game state enumeration
     */
    public enum GameState {
        WAITING,
        STARTING,
        PLAYING,
        ENDING
    }
}
