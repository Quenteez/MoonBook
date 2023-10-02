package ru.quenteez.moonbook.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.quenteez.moonbook.utils.MCUtils;

public class NewsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player player) {
            MCUtils.book(player, false);
        } else {
            commandSender.sendMessage(ChatColor.RED + "This command only for players!");
        }

        return true;
    }
}
