package com.kushsmp.essentials.commands;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Shared base for all plugin commands. Provides convenient access to the
 * plugin, config/messages, and common guards.
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final CustomEssentials plugin;

    protected BaseCommand(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    protected ConfigManager cfg() {
        return plugin.config();
    }

    /** Send a templated message from messages.yml. */
    protected void msg(CommandSender to, String key, String... replacements) {
        cfg().send(to, key, replacements);
    }

    /** Ensure the sender is a player; messages and returns null otherwise. */
    protected Player requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            msg(sender, "player-only");
            return null;
        }
        return (Player) sender;
    }

    /** Permission guard that messages on failure. */
    protected boolean require(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            msg(sender, "no-permission");
            return false;
        }
        return true;
    }

    /** Resolve an online player by name, messaging on failure. */
    protected Player onlineTarget(CommandSender sender, String name) {
        Player target = plugin.getServer().getPlayerExact(name);
        if (target == null) {
            msg(sender, "player-not-found", "player", name);
        }
        return target;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
