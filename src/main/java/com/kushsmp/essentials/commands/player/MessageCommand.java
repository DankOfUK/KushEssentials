package com.kushsmp.essentials.commands.player;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import com.kushsmp.essentials.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageCommand extends BaseCommand {

    public MessageCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!plugin.config().getConfig().getBoolean("regular.messaging.enabled", true)) {
            msg(player, "feature-disabled");
            return true;
        }
        if (!require(player, "essentials.msg")) return true;

        boolean reply = command.getName().equalsIgnoreCase("reply");
        Player target;
        int messageStart;

        if (reply) {
            UUID last = plugin.state().getLastMessaged(player.getUniqueId());
            if (last == null || plugin.getServer().getPlayer(last) == null) {
                msg(player, "message.no-target");
                return true;
            }
            target = plugin.getServer().getPlayer(last);
            messageStart = 0;
            if (args.length < 1) {
                msg(player, "unknown-command-usage", "usage", "/reply <message>");
                return true;
            }
        } else {
            if (args.length < 2) {
                msg(player, "unknown-command-usage", "usage", "/msg <player> <message>");
                return true;
            }
            target = onlineTarget(player, args[0]);
            if (target == null) return true;
            messageStart = 1;
        }

        if (target.equals(player)) {
            msg(player, "message.self");
            return true;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, messageStart, args.length));

        String sentFormat = plugin.config().getConfig().getString("regular.messaging.format-sender",
                "&7[me -> {target}] &f{message}");
        String recvFormat = plugin.config().getConfig().getString("regular.messaging.format-receiver",
                "&7[{sender} -> me] &f{message}");

        player.sendMessage(TextUtil.color(sentFormat
                .replace("{target}", target.getName()).replace("{message}", message)));
        target.sendMessage(TextUtil.color(recvFormat
                .replace("{sender}", player.getName()).replace("{message}", message)));

        // Update reply targets both ways.
        plugin.state().setLastMessaged(player.getUniqueId(), target.getUniqueId());
        plugin.state().setLastMessaged(target.getUniqueId(), player.getUniqueId());

        // Social spy for staff.
        if (plugin.config().getConfig().getBoolean("regular.messaging.social-spy", true)) {
            String spy = TextUtil.color("&8[Spy] &7" + player.getName() + " -> " + target.getName() + ": &f" + message);
            for (Player staff : plugin.getServer().getOnlinePlayers()) {
                if (staff.hasPermission("essentials.socialspy")
                        && !staff.equals(player) && !staff.equals(target)) {
                    staff.sendMessage(spy);
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("msg") && args.length == 1) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) out.add(p.getName());
            }
        }
        return out;
    }
}
