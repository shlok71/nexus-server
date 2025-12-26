package com.nexus.core.commands;

import com.nexus.core.NexusCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to send players to the hub/lobby
 */
public class HubCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    NexusCore.getInstance().getHubManager().sendToHub(target);
                    sender.sendMessage(ChatColor.AQUA + "Sent " + target.getName() + " to the hub!");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Usage: /hub [player]");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            if (!player.hasPermission("nexus.hub.others")) {
                player.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                NexusCore.getInstance().getHubManager().sendToHub(target);
                player.sendMessage(ChatColor.AQUA + "Sent " + target.getName() + " to the hub!");
                target.sendMessage(ChatColor.AQUA + "You were sent to the hub by " + player.getName() + "!");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
        }

        // Send sender to hub
        NexusCore.getInstance().getHubManager().sendToHub(player);
        return true;
    }
}
