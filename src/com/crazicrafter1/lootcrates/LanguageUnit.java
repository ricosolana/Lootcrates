package com.crazicrafter1.lootcrates;

import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;

import java.util.Map;

public class LanguageUnit {
    public String language;

    public String selectedDisplayName;
    public String selectedLore;

    public String unSelectedDisplayName;
    public String unSelectedLore;

    // lootSet id, lang
    public Map<String, Crate.Language> crates;
    // crate id, lang
    public Map<String, LootSet.Language> lootSets;

    public Map<String, String> editor;


}
