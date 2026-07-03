package com.kushsmp.essentials.managers;

import com.kushsmp.essentials.CustomEssentials;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

/**
 * Persists chat mutes in userdata.yml. A mute can be permanent (expiry == -1)
 * or timed (expiry == epoch millis).
 */
public class MuteManager {

    private static final String DATA = "userdata.yml";

    private final CustomEssentials plugin;

    public MuteManager(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration data() {
        return plugin.config().getData(DATA);
    }

    public void mute(UUID uuid, long expiryMillis, String reason) {
        data().set("mutes." + uuid + ".expiry", expiryMillis);
        data().set("mutes." + uuid + ".reason", reason);
        plugin.config().saveData(DATA);
    }

    public void unmute(UUID uuid) {
        data().set("mutes." + uuid, null);
        plugin.config().saveData(DATA);
    }

    /**
     * @return true if the player is currently muted. Expired mutes are cleared lazily.
     */
    public boolean isMuted(UUID uuid) {
        if (!data().contains("mutes." + uuid)) {
            return false;
        }
        long expiry = data().getLong("mutes." + uuid + ".expiry", -1);
        if (expiry == -1) {
            return true; // permanent
        }
        if (System.currentTimeMillis() >= expiry) {
            unmute(uuid);
            return false;
        }
        return true;
    }

    public long getExpiry(UUID uuid) {
        return data().getLong("mutes." + uuid + ".expiry", -1);
    }

    public String getReason(UUID uuid) {
        return data().getString("mutes." + uuid + ".reason", "Muted by staff.");
    }
}
