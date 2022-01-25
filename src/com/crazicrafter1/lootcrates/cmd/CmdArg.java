package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.crutils.Util;
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
     * Add all command args to this map,
     * IndexOutOfBoundsException safe,
     * TODO
     *  use a contracted command arg requirement with a map or something to streamline
     *  arg passing
     */
    static Map<String, CmdArg> args = new HashMap<>();

    static {
        args.put("save", new CmdArg((sender, args) -> {
            plugin.saveConfig();
            return feedback(sender, "Saved config to disk");
        }, null));

        args.put("crate", new CmdArg((sender, args) -> {
            Crate crate = LootCratesAPI.getCrateByID(args[0]);

            if (crate == null)
                return error(sender, "That crate doesn't exist");

            if (args.length == 1) {
                if (!(sender instanceof Player))
                    return error(sender, "You must be a player to give yourself a crate");
                Util.giveItemToPlayer((Player) sender, crate.itemStack((Player) sender));
                return feedback(sender, "Gave yourself 1 " + crate.id + " crate");
            }

            if (args[1].equals("*")) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.getInventory().addItem(crate.itemStack(p));
                    // Redundant spam
                    //if (p != sender)
                    //    p.sendMessage("You received 1 " + crate.id + " crate");
                });
                return feedback(sender, "Gave a " + crate.id + " crate to all players (" + ChatColor.LIGHT_PURPLE + Bukkit.getOnlinePlayers().size() + ChatColor.GRAY + " online)");
            }

            Player p = Bukkit.getServer().getPlayer(args[2]);
            if (p == null)
                return error(sender, "That player cannot be found");

            Util.giveItemToPlayer(p, crate.itemStack(p));

            // Redundant spam
            //if (p != sender)
            //    p.sendMessage("You received 1 " + crate.id + " crate");

            return feedback(sender, "Gave a " + crate.id + " crate to " + ChatColor.GOLD + p.getName());

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
            return feedback(sender, "Loaded default config");
        }, null));

        args.put("reload", new CmdArg((sender, args) -> {
            Main.get().reloadConfig();
            return feedback(sender, "Loaded config from disk");
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

        args.put("version", new CmdArg((sender, args) -> feedback(sender, "LootCrates version: " + plugin.getDescription().getVersion()), null));

        args.put("detect", new CmdArg((sender, args) -> {
            if (!(sender instanceof Player))
                return error(sender, "Only a player can execute this argument");

            Player p = (Player) sender;

            ItemStack itemStack = p.getInventory().getItemInMainHand();
            if (itemStack.getType() != Material.AIR) {
                Crate crate = LootCratesAPI.extractCrateFromItem(itemStack);
                if (crate != null) {
                    return feedback(sender, "Item is a crate (" + crate.id + ")");
                } else {
                    return feedback(sender, "Item is a not a crate");
                }
            }
            return error(sender, "Must hold an item to detect");
        }, null));
    }

    static boolean error(CommandSender sender, String message) {
        sender.sendMessage(plugin.prefix() + ChatColor.RED + message);
        return true;
    }

    static boolean feedback(CommandSender sender, String message) {
        sender.sendMessage(plugin.prefix() + ChatColor.AQUA + message);
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
