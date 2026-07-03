package com.kushsmp.essentials.commands.economy;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * /balance [player]  (aliases: /bal, /money)
 */
public class BalanceCommand extends BaseCommand {

    public BalanceCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.economy().enabled()) {
            msg(sender, "feature-disabled");
            return true;
        }

        // Check another player's balance.
        if (args.length >= 1) {
            if (!require(sender, "essentials.balance.others")) return true;

            Player online = plugin.getServer().getPlayerExact(args[0]);
            UUID id;
            String name;
            if (online != null) {
                id = online.getUniqueId();
                name = online.getName();
            } else {
                @SuppressWarnings("deprecation")
                OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
                if (!off.hasPlayedBefore()) {
                    msg(sender, "economy.player-never-joined");
                    return true;
                }
                id = off.getUniqueId();
                name = off.getName() != null ? off.getName() : args[0];
            }
            msg(sender, "economy.balance-other",
                    "player", name, "amount", plugin.economy().format(plugin.economy().getBalance(id)));
            return true;
        }

        // Own balance.
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!require(player, "essentials.balance")) return true;
        msg(player, "economy.balance-self",
                "amount", plugin.economy().format(plugin.economy().getBalance(player.getUniqueId())));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) out.add(p.getName());
            }
        }
        return out;
    }
}
