package com.kushsmp.essentials.commands.teleport;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand extends BaseCommand {

    public WarpCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.warps().warpsEnabled()) {
            msg(sender, "feature-disabled");
            return true;
        }
        switch (command.getName().toLowerCase()) {
            case "setwarp":
                return setWarp(sender, args);
            case "warp":
                return warp(sender, args);
            case "delwarp":
                return delWarp(sender, args);
            case "warps":
                return listWarps(sender);
            default:
                return true;
        }
    }

    private boolean setWarp(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!require(player, "essentials.setwarp")) return true;
        if (args.length < 1) {
            msg(player, "unknown-command-usage", "usage", "/setwarp <name>");
            return true;
        }
        plugin.warps().setWarp(args[0], player.getLocation());
        msg(player, "warps.set", "warp", args[0]);
        return true;
    }

    private boolean warp(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!require(player, "essentials.warp")) return true;
        if (args.length < 1) {
            List<String> warps = plugin.warps().getWarps();
            if (warps.isEmpty()) msg(player, "warps.none");
            else msg(player, "warps.list", "warps", String.join(", ", warps));
            return true;
        }
        Location loc = plugin.warps().getWarp(args[0]);
        if (loc == null) {
            msg(player, "warps.not-found", "warp", args[0]);
            return true;
        }
        // Per-warp permission support: essentials.warp.<name>
        String perWarp = "essentials.warp." + args[0].toLowerCase();
        if (!player.hasPermission("essentials.warp.*") && !player.isOp()
                && plugin.getServer().getPluginManager().getPermission(perWarp) != null
                && !player.hasPermission(perWarp)) {
            msg(player, "no-permission");
            return true;
        }
        int delay = plugin.config().getConfig().getInt("regular.warps.teleport-delay-seconds", 3);
        msg(player, "warps.teleporting", "warp", args[0]);
        plugin.teleports().teleportWithWarmup(player, loc, delay, true, null);
        return true;
    }

    private boolean delWarp(CommandSender sender, String[] args) {
        if (!require(sender, "essentials.delwarp")) return true;
        if (args.length < 1) {
            msg(sender, "unknown-command-usage", "usage", "/delwarp <name>");
            return true;
        }
        if (!plugin.warps().hasWarp(args[0])) {
            msg(sender, "warps.not-found", "warp", args[0]);
            return true;
        }
        plugin.warps().deleteWarp(args[0]);
        msg(sender, "warps.deleted", "warp", args[0]);
        return true;
    }

    private boolean listWarps(CommandSender sender) {
        if (!require(sender, "essentials.warp")) return true;
        List<String> warps = plugin.warps().getWarps();
        if (warps.isEmpty()) msg(sender, "warps.none");
        else msg(sender, "warps.list", "warps", String.join(", ", warps));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1 && (command.getName().equalsIgnoreCase("warp")
                || command.getName().equalsIgnoreCase("delwarp"))) {
            for (String w : plugin.warps().getWarps()) {
                if (w.startsWith(args[0].toLowerCase())) out.add(w);
            }
        }
        return out;
    }
}
