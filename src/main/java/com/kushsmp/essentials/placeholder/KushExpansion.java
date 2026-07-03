package com.kushsmp.essentials.placeholder;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.ranks.Rank;
import com.kushsmp.essentials.util.TextUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * Exposes this plugin's rank data to PlaceholderAPI so other plugins (and our
 * own tab/title configs) can use %kush_rank%, %kush_rank_prefix%, etc.
 *
 * IMPORTANT: this class references PlaceholderAPI types, so it must only be
 * loaded/instantiated when PlaceholderAPI is installed (the main class guards
 * this with a PapiBridge#isEnabled() check).
 */
public class KushExpansion extends PlaceholderExpansion {

    private final CustomEssentials plugin;

    public KushExpansion(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "kush";
    }

    @Override
    public String getAuthor() {
        return "DankOfUK";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        // Keep the expansion registered across PlaceholderAPI reloads.
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }
        Rank rank = plugin.ranks().getPlayerRank(player.getUniqueId());
        if (rank == null) {
            return "";
        }
        switch (params.toLowerCase()) {
            case "rank":
            case "rank_display":
                return rank.getDisplay();
            case "rank_name":
                return rank.getName();
            case "rank_prefix":
                return TextUtil.color(rank.getPrefix());
            case "rank_suffix":
                return TextUtil.color(rank.getSuffix());
            case "rank_color":
                return TextUtil.color(rank.getColor());
            case "rank_weight":
                return String.valueOf(rank.getWeight());
            default:
                return null;
        }
    }
}
