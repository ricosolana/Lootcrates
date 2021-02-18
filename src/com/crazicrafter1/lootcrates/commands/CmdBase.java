package com.crazicrafter1.lootcrates.commands;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class CmdBase implements CommandExecutor {

    protected static Main plugin;

    public CmdBase(Main plugin, String name) {
        plugin.getCommand(name).setExecutor(this);
        CmdBase.plugin = plugin;
    }

    boolean error(CommandSender sender, String message) {
        sender.sendMessage(plugin.prefix + ChatColor.RED + message);
        return true;
    }

    boolean feedback(CommandSender sender, String message) {
        sender.sendMessage(plugin.prefix + ChatColor.AQUA + message);
        return true;
    }

}
