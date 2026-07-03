package com.kushsmp.essentials.commands.staff;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand extends BaseCommand {

    public VanishCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!plugin.config().getConfig().getBoolean("staff.vanish.enabled", true)) {
            msg(player, "feature-disabled");
            return true;
        }
        if (!require(player, "essentials.vanish")) return true;

        boolean newState = !plugin.state().isVanished(player.getUniqueId());
        plugin.state().setVanished(player.getUniqueId(), newState);
        plugin.state().applyVanishState(player, newState);
        msg(player, newState ? "vanish.on" : "vanish.off");
        return true;
    }
}
