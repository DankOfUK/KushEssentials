package com.kushsmp.essentials.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Serializes Bukkit Locations to/from configuration sections.
 */
public final class LocationUtil {

    private LocationUtil() {
    }

    public static void write(ConfigurationSection section, Location loc) {
        section.set("world", loc.getWorld() != null ? loc.getWorld().getName() : "world");
        section.set("x", loc.getX());
        section.set("y", loc.getY());
        section.set("z", loc.getZ());
        section.set("yaw", (double) loc.getYaw());
        section.set("pitch", (double) loc.getPitch());
    }

    public static Location read(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String worldName = section.getString("world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }
}
