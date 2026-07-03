package com.kushsmp.essentials.commands.teleport;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends BaseCommand {

    public SpawnCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;

        if (!plugin.warps().spawnEnabled()) {
            msg(player, "feature-disabled");
            return true;
        }

        if (command.getName().equalsIgnoreCase("setspawn")) {
            if (!require(player, "essentials.setspawn")) return true;
            plugin.warps().setSpawn(player.getLocation());
            msg(player, "spawn.set");
            return true;
        }

        // /spawn
        if (!require(player, "essentials.spawn")) return true;
        Location spawn = plugin.warps().getSpawn();
        if (spawn == null) {
            // Fall back to the world spawn if none set.
            spawn = player.getWorld().getSpawnLocation();
        }
        msg(player, "spawn.teleporting");
        int delay = plugin.config().getConfig().getInt("regular.warps.teleport-delay-seconds", 3);
        plugin.teleports().teleportWithWarmup(player, spawn, delay, true, null);
        return true;
    }
}
