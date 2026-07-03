package com.kushsmp.essentials.managers;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.ranks.Rank;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Loads rank definitions from ranks.yml and tracks each player's assigned rank
 * (persisted in userdata.yml under "ranks.&lt;uuid&gt;"). Resolves permission
 * inheritance so a rank effectively grants its own nodes plus everything it
 * inherits.
 */
public class RankManager {

    private static final String USERDATA = "userdata.yml";

    private final CustomEssentials plugin;

    private final Map<String, Rank> ranks = new LinkedHashMap<>();
    private String defaultRank = "member";

    public RankManager(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    /** Load (or reload) ranks.yml. */
    public void load() {
        ranks.clear();

        File file = new File(plugin.getDataFolder(), "ranks.yml");
        if (!file.exists()) {
            plugin.saveResource("ranks.yml", false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        this.defaultRank = cfg.getString("default-rank", "member").toLowerCase();

        ConfigurationSection root = cfg.getConfigurationSection("ranks");
        if (root == null) {
            plugin.getLogger().warning("ranks.yml has no 'ranks' section!");
            return;
        }

        for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) continue;
            String name = key.toLowerCase();
            Rank rank = new Rank(
                    name,
                    s.getString("display", key),
                    s.getString("prefix", ""),
                    s.getString("suffix", ""),
                    s.getString("color", "&7"),
                    s.getInt("weight", 100),
                    s.getBoolean("default", false),
                    lower(s.getStringList("inherits")),
                    s.getStringList("permissions")
            );
            ranks.put(name, rank);
            if (rank.isDefault()) {
                defaultRank = name;
            }
        }

        if (!ranks.containsKey(defaultRank) && !ranks.isEmpty()) {
            // Fall back to the first defined rank if the named default is missing.
            defaultRank = ranks.keySet().iterator().next();
        }
        plugin.getLogger().info("Loaded " + ranks.size() + " ranks (default: " + defaultRank + ").");
    }

    private List<String> lower(List<String> in) {
        List<String> out = new ArrayList<>();
        for (String s : in) out.add(s.toLowerCase());
        return out;
    }

    // ----- Rank lookups -----

    public boolean rankExists(String name) {
        return name != null && ranks.containsKey(name.toLowerCase());
    }

    public Rank getRank(String name) {
        return name == null ? null : ranks.get(name.toLowerCase());
    }

    public List<String> getRankNames() {
        return new ArrayList<>(ranks.keySet());
    }

    public String getDefaultRankName() {
        return defaultRank;
    }

    public Rank getDefaultRank() {
        return ranks.get(defaultRank);
    }

    // ----- Per-player assignment -----

    private FileConfiguration data() {
        return plugin.config().getData(USERDATA);
    }

    public String getPlayerRankName(UUID uuid) {
        String name = data().getString("ranks." + uuid);
        if (name != null && ranks.containsKey(name.toLowerCase())) {
            return name.toLowerCase();
        }
        return defaultRank;
    }

    public Rank getPlayerRank(UUID uuid) {
        Rank rank = ranks.get(getPlayerRankName(uuid));
        return rank != null ? rank : getDefaultRank();
    }

    public void setPlayerRank(UUID uuid, String rankName) {
        data().set("ranks." + uuid, rankName.toLowerCase());
        plugin.config().saveData(USERDATA);
    }

    /**
     * Resolve the full permission set for a rank, following inheritance.
     * Cycle-safe.
     */
    public List<String> resolvePermissions(String rankName) {
        List<String> out = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        collect(rankName, out, visited);
        return out;
    }

    private void collect(String rankName, List<String> out, Set<String> visited) {
        if (rankName == null || !visited.add(rankName.toLowerCase())) {
            return;
        }
        Rank rank = ranks.get(rankName.toLowerCase());
        if (rank == null) return;
        for (String parent : rank.getInherits()) {
            collect(parent, out, visited);
        }
        for (String perm : rank.getPermissions()) {
            if (!out.contains(perm)) out.add(perm);
        }
    }
}
