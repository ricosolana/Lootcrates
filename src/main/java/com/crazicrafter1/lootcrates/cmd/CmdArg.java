package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.MutableString;
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
        args.put("throw", new CmdArg((sender, args, flags) -> {
            throw new RuntimeException("Test exception");
        }, null));

        args.put("lang", new CmdArg((sender, args, flags) -> {
            String lang = args[0];

            if (Lang.load(lang))
                return info(sender, "Successfully loaded language");
            return error(sender, "Failed to load language");
        }, null));

        args.put("populate", new CmdArg((sender, args, flags) -> {
            plugin.saveConfig();
            plugin.data = new Data();
            return info(sender, Lang.POPULATING);
        }, null));

        args.put("colors", new CmdArg((sender, args, flags) -> {
            if (args.length == 0)
                return error(sender, "Usage: crates colors <#883388>Inertia is a property of matter</#1144FF>");

            return info(sender, ColorUtil.renderAll(String.join(" ", args)));
        }, (sender, args) -> {

            String s = String.join(" ", args);

            MutableString m = new MutableString(s);

            int lastOpen = m.lastIndexOf('<');
            if (lastOpen != -1) {
                MutableString color = MutableString.mutable(m).subRight('<', lastOpen);

                // #RRGGBB
                // /#RRGGBB
                if (!color.startsWith('#')) {

                    // GR
                    int lastClose = color.indexOf('>');
                    if (lastClose == -1) {

                        int slashIndex = m.indexOf('/', lastOpen);

                        // determine whether this is contained within a closing gradient (either implicitly or explicitly)
                        // closing bracket detection is difficult
                        // without scanning the entire string prior for

                        // then get matches for the unclosed
                        return getMatches(color.subLeft('/').toString(), ColorUtil.COLORS.keySet(),
                                m.subLeft('<',lastOpen, 1, true)
                                        .subRight(' ', 0, args.length - 1).append(slashIndex != -1 ? "/" : "").append("%s").toString()
                        );
                    }
                }
            }

            return new ArrayList<>();
        }));

        args.put("rev", new CmdArg((sender, args, flags) -> {
            if (args[0].equalsIgnoreCase("latest")) {
                Main.get().rev = Main.REV_LATEST;
                Main.get().reloadConfig();
                return info(sender, String.format(Lang.READ_W_LATEST_REV, Main.REV_LATEST));
            } else {
                try {
                    int rev = Integer.parseInt(args[0]);
                    if (rev > Main.REV_LATEST) {
                        // err
                        return error(sender, String.format(Lang.REV_UNSUPPORTED, rev));
                    } if (rev < 0)
                        throw new RuntimeException();

                    Main.get().rev = rev;
                    Main.get().reloadConfig();
                    Main.get().rev = Main.REV_LATEST;

                    CmdArg.args.remove("rev");

                    return info(sender, String.format(Lang.READ_W_REV, rev));
                } catch (Exception e) {
                    return error(sender, Lang.REV_REQUIRE_INT);
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
                Util.give((Player) sender, crate.itemStack((Player) sender));
                return info(sender, String.format(Lang.SELF_GIVE_CRATE, crate.id));
            }

            if (args[1].equals("*")) {
                int given = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Util.give(p, crate.itemStack(p));
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

            Util.give(p, crate.itemStack(p));

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
                new Editor().open(p);

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

    //static List<String> getNonDestructiveMatches(String arg, Collection<String> samples, String)

    static List<String> getMatches(String arg, Collection<String> samples) {
        return getMatches(arg, samples, "%s");
    }

    static List<String> getMatches(String arg, Collection<String> samples, String format) {

        //Stream<SimilarString> similar = samples.stream().map(s -> new SimilarString(s, arg));

        return samples.parallelStream().filter(s -> s.toLowerCase().startsWith(arg.toLowerCase()))
                .limit(10).map(s -> String.format(format, s)).collect(Collectors.toList());

        //return similar.filter(s -> s.s.toLowerCase().replace(" ", "").contains(arg.toLowerCase()))
        //        .sorted().limit(10).map(s -> String.format(format, s)).collect(Collectors.toList());
    }
}
