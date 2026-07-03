package com.kushsmp.essentials.config;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Central loader for config.yml, kits.yml, messages.yml and the data files
 * (homes.yml, warps.yml). Each data file is saved on demand.
 */
public class ConfigManager {

    private final CustomEssentials plugin;

    private FileConfiguration config;
    private FileConfiguration kits;
    private FileConfiguration messages;

    private final Map<String, FileConfiguration> dataFiles = new HashMap<>();
    private final Map<String, File> dataFileHandles = new HashMap<>();

    public ConfigManager(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        // Bundled resources copied on first run.
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        kits = loadResource("kits.yml");
        messages = loadResource("messages.yml");

        // Pure data files (not shipped as resources).
        loadData("homes.yml");
        loadData("warps.yml");
        loadData("spawn.yml");
        loadData("userdata.yml");
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        kits = loadResource("kits.yml");
        messages = loadResource("messages.yml");
        // Data files are reloaded from disk too.
        reloadData("homes.yml");
        reloadData("warps.yml");
        reloadData("spawn.yml");
        reloadData("userdata.yml");
    }

    private FileConfiguration loadResource(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void loadData(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create data file " + name + ": " + e.getMessage());
            }
        }
        dataFileHandles.put(name, file);
        dataFiles.put(name, YamlConfiguration.loadConfiguration(file));
    }

    private void reloadData(String name) {
        File file = dataFileHandles.get(name);
        if (file != null) {
            dataFiles.put(name, YamlConfiguration.loadConfiguration(file));
        } else {
            loadData(name);
        }
    }

    /** Persist in-memory changes to kits.yml (used by /kit create and /kit delete). */
    public void saveKits() {
        try {
            kits.save(new File(plugin.getDataFolder(), "kits.yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save kits.yml: " + e.getMessage());
        }
    }

    public void saveData(String name) {
        FileConfiguration cfg = dataFiles.get(name);
        File file = dataFileHandles.get(name);
        if (cfg == null || file == null) {
            return;
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save data file " + name + ": " + e.getMessage());
        }
    }

    // ----- Accessors -----

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getKits() {
        return kits;
    }

    public FileConfiguration getData(String name) {
        return dataFiles.get(name);
    }

    // ----- Messaging -----

    public String prefix() {
        return TextUtil.color(config.getString("general.prefix", ""));
    }

    /**
     * Look up a message key from messages.yml, apply {prefix} and color, and
     * replace the supplied placeholders (passed as key, value, key, value...).
     */
    public String msg(String key, String... replacements) {
        String raw = messages.getString(key, "&cMissing message: " + key);
        raw = raw.replace("{prefix}", config.getString("general.prefix", ""));
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return TextUtil.color(raw);
    }

    /**
     * Send a templated message straight to a sender.
     */
    public void send(CommandSender to, String key, String... replacements) {
        to.sendMessage(msg(key, replacements));
    }
}
