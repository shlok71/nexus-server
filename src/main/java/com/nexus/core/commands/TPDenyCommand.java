package com.nexus.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to deny incoming teleport requests
 */
public class TPDenyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // Find the pending request for this player as target
        TPACommand.TPARequest request = findRequestForTarget(player);

        if (request == null) {
            player.sendMessage(ChatColor.RED + "You have no pending teleport requests!");
            return true;
        }

        Player requester = request.getRequester();

        // Notify the requester
        requester.sendMessage(ChatColor.RED + player.getName() + " denied your teleport request.");

        // Notify the target
        player.sendMessage(ChatColor.RED + "You denied " + requester.getName() + "'s teleport request.");

        // Remove the request
        TPACommand.removeRequest(requester.getUniqueId());

        return true;
    }

    private TPACommand.TPARequest findRequestForTarget(Player target) {
        // Similar to TPAcceptCommand, this needs proper request tracking
        // In production, maintain a Map<UUID, List<TPARequest>>
        return null;
    }
}
