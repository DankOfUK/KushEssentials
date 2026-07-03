package com.kushsmp.essentials.commands.teleport;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeCommand extends BaseCommand {

    public HomeCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;

        if (!plugin.homes().isEnabled()) {
            msg(player, "feature-disabled");
            return true;
        }

        UUID uuid = player.getUniqueId();
        switch (command.getName().toLowerCase()) {
            case "sethome":
                return setHome(player, uuid, args);
            case "home":
                return goHome(player, uuid, args);
            case "delhome":
                return delHome(player, uuid, args);
            case "homes":
                return listHomes(player, uuid);
            default:
                return true;
        }
    }

    private boolean setHome(Player player, UUID uuid, String[] args) {
        if (!require(player, "essentials.sethome")) return true;
        String name = args.length > 0 ? args[0] : "home";

        // Enforce the home limit only when adding a *new* home.
        if (!plugin.homes().hasHome(uuid, name)) {
            int max = plugin.homes().getMaxHomes(player);
            if (plugin.homes().getHomes(uuid).size() >= max) {
                msg(player, "homes.limit-reached", "max", String.valueOf(max));
                return true;
            }
        }
        plugin.homes().setHome(uuid, name, player.getLocation());
        msg(player, "homes.set", "home", name);
        return true;
    }

    private boolean goHome(Player player, UUID uuid, String[] args) {
        if (!require(player, "essentials.home")) return true;
        List<String> homes = plugin.homes().getHomes(uuid);
        if (homes.isEmpty()) {
            msg(player, "homes.none");
            return true;
        }
        String name = args.length > 0 ? args[0] : (homes.contains("home") ? "home" : homes.get(0));
        Location loc = plugin.homes().getHome(uuid, name);
        if (loc == null) {
            msg(player, "homes.not-found", "home", name);
            return true;
        }
        int delay = plugin.config().getConfig().getInt("regular.homes.teleport-delay-seconds", 3);
        boolean cancelOnMove = plugin.config().getConfig().getBoolean("regular.homes.cancel-on-move", true);
        msg(player, "homes.teleporting", "home", name);
        plugin.teleports().teleportWithWarmup(player, loc, delay, cancelOnMove, null);
        return true;
    }

    private boolean delHome(Player player, UUID uuid, String[] args) {
        if (!require(player, "essentials.delhome")) return true;
        if (args.length < 1) {
            msg(player, "unknown-command-usage", "usage", "/delhome <name>");
            return true;
        }
        if (!plugin.homes().hasHome(uuid, args[0])) {
            msg(player, "homes.not-found", "home", args[0]);
            return true;
        }
        plugin.homes().deleteHome(uuid, args[0]);
        msg(player, "homes.deleted", "home", args[0]);
        return true;
    }

    private boolean listHomes(Player player, UUID uuid) {
        if (!require(player, "essentials.home")) return true;
        List<String> homes = plugin.homes().getHomes(uuid);
        if (homes.isEmpty()) {
            msg(player, "homes.none");
            return true;
        }
        msg(player, "homes.list", "homes", String.join(", ", homes));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (sender instanceof Player && args.length == 1
                && (command.getName().equalsIgnoreCase("home") || command.getName().equalsIgnoreCase("delhome"))) {
            for (String h : plugin.homes().getHomes(((Player) sender).getUniqueId())) {
                if (h.startsWith(args[0].toLowerCase())) out.add(h);
            }
        }
        return out;
    }
}
