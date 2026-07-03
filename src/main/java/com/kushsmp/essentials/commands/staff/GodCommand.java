package com.kushsmp.essentials.commands.staff;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GodCommand extends BaseCommand {

    public GodCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.config().getConfig().getBoolean("staff.god.enabled", true)) {
            msg(sender, "feature-disabled");
            return true;
        }
        if (!require(sender, "essentials.god")) return true;

        if (args.length >= 1) {
            Player target = onlineTarget(sender, args[0]);
            if (target == null) return true;
            boolean newState = !plugin.state().isGod(target.getUniqueId());
            plugin.state().setGod(target.getUniqueId(), newState);
            target.setInvulnerable(newState);
            msg(sender, "god.other", "player", target.getName(), "state", newState ? "ON" : "OFF");
            msg(target, newState ? "god.on" : "god.off");
            return true;
        }

        Player player = requirePlayer(sender);
        if (player == null) return true;
        boolean newState = !plugin.state().isGod(player.getUniqueId());
        plugin.state().setGod(player.getUniqueId(), newState);
        player.setInvulnerable(newState);
        msg(player, newState ? "god.on" : "god.off");
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
