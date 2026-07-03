package com.kushsmp.essentials.util;

import com.kushsmp.essentials.CustomEssentials;
import org.bukkit.entity.Player;

/**
 * Optional PlaceholderAPI integration. If PlaceholderAPI is not installed the
 * plugin still works - placeholders are simply left untouched. The PlaceholderAPI
 * class is only referenced inside apply(), which is guarded by the enabled flag,
 * so no class-loading errors occur when PAPI is absent.
 */
public class PapiBridge {

    private final boolean enabled;

    public PapiBridge(CustomEssentials plugin) {
        this.enabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (enabled) {
            plugin.getLogger().info("PlaceholderAPI found - placeholder support enabled.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Resolve %placeholders% for the given player, or return the text unchanged. */
    public String apply(Player player, String text) {
        if (!enabled || text == null || player == null) {
            return text;
        }
        try {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        } catch (Throwable t) {
            return text;
        }
    }
}
