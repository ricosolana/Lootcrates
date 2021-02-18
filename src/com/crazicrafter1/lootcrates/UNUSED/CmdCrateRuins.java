package com.crazicrafter1.lootcrates.UNUSED;

import com.crazicrafter1.lootcrates.Crate;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdCrateRuins implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) return false;

        Player p = (Player)commandSender;

        Crate.spawnCrateRuins(p.getLocation());

        return false;
    }
}
