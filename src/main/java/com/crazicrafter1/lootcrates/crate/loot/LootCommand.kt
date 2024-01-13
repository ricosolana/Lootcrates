package com.crazicrafter1.lootcrates.crate.loot

import com.crazicrafter1.crutils.*
import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.crutils.ui.Button
import com.crazicrafter1.crutils.ui.ListMenu.LBuilder
import com.crazicrafter1.crutils.ui.Result
import com.crazicrafter1.lootcrates.*
import com.crazicrafter1.lootcrates.crate.CrateInstance
import com.crazicrafter1.lootcrates.listeners.ListenerOnEditorChatCommandLoot
import com.google.common.collect.Lists
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Function

class LootCommand : ILoot {
    var commands: MutableList<String?>? = null
    var itemStack: ItemStack? = null

    /**
     * Editor template LootCommand ctor
     */
    constructor() {
        commands = Lists.newArrayList("say %player_name% Hello, world!")
        itemStack = ItemStack(Material.COMPASS)
    }

    constructor(result: Map<String?, Any?>) {
        val rev: Int = LCMain.Companion.get()!!.rev

        // TODO eventually remove older revisions
        itemStack = if (rev < 2) result["itemStack"] as ItemStack? else if (rev < 6) (result["item"] as ItemBuilder?)!!.build() else result["item"] as ItemStack?
        commands = if (rev < 10) Lists.newArrayList(result["command"] as String?) else result["commands"] as MutableList<String?>?
    }

    protected constructor(other: LootCommand) {
        commands = ArrayList(other.commands)
        itemStack = other.itemStack
    }

    override fun getRenderIcon(p: Player): ItemStack {
        return ItemBuilder.copy(itemStack)
                .placeholders(p)
                .renderAll().build()
    }

    override val menuIcon: ItemStack
        get() = itemStack!!.clone()
    override val menuDesc: String
        get() {
            val builder = StringBuilder("&7Commands: ")
            for (command in commands!!) {
                builder.append("\n &7- &f").append(command)
            }
            return builder.toString()
            //return String.join("\n",
            //"&7Command: ", String.join("\n &7- &f", commands));
        }

    override fun execute(activeCrate: CrateInstance): Boolean {
        for (command in commands!!) {
            Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(),
                    Util.placeholders(activeCrate.player, command!!)
            )
        }
        return false
    }

    override val menuBuilder: AbstractMenu.Builder
        get() = ItemModifyMenu()
                .build(itemStack, Function<ItemStack?, ItemStack> { input: ItemStack? -> itemStack = input }) // OLD
                //.button(1, 0, new Button.Builder()
                //        .icon(p -> ItemBuilder.copy(Material.CHAIN_COMMAND_BLOCK).name("&6" + Lang.EDITOR_LOOT_COMMAND_EDIT).lore(Lang.EDITOR_LMB_EDIT).build())
                //        .lmb(event -> {
                //    // add command
                //    ListenerOnEditorChatCommandLoot.awaitingCommands.put(event.player.getUniqueId(),
                //            new Pair<>(this, event.menuBuilder));
                //    return Result.message(ChatColor.GOLD + "Type the command into chat (without the starting '/')")
                //            .andThen(Result.close());
                //}));
                .childButton(1, 0, { p: Player? -> ItemBuilder.copy(Material.CHAIN_COMMAND_BLOCK).name("&6" + Lang.EDITOR_LOOT_COMMAND_EDIT).lore(Lang.EDITOR_LMB_EDIT).build() },
                        LBuilder()
                                .title { p: Player? -> Lang.EDITOR_LOOT_COMMAND_EDIT } // ADD COMMANDS
                                //.onClose(player -> Result.parent())
                                .parentButton(4, 5)
                                .button(3, 5, Button.Builder()
                                        .icon { p: Player? -> ItemBuilder.copy(Material.PAPER).name(Lang.EDITOR_LOOT_COMMAND_ADD).lore(Lang.EDITOR_LMB_EDIT).build() }
                                        .lmb { event: Button.Event ->
                                            // add command
                                            ListenerOnEditorChatCommandLoot.awaitingCommands.put(event.player.uniqueId,
                                                    Pair<LootCommand, AbstractMenu.Builder>(this, event.menuBuilder))
                                            Result.message(ChatColor.GOLD.toString() + "Type the command into chat (without the starting '/')")
                                                    .andThen(Result.close())
                                        })
                                .addAll { self: LBuilder?, p1: Player? ->
                                    val result: MutableList<Button> = ArrayList()
                                    // add all commands as view / delete only
                                    for (command in commands!!) {
                                        result.add(Button.Builder()
                                                .icon { p000: Player? -> ItemBuilder.copy(Material.COMMAND_BLOCK).name("&2$command").lore(Lang.EDITOR_DELETE).build() }
                                                .rmb { event: Button.Event ->
                                                    if (event.shift) {
                                                        // removes first occurrence
                                                        // otherwise, why are dup commands being used?
                                                        commands!!.remove(command)
                                                        return@rmb Result.message("&6Popped command!").andThen(Result.refresh())
                                                    }
                                                    Result.refresh()
                                                }.get())
                                    }
                                    result
                                })

    override fun serialize(): Map<String, Any> {
        val result: MutableMap<String, Any> = LinkedHashMap()
        result["commands"] = commands!!
        result["item"] = itemStack!!
        return result
    }

    @Nonnull
    override fun copy(): LootCommand {
        return LootCommand(this)
    }

    companion object {
        val EDITOR_ICON = ItemBuilder.copy(Material.PAPER).name("&7Add command...").build()
    }
}
