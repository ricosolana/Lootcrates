package com.crazicrafter1.lootcrates;

import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;

import java.util.Map;

public class LanguageUnit {
    public final String LANGUAGE;

    public String selectedDisplayName;
    public String selectedLore;

    public String unSelectedDisplayName;
    public String unSelectedLore;

    // lootSet id, lang
    public Map<String, LootSet.Language> lootSets;

    // crate id, lang
    public Map<String, Crate.Language> crates;

    public Map<String, String> editor;

    public LanguageUnit(String language) {
        this.LANGUAGE = language;
    }
}
