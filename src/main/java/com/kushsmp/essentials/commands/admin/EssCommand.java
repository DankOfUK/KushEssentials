package com.kushsmp.essentials.commands.admin;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /ess &lt;reload|version&gt; - plugin administration.
 */
public class EssCommand extends BaseCommand {

    public EssCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!require(sender, "essentials.reload")) return true;
                plugin.config().reload();
                msg(sender, "reloaded");
                return true;
            case "version":
            case "ver":
                sendInfo(sender);
                return true;
            default:
                msg(sender, "unknown-command-usage", "usage", "/ess <reload|version>");
                return true;
        }
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(plugin.config().prefix()
                + "CustomEssentials v" + plugin.getDescription().getVersion());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("reload", "version"));
            options.removeIf(o -> !o.startsWith(args[0].toLowerCase()));
            return options;
        }
        return new ArrayList<>();
    }
}
