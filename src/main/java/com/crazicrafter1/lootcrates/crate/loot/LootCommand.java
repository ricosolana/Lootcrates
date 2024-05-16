package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Pair;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.crutils.ui.*;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.listeners.ListenerOnEditorChatCommandLoot;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

public class LootCommand implements ILoot {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copy(Material.PAPER).name("&7Add command...").build();

    public List<String> commands;
    public ItemStack itemStack;

    /**
     * Editor template LootCommand ctor
     */
    public LootCommand() {
        commands = Lists.newArrayList("say %player_name% Hello, world!");
        itemStack = new ItemStack(Material.COMPASS);
    }

    public LootCommand(Map<String, Object> result) {
        int rev = LCMain.get().rev;

        // TODO eventually remove older revisions
        if (rev < 2)
            itemStack = (ItemStack) result.get("itemStack");
        else if (rev < 6)
            itemStack = ((ItemBuilder) result.get("item")).build();
        else
            itemStack = (ItemStack) result.get("item");

        if (rev < 10)
            commands = Lists.newArrayList((String) result.get("command"));
        else
            commands = (List<String>) result.get("commands");
    }

    protected LootCommand(LootCommand other) {
        this.commands = new ArrayList<>(other.commands);
        this.itemStack = other.itemStack;
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return ItemBuilder.copy(itemStack)
                .placeholders(p)
                .renderAll().build();
    }

    @Nonnull
    @Override
    public ItemStack getMenuIcon() {
        return itemStack.clone();
    }

    @Nonnull
    @Override
    public String getMenuDesc() {
        StringBuilder builder = new StringBuilder("&7Commands: ");
        for (String command : commands) {
            builder.append("\n &7- &f").append(command);
        }
        return builder.toString();
        //return String.join("\n",
                //"&7Command: ", String.join("\n &7- &f", commands));
    }

    @Override
    public boolean execute(@Nonnull CrateInstance activeCrate) {
        for (String command : commands) {
            Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(),
                    Util.placeholders(activeCrate.getPlayer(), command)
            );
        }
        return false;
    }

    @Nonnull
    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ItemModifyMenu()
                .build(itemStack, input -> this.itemStack = input)
                // OLD
                //.button(1, 0, new Button.Builder()
                //        .icon(p -> ItemBuilder.copy(Material.CHAIN_COMMAND_BLOCK).name("&6" + Lang.EDITOR_LOOT_COMMAND_EDIT).lore(Lang.EDITOR_LMB_EDIT).build())
                //        .lmb(event -> {
                //    // add command
                //    ListenerOnEditorChatCommandLoot.awaitingCommands.put(event.player.getUniqueId(),
                //            new Pair<>(this, event.menuBuilder));
                //    return Result.message(ChatColor.GOLD + "Type the command into chat (without the starting '/')")
                //            .andThen(Result.close());
                //}));
                .childButton(1, 0, p -> ItemBuilder.copy(Material.CHAIN_COMMAND_BLOCK).name("&6" + Lang.EDITOR_LOOT_COMMAND_EDIT).lore(Lang.EDITOR_LMB_EDIT).build(),
                        new ListMenu.LBuilder()
                        .title(p -> Lang.EDITOR_LOOT_COMMAND_EDIT)
                        // ADD COMMANDS
                        //.onClose(player -> Result.parent())
                        .parentButton(4, 5)
                        .button(3, 5, new Button.Builder()
                                .icon(p -> ItemBuilder.copy(Material.PAPER).name(Lang.EDITOR_LOOT_COMMAND_ADD).lore(Lang.EDITOR_LMB_EDIT).build())
                                .lmb(event -> {
                            // add command
                            ListenerOnEditorChatCommandLoot.awaitingCommands.put(event.player.getUniqueId(),
                                    new Pair<>(this, event.menuBuilder));
                            return Result.message(ChatColor.GOLD + "Type the command into chat (without the starting '/')")
                                    .andThen(Result.close());
                        }))
                        .addAll((self, p1) -> {
                            List<Button> result = new ArrayList<>();
                            // add all commands as view / delete only
                            for (String command : commands) {
                                result.add(new Button.Builder()
                                        .icon(p000 -> ItemBuilder.copy(Material.COMMAND_BLOCK).name("&2" + command).lore(Lang.EDITOR_DELETE).build())
                                        .rmb(event -> {
                                            if (event.shift) {
                                                // removes first occurrence
                                                // otherwise, why are dup commands being used?
                                                commands.remove(command);
                                                return Result.message("&6Popped command!").andThen(Result.refresh());
                                            }
                                            return Result.refresh();
                                        }).get());
                            }

                            return result;
                        }));
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("commands", commands);
        result.put("item", itemStack);

        return result;
    }

    @Nonnull
    @Override
    public LootCommand copy() {
        return new LootCommand(this);
    }
}
