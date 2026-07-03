package com.kushsmp.essentials.gui;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.kit.KitCommand;
import com.kushsmp.essentials.managers.KitManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Drives the {@link KitMenu}: cancels item movement in the selector and claims
 * the clicked kit.
 */
public class KitMenuListener implements Listener {

    private final CustomEssentials plugin;

    public KitMenuListener(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof KitMenu)) {
            return;
        }
        // Menu is read-only: block every interaction (including shift-clicks from below).
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        // Only clicks on the menu's own slots claim a kit.
        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof KitMenu)) {
            return;
        }

        KitMenu menu = (KitMenu) event.getInventory().getHolder();
        String kit = menu.kitAt(event.getSlot());
        if (kit == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        player.closeInventory();

        if (!plugin.kits().isEnabled()) {
            plugin.config().send(player, "feature-disabled");
            return;
        }
        KitManager.ClaimResult result = plugin.kits().claim(player, kit, false);
        KitCommand.sendClaimFeedback(plugin, player, kit, result);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof KitMenu) {
            event.setCancelled(true);
        }
    }
}
