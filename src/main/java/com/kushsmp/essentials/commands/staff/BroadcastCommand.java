package com.kushsmp.essentials.commands.staff;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * /broadcast &lt;message&gt; - sends a formatted, server-wide announcement.
 */
public class BroadcastCommand extends BaseCommand {

    public BroadcastCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!require(sender, "essentials.broadcast")) return true;

        if (args.length < 1) {
            msg(sender, "unknown-command-usage", "usage", "/broadcast <message>");
            return true;
        }

        String message = String.join(" ", args);
        String rendered = plugin.config().msg("broadcast.format", "message", message);
        plugin.getServer().broadcastMessage(rendered);
        return true;
    }
}
