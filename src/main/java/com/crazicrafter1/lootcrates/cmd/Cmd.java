package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.crutils.Pair;
import com.crazicrafter1.crutils.TriFunction;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.LCMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.crazicrafter1.lootcrates.cmd.CmdArg.info;
import static com.crazicrafter1.lootcrates.cmd.CmdArg.severe;

public class Cmd implements CommandExecutor, TabCompleter {

    private LCMain plugin;

    public Cmd(LCMain plugin) {
        this.plugin = plugin;
        plugin.getCommand("crates").setExecutor(this);
        plugin.getCommand("crates").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {
        if (args.length == 0) {
            if (LCMain.get().rev == -1) {
                return plugin.notifier.commandSevere(sender, String.format(Lang.ASSIGN_REV, ChatColor.UNDERLINE + "/crates rev"));
            }
            info(sender, String.format(Lang.VERSION, LCMain.get().getDescription().getVersion()));
            info(sender, String.format(Lang.REV, LCMain.get().rev));
            return info(sender, Lang.USAGE + "/crates ["
                    + String.join(", ", CmdArg.args.keySet())
                    + "]");
        }

        if (LCMain.get().rev == -1
                && !args[0].equalsIgnoreCase("rev")) {
            return severe(sender, String.format(Lang.ASSIGN_REV, ChatColor.UNDERLINE + "/crates rev"));
        }

        final Pair<TriFunction<CommandSender, String[], Set<String>, Boolean>, BiFunction<CommandSender, String[], List<String>>> pair
                = CmdArg.args.get(args[0].toLowerCase());

        if (pair == null)
            return severe(sender, Lang.ERR_ARG_UNKNOWN);

        try {
            //String[] smartArgs = smartParse(Arrays.copyOfRange(args, 1, args.length)).toArray(new String[0]);
            String[] smartArgs = Arrays.copyOfRange(args, 1, args.length);
            pair.first.apply(sender,
                    smartArgs,
                    Arrays.stream(smartArgs).filter(arg -> arg.length() >= 2 && arg.startsWith("-")).map(arg -> arg.substring(1)).collect(Collectors.toSet()));
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            // Just ensure index with an error print
            return severe(sender, String.format(Lang.ERR_ARG_MORE, e.getMessage()));
        }
    }

    /**
     * Intelligently parse the args to include spaces only when quotes follow
     *  todo could be extended, but sort of feature creep
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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command c, @NotNull String s, String[] args) {
        if (args.length == 0)
            return new ArrayList<>();

        if (args.length == 1) {
            if (LCMain.get().rev == -1) {
                return Collections.singletonList("rev"); // todo remove post-migrate
            } else
                return CmdArg.getMatches(args[0], CmdArg.args.keySet());
        }

        final Pair<TriFunction<CommandSender, String[], Set<String>, Boolean>, BiFunction<CommandSender, String[], List<String>>> pair
                = CmdArg.args.get(args[0]);

        if (pair == null || pair.second == null)
            return new ArrayList<>();

        //String[] smartArgs = smartParse(Arrays.copyOfRange(args, 1, args.length)).toArray(new String[0]);
        String[] smartArgs = Arrays.copyOfRange(args, 1, args.length);
        return pair.second.apply(sender, smartArgs);
    }
}
