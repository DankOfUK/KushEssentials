package com.kushsmp.essentials.managers;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Loads kits from kits.yml, builds their item stacks, and enforces
 * per-kit cooldowns (persisted in userdata.yml).
 */
public class KitManager {

    private static final String DATA = "userdata.yml";

    private final CustomEssentials plugin;

    public KitManager(CustomEssentials plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration kits() {
        return plugin.config().getKits();
    }

    private FileConfiguration userData() {
        return plugin.config().getData(DATA);
    }

    public boolean isEnabled() {
        return plugin.config().getConfig().getBoolean("kits.enabled", true);
    }

    public List<String> getKitNames() {
        ConfigurationSection section = kits().getConfigurationSection("kits");
        if (section == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(section.getKeys(false));
    }

    public boolean kitExists(String name) {
        return kits().contains("kits." + name.toLowerCase());
    }

    public String permissionFor(String name) {
        String configured = kits().getString("kits." + name.toLowerCase() + ".permission");
        return configured != null ? configured : "essentials.kit." + name.toLowerCase();
    }

    public long cooldownSeconds(String name) {
        return kits().getLong("kits." + name.toLowerCase() + ".cooldown", 0);
    }

    /**
     * Remaining cooldown in milliseconds for a player/kit, or 0 if ready.
     * Returns Long.MAX_VALUE if it's a one-time kit already claimed.
     */
    public long remainingCooldown(UUID uuid, String name) {
        long cd = cooldownSeconds(name);
        String path = "cooldowns." + uuid + "." + name.toLowerCase();
        long last = userData().getLong(path, 0L);

        if (cd < 0) {
            // One-time kit: any non-zero timestamp means used.
            return last > 0 ? Long.MAX_VALUE : 0L;
        }
        if (cd == 0 || last == 0) {
            return 0L;
        }
        long elapsed = System.currentTimeMillis() - last;
        long total = cd * 1000L;
        return elapsed >= total ? 0L : total - elapsed;
    }

    private void markClaimed(UUID uuid, String name) {
        userData().set("cooldowns." + uuid + "." + name.toLowerCase(), System.currentTimeMillis());
        plugin.config().saveData(DATA);
    }

    /**
     * Attempt to give a kit, enforcing cooldown. Use {@code force} for /givekit
     * (bypasses cooldown and permission). Returns a result describing the outcome.
     */
    public ClaimResult claim(Player player, String name, boolean force) {
        name = name.toLowerCase();
        if (!kitExists(name)) {
            return ClaimResult.NOT_FOUND;
        }
        if (!force) {
            if (!player.hasPermission(permissionFor(name))) {
                return ClaimResult.NO_PERMISSION;
            }
            long remaining = remainingCooldown(player.getUniqueId(), name);
            if (remaining == Long.MAX_VALUE) {
                return ClaimResult.ONE_TIME_USED;
            }
            if (remaining > 0) {
                return ClaimResult.cooldown(remaining);
            }
        }

        boolean overflowed = giveItems(player, name);
        if (!force) {
            markClaimed(player.getUniqueId(), name);
        } else {
            // Force-give still records a timestamp so cooldown logic stays sane.
            markClaimed(player.getUniqueId(), name);
        }
        runKitCommands(player, name);

        ClaimResult result = ClaimResult.SUCCESS;
        result.overflowed = overflowed;
        return result;
    }

    /** Builds and gives the kit's items/armor. Returns true if any item overflowed. */
    private boolean giveItems(Player player, String name) {
        String base = "kits." + name + ".";
        PlayerInventory inv = player.getInventory();
        boolean overflowed = false;

        List<Map<?, ?>> items = kits().getMapList(base + "items");
        for (Map<?, ?> raw : items) {
            ItemStack item = buildItem(raw);
            if (item != null) {
                Map<Integer, ItemStack> leftover = inv.addItem(item);
                if (!leftover.isEmpty()) {
                    overflowed = true;
                    leftover.values().forEach(stack ->
                            player.getWorld().dropItemNaturally(player.getLocation(), stack));
                }
            }
        }

        ConfigurationSection armor = kits().getConfigurationSection(base + "armor");
        if (armor != null) {
            applyArmor(inv, "helmet", armor);
            applyArmor(inv, "chestplate", armor);
            applyArmor(inv, "leggings", armor);
            applyArmor(inv, "boots", armor);
        }
        return overflowed;
    }

    private void applyArmor(PlayerInventory inv, String slot, ConfigurationSection armor) {
        ConfigurationSection section = armor.getConfigurationSection(slot);
        if (section == null) {
            return;
        }
        ItemStack item = buildItem(section.getValues(false));
        if (item == null) {
            return;
        }
        switch (slot) {
            case "helmet": inv.setHelmet(item); break;
            case "chestplate": inv.setChestplate(item); break;
            case "leggings": inv.setLeggings(item); break;
            case "boots": inv.setBoots(item); break;
            default: break;
        }
    }

    /** Builds an ItemStack from a raw config map (material, amount, name, lore, enchants, unbreakable). */
    @SuppressWarnings("unchecked")
    private ItemStack buildItem(Map<?, ?> raw) {
        Object matObj = raw.get("material");
        if (matObj == null) {
            return null;
        }
        Material material = Material.matchMaterial(matObj.toString().toUpperCase());
        if (material == null) {
            plugin.getLogger().warning("Unknown kit material: " + matObj);
            return null;
        }
        int amount = raw.get("amount") instanceof Number ? ((Number) raw.get("amount")).intValue() : 1;
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        if (raw.get("name") != null) {
            meta.setDisplayName(TextUtil.color(raw.get("name").toString()));
        }
        if (raw.get("lore") instanceof List) {
            List<String> lore = new ArrayList<>();
            for (Object line : (List<Object>) raw.get("lore")) {
                lore.add(TextUtil.color(line.toString()));
            }
            meta.setLore(lore);
        }
        if (Boolean.TRUE.equals(raw.get("unbreakable"))) {
            meta.setUnbreakable(true);
        }
        if (raw.get("enchants") instanceof Map) {
            Map<?, ?> enchants = (Map<?, ?>) raw.get("enchants");
            for (Map.Entry<?, ?> entry : enchants.entrySet()) {
                Enchantment ench = resolveEnchant(entry.getKey().toString());
                int level = entry.getValue() instanceof Number ? ((Number) entry.getValue()).intValue() : 1;
                if (ench != null) {
                    meta.addEnchant(ench, level, true);
                } else {
                    plugin.getLogger().warning("Unknown enchant in kit: " + entry.getKey());
                }
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    @SuppressWarnings("deprecation")
    private Enchantment resolveEnchant(String key) {
        String lower = key.toLowerCase();
        Enchantment byKey = Enchantment.getByKey(NamespacedKey.minecraft(lower));
        if (byKey != null) {
            return byKey;
        }
        // Fall back to legacy name lookup for older API names.
        return Enchantment.getByName(key.toUpperCase());
    }

    private void runKitCommands(Player player, String name) {
        List<String> commands = kits().getStringList("kits." + name + ".commands");
        for (String command : commands) {
            String parsed = command.replace("{player}", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsed);
        }
    }

    // ---------------------------------------------------------------------
    //  Kit creation / deletion (admin)
    // ---------------------------------------------------------------------

    /** Outcome of a {@link #createKit} attempt. */
    public enum CreateResult { CREATED, ALREADY_EXISTS, EMPTY }

    /**
     * Build a new kit from the player's current inventory (storage slots,
     * off-hand and worn armor) and persist it to kits.yml with no cooldown.
     */
    public CreateResult createKit(Player player, String name) {
        name = name.toLowerCase();
        if (kitExists(name)) {
            return CreateResult.ALREADY_EXISTS;
        }

        PlayerInventory inv = player.getInventory();
        List<Map<String, Object>> items = new ArrayList<>();
        for (ItemStack stack : inv.getStorageContents()) {
            if (stack != null && stack.getType() != Material.AIR) {
                items.add(serializeItem(stack));
            }
        }
        ItemStack offHand = inv.getItemInOffHand();
        if (offHand != null && offHand.getType() != Material.AIR) {
            items.add(serializeItem(offHand));
        }

        Map<String, Object> armor = new LinkedHashMap<>();
        putArmor(armor, "helmet", inv.getHelmet());
        putArmor(armor, "chestplate", inv.getChestplate());
        putArmor(armor, "leggings", inv.getLeggings());
        putArmor(armor, "boots", inv.getBoots());

        if (items.isEmpty() && armor.isEmpty()) {
            return CreateResult.EMPTY;
        }

        String base = "kits." + name + ".";
        kits().set(base + "cooldown", 0);
        kits().set(base + "permission", "essentials.kit." + name);
        if (!items.isEmpty()) {
            kits().set(base + "items", items);
        }
        if (!armor.isEmpty()) {
            kits().set(base + "armor", armor);
        }
        plugin.config().saveKits();
        return CreateResult.CREATED;
    }

    /** Delete a kit from kits.yml. Returns false if it didn't exist. */
    public boolean deleteKit(String name) {
        name = name.toLowerCase();
        if (!kitExists(name)) {
            return false;
        }
        kits().set("kits." + name, null);
        plugin.config().saveKits();
        return true;
    }

    private void putArmor(Map<String, Object> armor, String slot, ItemStack piece) {
        if (piece != null && piece.getType() != Material.AIR) {
            armor.put(slot, serializeItem(piece));
        }
    }

    /** Inverse of {@link #buildItem}: turn an ItemStack into a kits.yml config map. */
    private Map<String, Object> serializeItem(ItemStack stack) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("material", stack.getType().name());
        if (stack.getAmount() != 1) {
            map.put("amount", stack.getAmount());
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                map.put("name", meta.getDisplayName().replace('§', '&'));
            }
            if (meta.hasLore() && meta.getLore() != null) {
                List<String> lore = new ArrayList<>();
                for (String line : meta.getLore()) {
                    lore.add(line.replace('§', '&'));
                }
                map.put("lore", lore);
            }
            if (meta.isUnbreakable()) {
                map.put("unbreakable", true);
            }
            if (meta.hasEnchants()) {
                Map<String, Object> enchants = new LinkedHashMap<>();
                for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    enchants.put(entry.getKey().getKey().getKey(), entry.getValue());
                }
                map.put("enchants", enchants);
            }
        }
        return map;
    }

    // ---------------------------------------------------------------------
    //  GUI helpers
    // ---------------------------------------------------------------------

    /** Display name for the menu: the kit's configured {@code display}, else its capitalized name. */
    public String displayName(String name) {
        String display = kits().getString("kits." + name.toLowerCase() + ".display");
        if (display != null) {
            return display;
        }
        String lower = name.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    /** Icon material for the menu: configured {@code icon}, else the first item, else CHEST. */
    public Material iconMaterial(String name) {
        String configured = kits().getString("kits." + name.toLowerCase() + ".icon");
        if (configured != null) {
            Material match = Material.matchMaterial(configured.toUpperCase());
            if (match != null) {
                return match;
            }
        }
        for (Map<?, ?> raw : kits().getMapList("kits." + name.toLowerCase() + ".items")) {
            Object mat = raw.get("material");
            if (mat != null) {
                Material match = Material.matchMaterial(mat.toString().toUpperCase());
                if (match != null) {
                    return match;
                }
            }
        }
        return Material.CHEST;
    }

    /** Result of a claim attempt. */
    public static class ClaimResult {
        public enum Type { SUCCESS, NOT_FOUND, NO_PERMISSION, COOLDOWN, ONE_TIME_USED }

        public static final ClaimResult SUCCESS = new ClaimResult(Type.SUCCESS, 0);
        public static final ClaimResult NOT_FOUND = new ClaimResult(Type.NOT_FOUND, 0);
        public static final ClaimResult NO_PERMISSION = new ClaimResult(Type.NO_PERMISSION, 0);
        public static final ClaimResult ONE_TIME_USED = new ClaimResult(Type.ONE_TIME_USED, 0);

        public final Type type;
        public final long remainingMillis;
        public boolean overflowed;

        private ClaimResult(Type type, long remainingMillis) {
            this.type = type;
            this.remainingMillis = remainingMillis;
        }

        public static ClaimResult cooldown(long remainingMillis) {
            return new ClaimResult(Type.COOLDOWN, remainingMillis);
        }
    }
}
