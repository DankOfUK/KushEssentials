package com.kushsmp.essentials.commands.kit;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import com.kushsmp.essentials.managers.KitManager;
import com.kushsmp.essentials.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KitCommand extends BaseCommand {

    public KitCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!plugin.kits().isEnabled()) {
            msg(player, "feature-disabled");
            return true;
        }
        if (!require(player, "essentials.kit")) return true;

        // No argument -> list kits the player can use.
        if (args.length < 1) {
            List<String> available = new ArrayList<>();
            for (String kit : plugin.kits().getKitNames()) {
                if (player.hasPermission(plugin.kits().permissionFor(kit))) {
                    available.add(kit);
                }
            }
            if (available.isEmpty()) {
                player.sendMessage(plugin.config().msg("kit.list", "kits", "(none available)"));
            } else {
                msg(player, "kit.list", "kits", String.join(", ", available));
            }
            return true;
        }

        String kitName = args[0];
        KitManager.ClaimResult result = plugin.kits().claim(player, kitName, false);
        switch (result.type) {
            case SUCCESS:
                msg(player, "kit.claimed", "kit", kitName);
                if (result.overflowed) msg(player, "kit.inventory-full");
                break;
            case NOT_FOUND:
                msg(player, "kit.not-found", "kit", kitName);
                break;
            case NO_PERMISSION:
                msg(player, "kit.no-permission", "kit", kitName);
                break;
            case ONE_TIME_USED:
                msg(player, "kit.one-time-used", "kit", kitName);
                break;
            case COOLDOWN:
                msg(player, "kit.cooldown", "kit", kitName,
                        "time", TextUtil.formatDuration(result.remainingMillis));
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (sender instanceof Player && args.length == 1) {
            Player player = (Player) sender;
            for (String kit : plugin.kits().getKitNames()) {
                if (kit.startsWith(args[0].toLowerCase())
                        && player.hasPermission(plugin.kits().permissionFor(kit))) {
                    out.add(kit);
                }
            }
        }
        return out;
    }
}
