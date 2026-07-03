package com.kushsmp.essentials.managers;

import com.kushsmp.essentials.CustomEssentials;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Grants each player the permissions of their assigned rank using a
 * PermissionAttachment. Re-applying replaces the previous attachment, so rank
 * changes take effect immediately.
 */
public class PermissionManager {

    private final CustomEssentials plugin;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public PermissionManager(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    /** (Re)apply the player's rank permissions. */
    public void apply(Player player) {
        remove(player.getUniqueId());

        String rankName = plugin.ranks().getPlayerRankName(player.getUniqueId());
        PermissionAttachment attachment = player.addAttachment(plugin);
        for (String perm : plugin.ranks().resolvePermissions(rankName)) {
            boolean value = true;
            String node = perm;
            if (perm.startsWith("-")) {        // allow explicit negations like "-essentials.fly"
                value = false;
                node = perm.substring(1);
            }
            attachment.setPermission(node, value);
        }
        attachments.put(player.getUniqueId(), attachment);
        player.recalculatePermissions();
    }

    public void remove(UUID uuid) {
        PermissionAttachment existing = attachments.remove(uuid);
        if (existing != null) {
            try {
                existing.remove();
            } catch (IllegalArgumentException ignored) {
                // Attachment already gone (e.g. player quit) - safe to ignore.
            }
        }
    }

    public void removeAll() {
        for (UUID uuid : new HashMap<>(attachments).keySet()) {
            remove(uuid);
        }
    }
}
