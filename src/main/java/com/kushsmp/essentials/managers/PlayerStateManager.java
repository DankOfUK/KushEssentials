package com.kushsmp.essentials.managers;

import com.kushsmp.essentials.CustomEssentials;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Holds transient per-player state that does not need to survive restarts:
 * vanish, god mode, freeze, AFK, staff-chat toggle, and last-message targets.
 */
public class PlayerStateManager {

    private final CustomEssentials plugin;

    private final Set<UUID> vanished = new HashSet<>();
    private final Set<UUID> god = new HashSet<>();
    private final Set<UUID> frozen = new HashSet<>();
    private final Set<UUID> afk = new HashSet<>();
    private final Set<UUID> staffChatToggled = new HashSet<>();

    // For /reply: maps a player to the last person they messaged or were messaged by.
    private final Map<UUID, UUID> lastMessaged = new HashMap<>();
    // Tracks last activity time for auto-AFK.
    private final Map<UUID, Long> lastActivity = new HashMap<>();

    public PlayerStateManager(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    // ----- Vanish -----
    public boolean isVanished(UUID uuid) { return vanished.contains(uuid); }
    public void setVanished(UUID uuid, boolean value) {
        if (value) vanished.add(uuid); else vanished.remove(uuid);
    }

    // ----- God -----
    public boolean isGod(UUID uuid) { return god.contains(uuid); }
    public void setGod(UUID uuid, boolean value) {
        if (value) god.add(uuid); else god.remove(uuid);
    }

    // ----- Freeze -----
    public boolean isFrozen(UUID uuid) { return frozen.contains(uuid); }
    public void setFrozen(UUID uuid, boolean value) {
        if (value) frozen.add(uuid); else frozen.remove(uuid);
    }

    // ----- AFK -----
    public boolean isAfk(UUID uuid) { return afk.contains(uuid); }
    public void setAfk(UUID uuid, boolean value) {
        if (value) afk.add(uuid); else afk.remove(uuid);
    }

    // ----- Staff chat toggle -----
    public boolean isStaffChatToggled(UUID uuid) { return staffChatToggled.contains(uuid); }
    public void setStaffChatToggled(UUID uuid, boolean value) {
        if (value) staffChatToggled.add(uuid); else staffChatToggled.remove(uuid);
    }

    // ----- Messaging -----
    public void setLastMessaged(UUID player, UUID target) { lastMessaged.put(player, target); }
    public UUID getLastMessaged(UUID player) { return lastMessaged.get(player); }

    // ----- Activity / auto-AFK -----
    public void markActive(UUID uuid) { lastActivity.put(uuid, System.currentTimeMillis()); }
    public long getLastActivity(UUID uuid) { return lastActivity.getOrDefault(uuid, System.currentTimeMillis()); }

    /** Wipe all transient state for a player (called on quit). */
    public void clear(UUID uuid) {
        vanished.remove(uuid);
        god.remove(uuid);
        frozen.remove(uuid);
        afk.remove(uuid);
        staffChatToggled.remove(uuid);
        lastMessaged.remove(uuid);
        lastActivity.remove(uuid);
    }

    /** Apply or remove vanish visibility/effects relative to all online players. */
    public void applyVanishState(Player player, boolean vanish) {
        for (Player other : plugin.getServer().getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }
            if (vanish && !other.hasPermission("essentials.vanish.see")) {
                other.hidePlayer(plugin, player);
            } else {
                other.showPlayer(plugin, player);
            }
        }
        if (vanish && plugin.config().getConfig().getBoolean("staff.vanish.night-vision", true)) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        } else {
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        }
    }
}
