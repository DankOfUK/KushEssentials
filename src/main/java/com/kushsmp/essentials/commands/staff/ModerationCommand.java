package com.kushsmp.essentials.commands.staff;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import com.kushsmp.essentials.util.TextUtil;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Single executor backing all moderation commands: kick, ban, tempban,
 * unban, mute and unmute. Bans use the server BanList; mutes use the
 * plugin's MuteManager (enforced in ChatListener).
 */
public class ModerationCommand extends BaseCommand {

    public ModerationCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.config().getConfig().getBoolean("staff.moderation.enabled", true)) {
            msg(sender, "feature-disabled");
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "kick":    return handleKick(sender, args);
            case "ban":     return handleBan(sender, args, false);
            case "tempban": return handleBan(sender, args, true);
            case "unban":   return handleUnban(sender, args);
            case "mute":    return handleMute(sender, args);
            case "unmute":  return handleUnmute(sender, args);
            default:        return false;
        }
    }

    // ---------------------------------------------------------------- kick

    private boolean handleKick(CommandSender sender, String[] args) {
        if (!require(sender, "essentials.kick")) return true;
        if (args.length < 1) {
            msg(sender, "unknown-command-usage", "usage", "/kick <player> [reason]");
            return true;
        }
        Player target = onlineTarget(sender, args[0]);
        if (target == null) return true;

        String reason = args.length > 1
                ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
                : plugin.config().getConfig().getString("staff.moderation.default-kick-reason", "Kicked by an operator.");

        target.kickPlayer(plugin.config().msg("moderation.kick-screen", "reason", reason));

        if (plugin.config().getConfig().getBoolean("staff.moderation.broadcast-kicks", true)) {
            plugin.getServer().broadcastMessage(
                    plugin.config().msg("moderation.kicked", "player", target.getName(), "reason", reason));
        } else {
            msg(sender, "moderation.kicked", "player", target.getName(), "reason", reason);
        }
        return true;
    }

    // ----------------------------------------------------------- ban/tempban

    @SuppressWarnings("deprecation")
    private boolean handleBan(CommandSender sender, String[] args, boolean temp) {
        if (!require(sender, temp ? "essentials.tempban" : "essentials.ban")) return true;

        int minArgs = temp ? 2 : 1;
        if (args.length < minArgs) {
            msg(sender, "unknown-command-usage", "usage",
                    temp ? "/tempban <player> <duration> [reason]" : "/ban <player> [reason]");
            return true;
        }

        String name = args[0];
        Date expires = null;
        int reasonStart = 1;

        if (temp) {
            long durationMillis = TextUtil.parseDuration(args[1]);
            if (durationMillis <= 0) {
                msg(sender, "moderation.invalid-duration");
                return true;
            }
            expires = new Date(System.currentTimeMillis() + durationMillis);
            reasonStart = 2;
        }

        String reason = args.length > reasonStart
                ? String.join(" ", Arrays.copyOfRange(args, reasonStart, args.length))
                : plugin.config().getConfig().getString("staff.moderation.default-ban-reason", "Banned by an operator.");

        String source = sender.getName();
        Bukkit.getBanList(BanList.Type.NAME).addBan(name, reason, expires, source);

        // Kick the player if they're online.
        Player online = plugin.getServer().getPlayerExact(name);
        if (online != null) {
            if (temp) {
                online.kickPlayer(plugin.config().msg("moderation.tempban-screen",
                        "reason", reason, "expires", TextUtil.formatDuration(expires.getTime() - System.currentTimeMillis())));
            } else {
                online.kickPlayer(plugin.config().msg("moderation.ban-screen", "reason", reason));
            }
        }

        if (plugin.config().getConfig().getBoolean("staff.moderation.broadcast-bans", true)) {
            plugin.getServer().broadcastMessage(
                    plugin.config().msg("moderation.banned", "player", name, "reason", reason));
        } else {
            msg(sender, "moderation.banned", "player", name, "reason", reason);
        }
        return true;
    }

    // --------------------------------------------------------------- unban

    @SuppressWarnings("deprecation")
    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!require(sender, "essentials.unban")) return true;
        if (args.length < 1) {
            msg(sender, "unknown-command-usage", "usage", "/unban <player>");
            return true;
        }
        String name = args[0];
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        if (!banList.isBanned(name)) {
            msg(sender, "moderation.not-banned");
            return true;
        }
        banList.pardon(name);
        msg(sender, "moderation.unbanned", "player", name);
        return true;
    }

    // ---------------------------------------------------------------- mute

    private boolean handleMute(CommandSender sender, String[] args) {
        if (!require(sender, "essentials.mute")) return true;
        if (args.length < 1) {
            msg(sender, "unknown-command-usage", "usage", "/mute <player> [duration] [reason]");
            return true;
        }

        UUID targetId = resolveUuid(args[0]);
        String targetName = resolveName(args[0]);

        long expiry = -1; // permanent
        int reasonStart = 1;
        String durationLabel = null;

        if (args.length >= 2) {
            long durationMillis = TextUtil.parseDuration(args[1]);
            if (durationMillis > 0) {
                expiry = System.currentTimeMillis() + durationMillis;
                durationLabel = TextUtil.formatDuration(durationMillis);
                reasonStart = 2;
            }
            // If args[1] isn't a duration, treat the rest as the reason (permanent mute).
        }

        String reason = args.length > reasonStart
                ? String.join(" ", Arrays.copyOfRange(args, reasonStart, args.length))
                : "Muted by staff.";

        plugin.mutes().mute(targetId, expiry, reason);

        Player online = plugin.getServer().getPlayerExact(args[0]);
        if (online != null) {
            online.sendMessage(plugin.config().msg("moderation.you-are-muted"));
        }

        if (durationLabel != null) {
            msg(sender, "moderation.muted-temp", "player", targetName, "duration", durationLabel, "reason", reason);
        } else {
            msg(sender, "moderation.muted", "player", targetName, "reason", reason);
        }
        return true;
    }

    // -------------------------------------------------------------- unmute

    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (!require(sender, "essentials.unmute")) return true;
        if (args.length < 1) {
            msg(sender, "unknown-command-usage", "usage", "/unmute <player>");
            return true;
        }
        UUID targetId = resolveUuid(args[0]);
        plugin.mutes().unmute(targetId);
        msg(sender, "moderation.unmuted", "player", resolveName(args[0]));
        return true;
    }

    // -------------------------------------------------------------- helpers

    @SuppressWarnings("deprecation")
    private UUID resolveUuid(String name) {
        Player online = plugin.getServer().getPlayerExact(name);
        if (online != null) return online.getUniqueId();
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        return offline.getUniqueId();
    }

    @SuppressWarnings("deprecation")
    private String resolveName(String name) {
        Player online = plugin.getServer().getPlayerExact(name);
        if (online != null) return online.getName();
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        return offline.getName() != null ? offline.getName() : name;
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
