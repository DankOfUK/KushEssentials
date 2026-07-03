package com.kushsmp.essentials.listeners;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.staff.StaffChatCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Enforces mutes, routes toggled staff-chat messages, and clears AFK on chat.
 */
public class ChatListener implements Listener {

    private final CustomEssentials plugin;

    public ChatListener(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Muted players cannot speak.
        if (plugin.mutes().isMuted(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(plugin.config().msg("moderation.you-are-muted"));
            return;
        }

        // Staff-chat routing when the sender has toggled it on.
        if (plugin.state().isStaffChatToggled(player.getUniqueId())
                && player.hasPermission("essentials.staffchat")) {
            event.setCancelled(true);
            StaffChatCommand.broadcastToStaff(plugin, player.getName(), event.getMessage());
            return;
        }

        // Talking clears AFK. Touch Bukkit/state on the main thread.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.state().markActive(player.getUniqueId());
            if (plugin.state().isAfk(player.getUniqueId())) {
                plugin.state().setAfk(player.getUniqueId(), false);
                if (plugin.config().getConfig().getBoolean("regular.afk.broadcast", true)) {
                    plugin.getServer().broadcastMessage(
                            plugin.config().msg("afk.no-longer-afk", "player", player.getName()));
                }
            }
        });
    }
}
