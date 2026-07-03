package com.kushsmp.essentials.managers;

import com.kushsmp.essentials.CustomEssentials;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Simple flat-file economy. Balances are stored in userdata.yml under
 * "economy.&lt;uuid&gt;" so it reuses the same data file as ranks/mutes/cooldowns.
 * All money values are rounded to 2 decimal places.
 */
public class EconomyManager {

    private static final String USERDATA = "userdata.yml";

    private final CustomEssentials plugin;
    private final DecimalFormat format;

    public EconomyManager(CustomEssentials plugin) {
        this.plugin = plugin;
        this.format = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.US));
    }

    private FileConfiguration data() {
        return plugin.config().getData(USERDATA);
    }

    private void save() {
        plugin.config().saveData(USERDATA);
    }

    // ----- Config-backed settings -----

    public boolean enabled() {
        return plugin.config().getConfig().getBoolean("economy.enabled", true);
    }

    public boolean payEnabled() {
        return plugin.config().getConfig().getBoolean("economy.pay-enabled", true);
    }

    public double startingBalance() {
        return plugin.config().getConfig().getDouble("economy.starting-balance", 100.0);
    }

    public double minPayment() {
        return plugin.config().getConfig().getDouble("economy.min-payment", 1.0);
    }

    public String symbol() {
        return plugin.config().getConfig().getString("economy.currency-symbol", "$");
    }

    // ----- Balance operations -----

    public double getBalance(UUID uuid) {
        return round(data().getDouble("economy." + uuid, startingBalance()));
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public void set(UUID uuid, double amount) {
        data().set("economy." + uuid, round(Math.max(0, amount)));
        save();
    }

    public void deposit(UUID uuid, double amount) {
        if (amount <= 0) return;
        set(uuid, getBalance(uuid) + amount);
    }

    /** Returns false (and changes nothing) if the player can't afford it. */
    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double bal = getBalance(uuid);
        if (bal < amount) return false;
        set(uuid, bal - amount);
        return true;
    }

    /** Atomic-ish move from one player to another with a single save. */
    public boolean transfer(UUID from, UUID to, double amount) {
        if (amount <= 0 || !has(from, amount)) return false;
        double f = getBalance(from);
        double t = getBalance(to);
        data().set("economy." + from, round(f - amount));
        data().set("economy." + to, round(t + amount));
        save();
        return true;
    }

    public void reset(UUID uuid) {
        set(uuid, startingBalance());
    }

    // ----- Display -----

    public String format(double amount) {
        return symbol() + format.format(round(amount));
    }

    /** Top balances, highest first. */
    public List<Map.Entry<UUID, Double>> top(int limit) {
        List<Map.Entry<UUID, Double>> list = new ArrayList<>();
        ConfigurationSection section = data().getConfigurationSection("economy");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    list.add(Map.entry(uuid, round(section.getDouble(key))));
                } catch (IllegalArgumentException ignored) {
                    // Not a UUID key - skip.
                }
            }
        }
        list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
