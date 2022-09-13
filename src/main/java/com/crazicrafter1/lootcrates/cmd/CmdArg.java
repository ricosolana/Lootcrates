package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CmdArg {

    private static final LCMain plugin = LCMain.get();

    private static void arg(@Nonnull String arg, @Nonnull TriFunction<CommandSender, String[], Set<String>, Boolean> executor) {
        arg(arg, executor, null);
    }

    private static void arg(@Nonnull String arg, @Nonnull TriFunction<CommandSender, String[], Set<String>, Boolean> executor,
                            @Nullable BiFunction<CommandSender, String[], List<String>> tabCompleter) {
        args.put(arg, new Pair<>(executor, tabCompleter));
    }

    /**
     * Command executor and tab-completer map
     */
    static Map<String,
            Pair<TriFunction<CommandSender, String[], Set<String>, Boolean>,
                    BiFunction<CommandSender, String[], List<String>>>> args = new HashMap<>();

    static {
        arg("throw", (sender, args, flags) -> {
            throw new RuntimeException("Test exception");
        });

        arg("class", (sender, args, flags) -> {
            try {
                Class<?> clazz = Class.forName(args[0]);
                try {
                    plugin.notifier.info("Package: " + clazz.getPackage().getName());
                } catch (Exception e) {
                    plugin.notifier.info("In default package?");
                }
                return info(sender, "Found: " + clazz.getName());
            } catch (Exception e) {
                e.printStackTrace();
                return severe(sender, e.getMessage());
            }
        });

        arg("method", (sender, args, flags) -> {
            try {
                Class<?> clazz = Class.forName(args[0]);
                try {
                    plugin.notifier.info("Package: " + clazz.getPackage().getName());
                } catch (Exception e) {
                    plugin.notifier.info("In default package?");
                }

                plugin.notifier.info("Methods:");
                for (Method method : clazz.getMethods()) {
                    if (method.getName().startsWith(args[1]))
                        plugin.notifier.info(" - " + method.getName());
                }

                return info(sender, "Found: " + clazz.getName());
            } catch (Exception e) {
                e.printStackTrace();
                return severe(sender, e.getMessage());
            }
        });

        arg("lang", (sender, args, flags) -> {
            if (Lang.load(sender, args[0]))
                LCMain.get().language = args[0];
            return true;
        }
                // todo autocompleting looks at disk each time, which is slow?
                /* (sender, args) -> {
            File[] files = Lang.langPath.listFiles();

            //return Arrays.stream(Lang.langPath.listFiles()).filter(file -> file.getName().endsWith(".yml"));

            ArrayList<String> ret = new ArrayList<>();
            if (args.length == 1) {
                ret.add("save");
                ret.add("load");
            } else if (args.length == 2) {
                ret.add("confirm");
            }
            return ret;
        }*/);

        //args.put("populate", new CmdArg((sender, args, flags) -> {
        //    plugin.saveConfig(sender);
        //    plugin.data = new Data();
        //    return info(sender, Lang.POPULATING);
        //}, null));

        arg("colors", (sender, args, flags) -> {
            if (args.length == 0)
                return severe(sender, "Usage: crates colors <#883388>Inertia is a property of matter</#1144FF>");

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
                        return new ArrayList<>();
                    }
                }
            }

            return new ArrayList<>();
        });

        // TODO this adds a lot of tackiness into plugin
        arg("rev", (sender, args, flags) -> {
            if (args[0].equalsIgnoreCase("latest")) {
                plugin.rev = LCMain.REV_LATEST;
                plugin.reloadConfig(sender);
                plugin.reloadData(sender);
                return info(sender, String.format(Lang.READ_W_LATEST_REV, LCMain.REV_LATEST));
            } else {
                try {
                    int rev = Integer.parseInt(args[0]);
                    if (rev > LCMain.REV_LATEST) {
                        // err
                        return severe(sender, String.format(Lang.REV_UNSUPPORTED, rev));
                    } if (rev < 0)
                        throw new RuntimeException();

                    plugin.rev = rev;
                    plugin.reloadConfig(sender);
                    plugin.reloadData(sender);
                    plugin.rev = LCMain.REV_LATEST;

                    CmdArg.args.remove("rev");

                    return info(sender, String.format(Lang.READ_W_REV, rev));
                } catch (Exception e) {
                    return severe(sender, Lang.REV_REQUIRE_INT);
                }
            }
        }, (sender, args) -> {
            ArrayList<String> ret = new ArrayList<>();
            for (int i = 0; i < LCMain.REV_LATEST; i++) {
                ret.add("" + i);
            }
            ret.add("latest");
            return ret;
        });

        arg("save", (sender, args, flags) -> {
            plugin.saveConfig(sender);
            plugin.saveOtherConfigs(sender);
            return info(sender, Lang.CONFIG_SAVED);
        });

        arg("crate", (sender, args, flags) -> {
            CrateSettings crate = Lootcrates.getCrate(args[0]);

            // the best way to represent this command structure would be with a treemap
            // crates -> display plugin info
            //  - crate
            //      - <crate>

            // STATIC and WILDCARD are more like specifiers
            //  STATIC is an expected arg among predetermined args
            //  WILDCARD is an unpredictable arg

            // flags:
            // flags could take a class type to construct
            //  - NUMBER_OR_STRING
            //  - NUMBER strict
            //  - STRING
            //  - PLAYER (online)
            //  - ANY_PLAYER (online/offline)

            if (crate == null)
                return severe(sender, Lang.ERR_CRATE_UNKNOWN);

            // /crates crate common
            if (args.length == 1) {
                if (!(sender instanceof Player))
                    return severe(sender, Lang.ERR_PLAYER_CRATE);
                Util.give((Player) sender, crate.itemStack((Player) sender));
                return info(sender, String.format(Lang.SELF_GIVE_CRATE, crate.id));
            }

            // crates crate common *
            if (args[1].equals("*")) {
                int given = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Util.give(p, crate.itemStack(p));
                    if (p != sender && !(flags.contains("s") || flags.contains("silent"))) {
                        info(p, String.format(Lang.RECEIVE_CRATE, crate.id));
                        given++;
                    }
                }

                if (given == 0)
                    return severe(sender, Lang.ERR_NONE_ONLINE);

                return info(sender, String.format(Lang.GIVE_CRATE_ALL, crate.id, given));
            }

            // crates crate common crazicrafter1
            Player p = Bukkit.getServer().getPlayer(args[1]);
            if (p == null)
                return severe(sender, Lang.ERR_PLAYER_UNKNOWN);

            Util.give(p, crate.itemStack(p));

            // now test quantity with args

            // Redundant spam
            if (p != sender) {
                if (!(flags.contains("s") || flags.contains("silent")))
                    info(p, String.format(Lang.RECEIVE_CRATE, crate.id));
                return info(sender, String.format(Lang.GIVE_CRATE_ALL, crate.id, Bukkit.getOnlinePlayers().size()));
            }

            return info(sender, String.format(Lang.SELF_GIVE_CRATE, crate.id));
        }, (sender, args) -> {
            if (args.length == 1) {
                return getMatches(args[0], LCMain.get().rewardSettings.crates.keySet());
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
        });

        arg("reset", (sender, args, flags) -> {
            plugin.saveDefaultConfig(sender, true);
            plugin.reloadConfig(sender);
            return info(sender, Lang.CONFIG_LOADED_DEFAULT);
        });

        arg("reload", (sender, args, flags) -> {
            plugin.reloadConfig(sender);
            plugin.reloadData(sender);
            return info(sender, Lang.CONFIG_LOADED_DISK);
        });

        arg("dbg-opened", (sender, args, flags) -> {
            return info(sender, "Open crates: " + String.join(", ",
                    CrateInstance.CRATES.values().stream().map(e -> e.getPlayer().getName()).collect(Collectors.toList()).toArray(new String[0])
            ));
            //return info(sender, "Updated");
        });

        arg("editor", (sender, args, flags) -> {
            if (sender instanceof Player) {
                // title, subtitle, fadein, stay, fadeout
                Player p = (Player) sender;
                // todo an opened crate holds a reference to the same object pointed to by CrateInstance.openCrates
                //  when this object is modified (not reassigned), issues might occur?
                //if (CrateInstance.openCrates.isEmpty())
                    new Editor().open(p);
                //else
                //    error(sender, "Can only be used when no crates are open");

                return true;
            }

            return severe(sender, Lang.ERR_NEED_PLAYER);
        });

        arg("which", (sender, args, flags) -> {
            if (!(sender instanceof Player))
                return severe(sender, Lang.ERR_NEED_PLAYER);

            Player p = (Player) sender;

            ItemStack itemStack = p.getInventory().getItemInMainHand();
            if (itemStack.getType() != Material.AIR) {
                CrateSettings crate = Lootcrates.getCrate(itemStack);
                if (crate != null) {
                    return info(sender, String.format(Lang.IS_CRATE, crate.id));
                } else {
                    return info(sender, Lang.NOT_CRATE);
                }
            }
            return severe(sender, Lang.REQUIRE_HELD);
        });
    }

    static boolean info(CommandSender sender, String message) {
        return plugin.notifier.commandInfo(sender, message);
    }

    static boolean warn(CommandSender sender, String message) {
        return plugin.notifier.commandWarn(sender, message);
    }

    static boolean severe(CommandSender sender, String message) {
        return plugin.notifier.commandSevere(sender, message);
    }

    //static List<String> getNonDestructiveMatches(String arg, Collection<String> samples, String)

    static List<String> getMatches(String arg, Collection<String> samples) {
        return getMatches(arg, samples, "%s");
    }

    static List<String> getMatches(String arg, Collection<String> samples, String format) {
        // todo this doesnt work as expected on vanilla client because the autocomplete is alphabetical
        //Stream<SimilarString> similar = samples.stream().map(s -> new SimilarString(s, arg));

        return samples.parallelStream().filter(s -> s.toLowerCase().startsWith(arg.toLowerCase()))
                .limit(10).map(s -> String.format(format, s)).collect(Collectors.toList());

        //return similar.filter(s -> s.s.toLowerCase().replace(" ", "").contains(arg.toLowerCase()))
        //        .sorted().limit(10).map(s -> String.format(format, s)).collect(Collectors.toList());
    }
}
