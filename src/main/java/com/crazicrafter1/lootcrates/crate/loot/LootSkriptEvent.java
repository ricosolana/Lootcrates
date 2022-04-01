package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.gapi.TextMenu;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
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

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.MAP).name("&aAdd Skript tag...").build();

    String tag;
    ItemBuilder item;

    public LootSkriptEvent() {
        tag = "awesome";
        item = ItemBuilder.copyOf(Material.JUKEBOX).name("my tag");
    }

    public LootSkriptEvent(Map<String, Object> result) {
        // idk
        tag = (String) result.get("tag");

        int rev = Main.get().rev;
        if (rev < 2)
            item = ItemBuilder.mutable((ItemStack) result.get("itemStack"));
        else
            item = (ItemBuilder) result.get("item");
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        return item.copy().placeholders(p).renderAll().build();
    }

    @Override
    public boolean execute(ActiveCrate activeCrate) {
        Bukkit.getServer().getPluginManager().callEvent(new SkriptLootEvent(tag, activeCrate.getPlayer()));
        return false;
    }

    @NotNull
    @Override
    public ItemStack getMenuIcon() {
        return item.buildCopy();
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
                .build(item.build(), input -> (this.item = ItemBuilder.mutable(input)).build())
                .childButton(1, 0, p -> ItemBuilder.copyOf(Material.PAPER).name(Lang.SKRIPT_EVENT_TAG).lore(Lang.LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.SKRIPT_EVENT_TAG)
                        .onClose((player) -> Result.PARENT())
                        .leftRaw(p -> tag)
                        .right(p -> Lang.SKRIPT_INPUT_TAG)
                        .onComplete((p, s, b) -> {
                            if (!s.isEmpty()) {
                                this.tag = s;
                                return Result.PARENT();
                            }
                            return Result.TEXT(Lang.ERR_INVALID);
                        }));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("tag", tag);
        result.put("item", item);

        return result;
    }
}
