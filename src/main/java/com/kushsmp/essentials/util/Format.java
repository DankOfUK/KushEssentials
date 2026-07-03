package com.kushsmp.essentials.util;

import com.kushsmp.essentials.CustomEssentials;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * Central formatting pipeline for tab/title/welcome text:
 *   1. replace built-in tokens ({player}, {online}, {max}) so they work even
 *      without PlaceholderAPI,
 *   2. resolve %papi% placeholders if PlaceholderAPI is present,
 *   3. translate & and &#RRGGBB colors via the existing TextUtil.
 */
public final class Format {

    private Format() {
    }

    /** Format to a legacy section-sign string. */
    public static String line(CustomEssentials plugin, Player player, String raw) {
        if (raw == null) return "";
        String s = raw
                .replace("{player}", player.getName())
                .replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(plugin.getServer().getMaxPlayers()));
        s = plugin.papi().apply(player, s);
        return TextUtil.color(s);
    }

    /** Format to an Adventure Component (for tab list / titles). */
    public static Component component(CustomEssentials plugin, Player player, String raw) {
        return Components.of(line(plugin, player, raw));
    }
}
