package com.crazicrafter1.lootcrates.tabs;

import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabCrates extends BaseTabCompleter {

    public TabCrates() {
        super("lootcrates");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        List<String> list = new ArrayList<>();
        if (args.length > 0) {
            if (args.length == 1) {
                return getMatches(args[0], new String[] {"crate", "detect", "editor", "flair", "reload", "version"});
            }
            if (args[0].equals("crate")) {
                if (args.length == 2) {
                    return BaseTabCompleter.getMatches(args[1], Data.crates.keySet().toArray(new String[0]));
                }
                if (args.length == 3) {
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        if (p.getName().startsWith(args[2])) list.add(p.getName());
                    }
                    list.add("*");
                    return list;
                }
            }
            return list;
        }

        return list;
    }

}
