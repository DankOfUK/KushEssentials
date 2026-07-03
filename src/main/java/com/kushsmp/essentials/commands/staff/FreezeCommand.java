package com.kushsmp.essentials.commands.staff;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /freeze &lt;player&gt; - toggles a movement freeze on the target. The actual
 * movement blocking and leash-back logic lives in PlayerListener.
 */
public class FreezeCommand extends BaseCommand {

    public FreezeCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.config().getConfig().getBoolean("staff.freeze.enabled", true)) {
            msg(sender, "feature-disabled");
            return true;
        }
        if (!require(sender, "essentials.freeze")) return true;

        if (args.length < 1) {
            msg(sender, "unknown-command-usage", "usage", "/freeze <player>");
            return true;
        }

        Player target = onlineTarget(sender, args[0]);
        if (target == null) return true;

        boolean newState = !plugin.state().isFrozen(target.getUniqueId());
        plugin.state().setFrozen(target.getUniqueId(), newState);

        msg(sender, newState ? "freeze.frozen" : "freeze.unfrozen", "player", target.getName());
        if (newState) {
            target.sendMessage(plugin.config().msg("freeze.you-are-frozen"));
        } else {
            msg(target, "freeze.unfrozen", "player", target.getName());
        }
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
