package com.crazicrafter1.lootcrates.editor.loot.impl;

import com.crazicrafter1.gapi.Menu;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItemCrate;

public class EditItemCrateMenu extends SimplexMenu {

    public EditItemCrateMenu(LootItemCrate loot, LootSet lootGroup, Class<? extends Menu> prevMenuClass) {
        super("Add loot: Crate", 3);

        // show a dialogue of which crates exist
        backButton(4, 2, BACK_1, prevMenuClass, lootGroup);

    }
}
