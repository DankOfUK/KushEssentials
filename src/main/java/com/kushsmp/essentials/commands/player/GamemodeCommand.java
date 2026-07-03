package com.kushsmp.essentials.commands.player;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GamemodeCommand extends BaseCommand {

    public GamemodeCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!require(sender, "essentials.gamemode")) return true;

        String name = command.getName().toLowerCase();
        GameMode mode;
        String[] targetArgs;

        switch (name) {
            case "gmc": mode = GameMode.CREATIVE; targetArgs = args; break;
            case "gms": mode = GameMode.SURVIVAL; targetArgs = args; break;
            case "gma": mode = GameMode.ADVENTURE; targetArgs = args; break;
            case "gmsp": mode = GameMode.SPECTATOR; targetArgs = args; break;
            default:
                // /gamemode <mode> [player]
                if (args.length < 1) {
                    msg(sender, "unknown-command-usage",
                            "usage", "/gamemode <survival|creative|adventure|spectator> [player]");
                    return true;
                }
                mode = parseMode(args[0]);
                if (mode == null) {
                    msg(sender, "unknown-command-usage",
                            "usage", "/gamemode <survival|creative|adventure|spectator> [player]");
                    return true;
                }
                targetArgs = args.length > 1 ? new String[]{args[1]} : new String[0];
                break;
        }

        Player target;
        if (targetArgs.length >= 1) {
            target = onlineTarget(sender, targetArgs[0]);
            if (target == null) return true;
            target.setGameMode(mode);
            msg(sender, "player.gamemode-other", "player", target.getName(), "mode", mode.name().toLowerCase());
            msg(target, "player.gamemode", "mode", mode.name().toLowerCase());
        } else {
            Player player = requirePlayer(sender);
            if (player == null) return true;
            player.setGameMode(mode);
            msg(player, "player.gamemode", "mode", mode.name().toLowerCase());
        }
        return true;
    }

    private GameMode parseMode(String input) {
        switch (input.toLowerCase()) {
            case "0": case "s": case "survival": return GameMode.SURVIVAL;
            case "1": case "c": case "creative": return GameMode.CREATIVE;
            case "2": case "a": case "adventure": return GameMode.ADVENTURE;
            case "3": case "sp": case "spectator": return GameMode.SPECTATOR;
            default: return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("gamemode") && args.length == 1) {
            for (String m : new String[]{"survival", "creative", "adventure", "spectator"}) {
                if (m.startsWith(args[0].toLowerCase())) out.add(m);
            }
        } else {
            int idx = command.getName().equalsIgnoreCase("gamemode") ? 2 : 1;
            if (args.length == idx) {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                        out.add(p.getName());
                    }
                }
            }
        }
        return out;
    }
}
