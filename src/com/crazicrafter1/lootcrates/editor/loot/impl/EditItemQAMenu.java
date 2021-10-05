package com.crazicrafter1.lootcrates.editor.loot.impl;

import com.crazicrafter1.gapi.Menu;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItemQA;

public class EditItemQAMenu extends SimplexMenu {

    public EditItemQAMenu(LootItemQA loot, LootSet lootGroup, Class<? extends Menu> prevMenuClass) {
        super("", 3);

        //setComponent(4, 2, n);

        backButton(4, 2, BACK_1, prevMenuClass, lootGroup);
    }
}
