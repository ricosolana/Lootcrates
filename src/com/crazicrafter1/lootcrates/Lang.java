package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.GoogleTranslate;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Lang {
    public static class Unit {
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

        public Unit(String language) {
            this.LANGUAGE = language;
        }
    }

    private final File langFolder = new File(Main.get().getDataFolder(), "lang");

    public Map<String, Unit> translations = new HashMap<>();
    public Map<String, String> editorEnglish = new HashMap<>();

    /**
     * Load all the language files located in lootcrates/lang/
     */
    public boolean loadLanguageFiles() {
        int i = 0;
        int total = 0;
        try {
            // load from lang folder
            for (File file : langFolder.listFiles()) {
                String lang = file.getName();

                if (lang.endsWith(".yml")) {
                    lang = lang.replace(".yml", "");

                    if (loadLanguageFile(lang))
                        i++;

                    total++;
                }
            }
        } catch (NullPointerException e) {
            Main.get().info("No languages to load");
        } catch (Exception e) {
            Main.get().error("Unable to load language files");
            e.printStackTrace();
            return false;
        }

        if (i != total) {
            Main.get().error("Unable to load all language yml files");
            return false;
        }

        return true;
    }

    public boolean loadLanguageFile(final String language) {
        try {
            Main.get().info("Loading language " + language);

            Lang.Unit unit = new Lang.Unit(language);

            FileConfiguration langConfig = new YamlConfiguration();
            langConfig.load(new File(langFolder, language + ".yml"));

            unit.selectedDisplayName = langConfig.getString("selectedDisplayName");
            unit.selectedLore = langConfig.getString("selectedLore");
            unit.unSelectedDisplayName = langConfig.getString("unSelectedDisplayName");
            unit.unSelectedLore = langConfig.getString("unSelectedLore");

            // load lootsets
            unit.lootSets = new HashMap<>();
            for (String id : Main.get().data.lootSets.keySet()) {
                String key = "lootSets." + id + ".";

                LootSet.Language ll = new LootSet.Language();

                ll.itemStackDisplayName = langConfig.getString(key + "itemStackDisplayName");
                ll.itemStackLore = langConfig.getString(key + "itemStackLore");

                unit.lootSets.put(id, ll);
            }

            // now load crates
            unit.crates = new HashMap<>();
            for (String id : Main.get().data.crates.keySet()) {
                String key = "crates." + id + ".";

                Crate.Language cl = new Crate.Language();

                cl.itemStackDisplayName = langConfig.getString(key + "itemStackDisplayName");
                cl.itemStackLore = langConfig.getString(key + "itemStackLore");
                cl.title = langConfig.getString(key + "title");

                unit.crates.put(id, cl);
            }

            // load editor translations
            unit.editor = new HashMap<>();
            if (langConfig.isSet("editor")) {
                for (String key : langConfig.getConfigurationSection("editor").getKeys(false)) {
                    unit.editor.put(key, langConfig.getString("editor." + key));
                }
            }

            translations.put(language, unit);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Save all the language files to lootcrates/lang/
     */
    public boolean saveLanguageFiles() {
        Main.get().info("Saving languages");

        int i = 0;
        for (Lang.Unit unit : translations.values()) {
            if (saveLanguageFile(unit))
                i++;
        }

        if (i != translations.size()) {
            Main.get().error("Saved " + i + "/" + translations.size() + " languages");
            return false;
        } else Main.get().info("Saved all " + translations.size() + " languages");

        return true;
    }

    /**
     * Save the specified language according to Google language code format
     * @param language language code in the form of 'en', 'es', 'fr', 'gr'
     */
    public boolean saveLanguageFile(final String language) {
        return saveLanguageFile(translations.get(language));
    }

    public boolean saveLanguageFile(final Lang.Unit unit) {
        try {
            Main.get().info("Saving language " + unit.LANGUAGE);

            FileConfiguration langConfig = new YamlConfiguration();
            langConfig.set("selectedDisplayName", unit.selectedDisplayName);
            langConfig.set("selectedLore", unit.selectedLore);
            langConfig.set("unSelectedDisplayName", unit.unSelectedDisplayName);
            langConfig.set("unSelectedLore", unit.unSelectedLore);

            // iterate lootsets, inserting into config
            for (String id : Main.get().data.lootSets.keySet()) {
                String key = "lootSets." + id + ".";

                LootSet.Language ll = unit.lootSets.get(id);

                langConfig.set(key + "itemStackDisplayName", ll.itemStackDisplayName);
                langConfig.set(key + "itemStackLore", ll.itemStackLore);
            }

            for (String id : Main.get().data.crates.keySet()) {
                String key = "crates." + id + ".";

                Crate.Language cl = unit.crates.get(id);

                langConfig.set(key + "itemStackDisplayName", cl.itemStackDisplayName);
                langConfig.set(key + "itemStackLore", cl.itemStackLore);
                langConfig.set(key + "title", cl.title);
            }

            for (Map.Entry<String, String> entry : unit.editor.entrySet()) {
                langConfig.set("editor." + entry.getKey(), Objects.requireNonNull(entry.getValue()));
            }

            // Save will create parent directories
            langConfig.save(new File(langFolder, unit.LANGUAGE + ".yml"));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Perform a translation on the config and editor
     * @param language language code in the form of 'en', 'es', 'fr', 'gr'
     */
    public boolean createLanguageFile(final String language) {
        try {
            Lang.Unit unit = new Lang.Unit(language);
            GoogleTranslate GOOG = new GoogleTranslate();

            ItemBuilder builder = ItemBuilder.copyOf(Main.get().data.unSelectedItem).transcribe("auto", language);
            unit.unSelectedDisplayName = builder.getName();
            unit.unSelectedLore = builder.getLoreString();

            builder = ItemBuilder.copyOf(Main.get().data.selectedItem).transcribe("auto", language);
            unit.selectedDisplayName = builder.getName();
            unit.selectedLore = builder.getLoreString();

            unit.lootSets = new HashMap<>();
            for (Map.Entry<String, LootSet> entry : Main.get().data.lootSets.entrySet()) {
                LootSet.Language ll = new LootSet.Language();

                builder = ItemBuilder.copyOf(entry.getValue().itemStack).transcribe("auto", language);
                ll.itemStackDisplayName = builder.getName();
                ll.itemStackLore = builder.getLoreString();

                unit.lootSets.put(entry.getKey(), ll);
            }

            unit.crates = new HashMap<>();
            for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {
                Crate.Language cl = new Crate.Language();

                builder = ItemBuilder.copyOf(entry.getValue().itemStack).transcribe("auto", language);
                cl.itemStackDisplayName = builder.getName();
                cl.itemStackLore = builder.getLoreString();
                cl.title = GOOG.translate(entry.getValue().title, "auto", language);

                unit.crates.put(entry.getKey(), cl);
            }

            unit.editor = new HashMap<>();
            for (Map.Entry<String, String> entry : editorEnglish.entrySet()) {
                unit.editor.put(entry.getKey(), GOOG.translate(entry.getValue(), "auto", language));
            }

            translations.put(language, unit);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
