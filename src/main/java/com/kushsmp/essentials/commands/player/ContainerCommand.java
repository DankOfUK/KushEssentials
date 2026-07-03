package com.kushsmp.essentials.commands.player;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ContainerCommand extends BaseCommand {

    public ContainerCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return true;

        switch (command.getName().toLowerCase()) {
            case "enderchest":
                if (!require(player, "essentials.enderchest")) return true;
                player.openInventory(player.getEnderChest());
                return true;
            case "workbench":
                if (!require(player, "essentials.workbench")) return true;
                player.openWorkbench(null, true);
                return true;
            case "anvil":
                if (!require(player, "essentials.anvil")) return true;
                player.openAnvil(null, true);
                return true;
            default:
                return true;
        }
    }
}
