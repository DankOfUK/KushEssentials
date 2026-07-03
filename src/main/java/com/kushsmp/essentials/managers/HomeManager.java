package com.kushsmp.essentials.managers;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stores and retrieves player homes in homes.yml under
 * homes.&lt;uuid&gt;.&lt;name&gt;.
 */
public class HomeManager {

    private static final String FILE = "homes.yml";

    private final CustomEssentials plugin;

    public HomeManager(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration data() {
        return plugin.config().getData(FILE);
    }

    public boolean isEnabled() {
        return plugin.config().getConfig().getBoolean("regular.homes.enabled", true);
    }

    public List<String> getHomes(UUID uuid) {
        ConfigurationSection section = data().getConfigurationSection("homes." + uuid);
        if (section == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(section.getKeys(false));
    }

    public Location getHome(UUID uuid, String name) {
        ConfigurationSection section =
                data().getConfigurationSection("homes." + uuid + "." + name.toLowerCase());
        return LocationUtil.read(section);
    }

    public boolean hasHome(UUID uuid, String name) {
        return data().contains("homes." + uuid + "." + name.toLowerCase());
    }

    public void setHome(UUID uuid, String name, Location loc) {
        ConfigurationSection section =
                data().createSection("homes." + uuid + "." + name.toLowerCase());
        LocationUtil.write(section, loc);
        plugin.config().saveData(FILE);
    }

    public void deleteHome(UUID uuid, String name) {
        data().set("homes." + uuid + "." + name.toLowerCase(), null);
        plugin.config().saveData(FILE);
    }

    /**
     * Resolve the maximum number of homes for a player. Scans for the highest
     * essentials.homes.&lt;n&gt; permission, falling back to the configured default.
     */
    public int getMaxHomes(Player player) {
        int max = plugin.config().getConfig().getInt("regular.homes.default-max-homes", 3);
        if (player.hasPermission("essentials.homes.unlimited")) {
            return Integer.MAX_VALUE;
        }
        for (org.bukkit.permissions.PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String perm = info.getPermission();
            if (info.getValue() && perm.startsWith("essentials.homes.")) {
                String suffix = perm.substring("essentials.homes.".length());
                try {
                    max = Math.max(max, Integer.parseInt(suffix));
                } catch (NumberFormatException ignored) {
                    // "unlimited" or non-numeric suffixes are skipped here.
                }
            }
        }
        return max;
    }
}
