package com.nexus.economy;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Economy manager for NexusBlock Network
 * Handles coins and gems currency system
 */
public class EconomyManager {

    private final NexusCore plugin;
    private final Map<UUID, Long> coins;
    private final Map<UUID, Integer> gems;
    private boolean useVault;
    private long startingCoins;
    private int startingGems;

    // Economy file path
    private static final String ECONOMY_FILE = "economy.json";

    public EconomyManager(NexusCore plugin) {
        this.plugin = plugin;
        this.coins = new HashMap<>();
        this.gems = new HashMap<>();
    }

    /**
     * Initialize economy system
     */
    public boolean initialize() {
        // Try to hook into Vault
        useVault = hookVault();

        if (useVault) {
            plugin.getNexusLogger().info("Economy system: Using Vault");
            return true;
        }

        // Use internal economy
        return initializeInternal();
    }

    /**
     * Initialize internal economy system
     */
    public boolean initializeInternal() {
        useVault = false;
        startingCoins = plugin.getConfigManager().getLong("economy.starting-coins", 1000);
        startingGems = plugin.getConfigManager().getInt("economy.starting-gems", 0);

        loadEconomyData();

        plugin.getNexusLogger().info("Economy system: Using internal economy");
        plugin.getNexusLogger().info("Starting balance: " + startingCoins + " coins, " + startingGems + " gems");
        return true;
    }

    /**
     * Hook into Vault economy
     */
    private boolean hookVault() {
        try {
            org.bukkit.plugin.Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
            if (vaultPlugin == null) {
                return false;
            }

            ServicesManager servicesManager = Bukkit.getServicesManager();
            net.milkbowl.vault.economy.Economy vaultEconomy = servicesManager.getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();

            if (vaultEconomy != null) {
                // Register our economy as fallback
                servicesManager.register(net.milkbowl.vault.economy.Economy.class, new NexusVaultEconomy(this), plugin, ServicePriority.Low);
                return true;
            }

        } catch (Exception e) {
            plugin.getNexusLogger().warning("Failed to hook Vault: " + e.getMessage());
        }

        return false;
    }

    /**
     * Load economy data from file
     */
    public void loadEconomyData() {
        File economyFile = new File(plugin.getDataFolder(), ECONOMY_FILE);

        if (!economyFile.exists()) {
            plugin.getNexusLogger().info("No economy data found, starting fresh");
            return;
        }

        try (FileReader reader = new FileReader(economyFile)) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            EconomyData data = gson.fromJson(reader, EconomyData.class);

            if (data != null) {
                if (data.coins != null) {
                    coins.putAll(data.coins);
                }
                if (data.gems != null) {
                    gems.putAll(data.gems);
                }
                plugin.getNexusLogger().info("Loaded economy data for " + coins.size() + " players");
            }

        } catch (IOException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to load economy data", e);
        }
    }

    /**
     * Save economy data to file
     */
    public void saveEconomyData() {
        File economyFile = new File(plugin.getDataFolder(), ECONOMY_FILE);

        try (FileWriter writer = new FileWriter(economyFile)) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            EconomyData data = new EconomyData();
            data.coins = new HashMap<>(coins);
            data.gems = new HashMap<>(gems);
            gson.toJson(data, writer);

            plugin.getNexusLogger().info("Saved economy data");

        } catch (IOException e) {
            plugin.getNexusLogger().log(Level.WARNING, "Failed to save economy data", e);
        }
    }

    /**
     * Save all player balances
     */
    public void saveAllBalances() {
        saveEconomyData();
    }

    /**
     * Get player balance
     */
    public long getBalance(UUID player) {
        if (useVault) {
            net.milkbowl.vault.economy.Economy vaultEco = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
            return (long) vaultEco.getBalance(Bukkit.getOfflinePlayer(player));
        }

        return coins.getOrDefault(player, startingCoins);
    }

    /**
     * Get player balance (Player overload)
     */
    public long getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    /**
     * Set player balance
     */
    public void setBalance(UUID player, long amount) {
        if (useVault) {
            net.milkbowl.vault.economy.Economy vaultEco = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
            net.milkbowl.vault.economy.EconomyResponse response = vaultEco.bankBalance(Bukkit.getOfflinePlayer(player).getName());
            if (response.transactionSuccess()) {
                vaultEco.bankWithdraw(Bukkit.getOfflinePlayer(player).getName(), response.balance);
                vaultEco.bankDeposit(Bukkit.getOfflinePlayer(player).getName(), amount);
            }
            return;
        }

        coins.put(player, Math.max(0, amount));
    }

    /**
     * Add coins to player
     */
    public boolean addCoins(UUID player, long amount) {
        if (amount <= 0) {
            return false;
        }

        if (useVault) {
            net.milkbowl.vault.economy.Economy vaultEco = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
            return vaultEco.depositPlayer(Bukkit.getOfflinePlayer(player), amount).transactionSuccess();
        }

        long current = coins.getOrDefault(player, startingCoins);
        coins.put(player, current + amount);
        return true;
    }

    /**
     * Remove coins from player
     */
    public boolean removeCoins(UUID player, long amount) {
        if (amount <= 0) {
            return false;
        }

        if (useVault) {
            net.milkbowl.vault.economy.Economy vaultEco = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
            return vaultEco.withdrawPlayer(Bukkit.getOfflinePlayer(player), amount).transactionSuccess();
        }

        long current = coins.getOrDefault(player, startingCoins);
        if (current < amount) {
            return false;
        }

        coins.put(player, current - amount);
        return true;
    }

    /**
     * Check if player has enough coins
     */
    public boolean hasCoins(UUID player, long amount) {
        return getBalance(player) >= amount;
    }

    /**
     * Get player gems
     */
    public int getGems(UUID player) {
        return gems.getOrDefault(player, startingGems);
    }

    /**
     * Set player gems
     */
    public void setGems(UUID player, int amount) {
        gems.put(player, Math.max(0, amount));
    }

    /**
     * Add gems to player
     */
    public void addGems(UUID player, int amount) {
        int current = gems.getOrDefault(player, startingGems);
        gems.put(player, current + amount);
    }

    /**
     * Remove gems from player
     */
    public boolean removeGems(UUID player, int amount) {
        if (amount <= 0) {
            return false;
        }

        int current = gems.getOrDefault(player, startingGems);
        if (current < amount) {
            return false;
        }

        gems.put(player, current - amount);
        return true;
    }

    /**
     * Format coins for display
     */
    public String formatCoins(long amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000.0);
        }
        return String.valueOf(amount);
    }

    /**
     * Format gems for display
     */
    public String formatGems(int amount) {
        return String.valueOf(amount);
    }

    /**
     * Save individual player balance
     */
    public void savePlayerBalance(UUID player) {
        // Balance is saved periodically with all data
    }

    /**
     * Get total server coins
     */
    public long getTotalCoins() {
        return coins.values().stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Get total server gems
     */
    public int getTotalGems() {
        return gems.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Economy data class for JSON serialization
     */
    private static class EconomyData {
        Map<UUID, Long> coins;
        Map<UUID, Integer> gems;
    }
}
