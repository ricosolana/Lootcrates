package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.gapi.TextMenu;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.sk.SkriptLootEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.crazicrafter1.lootcrates.Lang.L;

public class LootSkriptEvent implements ILoot {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.MAP).name("&aAdd Skript tag...").build();

    String tag;
    ItemStack itemStack;

    public LootSkriptEvent() {
        tag = "awesome";
        itemStack = ItemBuilder.copyOf(Material.JUKEBOX).name("my tag").build();
    }

    public LootSkriptEvent(Map<String, Object> result) {
        // idk
        tag = (String) result.get("tag");
        itemStack = (ItemStack) result.get("itemStack");
    }

    @Override
    public ItemStack getIcon(Player p) {
        return ItemBuilder.copyOf(itemStack).placeholders(p).build();
    }

    @Override
    public boolean execute(ActiveCrate activeCrate) {
        Bukkit.getServer().getPluginManager().callEvent(new SkriptLootEvent(tag, activeCrate.getPlayer()));
        return false;
    }

    @Override
    public String toString() {
        return "&7tag: &f" + tag;
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ItemModifyMenu()
                .build(itemStack, input -> this.itemStack = input)
                .childButton(5, 2, p -> ItemBuilder.copyOf(Material.PAPER).name("&6" + L(p, Lang.A.Event_tag)).lore("&7" + L(Lang.A.LMB) + ": &a" + L(p, Lang.A.Edit)).build(), new TextMenu.TBuilder()
                        .title(p -> L(p, Lang.A.Event_tag))
                        .onClose((player) -> Result.PARENT())
                        .leftRaw(p -> tag)
                        .right(p -> L(p, Lang.A.Input_a_tag))
                        .onComplete((p, s, b) -> {
                            if (!s.isEmpty()) {
                                this.tag = s;
                                return Result.PARENT();
                            }
                            return Result.TEXT(L(p, Lang.A.Invalid));
                        }));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("tag", tag);
        result.put("itemStack", itemStack);

        return result;
    }
}
