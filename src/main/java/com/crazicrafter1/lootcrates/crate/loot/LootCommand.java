package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.gapi.TextMenu;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

public class LootCommand implements ILoot {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.PAPER).name("&7Add command...").build();

    public String command;
    public ItemBuilder item;

    public LootCommand() {
        command = "say %player_name% hi there";
        item = ItemBuilder.copyOf(Material.CACTUS).name("Hello, world!");
    }

    public LootCommand(Map<String, Object> result) {
        command = (String) result.get("command");

        int rev = Main.get().rev;
        if (rev < 2) {
            item = ItemBuilder.mutable((ItemStack) result.get("itemStack"));
        } else if (rev == 2) {
            item = (ItemBuilder) result.get("item");
        }
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return ItemBuilder.copyOf(item).placeholders(p).renderAll().build();
    }

    @NotNull
    @Override
    public ItemStack getMenuIcon(@NotNull Player p) {
        return item.placeholders(p).build();
    }

    @NotNull
    @Override
    public String getMenuDesc(@NotNull Player p) {
        return String.format(Lang.LOOT_COMMAND, command);
    }

    @Override
    public boolean execute(ActiveCrate activeCrate) {
        Bukkit.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                Util.placeholders(activeCrate.getPlayer(), command)
        );
        return false;
    }

    @Nonnull
    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ItemModifyMenu()
                .build(item.build(), input -> (this.item = ItemBuilder.mutable(input)).build())
                .childButton(5, 2, p -> ItemBuilder.copyOf(Material.PAPER).name("&6" + Lang.EDIT_COMMAND).lore(Lang.LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.EDIT_COMMAND)
                        .onClose((player) -> Result.PARENT())
                        .leftRaw(p ->  command)
                        .right(p -> Lang.INPUT_COMMAND, p -> String.format(Lang.SUPPORT_PLUGIN_X, "PlaceholderAPI"))
                        .onComplete((p, s, b) -> {
                            if (!s.isEmpty()) {
                                this.command = s;
                                return Result.PARENT();
                            }
                            return Result.TEXT(Lang.ERR_INVALID);
                        }));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("command", command);
        result.put("itemStack", item);

        return result;
    }
}
