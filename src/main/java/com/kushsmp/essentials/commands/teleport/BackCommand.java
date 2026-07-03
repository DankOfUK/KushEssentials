package com.kushsmp.essentials.commands.teleport;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand extends BaseCommand {

    public BackCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!plugin.config().getConfig().getBoolean("regular.back.enabled", true)) {
            msg(player, "feature-disabled");
            return true;
        }
        if (!require(player, "essentials.back")) return true;

        Location last = plugin.teleports().getLastLocation(player.getUniqueId());
        if (last == null) {
            msg(player, "back.none");
            return true;
        }
        msg(player, "back.teleporting");
        // teleportWithWarmup records the current location, so /back toggles between points.
        plugin.teleports().teleportWithWarmup(player, last, 0, false, null);
        return true;
    }
}
