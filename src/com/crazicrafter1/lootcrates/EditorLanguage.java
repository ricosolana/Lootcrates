package com.crazicrafter1.lootcrates;

import java.lang.reflect.Field;
import java.util.HashMap;

public class EditorLanguage {
    public String IN_OUTLINE_Lang = "&7Set to";
    public String LORE_LMB_EDIT_Lang = "&7LMB: &aEdit";
    public String LORE_RMB_DEL_Lang = "&7RMB: &cDelete";
    public String LORE_LMB_NUM_Lang = "&7LMB&r&7: &c-";
    public String LORE_RMB_NUM_Lang = "&7RMB&r&7: &a+";
    public String LORE_SHIFT_NUM_Lang = "&7SHIFT&r&7: x5";
    public String ITEM_NEW_Lang = "&6New";
    public String NAME_EDIT_Lang = "&aEdit";

    public static String COLOR_PREFIX = "&7'&' " + (Editor.IS_NEW ?
            "or '#&': &#2367fbc&#3f83fbo&#5a9ffcl&#76bbfco&#91d7fcr&#acf2fds" : ": &fcolors") +
            "\n&7Macros: &6%lc_picks%&7, &6%lc_id%\n&7Supports PlaceholderAPI";

    private static final String LOREM_IPSUM = "Lorem ipsum";

    HashMap<String, String> messageMap = new HashMap<>();

    EditorLanguage() {
        for (Field field : EditorLanguage.class.getDeclaredFields()) {

            if (field.getName().endsWith("Lang")) {

                //if (field)

            }

        }

    }


}
