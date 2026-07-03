package com.kushsmp.essentials.managers;

import com.kushsmp.essentials.CustomEssentials;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Coordinates delayed teleports (with optional warmup and move-cancel),
 * /back history, and pending /tpa requests.
 */
public class TeleportManager {

    /** A pending teleport request between two players. */
    public static class Request {
        public final UUID from;
        public final UUID to;
        public final boolean here; // true = "from" wants "to" to come to them
        public final long expiresAt;

        Request(UUID from, UUID to, boolean here, long expiresAt) {
            this.from = from;
            this.to = to;
            this.here = here;
            this.expiresAt = expiresAt;
        }
    }

    private final CustomEssentials plugin;

    // Last location per player, for /back.
    private final Map<UUID, Location> lastLocation = new HashMap<>();
    // Pending requests keyed by the *recipient* uuid.
    private final Map<UUID, Request> pendingRequests = new HashMap<>();

    public TeleportManager(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    // ----- /back -----

    public void recordLocation(Player player) {
        lastLocation.put(player.getUniqueId(), player.getLocation());
    }

    public Location getLastLocation(UUID uuid) {
        return lastLocation.get(uuid);
    }

    public void clearLastLocation(UUID uuid) {
        lastLocation.remove(uuid);
    }

    // ----- Warmup teleport -----

    /**
     * Teleport with an optional warmup. Records /back location on success.
     *
     * @param delaySeconds warmup seconds (0 = instant)
     * @param cancelOnMove cancel if the player moves during warmup
     */
    public void teleportWithWarmup(Player player, Location destination, int delaySeconds,
                                   boolean cancelOnMove, Runnable onSuccess) {
        if (delaySeconds <= 0 || player.hasPermission("essentials.teleport.instant")) {
            doTeleport(player, destination);
            if (onSuccess != null) onSuccess.run();
            return;
        }

        plugin.config().send(player, "teleport.warmup", "seconds", String.valueOf(delaySeconds));
        final Location startLoc = player.getLocation().clone();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (cancelOnMove && hasMoved(startLoc, player.getLocation())) {
                    plugin.config().send(player, "teleport.cancelled-move");
                    cancel();
                    return;
                }
                doTeleport(player, destination);
                if (onSuccess != null) onSuccess.run();
            }
        }.runTaskLater(plugin, delaySeconds * 20L);
    }

    private boolean hasMoved(Location a, Location b) {
        return a.getWorld() != b.getWorld()
                || a.getBlockX() != b.getBlockX()
                || a.getBlockY() != b.getBlockY()
                || a.getBlockZ() != b.getBlockZ();
    }

    private void doTeleport(Player player, Location destination) {
        recordLocation(player);
        player.teleport(destination);
    }

    // ----- TPA requests -----

    public void createRequest(Player from, Player to, boolean here) {
        long timeout = plugin.config().getConfig().getLong("regular.tpa.request-timeout-seconds", 60) * 1000L;
        pendingRequests.put(to.getUniqueId(),
                new Request(from.getUniqueId(), to.getUniqueId(), here, System.currentTimeMillis() + timeout));
    }

    public Request getRequest(UUID recipient) {
        Request request = pendingRequests.get(recipient);
        if (request == null) {
            return null;
        }
        if (System.currentTimeMillis() > request.expiresAt) {
            pendingRequests.remove(recipient);
            return null;
        }
        return request;
    }

    public void removeRequest(UUID recipient) {
        pendingRequests.remove(recipient);
    }

    /**
     * Run the given consumer with the teleporting player and destination player
     * resolved according to the request direction, then clear the request.
     */
    public void completeRequest(Request request, Consumer<Player[]> action) {
        Player from = plugin.getServer().getPlayer(request.from);
        Player to = plugin.getServer().getPlayer(request.to);
        pendingRequests.remove(request.to);
        if (from == null || to == null) {
            return;
        }
        // here == false: "from" teleports to "to".
        // here == true:  "to" teleports to "from".
        Player mover = request.here ? to : from;
        Player anchor = request.here ? from : to;
        action.accept(new Player[]{mover, anchor});
    }
}
