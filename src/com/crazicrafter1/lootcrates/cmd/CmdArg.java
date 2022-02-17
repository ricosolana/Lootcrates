package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.Editor;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
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

class CmdArg {

    private static final Main plugin = Main.get();

    final BiFunction<CommandSender, String[], Boolean> exe;
    final BiFunction<CommandSender, String[], List<String>> tab;

    CmdArg(BiFunction<CommandSender, String[], Boolean> exe,
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

        args.put("lang", new CmdArg((sender, args) -> {
            if (args.length == 0) {
                return info(sender, "Currently " + Main.get().data.translations.size() + " languages are loaded\n" + Main.get().data.translations.keySet());
            }

            warn(sender, "The server will freeze momentarily");
            warn(sender, "If a timeout crash occurs, increase the server respond timeout");
            warn(sender, "The more stuff you have created, the longer this will take");

            final long start = System.currentTimeMillis();
            boolean success;
            if ((args.length == 1 || args[1].equalsIgnoreCase("confirm"))
                && (args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("load"))) {

                if (args.length == 1)
                    return warn(sender, "Are you sure you want to save/load all language files? If so, append 'confirm' to the command");

                switch (args[0].toLowerCase()) {
                    case "save": {
                        success = Main.get().data.saveLanguageFiles();
                        break;
                    }
                    case "load": {
                        success = Main.get().data.loadLanguageFiles();
                        break;
                    }
                    default:
                        return error(sender, "Unknown argument");
                }
            } else {
                String lang = args[1];
                switch (args[0].toLowerCase()) {
                    case "save": {
                        success = Main.get().data.saveLanguageFile(lang);
                        break;
                    }
                    case "load": {
                        success = Main.get().data.loadLanguageFile(lang);
                        break;
                    }
                    case "translate": {
                        info(sender, "Translating language unit to " + lang);
                        success = Main.get().data.createLanguageFile(lang);
                        break;
                    }
                    default:
                        return error(sender, "Unknown argument");
                }
            }

            final long end = System.currentTimeMillis();

            return !success ?
                    error(sender, "Operation failed (see console)") :
                    info(sender, String.format("Operation took %.02fs", (float)(end-start)/1000.f));
        }, (sender, args) -> {
            if (args.length == 1) {
                return getMatches(args[0], Arrays.asList("save", "load", "translate"));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("load"))
                return getMatches(args[1], Main.get().data.translations.keySet());
            return new ArrayList<>();
        }));

        args.put("populate", new CmdArg((sender, args) -> {
            plugin.saveConfig();
            plugin.data = new Data();
            return info(sender, "Populating config with built-ins");
        }, null));

        args.put("save", new CmdArg((sender, args) -> {
            plugin.saveConfig();
            return info(sender, "Saved config to disk");
        }, null));

        args.put("crate", new CmdArg((sender, args) -> {
            Crate crate = LootCratesAPI.getCrateByID(args[0]);

            if (crate == null)
                return error(sender, "That crate doesn't exist");

            if (args.length == 1) {
                if (!(sender instanceof Player))
                    return error(sender, "You must be a player to give yourself a crate");
                Util.giveItemToPlayer((Player) sender, crate.itemStack((Player) sender));
                return info(sender, "Gave yourself 1 " + crate.id + " crate");
            }

            if (args[1].equals("*")) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.getInventory().addItem(crate.itemStack(p));
                    // Redundant spam
                    //if (p != sender)
                    //    p.sendMessage("You received 1 " + crate.id + " crate");
                });
                return info(sender, "Gave a " + crate.id + " crate to all players (" + ChatColor.LIGHT_PURPLE + Bukkit.getOnlinePlayers().size() + ChatColor.GRAY + " online)");
            }

            Player p = Bukkit.getServer().getPlayer(args[1]);
            if (p == null)
                return error(sender, "That player cannot be found");

            Util.giveItemToPlayer(p, crate.itemStack(p));

            // Redundant spam
            //if (p != sender)
            //    p.sendMessage("You received 1 " + crate.id + " crate");

            return info(sender, "Gave a " + crate.id + " crate to " + ChatColor.GOLD + p.getName());

        }, (sender, args) -> {
            if (args.length == 1) {
                return getMatches(args[0], Main.get().data.crates.keySet());
            }
            if (args.length == 2) {
                return getMatches(args[1],
                        Stream.concat(
                                        Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName),
                                        Stream.of("*"))
                                .collect(Collectors.toList()));
            }
            return new ArrayList<>();
        }));

        args.put("reset", new CmdArg((sender, args) -> {
            Main.get().saveDefaultConfig(true);
            Main.get().reloadConfig();
            return info(sender, "Loaded default config");
        }, null));

        args.put("reload", new CmdArg((sender, args) -> {
            Main.get().reloadConfig();
            return info(sender, "Loaded config from disk");
        }, null));

        args.put("editor", new CmdArg((sender, args) -> {
            if (sender instanceof Player) {
                // title, subtitle, fadein, stay, fadeout
                Player p = (Player) sender;
                if (!plugin.data.alertedPlayers.contains(p.getUniqueId())) {

                    p.sendTitle(ChatColor.RED + "Warning",
                            ChatColor.YELLOW + "Editor might be buggy",
                            5, 20 * 2, 5);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.resetTitle();
                            p.sendTitle(" ",
                                    ChatColor.YELLOW + "Submit bugs/requests to my Github",
                                    5, 20 * 2, 5);

                            p.sendMessage("" + ChatColor.DARK_GRAY + ChatColor.UNDERLINE + "https://github.com/PeriodicSeizures/LootCrates");

                            new BukkitRunnable() {
                                @Override
                                public void run() {


                                    p.resetTitle();
                                    p.sendTitle(" ",
                                            ChatColor.GOLD + "Constructive feedback is appreciated!",
                                            5, 20 * 2, 5);

                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            //new MainMenu().show(p);
                                            Editor.open(p);
                                        }
                                    }.runTaskLater(plugin, 20 * 2 + 10);


                                }
                            }.runTaskLater(plugin, 20 * 2 + 10);


                        }
                    }.runTaskLater(plugin, 20 * 2 + 10);

                } else {
                    Editor.open(p);
                }

                plugin.data.alertedPlayers.add(p.getUniqueId());

                return true;
            }

            return error(sender, "Can only be executed by a player");
        }, null));

        args.put("version", new CmdArg((sender, args) -> info(sender, "LootCrates version: " + plugin.getDescription().getVersion()), null));

        args.put("detect", new CmdArg((sender, args) -> {
            if (!(sender instanceof Player))
                return error(sender, "Only a player can execute this argument");

            Player p = (Player) sender;

            ItemStack itemStack = p.getInventory().getItemInMainHand();
            if (itemStack.getType() != Material.AIR) {
                Crate crate = LootCratesAPI.extractCrateFromItem(itemStack);
                if (crate != null) {
                    return info(sender, "Item is a crate (" + crate.id + ")");
                } else {
                    return info(sender, "Item is a not a crate");
                }
            }
            return error(sender, "Must hold an item to detect");
        }, null));
    }

    static boolean error(CommandSender sender, String message) {
        sender.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "\u26A0 " + ChatColor.RESET + ChatColor.RED + message);
        return true;
    }

    static boolean info(CommandSender sender, String message) {
        sender.sendMessage("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "\u24D8 " + ChatColor.RESET + ChatColor.GRAY + message);
        return true;
    }

    static boolean warn(CommandSender sender, String message) {
        sender.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "\u26A1 " + ChatColor.RESET + ChatColor.YELLOW + message);
        return true;
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
