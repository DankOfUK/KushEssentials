package com.kushsmp.essentials.commands.player;

import com.kushsmp.essentials.CustomEssentials;
import com.kushsmp.essentials.commands.BaseCommand;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HealFeedCommand extends BaseCommand {

    public HealFeedCommand(CustomEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean feed = command.getName().equalsIgnoreCase("feed");
        String basePerm = feed ? "essentials.feed" : "essentials.heal";

        if (args.length >= 1) {
            if (!require(sender, basePerm + ".others")) return true;
            Player target = onlineTarget(sender, args[0]);
            if (target == null) return true;
            apply(target, feed);
            msg(sender, feed ? "player.fed-other" : "player.healed-other", "player", target.getName());
            return true;
        }

        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!require(player, basePerm)) return true;
        apply(player, feed);
        msg(player, feed ? "player.fed" : "player.healed");
        return true;
    }

    private void apply(Player player, boolean feed) {
        if (feed) {
            player.setFoodLevel(20);
            player.setSaturation(20f);
            player.setExhaustion(0f);
        } else {
            double max = player.getAttribute(Attribute.MAX_HEALTH).getValue();
            player.setHealth(max);
            player.setFireTicks(0);
            player.setFoodLevel(20);
            player.setSaturation(20f);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) out.add(p.getName());
            }
        }
        return out;
    }
}
