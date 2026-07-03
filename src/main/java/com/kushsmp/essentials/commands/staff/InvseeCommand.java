package com.kushsmp.essentials.commands.staff;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /invsee &lt;player&gt; - opens a live view of another player's inventory.
 */
public class InvseeCommand extends BaseCommand {

    public InvseeCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!require(player, "essentials.invsee")) return true;

        if (args.length < 1) {
            msg(player, "unknown-command-usage", "usage", "/invsee <player>");
            return true;
        }

        Player target = onlineTarget(player, args[0]);
        if (target == null) return true;

        player.openInventory(target.getInventory());
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
