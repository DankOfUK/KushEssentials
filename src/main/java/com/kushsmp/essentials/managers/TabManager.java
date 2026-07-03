package com.kushsmp.essentials.managers;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.ranks.Rank;
import com.kushsmp.essentials.util.Components;
import com.kushsmp.essentials.util.Format;
import com.kushsmp.essentials.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

/**
 * Builds the custom tab list: header/footer (with PlaceholderAPI support),
 * rank-colored per-player list names, and optional rank-weighted sorting via
 * hidden scoreboard teams on the main scoreboard.
 */
public class TabManager {

    private final CustomEssentials plugin;
    private BukkitTask task;

    public TabManager(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    private boolean enabled() {
        return plugin.config().getConfig().getBoolean("tab.enabled", true);
    }

    /** Start the periodic refresh loop so placeholders stay current. */
    public void start() {
        stop();
        if (!enabled()) return;
        long interval = Math.max(1, plugin.config().getConfig().getLong("tab.update-interval-seconds", 3)) * 20L;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::refreshAll, 20L, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /** Apply header/footer + list name + sorting to every online player. */
    public void refreshAll() {
        if (!enabled()) return;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyToPlayer(player);
        }
    }

    /** Apply everything for one player. */
    public void applyToPlayer(Player player) {
        if (!enabled()) return;
        refreshHeaderFooter(player);
        refreshListName(player);
        if (plugin.config().getConfig().getBoolean("tab.sort-by-rank", true)) {
            applySorting(player);
        }
    }

    private void refreshHeaderFooter(Player player) {
        List<String> headerLines = plugin.config().getConfig().getStringList("tab.header");
        List<String> footerLines = plugin.config().getConfig().getStringList("tab.footer");
        Component header = joinLines(player, headerLines);
        Component footer = joinLines(player, footerLines);
        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    private Component joinLines(Player player, List<String> lines) {
        Component result = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            result = result.append(Format.component(plugin, player, lines.get(i)));
            if (i < lines.size() - 1) {
                result = result.append(Component.newline());
            }
        }
        return result;
    }

    /** Set the player's displayed tab name to prefix + colored name + suffix. */
    private void refreshListName(Player player) {
        Rank rank = plugin.ranks().getPlayerRank(player.getUniqueId());
        String prefix = rank != null ? rank.getPrefix() : "";
        String suffix = rank != null ? rank.getSuffix() : "";
        String color = rank != null ? rank.getColor() : "&7";
        String legacy = TextUtil.color(prefix + color + player.getName() + suffix);
        player.playerListName(Components.of(legacy));
    }

    // ----- Rank-weighted sorting via scoreboard teams -----

    private void applySorting(Player player) {
        try {
            Scoreboard board = plugin.getServer().getScoreboardManager().getMainScoreboard();
            Rank rank = plugin.ranks().getPlayerRank(player.getUniqueId());
            int weight = rank != null ? Math.max(0, Math.min(9999, rank.getWeight())) : 9999;
            String rankName = rank != null ? rank.getName() : "default";

            String teamName = sanitizeTeamName("ke" + String.format("%04d", weight) + rankName);

            // Remove the player from any of our previous teams.
            for (Team t : board.getTeams()) {
                if (t.getName().startsWith("ke") && t.hasEntry(player.getName()) && !t.getName().equals(teamName)) {
                    t.removeEntry(player.getName());
                }
            }

            Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        } catch (Throwable t) {
            // Sorting is best-effort; never let it break a join.
            plugin.getLogger().fine("Tab sorting skipped: " + t.getMessage());
        }
    }

    /** Team names must be <= 16 chars and reasonably simple. */
    private String sanitizeTeamName(String in) {
        String cleaned = in.toLowerCase().replaceAll("[^a-z0-9]", "");
        return cleaned.length() > 16 ? cleaned.substring(0, 16) : cleaned;
    }

    /** Remove the player from our sorting teams (on quit). */
    public void clearSorting(Player player) {
        try {
            Scoreboard board = plugin.getServer().getScoreboardManager().getMainScoreboard();
            for (Team t : board.getTeams()) {
                if (t.getName().startsWith("ke") && t.hasEntry(player.getName())) {
                    t.removeEntry(player.getName());
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
