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
 * /pay &lt;player&gt; &lt;amount&gt; - send money to another player.
 */
public class PayCommand extends BaseCommand {

    public PayCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.economy().enabled()) {
            msg(sender, "feature-disabled");
            return true;
        }
        if (!plugin.economy().payEnabled()) {
            msg(sender, "economy.pay-disabled");
            return true;
        }

        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!require(player, "essentials.pay")) return true;

        if (args.length < 2) {
            msg(player, "unknown-command-usage", "usage", "/pay <player> <amount>");
            return true;
        }

        // Resolve recipient (online or known offline player).
        Player online = plugin.getServer().getPlayerExact(args[0]);
        UUID targetId;
        String targetName;
        if (online != null) {
            targetId = online.getUniqueId();
            targetName = online.getName();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
            if (!off.hasPlayedBefore()) {
                msg(player, "economy.player-never-joined");
                return true;
            }
            targetId = off.getUniqueId();
            targetName = off.getName() != null ? off.getName() : args[0];
        }

        if (targetId.equals(player.getUniqueId())) {
            msg(player, "economy.cannot-pay-self");
            return true;
        }

        double amount = parseAmount(args[1]);
        if (amount <= 0) {
            msg(player, "economy.invalid-amount");
            return true;
        }
        if (amount < plugin.economy().minPayment()) {
            msg(player, "economy.min-payment", "amount", plugin.economy().format(plugin.economy().minPayment()));
            return true;
        }
        if (!plugin.economy().has(player.getUniqueId(), amount)) {
            msg(player, "economy.insufficient-funds");
            return true;
        }

        if (!plugin.economy().transfer(player.getUniqueId(), targetId, amount)) {
            msg(player, "economy.insufficient-funds");
            return true;
        }

        String formatted = plugin.economy().format(amount);
        msg(player, "economy.paid", "player", targetName, "amount", formatted);
        if (online != null) {
            msg(online, "economy.received", "player", player.getName(), "amount", formatted);
        }
        return true;
    }

    /** Parse a positive money amount; returns -1 if invalid. */
    private double parseAmount(String s) {
        try {
            double v = Double.parseDouble(s);
            if (Double.isNaN(v) || Double.isInfinite(v) || v <= 0) return -1;
            return Math.round(v * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            return -1;
        }
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
