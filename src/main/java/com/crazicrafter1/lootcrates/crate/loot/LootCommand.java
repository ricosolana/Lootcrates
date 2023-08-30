package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.crutils.ui.*;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

public class LootCommand implements ILoot {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copy(Material.PAPER).name("&7Add command...").build();

    public String command;
    public ItemStack itemStack;

    /**
     * Editor template LootCommand ctor
     */
    public LootCommand() {
        command = "say %player_name% Hello, world!";
        itemStack = new ItemStack(Material.COMPASS);
    }

    public LootCommand(Map<String, Object> result) {
        command = (String) result.get("command");

        // TODO eventually remove older revisions
        int rev = LCMain.get().rev;
        if (rev < 2)
            itemStack = (ItemStack) result.get("itemStack");
        else if (rev < 6)
            itemStack = ((ItemBuilder) result.get("item")).build();
        else
            itemStack = (ItemStack) result.get("item");
    }

    protected LootCommand(LootCommand other) {
        this.command = other.command;
        this.itemStack = other.itemStack;
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return ItemBuilder.copy(itemStack)
                .placeholders(p)
                .renderAll().build();
    }

    @NotNull
    @Override
    public ItemStack getMenuIcon() {
        return itemStack.clone();
    }

    @NotNull
    @Override
    public String getMenuDesc() {
        return String.format(Lang.ED_LootSets_PROTO_BTN_Command, command);
    }

    @Override
    public boolean execute(CrateInstance activeCrate) {
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
                .build(itemStack, input -> this.itemStack = input)
                .childButton(1, 0, p -> ItemBuilder.copy(Material.PAPER).name("&6" + Lang.ED_LootSets_PROTO_Command_TI).lore(Lang.ED_LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.ED_LootSets_PROTO_Command_TI)
                        .onClose((player) -> Result.PARENT())
                        .leftRaw(p ->  command)
                        .right(p -> Lang.ED_LootSets_PROTO_Command_R, p -> String.format(Lang.SUPPORT_PLUGIN_X, "PlaceholderAPI"))
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
        result.put("item", itemStack);

        return result;
    }

    @Nonnull
    @Override
    public LootCommand copy() {
        return new LootCommand(this);
    }
}
