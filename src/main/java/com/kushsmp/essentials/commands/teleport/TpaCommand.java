package com.kushsmp.essentials.commands.teleport;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import com.kushsmp.essentials.managers.TeleportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpaCommand extends BaseCommand {

    public TpaCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!plugin.config().getConfig().getBoolean("regular.tpa.enabled", true)) {
            msg(player, "feature-disabled");
            return true;
        }
        if (!require(player, "essentials.tpa")) return true;

        switch (command.getName().toLowerCase()) {
            case "tpa":
                return request(player, args, false);
            case "tpahere":
                return request(player, args, true);
            case "tpaccept":
                return accept(player);
            case "tpdeny":
                return deny(player);
            default:
                return true;
        }
    }

    private boolean request(Player player, String[] args, boolean here) {
        if (args.length < 1) {
            msg(player, "unknown-command-usage", "usage", here ? "/tpahere <player>" : "/tpa <player>");
            return true;
        }
        Player target = onlineTarget(player, args[0]);
        if (target == null) return true;
        if (target.equals(player)) {
            msg(player, "message.self");
            return true;
        }
        plugin.teleports().createRequest(player, target, here);
        msg(player, here ? "tpa.sent-here" : "tpa.sent", "target", target.getName());
        msg(target, here ? "tpa.received-here" : "tpa.received", "sender", player.getName());
        return true;
    }

    private boolean accept(Player player) {
        TeleportManager.Request request = plugin.teleports().getRequest(player.getUniqueId());
        if (request == null) {
            msg(player, "tpa.none-pending");
            return true;
        }
        msg(player, "tpa.accepted");
        plugin.teleports().completeRequest(request, players -> {
            Player mover = players[0];
            Player anchor = players[1];
            int delay = plugin.config().getConfig().getInt("regular.homes.teleport-delay-seconds", 3);
            plugin.config().send(mover, "tpa.accepted");
            plugin.teleports().teleportWithWarmup(mover, anchor.getLocation(), delay, true, null);
        });
        return true;
    }

    private boolean deny(Player player) {
        TeleportManager.Request request = plugin.teleports().getRequest(player.getUniqueId());
        if (request == null) {
            msg(player, "tpa.none-pending");
            return true;
        }
        plugin.teleports().removeRequest(player.getUniqueId());
        msg(player, "tpa.denied");
        Player from = plugin.getServer().getPlayer(request.from);
        if (from != null) {
            plugin.config().send(from, "tpa.denied");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1 && (command.getName().equalsIgnoreCase("tpa")
                || command.getName().equalsIgnoreCase("tpahere"))) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) out.add(p.getName());
            }
        }
        return out;
    }
}
