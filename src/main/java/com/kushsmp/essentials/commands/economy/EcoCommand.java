package com.kushsmp.essentials.commands.economy;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * /eco &lt;give|take|set|reset&gt; &lt;player&gt; [amount]   (alias: /economy)
 * Admin command for adjusting balances.
 */
public class EcoCommand extends BaseCommand {

    public EcoCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.economy().enabled()) {
            msg(sender, "feature-disabled");
            return true;
        }
        if (!require(sender, "essentials.eco")) return true;

        if (args.length < 2) {
            msg(sender, "economy.eco-usage");
            return true;
        }

        String action = args[0].toLowerCase();

        // Resolve target (online or known offline).
        Player online = plugin.getServer().getPlayerExact(args[1]);
        UUID targetId;
        String targetName;
        if (online != null) {
            targetId = online.getUniqueId();
            targetName = online.getName();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
            if (!off.hasPlayedBefore()) {
                msg(sender, "economy.player-never-joined");
                return true;
            }
            targetId = off.getUniqueId();
            targetName = off.getName() != null ? off.getName() : args[1];
        }

        if (action.equals("reset")) {
            plugin.economy().reset(targetId);
            msg(sender, "economy.eco-reset",
                    "player", targetName, "amount", plugin.economy().format(plugin.economy().getBalance(targetId)));
            return true;
        }

        // give / take / set all need an amount.
        if (args.length < 3) {
            msg(sender, "economy.eco-usage");
            return true;
        }
        double amount = parseAmount(args[2]);
        if (amount < 0) {
            msg(sender, "economy.invalid-amount");
            return true;
        }

        switch (action) {
            case "give":
                plugin.economy().deposit(targetId, amount);
                msg(sender, "economy.eco-give", "player", targetName,
                        "amount", plugin.economy().format(amount),
                        "balance", plugin.economy().format(plugin.economy().getBalance(targetId)));
                break;
            case "take":
                double current = plugin.economy().getBalance(targetId);
                plugin.economy().set(targetId, Math.max(0, current - amount));
                msg(sender, "economy.eco-take", "player", targetName,
                        "amount", plugin.economy().format(amount),
                        "balance", plugin.economy().format(plugin.economy().getBalance(targetId)));
                break;
            case "set":
                plugin.economy().set(targetId, amount);
                msg(sender, "economy.eco-set", "player", targetName,
                        "amount", plugin.economy().format(amount));
                break;
            default:
                msg(sender, "economy.eco-usage");
        }
        return true;
    }

    /** Parse a non-negative money amount; returns -1 if invalid. */
    private double parseAmount(String s) {
        try {
            double v = Double.parseDouble(s);
            if (Double.isNaN(v) || Double.isInfinite(v) || v < 0) return -1;
            return Math.round(v * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : Arrays.asList("give", "take", "set", "reset")) {
                if (sub.startsWith(args[0].toLowerCase())) out.add(sub);
            }
        } else if (args.length == 2) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
            }
        }
        return out;
    }
}
