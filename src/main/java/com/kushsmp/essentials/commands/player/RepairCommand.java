package com.kushsmp.essentials.commands.player;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class RepairCommand extends BaseCommand {

    public RepairCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!require(player, "essentials.repair")) return true;

        if (args.length >= 1 && args[0].equalsIgnoreCase("all")) {
            for (ItemStack item : player.getInventory().getContents()) {
                repair(item);
            }
            for (ItemStack item : player.getInventory().getArmorContents()) {
                repair(item);
            }
            msg(player, "player.repaired-all");
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (repair(hand)) {
            msg(player, "player.repaired");
        } else {
            msg(player, "player.nothing-to-repair");
        }
        return true;
    }

    private boolean repair(ItemStack item) {
        if (item == null || item.getType().getMaxDurability() <= 0) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(0);
            item.setItemMeta(meta);
            return true;
        }
        return false;
    }
}
