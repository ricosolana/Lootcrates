package com.crazicrafter1.lootcrates.lootwrapper;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.SimpleMenu;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class LMItem extends LMWrapper {

    //@Override
    //public AbstractMenu.Builder getMenu() {
    //    Button.Builder b = new Button.Builder().icon(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("&eSet to").toItem());
    //    return new SimpleMenu.SBuilder(5)
    //            .title(ChatColor.DARK_GRAY + "LootItem editor")
    //            .parentButton(4, 4)
    //            .button(2, 1, b)
    //            .button(3, 2, b)
    //            .button(2, 3, b)
    //            .button(1, 2, b)
    //            .button(2, 2, new Button.Builder()
    //                    .icon(lootItem.itemStack)
    //                    .lmb(interact -> Button.Result.take()));
    //}

    public LMItem(final LootItem lootItem, final LootSet lootSet) {
        Button.Builder b = new Button.Builder().icon(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("&eSet to").toItem());
        menu = new SimpleMenu.SBuilder(5)
                .title(ChatColor.DARK_GRAY + "LootItem editor")
                .parentButton(4, 4)
                .button(2, 1, b)
                .button(3, 2, b)
                .button(2, 3, b)
                .button(1, 2, b)
                .button(2, 2, new Button.Builder()
                        .icon(lootItem.itemStack)
                        .lmb(interact -> Button.Result.take()))
                .validate();
    }



}
