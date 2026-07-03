package com.kushsmp.essentials.commands.player;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkCommand extends BaseCommand {

    public AfkCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!plugin.config().getConfig().getBoolean("regular.afk.enabled", true)) {
            msg(player, "feature-disabled");
            return true;
        }
        if (!require(player, "essentials.afk")) return true;

        boolean newState = !plugin.state().isAfk(player.getUniqueId());
        plugin.state().setAfk(player.getUniqueId(), newState);
        // Reset activity so auto-AFK doesn't immediately re-flip the state.
        plugin.state().markActive(player.getUniqueId());

        if (plugin.config().getConfig().getBoolean("regular.afk.broadcast", true)) {
            String key = newState ? "afk.now-afk" : "afk.no-longer-afk";
            plugin.getServer().broadcastMessage(plugin.config().msg(key, "player", player.getName()));
        }
        return true;
    }
}
