package com.kushsmp.essentials.commands.kit;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import com.kushsmp.essentials.managers.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GiveKitCommand extends BaseCommand {

    public GiveKitCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!require(sender, "essentials.givekit")) return true;
        if (args.length < 2) {
            msg(sender, "unknown-command-usage", "usage", "/givekit <kit> <player>");
            return true;
        }
        String kitName = args[0];
        if (!plugin.kits().kitExists(kitName)) {
            msg(sender, "kit.not-found", "kit", kitName);
            return true;
        }
        Player target = onlineTarget(sender, args[1]);
        if (target == null) return true;

        KitManager.ClaimResult result = plugin.kits().claim(target, kitName, true);
        if (result.type == KitManager.ClaimResult.Type.SUCCESS) {
            msg(sender, "kit.given", "kit", kitName, "player", target.getName());
            msg(target, "kit.received", "kit", kitName);
            if (result.overflowed) msg(target, "kit.inventory-full");
        } else {
            msg(sender, "kit.not-found", "kit", kitName);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String kit : plugin.kits().getKitNames()) {
                if (kit.startsWith(args[0].toLowerCase())) out.add(kit);
            }
        } else if (args.length == 2) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
            }
        }
        return out;
    }
}
