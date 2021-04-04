package com.crazicrafter1.lootcrates.commands;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class CmdBase implements CommandExecutor {

    protected static Main plugin = Main.getInstance();
    protected static List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();

    public CmdBase(String name) {
        plugin.getCommand(name).setExecutor(this);
    }

    boolean error(CommandSender sender, String message) {
        sender.sendMessage(plugin.prefix + " " + ChatColor.RED + message);
        return true;
    }

    boolean feedback(CommandSender sender, String message) {
        sender.sendMessage(plugin.prefix + " " + ChatColor.AQUA + message);
        return true;
    }

}
