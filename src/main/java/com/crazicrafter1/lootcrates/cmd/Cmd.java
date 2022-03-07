package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        if (args.length == 0) {
            if (Main.get().rev == -1) {
                return Main.get().error(sender, String.format(Lang.ASSIGN_REV, ChatColor.UNDERLINE + "/crates rev"));
            }
            Main.get().popup(sender, String.format(Lang.VERSION, Main.get().getDescription().getVersion()));
            Main.get().popup(sender, String.format(Lang.REV, Main.get().rev));
            return Main.get().popup(sender, Lang.USAGE + "/crates ["
                    + String.join(", ", CmdArg.args.keySet())
                    + "]");
        }

        if (Main.get().rev == -1
                && !args[0].equalsIgnoreCase("rev")) {
            return error(sender, String.format(Lang.ASSIGN_REV, ChatColor.UNDERLINE + "/crates rev"));
        }

        CmdArg cmdArg = CmdArg.args.get(args[0].toLowerCase());

        if (cmdArg == null)
            return error(sender, Lang.ERR_ARG_UNKNOWN);

        try {
            //String[] smartArgs = smartParse(Arrays.copyOfRange(args, 1, args.length)).toArray(new String[0]);
            String[] smartArgs = Arrays.copyOfRange(args, 1, args.length);
            cmdArg.exe.apply(sender,
                    smartArgs,
                    Arrays.stream(smartArgs).filter(arg -> arg.length() >= 2 && arg.startsWith("-")).map(arg -> arg.substring(1)).collect(Collectors.toSet()));
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            // Just ensure index with an error print
            return error(sender, String.format(Lang.ERR_ARG_MORE, e.getMessage()));
        }
    }

    /**
     * Intelligently parse the args to include spaces only when quotes follow
     *
     */
    @Deprecated
    static ArrayList<String> smartParse(String[] args) {
        String combined = String.join(" ", args);

        ArrayList<String> newArgs = new ArrayList<>();

        int delimStart = -1;
        int start = 0;
        char prev = '\0';
        for (int i=0; i < combined.length(); i++) {
            char c = combined.charAt(i);
            // if theres a separator and no prior delimiter
            if (delimStart == -1) {
                if (i == combined.length()-1) {
                    newArgs.add(combined.substring(start, i + 1).replace("\\\"", "\""));
                } else if (Character.isWhitespace(c)) {
                    newArgs.add(combined.substring(start, i).replace("\\\"", "\""));
                    start = i + 1;
                } else if (c == '"' && prev != '\\') {
                    delimStart = i + 1;
                }
            } else {
                if (c == '"' && prev != '\\') {
                    newArgs.add(combined.substring(delimStart, i).replace("\\\"", "\""));
                    i++;
                    delimStart = -1;
                    start = i + 1;
                }
            }
            prev = c;
        }

        return newArgs;
    }

    // An efficient tab complete algorithm would involve a tree map
    // only worth it for many matches
    @Override
    public List<String> onTabComplete(CommandSender sender, Command c, String s, String[] args) {
        if (args.length == 0)
            return new ArrayList<>();

        if (args.length == 1) {
            if (Main.get().rev == -1) {
                return Collections.singletonList("rev");
            } else
                return CmdArg.getMatches(args[0], CmdArg.args.keySet());
        }

        CmdArg arg = CmdArg.args.get(args[0]);

        if (arg == null || arg.tab == null)
            return new ArrayList<>();

        //String[] smartArgs = smartParse(Arrays.copyOfRange(args, 1, args.length)).toArray(new String[0]);
        String[] smartArgs = Arrays.copyOfRange(args, 1, args.length);
        return arg.tab.apply(sender, smartArgs);
    }
}
