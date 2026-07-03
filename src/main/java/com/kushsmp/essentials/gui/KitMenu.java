package com.kushsmp.essentials.gui;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The clickable kit selector. Holds one icon per kit the viewing player has
 * permission for; clicking a ready kit claims it. Implements {@link InventoryHolder}
 * so {@link KitMenuListener} can recognise our inventory.
 */
public class KitMenu implements InventoryHolder {

    private final Inventory inventory;
    private final Map<Integer, String> slotKits = new HashMap<>();

    public KitMenu(CustomEssentials plugin, Player player) {
        List<String> available = new ArrayList<>();
        for (String kit : plugin.kits().getKitNames()) {
            if (player.hasPermission(plugin.kits().permissionFor(kit))) {
                available.add(kit);
            }
        }

        int rows = Math.max(1, Math.min(6, (available.size() + 8) / 9));
        String title = plugin.config().msg("kit.menu-title");
        this.inventory = Bukkit.createInventory(this, rows * 9, title);

        int slot = 0;
        for (String kit : available) {
            if (slot >= inventory.getSize()) {
                break;
            }
            inventory.setItem(slot, buildIcon(plugin, player, kit));
            slotKits.put(slot, kit);
            slot++;
        }
    }

    /** The kit bound to a raw slot, or null if that slot isn't a kit icon. */
    public String kitAt(int rawSlot) {
        return slotKits.get(rawSlot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private ItemStack buildIcon(CustomEssentials plugin, Player player, String kit) {
        ItemStack item = new ItemStack(plugin.kits().iconMaterial(kit));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName(TextUtil.color("&e&l" + plugin.kits().displayName(kit)));

        List<String> lore = new ArrayList<>();
        long cooldown = plugin.kits().cooldownSeconds(kit);
        String cooldownText = cooldown < 0 ? "One-time"
                : cooldown == 0 ? "None"
                : TextUtil.formatDuration(cooldown * 1000L);
        lore.add(TextUtil.color("&7Cooldown: &f" + cooldownText));

        long remaining = plugin.kits().remainingCooldown(player.getUniqueId(), kit);
        if (remaining == Long.MAX_VALUE) {
            lore.add(TextUtil.color("&cAlready claimed"));
        } else if (remaining > 0) {
            lore.add(TextUtil.color("&cAvailable in " + TextUtil.formatDuration(remaining)));
        } else {
            lore.add(TextUtil.color("&aReady to claim!"));
            lore.add(TextUtil.color("&8» &7Click to claim"));
        }
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
}
