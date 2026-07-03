package com.kushsmp.essentials.listeners;

import com.kushsmp.essentials.CustomEssentials;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Core gameplay listener: first-join kit + spawn teleport, vanish hiding for
 * new joiners, god-mode damage cancel, no-mob-targeting for vanished staff,
 * the freeze leash, /back recording on death, and AFK activity tracking.
 */
public class PlayerListener implements Listener {

    private final CustomEssentials plugin;

    public PlayerListener(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------- join

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.state().markActive(player.getUniqueId());

        // Hide currently-vanished staff from the new arrival.
        for (Player other : plugin.getServer().getOnlinePlayers()) {
            if (plugin.state().isVanished(other.getUniqueId())
                    && !player.hasPermission("essentials.vanish.see")) {
                player.hidePlayer(plugin, other);
            }
        }

        // First join handling.
        if (!player.hasPlayedBefore()) {
            // First-join kit.
            if (plugin.config().getConfig().getBoolean("kits.enabled", true)) {
                String firstKit = plugin.config().getConfig().getString("kits.first-join-kit", "");
                if (firstKit != null && !firstKit.isEmpty() && plugin.kits().kitExists(firstKit)) {
                    plugin.kits().claim(player, firstKit, true);
                }
            }
            // Teleport to spawn on first join.
            if (plugin.config().getConfig().getBoolean("regular.spawn.enabled", true)
                    && plugin.config().getConfig().getBoolean("regular.spawn.teleport-on-first-join", true)
                    && plugin.warps().isSpawnSet()) {
                player.teleport(plugin.warps().getSpawn());
            }
        }
    }

    // ------------------------------------------------------------- quit

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.state().clear(event.getPlayer().getUniqueId());
    }

    // ------------------------------------------------------------- move

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        boolean movedBlock = from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();

        // Freeze leash: pull the player back if they stray too far.
        if (plugin.state().isFrozen(player.getUniqueId())) {
            double radius = plugin.config().getConfig().getDouble("staff.freeze.leash-radius", 1.0);
            if (movedBlock) {
                // Keep them anchored: cancel horizontal/vertical block movement.
                Location anchored = from.clone();
                anchored.setPitch(to.getPitch());
                anchored.setYaw(to.getYaw());
                event.setTo(anchored);
                player.sendMessage(plugin.config().msg("freeze.cannot-move"));
            }
            return;
        }

        if (!movedBlock) {
            return;
        }

        // Activity tracking + un-AFK on movement.
        plugin.state().markActive(player.getUniqueId());
        if (plugin.state().isAfk(player.getUniqueId())) {
            plugin.state().setAfk(player.getUniqueId(), false);
            if (plugin.config().getConfig().getBoolean("regular.afk.broadcast", true)) {
                plugin.getServer().broadcastMessage(
                        plugin.config().msg("afk.no-longer-afk", "player", player.getName()));
            }
        }
    }

    // ---------------------------------------------------------- respawn

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (plugin.config().getConfig().getBoolean("regular.spawn.enabled", true)
                && plugin.config().getConfig().getBoolean("regular.spawn.teleport-on-respawn", false)
                && plugin.warps().isSpawnSet()) {
            event.setRespawnLocation(plugin.warps().getSpawn());
        }
    }

    // ------------------------------------------------------------ death

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (plugin.config().getConfig().getBoolean("regular.back.enabled", true)
                && plugin.config().getConfig().getBoolean("regular.back.on-death", true)) {
            plugin.teleports().recordLocation(event.getEntity());
        }
    }

    // ------------------------------------------------------- god / damage

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.state().isGod(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    // -------------------------------------------- vanish: no mob targeting

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!plugin.config().getConfig().getBoolean("staff.vanish.no-mob-targeting", true)) {
            return;
        }
        if (event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            if (plugin.state().isVanished(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
