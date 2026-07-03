package com.kushsmp.essentials.commands.player;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ClearInventoryCommand extends BaseCommand {

    public ClearInventoryCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!require(sender, "essentials.clearinventory")) return true;

        if (args.length >= 1) {
            Player target = onlineTarget(sender, args[0]);
            if (target == null) return true;
            target.getInventory().clear();
            msg(sender, "player.inventory-cleared-other", "player", target.getName());
            msg(target, "player.inventory-cleared");
            return true;
        }

        Player player = requirePlayer(sender);
        if (player == null) return true;
        player.getInventory().clear();
        msg(player, "player.inventory-cleared");
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
