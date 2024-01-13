package com.crazicrafter1.lootcrates.cmd

import com.crazicrafter1.lootcrates.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.*
import java.util.stream.Collectors

class Cmd(private val plugin: LCMain) : CommandExecutor, TabCompleter {
    init {
        plugin.getCommand("crates")!!.setExecutor(this)
        plugin.getCommand("crates")!!.tabCompleter = this
    }

    override fun onCommand(sender: CommandSender, c: Command, s: String, args: Array<String>): Boolean {
        if (args.size == 0) {
            CmdArg.info(sender, String.format(Lang.MESSAGE_VERSION, LCMain.Companion.get()!!.getDescription().getVersion()))
            CmdArg.info(sender, String.format(Lang.MESSAGE_REV, LCMain.Companion.get()!!.rev))
            return CmdArg.info(sender, Lang.MESSAGE_COMMAND_USAGE + "/crates ["
                    + java.lang.String.join(", ", CmdArg.args.keys)
                    + "]")
        }
        val pair = CmdArg.args[args[0].lowercase(Locale.getDefault())]
                ?: return CmdArg.severe(sender, Lang.COMMAND_ERROR_ARGS1)
        return try {
            //String[] smartArgs = smartParse(Arrays.copyOfRange(args, 1, args.length)).toArray(new String[0]);
            val smartArgs = Arrays.copyOfRange(args, 1, args.size)
            pair.first!!.apply(sender,
                    smartArgs,
                    Arrays.stream(smartArgs).filter { arg: String -> arg.length >= 2 && arg.startsWith("-") }.map { arg: String -> arg.substring(1) }.collect(Collectors.toSet()))
            true
        } catch (e: ArrayIndexOutOfBoundsException) {
            // Just ensure index with an error print
            CmdArg.severe(sender, String.format(Lang.COMMAND_ERROR_ARGS0, e.message))
        }
    }

    // An efficient tab complete algorithm would involve a tree map
    // only worth it for many matches
    override fun onTabComplete(sender: CommandSender, c: Command, s: String, args: Array<String>): List<String>? {
        if (args.size == 0) return ArrayList()
        if (args.size == 1) {
            return if (LCMain.Companion.get()!!.rev == -1) {
                listOf<String>("rev") // todo remove post-migrate
            } else CmdArg.getMatches(args[0], CmdArg.args.keys)
        }
        val pair = CmdArg.args[args[0]]
        if (pair == null || pair.second == null) return ArrayList()

        //String[] smartArgs = smartParse(Arrays.copyOfRange(args, 1, args.length)).toArray(new String[0]);
        val smartArgs = Arrays.copyOfRange(args, 1, args.size)
        return pair.second!!.apply(sender, smartArgs)
    }

    companion object {
        /**
         * Intelligently parse the args to include spaces only when quotes follow
         * todo could be extended, but sort of feature creep
         */
        @Deprecated("")
        fun smartParse(args: Array<String?>): ArrayList<String> {
            val combined = java.lang.String.join(" ", *args)
            val newArgs = ArrayList<String>()
            var delimStart = -1
            var start = 0
            var prev = '\u0000'
            var i = 0
            while (i < combined.length) {
                val c = combined[i]
                // if theres a separator and no prior delimiter
                if (delimStart == -1) {
                    if (i == combined.length - 1) {
                        newArgs.add(combined.substring(start, i + 1).replace("\\\"", "\""))
                    } else if (Character.isWhitespace(c)) {
                        newArgs.add(combined.substring(start, i).replace("\\\"", "\""))
                        start = i + 1
                    } else if (c == '"' && prev != '\\') {
                        delimStart = i + 1
                    }
                } else {
                    if (c == '"' && prev != '\\') {
                        newArgs.add(combined.substring(delimStart, i).replace("\\\"", "\""))
                        i++
                        delimStart = -1
                        start = i + 1
                    }
                }
                prev = c
                i++
            }
            return newArgs
        }
    }
}
