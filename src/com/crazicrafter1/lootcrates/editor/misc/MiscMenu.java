package com.crazicrafter1.lootcrates.editor.misc;

import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.lootcrates.editor.MainMenu;

public class MiscMenu extends SimplexMenu {

    public MiscMenu() {
        super("&8Misc", 3, BACKGROUND_1);

        this.backButton(4, 2, BACK_1, MainMenu.class);
    }

}
