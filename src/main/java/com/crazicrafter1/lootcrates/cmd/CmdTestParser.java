package com.crazicrafter1.lootcrates.cmd;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CmdTestParser implements CommandExecutor {

    private final Main plugin;

    public CmdTestParser(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginCommand("testparser").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command c, @NotNull String l, @NotNull String[] args) {

        //new Flowchain("")

        //new CmdParser(sender, args)
        //        .exact("crate", // crate
        //                parser -> parser.exact(plugin.data.crates.keySet(), // peasant, knight, king
        //                        parser1 -> parser1.expect(Expect.PLAYER,
        //                                parser2 -> parser2.expect(Expect.NUMBER,
        //                                        parser3 -> {
        //                                            Player p = parser3.getPlayer();
        //                                            Util.give(p, LootCratesAPI.getCrateByID(parser3.getString()).itemStack(p));
        //                                        }
        //                                )
        //                        )
        //                )
        //        ); // self OR player OR NUMBER OR

        // a tokenizer will have match options for:
        //  - check
        //  - check + save
        //  - check + save + fallthrough

        // possible names:
        //  - token, require, acquire, match, with, optional, is

        // isToken:         check
        // requireToken:    check + save
        // orToken:         check + save + always fallthrough

        //new CmdParser(sender, args)
        //        .isToken("say", // require: fallthrough on match, else error handle
        //                            // requireOr: fallthrough on match, else do nothing
        //                parser -> parser.optional(Expect.PLAYER, e -> Main.get().error(sender, e), // optional: fallthrough
        //                        parser1 -> parser1.getPlayer().sendMessage("Hello")
        //                )
        //        );

        return true;
    }
}
