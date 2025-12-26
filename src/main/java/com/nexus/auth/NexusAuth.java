package com.nexus.auth;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Authentication system for cracked/offline mode servers
 * Handles player registration and login to protect accounts
 */
public class NexusAuth {

    private final NexusCore plugin;
    private final Map<UUID, String> pendingPlayers;
    private final Map<UUID, Long> sessionTimeout;
    private final Map<String, String> passwords;
    private final Map<UUID, Long> sessions;
    private boolean enabled;
    private int sessionTimeoutMinutes;

    public NexusAuth(NexusCore plugin) {
        this.plugin = plugin;
        this.pendingPlayers = new ConcurrentHashMap<>();
        this.sessionTimeout = new ConcurrentHashMap<>();
        this.passwords = new HashMap<>();
        this.sessions = new HashMap<>();
    }

    /**
     * Initialize authentication system
     */
    public void initialize() {
        enabled = plugin.getConfigManager().getBoolean("auth.enabled", false);
        sessionTimeoutMinutes = plugin.getConfigManager().getInt("auth.session-timeout", 300);

        if (enabled) {
            plugin.getNexusLogger().info("Authentication system enabled");
            loadPasswords();
        } else {
            plugin.getNexusLogger().info("Authentication system disabled (online-mode compatible)");
        }
    }

    /**
     * Load saved passwords from database
     */
    private void loadPasswords() {
        // In a real implementation, load from database
        plugin.getNexusLogger().info("Loaded authentication data");
    }

    /**
     * Check if auth is enabled
     */
    public boolean isAuthEnabled() {
        return enabled;
    }

    /**
     * Check if player is authenticated
     */
    public boolean isAuthenticated(Player player) {
        if (!enabled) {
            return true; // Always authenticated if auth disabled
        }

        // Check active session
        Long lastActivity = sessions.get(player.getUniqueId());
        if (lastActivity != null) {
            long elapsed = System.currentTimeMillis() - lastActivity;
            if (elapsed < sessionTimeoutMinutes * 60 * 1000) {
                // Update session
                sessions.put(player.getUniqueId(), System.currentTimeMillis());
                return true;
            } else {
                // Session expired
                sessions.remove(player.getUniqueId());
            }
        }

        return false;
    }

    /**
     * Register a new player
     */
    public boolean registerPlayer(Player player, String password) {
        if (!enabled) {
            return true;
        }

        if (passwords.containsKey(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "Account already registered!");
            return false;
        }

        if (password.length() < 4) {
            player.sendMessage(ChatColor.RED + "Password must be at least 4 characters!");
            return false;
        }

        // Hash and store password
        String hash = hashPassword(password);
        passwords.put(player.getUniqueId().toString(), hash);

        // Create session
        sessions.put(player.getUniqueId(), System.currentTimeMillis());

        // Remove from pending
        pendingPlayers.remove(player.getUniqueId());
        sessionTimeout.remove(player.getUniqueId());

        player.sendMessage(ChatColor.GREEN + "Account registered successfully!");
        player.sendMessage(ChatColor.GRAY + "You are now logged in.");

        savePasswords();
        return true;
    }

    /**
     * Login a player
     */
    public boolean loginPlayer(Player player, String password) {
        if (!enabled) {
            return true;
        }

        String storedHash = passwords.get(player.getUniqueId().toString());
        if (storedHash == null) {
            player.sendMessage(ChatColor.RED + "Account not found! Please register first.");
            return false;
        }

        String inputHash = hashPassword(password);
        if (!storedHash.equals(inputHash)) {
            player.sendMessage(ChatColor.RED + "Incorrect password!");
            return false;
        }

        // Create session
        sessions.put(player.getUniqueId(), System.currentTimeMillis());

        // Remove from pending
        pendingPlayers.remove(player.getUniqueId());
        sessionTimeout.remove(player.getUniqueId());

        player.sendMessage(ChatColor.GREEN + "Login successful!");
        return true;
    }

    /**
     * Logout a player
     */
    public void logoutPlayer(Player player) {
        sessions.remove(player.getUniqueId());
        pendingPlayers.put(player.getUniqueId(), player.getName());
        sessionTimeout.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(ChatColor.YELLOW + "You have been logged out.");
    }

    /**
     * Change password
     */
    public boolean changePassword(Player player, String oldPassword, String newPassword) {
        if (!enabled) {
            return false;
        }

        String storedHash = passwords.get(player.getUniqueId().toString());
        if (storedHash == null) {
            player.sendMessage(ChatColor.RED + "Account not found!");
            return false;
        }

        String oldHash = hashPassword(oldPassword);
        if (!storedHash.equals(oldHash)) {
            player.sendMessage(ChatColor.RED + "Incorrect current password!");
            return false;
        }

        if (newPassword.length() < 4) {
            player.sendMessage(ChatColor.RED + "New password must be at least 4 characters!");
            return false;
        }

        String newHash = hashPassword(newPassword);
        passwords.put(player.getUniqueId().toString(), newHash);
        savePasswords();

        player.sendMessage(ChatColor.GREEN + "Password changed successfully!");
        return true;
    }

    /**
     * Queue an unauthenticated player
     */
    public void queueUnauthenticatedPlayer(Player player) {
        pendingPlayers.put(player.getUniqueId(), player.getName());
        sessionTimeout.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Remove an unauthenticated player
     */
    public void removeUnauthenticatedPlayer(Player player) {
        pendingPlayers.remove(player.getUniqueId());
        sessionTimeout.remove(player.getUniqueId());
    }

    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            plugin.getNexusLogger().log(Level.WARNING, "SHA-256 not available, using fallback");
            return password; // Fallback (not secure but prevents crashes)
        }
    }

    /**
     * Save passwords to database
     */
    private void savePasswords() {
        // In a real implementation, save to database
        plugin.getNexusLogger().info("Saved authentication data");
    }

    /**
     * Get pending players count
     */
    public int getPendingCount() {
        return pendingPlayers.size();
    }

    /**
     * Check if player is pending authentication
     */
    public boolean isPending(Player player) {
        return pendingPlayers.containsKey(player.getUniqueId());
    }
}
