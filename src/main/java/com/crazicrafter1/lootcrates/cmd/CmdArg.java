package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.crutils.TriFunction;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.crazicrafter1.lootcrates.Lang.L;

class CmdArg {

    private static final Main plugin = Main.get();

    final TriFunction<CommandSender, String[], Set<String>, Boolean> exe;
    final BiFunction<CommandSender, String[], List<String>> tab;

    CmdArg(TriFunction<CommandSender, String[], Set<String>, Boolean> exe,
           BiFunction<CommandSender, String[], List<String>> tab) {
        this.exe = exe;
        this.tab = tab;
    }

    /**
     * Command executor and tab-completer map
     */
    static Map<String, CmdArg> args = new HashMap<>();

    static {
        //args.put("locale", new CmdArg((sender, args) ->
        //        feedback(sender,"Locale: " + ((Player)sender).getLocale()), null));

        args.put("lang", new CmdArg((sender, args, flags) -> {
            if (args.length == 0) {
                return info(sender, L("Currently ") + Main.get().lang.translations.size() + L(" languages are loaded)") + "\n" + Main.get().lang.translations.keySet());
            }

            boolean success;
            if ((args.length == 1 || args[1].equalsIgnoreCase("confirm"))
                && (args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("load"))) {

                if (args.length == 1)
                    return warn(sender, L("Are you sure you want to save/load all language files? If so, append") + " 'CONFIRM'" + L(" to the command"));

                switch (args[0].toLowerCase()) {
                    case "save": {
                        success = Main.get().lang.saveLanguageFiles();
                        break;
                    }
                    case "load": {
                        success = Main.get().lang.loadLanguageFiles();
                        break;
                    }
                    default:
                        return error(sender, L("Unknown argument"));
                }
            } else {
                String lang = args[1];
                switch (args[0].toLowerCase()) {
                    case "save": {
                        success = Main.get().lang.saveLanguageFile(lang);
                        break;
                    }
                    case "load": {
                        success = Main.get().lang.loadLanguageFile(lang);
                        break;
                    }
                    case "translate": {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                final long start = System.currentTimeMillis();
                                Lang.Unit unit = Main.get().lang.createLanguageFile(lang);
                                final long end = System.currentTimeMillis();



                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (unit != null) {
                                            info(sender, "Successfully created language");
                                            info(sender, String.format(L("Operation took") + " %.02fs", (float) (end - start) / 1000.f));
                                            Main.get().lang.translations.put(lang, unit);
                                        }
                                        else
                                            error(sender, L("Operation failed (see console)"));
                                    }
                                }.runTaskLater(Main.get(), 0);



                            }
                        }.runTaskAsynchronously(Main.get());

                        return info(sender, L("Asynchronously creating language ") + lang);
                    }
                    default:
                        return error(sender, L("Unknown argument"));
                }
            }

            return success ?
                    info(sender, L("Operation success")) :
                    error(sender, L("Operation failed (see console)"));
        }, (sender, args) -> {
            if (args.length == 1) {
                return getMatches(args[0], Arrays.asList("save", "load", "translate"));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("load"))
                return getMatches(args[1], Main.get().lang.translations.keySet());
            return new ArrayList<>();
        }));

        args.put("populate", new CmdArg((sender, args, flags) -> {
            plugin.saveConfig();
            plugin.data = new Data();
            return info(sender, L("Populating config with built-ins"));
        }, null));

        args.put("save", new CmdArg((sender, args, flags) -> {
            plugin.saveConfig();
            return info(sender, L("Saved config to disk"));
        }, null));

        args.put("crate", new CmdArg((sender, args, flags) -> {
            Crate crate = LootCratesAPI.getCrateByID(args[0]);

            if (crate == null)
                return error(sender, L("That crate doesn't exist"));

            if (args.length == 1) {
                if (!(sender instanceof Player))
                    return error(sender, L("You must be a player to give yourself a crate"));
                Util.giveItemToPlayer((Player) sender, crate.itemStack((Player) sender));
                return info(sender, L("Gave yourself 1 ") + crate.id + L(" crate"));
            }

            if (args[1].equals("*")) {
                int given = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Util.giveItemToPlayer(p, crate.itemStack(p));
                    if (p != sender && !(flags.contains("s") || flags.contains("silent"))) info(p, L("You received 1 ") + crate.id + L(" crate"));
                    given++;
                }

                if (given == 0)
                    return info(sender, L("No players online"));

                return info(sender, L("Gave a ") + crate.id + L(" crate to all players (") + ChatColor.LIGHT_PURPLE + Bukkit.getOnlinePlayers().size() + ChatColor.GRAY + L(" online)"));
            }

            Player p = Bukkit.getServer().getPlayer(args[1]);
            if (p == null)
                return error(sender, L("That player cannot be found"));

            Util.giveItemToPlayer(p, crate.itemStack(p));

            // Redundant spam
            if (p != sender) {
                if (!(flags.contains("s") || flags.contains("silent")))
                    info(p, L("You received 1 ") + crate.id + L(" crate"));
                return info(sender, L("Gave a ") + crate.id + L(" crate to ") + ChatColor.GOLD + p.getName());
            }

            return info(sender, L("Gave yourself 1 ") + crate.id + " crate");
        }, (sender, args) -> {
            if (args.length == 1) {
                return getMatches(args[0], Main.get().data.crates.keySet());
            }
            if (args.length == 2) {
                return getMatches(args[1],
                        Stream.concat(
                            Stream.concat(
                                            Bukkit.getServer().getOnlinePlayers().stream().filter(p -> p != sender).map(Player::getName),
                                            (Bukkit.getServer().getOnlinePlayers().size() > 1) ? // Removing redundant identifiers for discrete
                                                    Stream.of("*") : Stream.empty()),
                                sender instanceof Player ? Stream.of("-s", "-silent") : Stream.empty()
                                )
                                .collect(Collectors.toList()));
            }
            if (args.length == 3) {
                return getMatches(args[2], Stream.of("-s", "-silent")
                                .collect(Collectors.toList()));
            }
            return new ArrayList<>();
        }));

        args.put("reset", new CmdArg((sender, args, flags) -> {
            Main.get().saveDefaultConfig(true);
            Main.get().reloadConfig();
            return info(sender, L("Loaded default config"));
        }, null));

        args.put("reload", new CmdArg((sender, args, flags) -> {
            Main.get().reloadConfig();
            return info(sender, L("Loaded config from disk"));
        }, null));

        args.put("editor", new CmdArg((sender, args, flags) -> {
            if (sender instanceof Player) {
                // title, subtitle, fadein, stay, fadeout
                Player p = (Player) sender;
                PlayerStat stat = plugin.getStat(p.getUniqueId());
                if (!stat.editorMessaged) {

                    p.sendTitle(ChatColor.RED + L("Warning"),
                            ChatColor.YELLOW + L("Editor might be buggy"),
                            5, 20 * 2, 5);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.resetTitle();
                            p.sendTitle(" ",
                                    ChatColor.YELLOW + L("Submit bugs/requests to my Github"),
                                    5, 20 * 2, 5);

                            p.sendMessage("" + ChatColor.DARK_GRAY + ChatColor.UNDERLINE + "https://github.com/PeriodicSeizures/LootCrates");

                            new BukkitRunnable() {
                                @Override
                                public void run() {


                                    p.resetTitle();
                                    p.sendTitle(" ",
                                            ChatColor.GOLD + L("Constructive feedback is appreciated!"),
                                            5, 20 * 2, 5);

                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            //new MainMenu().show(p);
                                            new Editor(p).open();
                                        }
                                    }.runTaskLater(plugin, 20 * 2 + 10);


                                }
                            }.runTaskLater(plugin, 20 * 2 + 10);


                        }
                    }.runTaskLater(plugin, 20 * 2 + 10);

                } else {
                    new Editor(p).open();
                }

                stat.editorMessaged = true;

                return true;
            }

            return error(sender, L("Can only be executed by a player"));
        }, null));

        args.put("version", new CmdArg((sender, args, flags) -> info(sender, L("LootCrates version: ") + plugin.getDescription().getVersion()), null));

        args.put("detect", new CmdArg((sender, args, flags) -> {
            if (!(sender instanceof Player))
                return error(sender, L("Only a player can execute this argument"));

            Player p = (Player) sender;

            ItemStack itemStack = p.getInventory().getItemInMainHand();
            if (itemStack.getType() != Material.AIR) {
                Crate crate = LootCratesAPI.extractCrateFromItem(itemStack);
                if (crate != null) {
                    return info(sender, L("Item is a crate (" + crate.id + ")"));
                } else {
                    return info(sender, L("Item is a not a crate"));
                }
            }
            return error(sender, L("Must hold an item to detect"));
        }, null));
    }

    static boolean info(CommandSender sender, String message) {
        return Main.get().info(sender, message);
    }

    static boolean warn(CommandSender sender, String message) {
        return Main.get().warn(sender, message);
    }

    static boolean error(CommandSender sender, String message) {
        return Main.get().error(sender, message);
    }

    static List<String> getMatches(String arg, Collection<String> samples) {
        List<String> matches = new ArrayList<>();

        // limit returned result set to 8 entries
        int c = 0;
        for (String s : samples) {
            if (c > 8)
                break;

            if (s.toLowerCase().replace(" ", "").startsWith(arg.toLowerCase())) {
                matches.add(s);
                c++;
            }
        }

        return matches;
    }
}
