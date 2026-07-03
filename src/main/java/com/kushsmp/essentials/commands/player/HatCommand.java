package com.kushsmp.essentials.commands.player;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HatCommand extends BaseCommand {

    public HatCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!require(player, "essentials.hat")) return true;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            msg(player, "player.hat-empty");
            return true;
        }
        ItemStack current = player.getInventory().getHelmet();
        ItemStack hat = hand.clone();
        hat.setAmount(1);

        // Swap: remove one from hand, put it on the head, return old helmet.
        hand.setAmount(hand.getAmount() - 1);
        player.getInventory().setItemInMainHand(hand.getAmount() > 0 ? hand : null);
        player.getInventory().setHelmet(hat);
        if (current != null && current.getType() != Material.AIR) {
            player.getInventory().addItem(current);
        }
        msg(player, "player.hat-set");
        return true;
    }
}
