package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.crazicrafter1.lootcrates.cmd.CmdArg.error;

public class Cmd implements CommandExecutor, TabCompleter {

    public Cmd(Main plugin) {
        plugin.getCommand("crates").setExecutor(this);
        plugin.getCommand("crates").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {
        if (args.length == 0)
            return error(sender, "Usage: /crates ["
                    + String.join(", ", CmdArg.args.keySet())
            + "]");

        CmdArg cmdArg = CmdArg.args.get(args[0]);

        if (cmdArg == null)
            return error(sender, "Unknown argument");

        try {
            cmdArg.exe.apply(sender,
                    Arrays.copyOfRange(args, 1, args.length),
                    Arrays.stream(args).filter(arg -> arg.length() >= 2 && arg.startsWith("-")).map(arg -> arg.substring(1)).collect(Collectors.toSet()));
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            // Just ensure index with an error print
            return error(sender, "Input more arguments: " + e.getMessage());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command c, String s, String[] args) {
        if (args.length == 0)
            return new ArrayList<>();

        if (args.length == 1) {
            return CmdArg.getMatches(args[0], CmdArg.args.keySet());
        }

        CmdArg arg = CmdArg.args.get(args[0]);

        if (arg == null || arg.tab == null)
            return new ArrayList<>();

        return arg.tab.apply(sender, Arrays.copyOfRange(args, 1, args.length));
    }
}
