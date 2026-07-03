package com.kushsmp.essentials.commands.player;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NearCommand extends BaseCommand {

    public NearCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!require(player, "essentials.near")) return true;

        int radius = 100;
        if (args.length >= 1) {
            try {
                radius = Math.max(1, Math.min(1000, Integer.parseInt(args[0])));
            } catch (NumberFormatException ignored) {
                // keep default
            }
        }

        double radiusSq = (double) radius * radius;
        List<String> nearby = new ArrayList<>();
        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) continue;
            if (plugin.state().isVanished(other.getUniqueId())
                    && !player.hasPermission("essentials.vanish.see")) {
                continue;
            }
            double distSq = other.getLocation().distanceSquared(player.getLocation());
            if (distSq <= radiusSq) {
                nearby.add(other.getName() + " (" + (int) Math.sqrt(distSq) + "m)");
            }
        }

        if (nearby.isEmpty()) {
            msg(player, "player.near-none");
        } else {
            msg(player, "player.near", "radius", String.valueOf(radius), "players", String.join(", ", nearby));
        }
        return true;
    }
}
