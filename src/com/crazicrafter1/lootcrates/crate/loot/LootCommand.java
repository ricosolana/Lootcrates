package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.gapi.TextMenu;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.crazicrafter1.lootcrates.Lang.L;

public class LootCommand implements ILoot {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.PAPER).name("&7Add command...").build();

    public String command;
    public ItemStack itemStack;

    public LootCommand() {
        command = "say %player_name% hi there";
        itemStack = ItemBuilder.copyOf(Material.CACTUS).name("Hello, world!").build();
    }

    public LootCommand(Map<String, Object> result) {
        command = (String) result.get("command");
        itemStack = (ItemStack) result.get("itemStack");
    }

    @Override
    public ItemStack getIcon(Player p) {
        return ItemBuilder.copyOf(itemStack).placeholders(p).build();
    }

    @Override
    public boolean execute(ActiveCrate activeCrate) {
        Bukkit.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                Util.placeholders(activeCrate.getPlayer(), command)
        );
        return false;
    }

    @Override
    public String toString() {
        return "&7command: &f" + command;
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ItemModifyMenu()
                .build(itemStack, input -> this.itemStack = input)
                .childButton(5, 2, p -> ItemBuilder.copyOf(Material.PAPER).name("&6" + L(p, Lang.A.Edit_command)).lore("&7" + L(Lang.A.LMB) + ": &a" + L(p, Lang.A.Edit)).build(), new TextMenu.TBuilder()
                        .title(p -> L(p, Lang.A.Edit_command))
                        .onClose((player) -> Result.PARENT())
                        .leftRaw(p ->  command)
                        .right(p ->  L(p, Lang.A.Input_command), p -> L(p, Lang.A.support_PAPI))
                        .onComplete((p, s, b) -> {
                            if (!s.isEmpty()) {
                                this.command = s;
                                return Result.PARENT();
                            }
                            return Result.TEXT(L(p, Lang.A.Invalid));
                        }));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("command", command);
        result.put("itemStack", itemStack);

        return result;
    }
}
