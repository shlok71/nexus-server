package com.nexus.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to reply to the last private message received
 */
public class ReplyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /reply <message>");
            return true;
        }

        UUID lastMessaged = MessageCommand.getLastMessaged(player.getUniqueId());
        if (lastMessaged == null) {
            player.sendMessage(ChatColor.RED + "You have no one to reply to!");
            return true;
        }

        Player target = Bukkit.getPlayer(lastMessaged);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "That player is no longer online!");
            MessageCommand.clearLastMessage(player.getUniqueId());
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            if (message.length() > 0) message.append(" ");
            message.append(arg);
        }

        String formattedMsg = ChatColor.translateAlternateColorCodes('&', message.toString());

        // Send to target
        target.sendMessage(ChatColor.LIGHT_PURPLE + "[From " + getPlayerPrefix(player) + player.getName() + ChatColor.LIGHT_PURPLE + "] " + ChatColor.WHITE + formattedMsg);

        // Send confirmation
        player.sendMessage(ChatColor.LIGHT_PURPLE + "[To " + getPlayerPrefix(target) + target.getName() + ChatColor.LIGHT_PURPLE + "] " + ChatColor.WHITE + formattedMsg);

        return true;
    }

    private String getPlayerPrefix(Player player) {
        if (player.isOp()) {
            return ChatColor.RED + "";
        } else if (player.hasPermission("nexus.vip")) {
            return ChatColor.GOLD + "";
        }
        return ChatColor.WHITE + "";
    }
}
