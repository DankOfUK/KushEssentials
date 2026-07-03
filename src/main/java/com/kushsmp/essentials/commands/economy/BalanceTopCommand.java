package com.kushsmp.essentials.commands.economy;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * /baltop - shows the richest players.
 */
public class BalanceTopCommand extends BaseCommand {

    private static final int TOP_LIMIT = 10;

    public BalanceTopCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.economy().enabled()) {
            msg(sender, "feature-disabled");
            return true;
        }
        if (!require(sender, "essentials.balancetop")) return true;

        List<Map.Entry<UUID, Double>> top = plugin.economy().top(TOP_LIMIT);

        msg(sender, "economy.baltop-header");
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : top) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(entry.getKey());
            String name = off.getName() != null ? off.getName() : entry.getKey().toString().substring(0, 8);
            sender.sendMessage(plugin.config().msg("economy.baltop-entry",
                    "rank", String.valueOf(rank),
                    "player", name,
                    "amount", plugin.economy().format(entry.getValue())));
            rank++;
        }
        return true;
    }
}
