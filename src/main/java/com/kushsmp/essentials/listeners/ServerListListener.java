package com.kushsmp.essentials.listeners;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.util.TextUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * Replaces the server-list MOTD with the two configured lines. There is no
 * player context during a ping, so only the built-in {online}/{max} tokens and
 * color codes are applied (PlaceholderAPI player placeholders don't apply here).
 */
public class ServerListListener implements Listener {

    private final CustomEssentials plugin;

    public ServerListListener(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if (!plugin.config().getConfig().getBoolean("motd.enabled", true)) {
            return;
        }
        String line1 = plugin.config().getConfig().getString("motd.line1", "");
        String line2 = plugin.config().getConfig().getString("motd.line2", "");

        int online = plugin.getServer().getOnlinePlayers().size();
        int max = event.getMaxPlayers();

        line1 = applyTokens(line1, online, max);
        line2 = applyTokens(line2, online, max);

        String motd = line2.isEmpty() ? line1 : (line1 + "\n" + line2);
        event.setMotd(TextUtil.color(motd));
    }

    private String applyTokens(String s, int online, int max) {
        return s.replace("{online}", String.valueOf(online))
                .replace("{max}", String.valueOf(max));
    }
}
