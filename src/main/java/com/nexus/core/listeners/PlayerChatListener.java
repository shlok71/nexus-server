package com.nexus.core.listeners;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Pattern;

/**
 * Listener for player chat events - handles chat formatting and filtering
 */
public class PlayerChatListener implements Listener {

    private final NexusCore plugin;

    // Patterns for chat filtering
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?"
    );
    private static final Pattern IP_PATTERN = Pattern.compile(
        "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b"
    );

    public PlayerChatListener(NexusCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if player is authenticated (for cracked servers)
        if (plugin.getAuthSystem().isAuthEnabled() && !plugin.getAuthSystem().isAuthenticated(player)) {
            player.sendMessage(ChatColor.RED + "You must be authenticated to chat!");
            event.setCancelled(true);
            return;
        }

        // Apply chat format
        String format = getChatFormat(player);
        event.setFormat(format);

        // Filter message
        message = filterMessage(player, message);
        event.setMessage(message);

        // Add chat cooldown
        if (!checkChatCooldown(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Chat cooldown active! Please wait before sending another message.");
        }
    }

    /**
     * Get formatted chat message
     */
    private String getChatFormat(Player player) {
        String prefix = getPlayerPrefix(player);
        String name = player.getDisplayName();
        String group = getPlayerGroup(player);

        // Format: [Group] <Name> Message
        return ChatColor.GRAY + "[" + group + "] " + ChatColor.WHITE + name + ChatColor.GRAY + ": " + "%2$s";
    }

    /**
     * Get player's rank prefix
     */
    private String getPlayerPrefix(Player player) {
        if (player.isOp()) {
            return ChatColor.RED + "Admin";
        } else if (player.hasPermission("nexus.mod")) {
            return ChatColor.DARK_PURPLE + "Mod";
        } else if (player.hasPermission("nexus.vip")) {
            return ChatColor.GOLD + "VIP";
        } else if (player.hasPermission("nexus.premium")) {
            return ChatColor.AQUA + "Premium";
        }
        return ChatColor.WHITE + "Player";
    }

    /**
     * Get player's group name
     */
    private String getPlayerGroup(Player player) {
        if (player.isOp()) {
            return ChatColor.RED + "[OP]";
        } else if (player.hasPermission("nexus.mod")) {
            return ChatColor.DARK_PURPLE + "[Mod]";
        } else if (player.hasPermission("nexus.vip")) {
            return ChatColor.GOLD + "[VIP]";
        } else if (player.hasPermission("nexus.premium")) {
            return ChatColor.AQUA + "[Premium]";
        }
        return "";
    }

    /**
     * Filter chat message for spam, links, and inappropriate content
     */
    private String filterMessage(Player player, String message) {
        // Translate color codes
        message = ChatColor.translateAlternateColorCodes('&', message);

        // Check for URLs
        if (!player.hasPermission("nexus.chat.links")) {
            message = URL_PATTERN.matcher(message).replaceAll(ChatColor.GRAY + "[link removed]");
        }

        // Check for IP addresses
        if (!player.hasPermission("nexus.chat.ips")) {
            message = IP_PATTERN.matcher(message).replaceAll(ChatColor.GRAY + "[IP removed]");
        }

        // Check for spam (repeated characters)
        if (hasSpam(message)) {
            message = ChatColor.RED + "[Blocked: Spam]";
        }

        // Limit message length
        if (message.length() > 100) {
            message = message.substring(0, 97) + "...";
        }

        return message;
    }

    /**
     * Check for spam patterns
     */
    private boolean hasSpam(String message) {
        // Check for repeated characters (more than 5 same chars in a row)
        for (int i = 0; i < message.length() - 5; i++) {
            char c = message.charAt(i);
            boolean repeated = true;
            for (int j = 1; j < 5; j++) {
                if (message.charAt(i + j) != c) {
                    repeated = false;
                    break;
                }
            }
            if (repeated) {
                return true;
            }
        }

        // Check for repeated words
        String[] words = message.toLowerCase().split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].equals(words[i + 1]) && words[i].length() > 2) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check chat cooldown for player
     */
    private java.util.concurrent.ConcurrentHashMap<Player, Long> chatCooldowns = new java.util.concurrent.ConcurrentHashMap<>();

    private boolean checkChatCooldown(Player player) {
        long cooldown = plugin.getConfigManager().getInt("chat.cooldown-seconds", 2) * 1000L;
        long lastChat = chatCooldowns.getOrDefault(player, 0L);

        if (System.currentTimeMillis() - lastChat < cooldown) {
            return false;
        }

        chatCooldowns.put(player, System.currentTimeMillis());
        return true;
    }
}
