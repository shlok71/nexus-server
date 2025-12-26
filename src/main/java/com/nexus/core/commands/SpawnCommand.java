package com.nexus.core.commands;

import com.nexus.core.NexusCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to teleport to spawn point
 */
public class SpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("nexus.spawn")) {
            player.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }

        Location spawn = NexusCore.getInstance().getHubManager().getSpawnLocation();

        if (spawn == null) {
            player.sendMessage(ChatColor.RED + "Spawn location not set!");
            return true;
        }

        // Teleport with some effects
        player.teleport(spawn);
        player.sendMessage(ChatColor.AQUA + "Teleported to spawn!");

        // Play sound effect if available
        NexusCore.getInstance().getHubManager().playSpawnEffect(player);

        return true;
    }
}
