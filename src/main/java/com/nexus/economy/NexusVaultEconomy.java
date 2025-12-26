package com.nexus.economy;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.logging.Level;

/**
 * Vault economy implementation for NexusCore
 * Bridges internal economy with Vault
 */
public class NexusVaultEconomy implements net.milkbowl.vault.economy.Economy {

    private final EconomyManager economyManager;

    public NexusVaultEconomy(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public String getName() {
        return "NexusCore Economy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        return economyManager.formatCoins((long) amount);
    }

    @Override
    public String currencyNamePlural() {
        return "coins";
    }

    @Override
    public String currencyNameSingular() {
        return "coin";
    }

    @Override
    public boolean hasAccount(String playerName) {
        return true; // All players have accounts
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public double getBalance(String playerName) {
        return economyManager.getBalance(java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes()));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economyManager.getBalance(player.getUniqueId());
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount");
        }

        boolean success = economyManager.removeCoins(player.getUniqueId(), (long) amount);
        if (success) {
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
        } else {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount");
        }

        economyManager.addCoins(player.getUniqueId(), (long) amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private OfflinePlayer getOfflinePlayer(String playerName) {
        // Try to find online player first
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(playerName);
        if (player != null) {
            return player;
        }

        // Fallback to offline player
        return org.bukkit.Bukkit.getOfflinePlayer(java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes()));
    }
}
