package com.kushsmp.essentials.vault;

import com.kushsmp.essentials.CustomEssentials;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;

/**
 * Exposes KushEssentials' economy to Vault, so any Vault-aware plugin (shops,
 * jobs, etc.) reads and writes the same balances as /balance, /pay and /eco.
 *
 * Player operations delegate to EconomyManager. Banks are not supported and
 * return NOT_IMPLEMENTED. This class references Vault types, so it must only be
 * loaded when Vault is installed (the main class guards registration with a
 * Vault presence check).
 */
public class VaultEconomyProvider implements Economy {

    private static final String NO_BANKS = "KushEssentials does not support bank accounts.";

    private final CustomEssentials plugin;

    public VaultEconomyProvider(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    // ----- Metadata -----

    @Override
    public boolean isEnabled() {
        return plugin.economy().enabled();
    }

    @Override
    public String getName() {
        return "KushEssentials";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return plugin.economy().format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return plugin.config().getConfig().getString("economy.currency-name-plural", "Coins");
    }

    @Override
    public String currencyNameSingular() {
        return plugin.config().getConfig().getString("economy.currency-name-singular", "Coin");
    }

    // ----- Accounts -----

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true; // implicit accounts; everyone starts at starting-balance
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAccount(String playerName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true; // nothing to do - accounts are implicit
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    // ----- Balances -----

    @Override
    public double getBalance(OfflinePlayer player) {
        return plugin.economy().getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    @SuppressWarnings("deprecation")
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    @SuppressWarnings("deprecation")
    public double getBalance(String playerName, String world) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return plugin.economy().has(player.getUniqueId(), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean has(String playerName, double amount) {
        return has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean has(String playerName, String worldName, double amount) {
        return has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    // ----- Withdraw / deposit -----

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return fail("Cannot withdraw a negative amount.");
        }
        if (!plugin.economy().withdraw(player.getUniqueId(), amount)) {
            return new EconomyResponse(0, plugin.economy().getBalance(player.getUniqueId()),
                    ResponseType.FAILURE, "Insufficient funds.");
        }
        return ok(amount, plugin.economy().getBalance(player.getUniqueId()));
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return fail("Cannot deposit a negative amount.");
        }
        plugin.economy().deposit(player.getUniqueId(), amount);
        return ok(amount, plugin.economy().getBalance(player.getUniqueId()));
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    // ----- Banks (unsupported) -----

    @Override
    public EconomyResponse createBank(String name, String player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    // ----- Helpers -----

    private EconomyResponse ok(double amount, double balance) {
        return new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
    }

    private EconomyResponse fail(String message) {
        return new EconomyResponse(0, 0, ResponseType.FAILURE, message);
    }

    private EconomyResponse notImplemented() {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, NO_BANKS);
    }
}
