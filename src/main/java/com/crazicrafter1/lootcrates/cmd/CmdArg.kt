package com.crazicrafter1.lootcrates.cmd

import com.crazicrafter1.crutils.*
import com.crazicrafter1.lootcrates.*
import com.crazicrafter1.lootcrates.crate.CrateInstance
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import java.util.function.IntFunction
import java.util.stream.Collectors
import java.util.stream.Stream

internal object CmdArg {
    private val plugin: LCMain? = LCMain.Companion.get()
    private fun arg(@Nonnull arg: String, @Nonnull executor: TriFunction<CommandSender, Array<String>, Set<String>, Boolean>,
                    tabCompleter: BiFunction<CommandSender, Array<String>, List<String>>? = null) {
        args[arg] = Pair(executor, tabCompleter)
    }

    /**
     * Command executor and tab-completer map
     */
    var args: MutableMap<String?, Pair<TriFunction<CommandSender, Array<String>, Set<String>, Boolean>, BiFunction<CommandSender, Array<String>, List<String>>?>> = HashMap()

    init {
        if (plugin!!.debug) {
            arg("class", { sender: CommandSender?, args: Array<String>, flags: Set<String>? ->
                try {
                    val clazz = Class.forName(args[0])
                    try {
                        plugin.notifier!!.info("Package: " + clazz.getPackage().name)
                    } catch (e: Exception) {
                        plugin.notifier!!.info("In default package?")
                    }
                    return@arg info(sender, "Found: " + clazz.getName())
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@arg severe(sender, e.message)
                }
            })
            arg("opened", { sender: CommandSender?, args: Array<String>?, flags: Set<String>? ->
                info(sender, "Open crates: " + java.lang.String.join(", ",
                        *CrateInstance.Companion.CRATES.values.stream().map<String>(Function<CrateInstance, String> { e: CrateInstance -> e.player.name }).toArray<String>(IntFunction<Array<String>> { _Dummy_.__Array__() })
                ))
            })
            arg("method", { sender: CommandSender?, args: Array<String>, flags: Set<String>? ->
                try {
                    val clazz = Class.forName(args[0])
                    try {
                        plugin.notifier!!.info("Package: " + clazz.getPackage().name)
                    } catch (e: Exception) {
                        plugin.notifier!!.info("In default package?")
                    }
                    plugin.notifier!!.info("Methods:")
                    for (method in clazz.getMethods()) {
                        if (method.name.startsWith(args[1])) plugin.notifier!!.info(" - " + method.name)
                    }
                    return@arg info(sender, "Found: " + clazz.getName())
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@arg severe(sender, e.message)
                }
            })
            arg("throw", { sender: CommandSender?, args: Array<String>?, flags: Set<String>? -> throw RuntimeException("Test exception") })
        }
        arg("colors", { sender: CommandSender?, args: Array<String>, flags: Set<String>? ->
            if (args.size == 0) return@arg severe(sender, "Usage: crates colors <#883388>Inertia is a property of matter</#1144FF>")
            info(sender, ColorUtil.renderAll(java.lang.String.join(" ", *args)))
        }) { sender: CommandSender?, args: Array<String> ->
            val s = java.lang.String.join(" ", *args)
            val m = MutableString(s)
            val lastOpen = m.lastIndexOf('<')
            if (lastOpen != -1) {
                val color = MutableString.mutable(m).subRight('<', lastOpen)

                // #RRGGBB
                // /#RRGGBB
                if (!color.startsWith('#')) {

                    // GR
                    val lastClose = color.indexOf('>')
                    if (lastClose == -1) {
                        val slashIndex = m.indexOf('/', lastOpen)

                        // determine whether this is contained within a closing gradient (either implicitly or explicitly)
                        // closing bracket detection is difficult
                        // without scanning the entire string prior for

                        // then get matches for the unclosed
                        return@arg ArrayList<String>()
                    }
                }
            }
            ArrayList()
        }
        arg("crate", { sender: CommandSender, args: Array<String>, flags: Set<String> ->
            val crate = Lootcrates.getCrate(args[0])
                    ?: return@arg severe(sender, Lang.COMMAND_ERROR_CRATE)

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

            // /crates crate common
            if (args.size == 1) {
                if (sender !is Player) return@arg severe(sender, Lang.CRATE_ERROR_PLAYER0)
                Util.give(sender, crate.itemStack(sender))
                return@arg info(sender, String.format(Lang.MESSAGE_RECEIVE_CRATE, crate.id))
            }

            // crates crate common *
            if (args[1] == "*") {
                var given = 0
                for (p in Bukkit.getOnlinePlayers()) {
                    Util.give(p, crate.itemStack(p))
                    if (p !== sender && !(flags.contains("s") || flags.contains("silent"))) {
                        info(p, String.format(Lang.MESSAGE_RECEIVE_CRATE, crate.id))
                        given++
                    }
                }
                if (given == 0) return@arg severe(sender, Lang.COMMAND_ERROR_PLAYERS)
                return@arg info(sender, String.format(Lang.COMMAND_GIVE_ALL, crate.id, given))
            }

            // crates crate common crazicrafter1
            val p = Bukkit.getServer().onlinePlayers.stream().filter { player: Player -> player.name == args[1] }.findFirst().orElse(null)
                    ?: return@arg severe(sender, Lang.CRATE_ERROR_PLAYER1)
            Util.give(p, crate.itemStack(p))

            // now test quantity with args

            // Redundant spam
            if (p !== sender) {
                if (!(flags.contains("s") || flags.contains("silent"))) info(p, String.format(Lang.MESSAGE_RECEIVE_CRATE, crate.id))
                return@arg info(sender, String.format(Lang.COMMAND_GIVE, crate.id, p.displayName))
            }
            info(sender, String.format(Lang.MESSAGE_RECEIVE_CRATE, crate.id))
        }) { sender: CommandSender, args: Array<String> ->
            if (args.size == 1) {
                return@arg getMatches(args[0], plugin.rewardSettings!!.crates!!.keys)
            }
            if (args.size == 2) {
                return@arg getMatches(args[1],
                        Stream.concat<String?>(
                                Stream.concat<String?>(
                                        Bukkit.getServer().onlinePlayers.stream().filter { p: Player -> p !== sender }.map<String?> { obj: Player -> obj.name },
                                        if (Bukkit.getServer().onlinePlayers.size > 1) // Removing redundant identifiers for discrete
                                            Stream.of<String?>("*") else Stream.empty<String>()),
                                if (sender is Player) Stream.of<String?>("-s", "-silent") else Stream.empty<String>()
                        )
                                .collect(Collectors.toList<String?>()))
            }
            if (args.size == 3 && !args[args.size - 1].startsWith("-s")) {
                return@arg getMatches(args[2], Stream.of<String?>("-s", "-silent")
                        .collect(Collectors.toList<String?>()))
            }
            ArrayList()
        }
        arg("editor", { sender: CommandSender?, args: Array<String>?, flags: Set<String>? ->
            if (sender is Player) {
                Editor().open(sender)
                return@arg true
            }
            severe(sender, Lang.COMMAND_ERROR_PLAYER)
        })
        arg("identify", { sender: CommandSender?, args: Array<String>?, flags: Set<String>? ->
            if (sender !is Player) return@arg severe(sender, Lang.COMMAND_ERROR_PLAYER)
            val itemStack = sender.inventory.itemInMainHand
            if (itemStack.type != Material.AIR) {
                val crate = Lootcrates.getCrate(itemStack)
                if (crate != null) {
                    return@arg info(sender, String.format(Lang.COMMAND_IDENTIFY, crate.id))
                } else {
                    return@arg info(sender, Lang.MESSAGE_NOT_CRATE)
                }
            }
            severe(sender, Lang.MESSAGE_REQUIRE_ITEM)
        })
        arg("lang", { sender: CommandSender?, args: Array<String>, flags: Set<String>? ->
            if (Lang.load(sender, args[0])) plugin.language = args[0]
            true
        }) { sender: CommandSender?, args: Array<String>? ->
            try {
                return@arg Files.list(Lang.PATH.toPath()).filter { f: Path -> f.endsWith(".yml") }.map<String> { f: Path -> f.fileName.toString() }.collect(Collectors.toList<String>())
            } catch (e: IOException) {
                //return severe(sender, e.getMessage());
                //throw new RuntimeException(e);
                return@arg null
            }
        }
        arg("reload", { sender: CommandSender?, args: Array<String>?, flags: Set<String>? ->
            plugin.reloadConfig(sender)
            plugin.reloadData(sender)
            info(sender, Lang.CONFIG_LOADED_DISK)
        })
        arg("reset", { sender: CommandSender?, args: Array<String>?, flags: Set<String>? ->
            plugin.saveDefaultConfig(sender, true)
            plugin.reloadConfig(sender)
            info(sender, Lang.CONFIG_LOADED_DEFAULT)
        })
        arg("save", { sender: CommandSender?, args: Array<String>?, flags: Set<String>? ->
            plugin.saveConfig(sender)
            plugin.saveOtherConfigs(sender)
            info(sender, Lang.CONFIG_SAVED)
        })
    }

    fun info(sender: CommandSender?, message: String?): Boolean {
        return plugin!!.notifier!!.commandInfo(sender!!, message!!)
    }

    fun warn(sender: CommandSender?, message: String?): Boolean {
        return plugin!!.notifier!!.commandWarn(sender!!, message!!)
    }

    fun severe(sender: CommandSender?, message: String?): Boolean {
        return plugin!!.notifier!!.commandSevere(sender!!, message!!)
    }

    //static List<String> getNonDestructiveMatches(String arg, Collection<String> samples, String)
    fun getMatches(arg: String, samples: Collection<String?>): List<String> {
        return getMatches(arg, samples, "%s")
    }

    fun getMatches(arg: String, samples: Collection<String?>, format: String?): List<String> {
        // todo this doesnt work as expected on vanilla client because the autocomplete is alphabetical
        //Stream<SimilarString> similar = samples.stream().map(s -> new SimilarString(s, arg));
        return samples.parallelStream().filter { s: String? -> s!!.lowercase(Locale.getDefault()).startsWith(arg.lowercase(Locale.getDefault())) }
                .limit(10).map { s: String? -> String.format(format!!, s) }.collect(Collectors.toList())

        //return similar.filter(s -> s.s.toLowerCase().replace(" ", "").contains(arg.toLowerCase()))
        //        .sorted().limit(10).map(s -> String.format(format, s)).collect(Collectors.toList());
    }
}
