package com.kushsmp.essentials.commands.player;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FlyCommand extends BaseCommand {

    public FlyCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            if (!require(sender, "essentials.fly.others")) return true;
            Player target = onlineTarget(sender, args[0]);
            if (target == null) return true;
            boolean newState = !target.getAllowFlight();
            target.setAllowFlight(newState);
            target.setFlying(newState);
            msg(sender, "player.fly-other", "player", target.getName(), "state", newState ? "ON" : "OFF");
            msg(target, newState ? "player.fly-on" : "player.fly-off");
            return true;
        }

        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!require(player, "essentials.fly")) return true;
        boolean newState = !player.getAllowFlight();
        player.setAllowFlight(newState);
        player.setFlying(newState);
        msg(player, newState ? "player.fly-on" : "player.fly-off");
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
