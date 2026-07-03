package com.kushsmp.essentials.commands.admin;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import com.kushsmp.essentials.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * /rank list                 - show all ranks
 * /rank info [player]        - show a player's rank
 * /rank set &lt;player&gt; &lt;rank&gt;   - assign a rank (essentials.rank.admin)
 * /rank reload               - reload ranks.yml (essentials.rank.admin)
 */
public class RankCommand extends BaseCommand {

    public RankCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            msg(sender, "rank.usage");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                if (!require(sender, "essentials.rank")) return true;
                msg(sender, "rank.list", "ranks", String.join(", ", plugin.ranks().getRankNames()));
                return true;

            case "info":
                return handleInfo(sender, args);

            case "set":
                return handleSet(sender, args);

            case "reload":
                if (!require(sender, "essentials.rank.admin")) return true;
                plugin.ranks().load();
                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    plugin.permissions().apply(online);
                }
                plugin.tab().refreshAll();
                msg(sender, "rank.reloaded");
                return true;

            default:
                msg(sender, "rank.usage");
                return true;
        }
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!require(sender, "essentials.rank")) return true;

        UUID targetId;
        String targetName;
        if (args.length >= 2) {
            Player online = plugin.getServer().getPlayerExact(args[1]);
            if (online != null) {
                targetId = online.getUniqueId();
                targetName = online.getName();
            } else {
                @SuppressWarnings("deprecation")
                OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);
                targetId = offline.getUniqueId();
                targetName = offline.getName() != null ? offline.getName() : args[1];
            }
        } else {
            Player self = requirePlayer(sender);
            if (self == null) return true;
            targetId = self.getUniqueId();
            targetName = self.getName();
        }

        Rank rank = plugin.ranks().getPlayerRank(targetId);
        msg(sender, "rank.info", "player", targetName, "rank", rank != null ? rank.getName() : "none");
        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!require(sender, "essentials.rank.admin")) return true;
        if (args.length < 3) {
            msg(sender, "unknown-command-usage", "usage", "/rank set <player> <rank>");
            return true;
        }

        String rankName = args[2].toLowerCase();
        if (!plugin.ranks().rankExists(rankName)) {
            msg(sender, "rank.not-found", "rank", rankName);
            return true;
        }

        Player online = plugin.getServer().getPlayerExact(args[1]);
        UUID targetId;
        String targetName;
        if (online != null) {
            targetId = online.getUniqueId();
            targetName = online.getName();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);
            targetId = offline.getUniqueId();
            targetName = offline.getName() != null ? offline.getName() : args[1];
        }

        plugin.ranks().setPlayerRank(targetId, rankName);

        // Apply live if the player is online.
        if (online != null) {
            plugin.permissions().apply(online);
            plugin.tab().refreshAll();
        }

        msg(sender, "rank.set", "player", targetName, "rank", rankName);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : new String[]{"list", "info", "set", "reload"}) {
                if (sub.startsWith(args[0].toLowerCase())) out.add(sub);
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("info"))) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            for (String r : plugin.ranks().getRankNames()) {
                if (r.startsWith(args[2].toLowerCase())) out.add(r);
            }
        }
        return out;
    }
}
