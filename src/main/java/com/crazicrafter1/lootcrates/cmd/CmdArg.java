package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.crutils.ColorUtil;
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

            String lang = args[0];
            Lang.load(lang);

            return info(sender, "Loading language");
        }, null));

        args.put("populate", new CmdArg((sender, args, flags) -> {
            plugin.saveConfig();
            plugin.data = new Data();
            return info(sender, Lang.POPULATING);
        }, null));

        args.put("gradient", new CmdArg((sender, args, flags) -> {
            if (args.length == 0)
                return error(sender, "Usage: crates gradient \"<#883388>Inertia is a property of matter</#1144FF>\"");

            return info(sender, ColorUtil.renderAll(args[0]));
        }, null));

        args.put("rev", new CmdArg((sender, args, flags) -> {
            if (args[0].equalsIgnoreCase("latest")) {
                Main.get().rev = Main.REV_LATEST;
                Main.get().reloadConfig();
                return info(sender, "Read config using latest revision (" + Main.REV_LATEST + ")");
            } else {
                try {
                    int rev = Integer.parseInt(args[0]);
                    if (rev > Main.REV_LATEST) {
                        // err
                        return error(sender, "Revision " + rev + " is not yet implemented");
                    } if (rev < 0)
                        throw new RuntimeException();

                    Main.get().rev = rev;
                    Main.get().reloadConfig();
                    Main.get().rev = Main.REV_LATEST;

                    CmdArg.args.remove("rev");

                    return info(sender, "Read config using revision " + rev);
                } catch (Exception e) {
                    return error(sender, "Revision must be a integer (x>=0) or 'latest'");
                }
            }
        }, null));

        args.put("save", new CmdArg((sender, args, flags) -> {
            plugin.saveConfig();
            return info(sender, Lang.CONFIG_SAVED);
        }, null));

        args.put("crate", new CmdArg((sender, args, flags) -> {
            Crate crate = LootCratesAPI.getCrateByID(args[0]);

            if (crate == null)
                return error(sender, Lang.ERR_CRATE_UNKNOWN);

            if (args.length == 1) {
                if (!(sender instanceof Player))
                    return error(sender, Lang.ERR_PLAYER_CRATE);
                Util.giveItemToPlayer((Player) sender, crate.itemStack((Player) sender, true));
                return info(sender, String.format(Lang.SELF_GIVE_CRATE, crate.id));
            }

            if (args[1].equals("*")) {
                int given = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Util.giveItemToPlayer(p, crate.itemStack(p, true));
                    if (p != sender && !(flags.contains("s") || flags.contains("silent")))
                        info(p, String.format(Lang.RECEIVE_CRATE, crate.id));
                    given++;
                }

                if (given == 0)
                    return error(sender, Lang.ERR_NONE_ONLINE);

                return info(sender, String.format(Lang.GIVE_CRATE_ALL, crate.id, Bukkit.getOnlinePlayers().size()));
            }

            Player p = Bukkit.getServer().getPlayer(args[1]);
            if (p == null)
                return error(sender, Lang.ERR_PLAYER_UNKNOWN);

            Util.giveItemToPlayer(p, crate.itemStack(p, true));

            // Redundant spam
            if (p != sender) {
                if (!(flags.contains("s") || flags.contains("silent")))
                    info(p, String.format(Lang.RECEIVE_CRATE, crate.id));
                return info(sender, String.format(Lang.GIVE_CRATE_ALL, crate.id, Bukkit.getOnlinePlayers().size()));
            }

            return info(sender, String.format(Lang.SELF_GIVE_CRATE, crate.id));
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
            return info(sender, Lang.CONFIG_LOADED_DEFAULT);
        }, null));

        args.put("reload", new CmdArg((sender, args, flags) -> {
            Main.get().reloadConfig();
            return info(sender, Lang.CONFIG_LOADED_DISK);
        }, null));

        args.put("editor", new CmdArg((sender, args, flags) -> {
            if (sender instanceof Player) {
                // title, subtitle, fadein, stay, fadeout
                Player p = (Player) sender;
                PlayerStat stat = plugin.getStat(p.getUniqueId());
                if (!stat.editorMessaged) {

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

            return error(sender, Lang.ERR_NEED_PLAYER);
        }, null));

        args.put("detect", new CmdArg((sender, args, flags) -> {
            if (!(sender instanceof Player))
                return error(sender, Lang.ERR_NEED_PLAYER);

            Player p = (Player) sender;

            ItemStack itemStack = p.getInventory().getItemInMainHand();
            if (itemStack.getType() != Material.AIR) {
                Crate crate = LootCratesAPI.extractCrateFromItem(itemStack);
                if (crate != null) {
                    return info(sender, String.format(Lang.IS_CRATE, crate.id));
                } else {
                    return info(sender, Lang.NOT_CRATE);
                }
            }
            return error(sender, Lang.REQUIRE_HELD);
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
