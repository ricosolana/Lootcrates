package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ui.*;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.sk.SkriptLootEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

public class LootSkriptEvent implements ILoot {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copy(Material.MAP).name("&aAdd Skript tag...").build();

    String tag;
    ItemStack item;

    public LootSkriptEvent() {
        tag = "my_skript_tag";
        item = new ItemStack(Material.JUKEBOX);
    }

    protected LootSkriptEvent(LootSkriptEvent other) {
        this.tag = other.tag;
        this.item = other.item;
    }

    public LootSkriptEvent(Map<String, Object> result) {
        // idk
        tag = (String) result.get("tag");

        // TODO eventually remove older revisions
        int rev = Main.get().rev;
        if (rev < 2)
            item = (ItemStack) result.get("itemStack");
        else if (rev < 6)
            item = ((ItemBuilder) result.get("item")).build();
        else
            item = (ItemStack) result.get("item");
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return ItemBuilder.copy(item).placeholders(p).renderAll().build();
    }

    @Override
    public boolean execute(CrateInstance activeCrate) {
        Bukkit.getServer().getPluginManager().callEvent(new SkriptLootEvent(tag, activeCrate.getPlayer()));
        return false;
    }

    @NotNull
    @Override
    public ItemStack getMenuIcon() {
        return item.clone();
    }

    @NotNull
    @Override
    public String getMenuDesc() {
        return "&7tag: &f" + tag;
    }

    @Nonnull
    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ItemModifyMenu()
                .build(item, input -> this.item = input)
                .childButton(1, 0, p -> ItemBuilder.copy(Material.PAPER).name(Lang.ED_LootSets_PROTO_Skript_TI).lore(Lang.ED_LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.ED_LootSets_PROTO_Skript_TI)
                        .onClose((player) -> Result.PARENT())
                        .leftRaw(p -> tag)
                        .right(p -> Lang.ED_LootSets_PROTO_Skript_R)
                        .onComplete((p, s, b) -> {
                            if (!s.isEmpty()) {
                                this.tag = s;
                                return Result.PARENT();
                            }
                            return Result.TEXT(Lang.ERR_INVALID);
                        }));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("tag", tag);
        result.put("item", item);

        return result;
    }

    @NotNull
    @Override
    public LootSkriptEvent copy() {
        return new LootSkriptEvent(this);
    }
}
