package com.kushsmp.essentials.managers;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages server warps (warps.yml) and the global spawn point (spawn.yml).
 */
public class WarpManager {

    private static final String WARP_FILE = "warps.yml";
    private static final String SPAWN_FILE = "spawn.yml";

    private final CustomEssentials plugin;

    public WarpManager(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration warpData() {
        return plugin.config().getData(WARP_FILE);
    }

    private FileConfiguration spawnData() {
        return plugin.config().getData(SPAWN_FILE);
    }

    // ----- Warps -----

    public boolean warpsEnabled() {
        return plugin.config().getConfig().getBoolean("regular.warps.enabled", true);
    }

    public List<String> getWarps() {
        ConfigurationSection section = warpData().getConfigurationSection("warps");
        if (section == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(section.getKeys(false));
    }

    public boolean hasWarp(String name) {
        return warpData().contains("warps." + name.toLowerCase());
    }

    public Location getWarp(String name) {
        return LocationUtil.read(warpData().getConfigurationSection("warps." + name.toLowerCase()));
    }

    public void setWarp(String name, Location loc) {
        ConfigurationSection section = warpData().createSection("warps." + name.toLowerCase());
        LocationUtil.write(section, loc);
        plugin.config().saveData(WARP_FILE);
    }

    public void deleteWarp(String name) {
        warpData().set("warps." + name.toLowerCase(), null);
        plugin.config().saveData(WARP_FILE);
    }

    // ----- Spawn -----

    public boolean spawnEnabled() {
        return plugin.config().getConfig().getBoolean("regular.spawn.enabled", true);
    }

    public Location getSpawn() {
        return LocationUtil.read(spawnData().getConfigurationSection("spawn"));
    }

    public boolean isSpawnSet() {
        return spawnData().contains("spawn");
    }

    public void setSpawn(Location loc) {
        ConfigurationSection section = spawnData().createSection("spawn");
        LocationUtil.write(section, loc);
        plugin.config().saveData(SPAWN_FILE);
    }
}
