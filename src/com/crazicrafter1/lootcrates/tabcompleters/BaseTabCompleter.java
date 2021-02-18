package com.crazicrafter1.lootcrates.tabcompleters;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseTabCompleter implements TabCompleter {

    protected static Main plugin; //Main.getInstance();

    BaseTabCompleter(Main plugin, String name) {
        BaseTabCompleter.plugin = plugin;
        plugin.getCommand(name).setTabCompleter(this);
    }

    @Override
    public abstract List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args);

    final ArrayList<String> getMatches(String arg, String[] samples){

        ArrayList<String> matches = new ArrayList<>();

        for (String s : samples) {
            if (s.startsWith(arg.toLowerCase())) matches.add(s);
        }

        return matches;
    }

}
