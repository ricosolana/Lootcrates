package com.crazicrafter1.lootcrates.tabcompleters;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabCrates extends BaseTabCompleter {

    public TabCrates(Main plugin) {
        super(plugin, "lootcrates");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        List<String> list = new ArrayList<>();
        if (args.length > 0) {
            // /crates loot rare crazicrafter1
            // /crates crate rare crazicrafter1 54
            if (args.length == 1) {
                /*for (String arg : args)
                {
                    if (arg.contains("crate"))
                    {

                    }
                }
                 */
                return getMatches(args[0], new String[] {"crate", "reload", "editor"});
            }
            if (args[0].equals("crate")) {
                if (args.length == 2) {
                    // set string -> string[]

                    return getMatches(args[1], Main.crates.keySet().toArray(new String[0]));
                }
                if (args.length == 3) {
                    // add player name
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        if (p.getName().startsWith(args[2])) list.add(p.getName());
                        //if (p.getDisplayName().substring(0, args[1].length()+1).equals(args[1]))
                        //    list.add(p.getDisplayName());
                    }
                    return list;
                }
            }
            // /crates open legendary crazi

            /*
            if (args[0].equals("open")) {
                if (args.length == 2) {
                    return getMatches(args[1], (String[]) plugin.config.crateNameIds.keySet().toArray());
                }
                if (args.length == 3) {
                    // add player name
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        if (p.getName().startsWith(args[2])) list.add(p.getName());
                        //if (p.getDisplayName().substring(0, args[1].length()+1).equals(args[1]))
                        //    list.add(p.getDisplayName());
                    }
                    return list;
                }
            }
            if (args[0].equals("close")) {
                if (args.length == 3) {
                    // add player name
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        if (p.getName().startsWith(args[2])) list.add(p.getName());
                        //if (p.getDisplayName().substring(0, args[1].length()+1).equals(args[1]))
                        //    list.add(p.getDisplayName());
                    }
                    return list;
                }
            }*/
            return list;


        }

        return list;
    }

}
