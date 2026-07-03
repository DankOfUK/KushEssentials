package com.kushsmp.essentials.commands.teleport;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpCommand extends BaseCommand {

    public TpCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tphere")) {
            return tpHere(sender, args);
        }
        return tp(sender, args);
    }

    private boolean tp(CommandSender sender, String[] args) {
        if (!require(sender, "essentials.tp")) return true;

        if (args.length == 1) {
            // /tp <target> : sender teleports to target
            Player self = requirePlayer(sender);
            if (self == null) return true;
            Player target = onlineTarget(sender, args[0]);
            if (target == null) return true;
            self.teleport(target.getLocation());
            msg(self, "teleport.done");
            return true;
        }
        if (args.length >= 2) {
            // /tp <player> <target> : move player to target (needs others perm)
            if (!require(sender, "essentials.tp.others")) return true;
            Player who = onlineTarget(sender, args[0]);
            Player target = onlineTarget(sender, args[1]);
            if (who == null || target == null) return true;
            who.teleport(target.getLocation());
            msg(sender, "teleport.done");
            return true;
        }
        msg(sender, "unknown-command-usage", "usage", "/tp <player> [target]");
        return true;
    }

    private boolean tpHere(CommandSender sender, String[] args) {
        Player self = requirePlayer(sender);
        if (self == null) return true;
        if (!require(self, "essentials.tphere")) return true;
        if (args.length < 1) {
            msg(self, "unknown-command-usage", "usage", "/tphere <player>");
            return true;
        }
        Player target = onlineTarget(self, args[0]);
        if (target == null) return true;
        target.teleport(self.getLocation());
        msg(self, "teleport.done");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length >= 1) {
            String prefix = args[args.length - 1].toLowerCase();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) out.add(p.getName());
            }
        }
        return out;
    }
}
