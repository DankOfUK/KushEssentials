package com.kushsmp.essentials.commands.kit;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import com.kushsmp.essentials.gui.KitMenu;
import com.kushsmp.essentials.managers.KitManager;
import com.kushsmp.essentials.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KitCommand extends BaseCommand {

    private static final List<String> SUBCOMMANDS = Arrays.asList("create", "delete", "list");

    public KitCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!plugin.kits().isEnabled()) {
            msg(player, "feature-disabled");
            return true;
        }
        if (!require(player, "essentials.kit")) return true;

        // Admin subcommands.
        if (args.length >= 1 && args[0].equalsIgnoreCase("create")) {
            return handleCreate(player, args);
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("delete")) {
            return handleDelete(player, args);
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
            sendKitList(player);
            return true;
        }

        // No argument -> open the GUI selector.
        if (args.length < 1) {
            openMenu(player);
            return true;
        }

        // Otherwise treat the argument as a kit name to claim.
        String kitName = args[0];
        KitManager.ClaimResult result = plugin.kits().claim(player, kitName, false);
        sendClaimFeedback(plugin, player, kitName, result);
        return true;
    }

    private void openMenu(Player player) {
        boolean any = false;
        for (String kit : plugin.kits().getKitNames()) {
            if (player.hasPermission(plugin.kits().permissionFor(kit))) {
                any = true;
                break;
            }
        }
        if (!any) {
            msg(player, "kit.none-available");
            return;
        }
        player.openInventory(new KitMenu(plugin, player).getInventory());
    }

    private void sendKitList(Player player) {
        List<String> available = new ArrayList<>();
        for (String kit : plugin.kits().getKitNames()) {
            if (player.hasPermission(plugin.kits().permissionFor(kit))) {
                available.add(kit);
            }
        }
        if (available.isEmpty()) {
            player.sendMessage(plugin.config().msg("kit.list", "kits", "(none available)"));
        } else {
            msg(player, "kit.list", "kits", String.join(", ", available));
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (!require(player, "essentials.kit.admin")) return true;
        if (args.length < 2) {
            msg(player, "kit.create-usage");
            return true;
        }
        String name = args[1];
        KitManager.CreateResult result = plugin.kits().createKit(player, name);
        switch (result) {
            case CREATED:
                msg(player, "kit.created", "kit", name.toLowerCase());
                break;
            case ALREADY_EXISTS:
                msg(player, "kit.already-exists", "kit", name.toLowerCase());
                break;
            case EMPTY:
                msg(player, "kit.empty-inventory");
                break;
            default:
                break;
        }
        return true;
    }

    private boolean handleDelete(Player player, String[] args) {
        if (!require(player, "essentials.kit.admin")) return true;
        if (args.length < 2) {
            msg(player, "kit.delete-usage");
            return true;
        }
        String name = args[1];
        if (plugin.kits().deleteKit(name)) {
            msg(player, "kit.deleted", "kit", name.toLowerCase());
        } else {
            msg(player, "kit.not-found", "kit", name);
        }
        return true;
    }

    /**
     * Shared claim-result messaging, used by both {@code /kit <name>} and the
     * {@link KitMenu} selector.
     */
    public static void sendClaimFeedback(CustomEssentials plugin, Player player,
                                         String kitName, KitManager.ClaimResult result) {
        switch (result.type) {
            case SUCCESS:
                plugin.config().send(player, "kit.claimed", "kit", kitName);
                if (result.overflowed) plugin.config().send(player, "kit.inventory-full");
                break;
            case NOT_FOUND:
                plugin.config().send(player, "kit.not-found", "kit", kitName);
                break;
            case NO_PERMISSION:
                plugin.config().send(player, "kit.no-permission", "kit", kitName);
                break;
            case ONE_TIME_USED:
                plugin.config().send(player, "kit.one-time-used", "kit", kitName);
                break;
            case COOLDOWN:
                plugin.config().send(player, "kit.cooldown", "kit", kitName,
                        "time", TextUtil.formatDuration(result.remainingMillis));
                break;
            default:
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!(sender instanceof Player)) {
            return out;
        }
        Player player = (Player) sender;

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            if (player.hasPermission("essentials.kit.admin")) {
                for (String sub : SUBCOMMANDS) {
                    if (sub.startsWith(prefix)) out.add(sub);
                }
            }
            for (String kit : plugin.kits().getKitNames()) {
                if (kit.startsWith(prefix)
                        && player.hasPermission(plugin.kits().permissionFor(kit))) {
                    out.add(kit);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")
                && player.hasPermission("essentials.kit.admin")) {
            for (String kit : plugin.kits().getKitNames()) {
                if (kit.startsWith(args[1].toLowerCase())) out.add(kit);
            }
        }
        return out;
    }
}
