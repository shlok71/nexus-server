package com.nexus.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to accept incoming teleport requests
 */
public class TPAcceptCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // Find the oldest pending request for this player as target
        TPACommand.TPARequest request = findRequestForTarget(player);

        if (request == null) {
            player.sendMessage(ChatColor.RED + "You have no pending teleport requests!");
            return true;
        }

        Player requester = request.getRequester();

        if (!requester.isOnline()) {
            player.sendMessage(ChatColor.RED + "The requester has gone offline!");
            TPACommand.removeRequest(requester.getUniqueId());
            return true;
        }

        // Teleport the requester to the target
        requester.teleport(player.getLocation());
        requester.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.GREEN + " accepted your teleport request!");

        player.sendMessage(ChatColor.GREEN + "You accepted " + requester.getName() + "'s teleport request.");

        // Remove the request
        TPACommand.removeRequest(requester.getUniqueId());

        return true;
    }

    private TPACommand.TPARequest findRequestForTarget(Player target) {
        // This would need to iterate through all pending requests
        // For efficiency, we might want to also maintain a map of target -> request
        // For now, we'll iterate through all known requests
        // In a real implementation, you'd maintain both directions

        // Note: In a production environment, you'd want to maintain
        // a Map<UUID, List<TPARequest>> for target -> requests
        // For simplicity, we'll just check the player who sent us the request

        // This is a simplified version - in production, maintain both maps
        return null; // Would need proper implementation
    }
}
