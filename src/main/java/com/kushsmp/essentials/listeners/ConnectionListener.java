package com.kushsmp.essentials.listeners;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.util.Format;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;

/**
 * Wires rank permissions, the tab list, and the welcome title/messages into the
 * join/quit lifecycle. This runs alongside the existing PlayerListener; both
 * firing is fine.
 */
public class ConnectionListener implements Listener {

    private final CustomEssentials plugin;

    public ConnectionListener(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean firstJoin = !player.hasPlayedBefore();

        // Ensure the player has a rank stored, then apply permissions.
        if (plugin.config().getData("userdata.yml").getString("ranks." + player.getUniqueId()) == null) {
            plugin.ranks().setPlayerRank(player.getUniqueId(), plugin.ranks().getDefaultRankName());
        }
        plugin.permissions().apply(player);

        // Tab list for the joiner and everyone else (so prefixes/sorting update).
        plugin.tab().refreshAll();

        // Welcome title.
        showWelcomeTitle(player, firstJoin);

        // Custom join messages (override the vanilla message when enabled).
        handleJoinMessage(event, player, firstJoin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.permissions().remove(player.getUniqueId());
        plugin.tab().clearSorting(player);
        // Refresh remaining players' tab on the next tick (player is gone by then).
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.tab().refreshAll());
    }

    private void showWelcomeTitle(Player player, boolean firstJoin) {
        if (!plugin.config().getConfig().getBoolean("welcome.title.enabled", true)) {
            return;
        }
        String titleRaw;
        String subRaw;
        if (firstJoin) {
            titleRaw = plugin.config().getConfig().getString("welcome.first-join.title",
                    plugin.config().getConfig().getString("welcome.title.title", "&bWelcome"));
            subRaw = plugin.config().getConfig().getString("welcome.first-join.subtitle",
                    plugin.config().getConfig().getString("welcome.title.subtitle", "{player}"));
        } else {
            titleRaw = plugin.config().getConfig().getString("welcome.title.title", "&bWelcome");
            subRaw = plugin.config().getConfig().getString("welcome.title.subtitle", "{player}");
        }

        long fadeIn = plugin.config().getConfig().getLong("welcome.title.fade-in-ms", 500);
        long stay = plugin.config().getConfig().getLong("welcome.title.stay-ms", 3500);
        long fadeOut = plugin.config().getConfig().getLong("welcome.title.fade-out-ms", 1000);

        Component titleComp = Format.component(plugin, player, titleRaw);
        Component subComp = Format.component(plugin, player, subRaw);
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));

        player.showTitle(Title.title(titleComp, subComp, times));
    }

    private void handleJoinMessage(PlayerJoinEvent event, Player player, boolean firstJoin) {
        if (firstJoin && plugin.config().getConfig().getBoolean("welcome.first-join-message.enabled", true)) {
            String raw = plugin.config().getConfig().getString("welcome.first-join-message.message", "");
            event.joinMessage(Format.component(plugin, player, raw));
        } else if (plugin.config().getConfig().getBoolean("welcome.join-message.enabled", false)) {
            String raw = plugin.config().getConfig().getString("welcome.join-message.message", "");
            event.joinMessage(Format.component(plugin, player, raw));
        }
        // If neither override is enabled, the vanilla join message is left untouched.
    }
}
