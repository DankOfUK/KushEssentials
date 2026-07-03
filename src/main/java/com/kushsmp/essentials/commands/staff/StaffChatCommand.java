package com.kushsmp.essentials.commands.staff;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import com.kushsmp.essentials.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /staffchat [message]
 * - With a message: sends a one-off message to staff chat.
 * - Without a message: toggles whether the sender's normal chat is routed
 *   to staff chat (handled in ChatListener).
 */
public class StaffChatCommand extends BaseCommand {

    public StaffChatCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.config().getConfig().getBoolean("staff.staff-chat.enabled", true)) {
            msg(sender, "feature-disabled");
            return true;
        }
        if (!require(sender, "essentials.staffchat")) return true;

        // No args from a player -> toggle routing.
        if (args.length == 0) {
            Player player = requirePlayer(sender);
            if (player == null) return true;
            boolean newState = !plugin.state().isStaffChatToggled(player.getUniqueId());
            plugin.state().setStaffChatToggled(player.getUniqueId(), newState);
            msg(player, newState ? "staffchat.toggled-on" : "staffchat.toggled-off");
            return true;
        }

        String message = String.join(" ", args);
        String senderName = (sender instanceof Player) ? sender.getName() : "Console";
        broadcastToStaff(plugin, senderName, message);
        return true;
    }

    /** Render and dispatch a staff-chat line to all staff-chat receivers (and console). */
    public static void broadcastToStaff(CustomEssentials plugin, String senderName, String message) {
        String format = plugin.config().getConfig().getString(
                "staff.staff-chat.format", "&8[&cStaff&8] &7{player}&8: &f{message}");
        String rendered = TextUtil.color(format
                .replace("{player}", senderName)
                .replace("{message}", message));

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.hasPermission("essentials.staffchat.receive") || p.hasPermission("essentials.staffchat")) {
                p.sendMessage(rendered);
            }
        }
        plugin.getServer().getConsoleSender().sendMessage(rendered);
    }
}
