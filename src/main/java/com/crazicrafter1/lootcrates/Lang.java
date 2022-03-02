package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Lang {

    private static final Main plugin = Main.get();

    /**
     * Shorthand for getUnitText for static import access
     * @param keyMsg
     * @return
     */
    @Nonnull
    public static String L(@Nonnull String keyMsg) {
        return plugin.lang.getUnitText(keyMsg);
    }

    /**
     * Shorthand for getUnitText for static import access
     * @param p
     * @param keyMsg
     * @return
     */
    @Nullable
    public static String L(@Nullable Player p, @Nonnull String keyMsg) {
        return plugin.lang.getUnitText(p, keyMsg);
    }

    /**
     * Get textual translations by Player locale
     * Return null if player is null, otherwise nonnull
     * @param p
     * @param keyMsg
     * @return
     */
    @Nullable
    public String getUnitText(@Nullable Player p, @Nonnull String keyMsg) {
        return p != null ? getUnitText(getUnitCode(p.getLocale()), keyMsg) : keyMsg;
    }

    /**
     * Get textual translations by config default
     * @param keyMsg
     * @return
     */
    @Nonnull
    public String getUnitText(String keyMsg) {
        return getUnitText(language, keyMsg);
    }

    /**
     * Get the unit code from player locale
     * @param locale 'en_us'
     * @return 'en'
     */
    @Nullable
    public String getUnitCode(@Nonnull String locale) {
        String langCode = null;
        int index = locale.indexOf("_");
        if (index != -1) {
            langCode = locale.toLowerCase(Locale.ROOT).substring(0, index);
        }
        return langCode;
    }

    /**
     * Get the {@link Unit} from player -> locale -> unit code
     * @param p {@link Player} instance
     * @return {@link Unit} instance
     */
    @Nullable
    public Unit getUnit(@Nullable Player p) {
        // If translations are disabled return
        if (!translate || p == null)
            return null;

        return getUnit(getUnitCode(p.getLocale()));
    }

    /**
     * Get the {@link Unit} from unit code
     * @param unitCode 'en'
     * @return {@link Unit} instance or null
     */
    @Nullable
    public Unit getUnit(@Nullable String unitCode) {
        if (!translate)
            return null;

        Unit unit;

        if (unitCode == null
                || unitCode.equals("en")
                || (unit = translations.get(unitCode)) == null)
            return null;

        return unit;
    }

    @Nonnull
    public String getUnitText(@Nullable String unitCode, @Nonnull String keyMsg) {

        String key = ColorUtil.render(keyMsg);
        if (!keyMsg.equals(key))
            throw new RuntimeException("Must not contain formatted colors: " + key + " (" + ColorMode.INVERT.a(key) + ")");

        key = ColorUtil.strip(keyMsg);
        if (!keyMsg.equals(key))
            throw new RuntimeException("Must not contain color codes: " + key + " (" + ColorMode.INVERT.a(key) + ")");

        key = keyMsg.trim().replace(" ", "_");
        key = Editor.TRANSLATION_STRIPPER_PATTERN.matcher(key).replaceAll("").toLowerCase();

        if (key.length() > 20)
            key = key.substring(0, 20);

        Unit unit = getUnit(unitCode);
        if (unit == null) {
            // Insert english default
            textDef.put(key, keyMsg);// This can be removed once all translations have been inlined in A.class
            return keyMsg;
        }

        //return Objects.requireNonNull(unit.text.get(key), "Must translate all keys first");
        return unit.text.getOrDefault(key, keyMsg);
    }

    // If this were an enum, a 'private static VALUES' field would be included, and would throw an error
    public static class A {
        private A() {}

        public static final String id = "id";
        public static final String count = "count";
        public static final String range = "range";
        public static final String Edit_command = "Edit command";
        public static final String Input_command = "Input the command";
        public static final String support_PAPI = "(supports PlaceholderAPI)";
        public static final String Assign_by_name = "Assign by name";
        public static final String Set_item_by_name = "Set item by name";
        public static final String MMO_Manually_assign = "Manually assign";
        public static final String MMO_Enter = "Enter as type:name";
        public static final String MMO_Edit_tiers = "Edit tiers";
        public static final String Format = "Format";
        public static final String MMO_Format1 = "exact level and tier";
        public static final String MMO_Format2 = "random level and tier";
        public static final String MMO_Format3 = "scale with player";
        public static final String Example = "Example";
        public static final String Invalid_tier = "Invalid tier";
        public static final String Event_tag = "Event tag";
        public static final String Input_a_tag = "Input a tag";

        public static final String Editor = "Editor";
        public static final String Crates = "Crates";
        public static final String New = "New";
        public static final String New_crate = "New crate";
        public static final String Lorem_ipsum = "Lorem ipsum";
        public static final String Format_strict = "Format (strict)";
        public static final String Format_strict1 = "Lowercase";
        public static final String Format_strict2 = "Optional underscores";
        public static final String Duplicate = "Invalid or duplicate";
        public static final String Edit = "Edit";
        public static final String Delete = "Delete";
        public static final String ItemStack = "ItemStack";
        public static final String Title = "Title";
        public static final String Invalid = "Invalid";
        public static final String Loot = "Loot";
        public static final String Columns = "Columns";
        public static final String Picks = "Picks";
        public static final String Sound = "Sound";
        public static final String Input_a_sound = "Input a sound";
        public static final String LootSets = "Lootsets";
        public static final String Elements = "Elements";
        public static final String New_LootSet = "New LootSet";
        public static final String Firework = "Firework";
        public static final String Must_have_effect = "Must have effect";
        public static final String Language = "Languages";
        public static final String Translations = "Translations";
        public static final String Enabled = "Enabled";
        public static final String Disabled = "Disabled";

        public static final String Minimum = "Minimum";
        public static final String Maximum = "Maximum";

        public static final String Set_to = "Set to";
        public static final String Lore = "Lore";
        public static final String Custom_model_data = "Custom model data";
        public static final String Toggle = "Toggle";

        public static final String LMB = "LMB";
        public static final String RMB = "RMB";
        public static final String MMB = "MMB";
        public static final String SHIFT_Mul = "Shift";
    }

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

        public Map<String, String> text;

        public Unit(String language) {
            this.LANGUAGE = language;
        }
    }

    private final File langFolder = new File(plugin.getDataFolder(), "lang");
    private final File langConfigFile = new File(plugin.getDataFolder(), "lang.yml");

    public final Map<String, Unit> translations = new HashMap<>();
    public final Map<String, String> textDef = new HashMap<>();

    public boolean translate;
    public String language;

    /**
     * Load all the language files located in lootcrates/lang/
     */
    public boolean loadLanguageFiles() {
        int i = 0;
        int total = 0;
        try {
            // load server global lang
            YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(this.langConfigFile);
            if (!langConfig.isSet("language")) {
                langConfig.addDefault("translate", false);
                langConfig.addDefault("language", "en");
                langConfig.options().header(
                        "These are the global language settings for the server")
                        .copyDefaults(true);
                langConfig.save(this.langConfigFile);
            }
            translate = langConfig.getBoolean("translate");
            language = langConfig.getString("language");



            try {
                for (Field field : A.class.getDeclaredFields()) {
                    try {
                        String def = (String) field.get(null);
                        L(def);
                        Main.get().info("Setting default translation: " + def);
                    } catch (Exception e) {
                        Main.get().error("Error on field: " + field.getName());
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }



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
            plugin.info("No languages to load");
        } catch (Exception e) {
            plugin.error("Unable to load language files");
            e.printStackTrace();
            return false;
        }

        if (i != total) {
            plugin.error("Unable to load all language yml files");
            return false;
        }

        return true;
    }

    public boolean loadLanguageFile(final String language) {
        try {
            plugin.info("Loading language " + language);

            Unit unit = new Unit(language);

            FileConfiguration langConfig = new YamlConfiguration();
            langConfig.load(new File(langFolder, language + ".yml"));

            unit.selectedDisplayName = langConfig.getString("selectedDisplayName");
            unit.selectedLore = langConfig.getString("selectedLore");
            unit.unSelectedDisplayName = langConfig.getString("unSelectedDisplayName");
            unit.unSelectedLore = langConfig.getString("unSelectedLore");

            // load lootsets
            unit.lootSets = new HashMap<>();
            for (String id : plugin.data.lootSets.keySet()) {
                String key = "lootSets." + id + ".";

                LootSet.Language ll = new LootSet.Language();

                ll.itemStackDisplayName = langConfig.getString(key + "itemStackDisplayName");
                ll.itemStackLore = langConfig.getString(key + "itemStackLore");

                unit.lootSets.put(id, ll);
            }

            // now load crates
            unit.crates = new HashMap<>();
            for (String id : plugin.data.crates.keySet()) {
                String key = "crates." + id + ".";

                Crate.Language cl = new Crate.Language();

                cl.itemStackDisplayName = langConfig.getString(key + "itemStackDisplayName");
                cl.itemStackLore = langConfig.getString(key + "itemStackLore");
                cl.title = langConfig.getString(key + "title");

                unit.crates.put(id, cl);
            }

            // load editor translations
            unit.text = new HashMap<>();
            if (langConfig.isSet("editor")) {
                for (String key : langConfig.getConfigurationSection("editor").getKeys(false)) {
                    unit.text.put(key, langConfig.getString("editor." + key));
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
        plugin.info("Saving languages");



        try {
            YamlConfiguration langConfig = new YamlConfiguration();

            langConfig.set("translate", translate);
            langConfig.set("language", language);
            langConfig.save(this.langConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }



        int i = 0;
        for (Unit unit : translations.values()) {
            if (saveLanguageFile(unit))
                i++;
        }

        if (i != translations.size()) {
            plugin.error("Saved " + i + "/" + translations.size() + " languages");
            return false;
        } else plugin.info("Saved all " + translations.size() + " languages");

        return true;
    }

    /**
     * Save the specified language according to Google language code format
     * @param language language code in the form of 'en', 'es', 'fr', 'gr'
     */
    public boolean saveLanguageFile(final String language) {
        return saveLanguageFile(translations.get(language));
    }

    public boolean saveLanguageFile(final Unit unit) {
        try {
            plugin.info("Saving language " + unit.LANGUAGE);

            FileConfiguration langConfig = new YamlConfiguration();
            langConfig.set("selectedDisplayName", unit.selectedDisplayName);
            langConfig.set("selectedLore", unit.selectedLore);
            langConfig.set("unSelectedDisplayName", unit.unSelectedDisplayName);
            langConfig.set("unSelectedLore", unit.unSelectedLore);

            // iterate lootsets, inserting into config
            for (String id : plugin.data.lootSets.keySet()) {
                String key = "lootSets." + id + ".";

                LootSet.Language ll = unit.lootSets.get(id);

                langConfig.set(key + "itemStackDisplayName", ll.itemStackDisplayName);
                langConfig.set(key + "itemStackLore", ll.itemStackLore);
            }

            for (String id : plugin.data.crates.keySet()) {
                String key = "crates." + id + ".";

                Crate.Language cl = unit.crates.get(id);

                langConfig.set(key + "itemStackDisplayName", cl.itemStackDisplayName);
                langConfig.set(key + "itemStackLore", cl.itemStackLore);
                langConfig.set(key + "title", cl.title);
            }

            for (Map.Entry<String, String> entry : unit.text.entrySet()) {
                langConfig.set("editor." + entry.getKey(), Objects.requireNonNull(entry.getValue()));
            }

            // Save will create parent directories
            // make a backup of the previous

            File saveTo = new File(langFolder, unit.LANGUAGE + ".yml");

            if (Util.backupZip(saveTo, new File(langConfigFile.getParentFile(), unit.LANGUAGE + ".zip"))) {
                langConfig.save(saveTo);
                return true;
            }
        } catch (Exception e) {
            Main.get().error("Failed to save language " + unit.LANGUAGE + " (translating immediately after a crate/lootset addition might fix this)");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Perform a translation on the config and editor
     * @param language language code in the form of 'en', 'es', 'fr', 'gr'
     */
    public Unit createLanguageFile(final String language) {
        try {
            Unit unit = new Unit(language);
            GoogleTranslate GOOG = new GoogleTranslate();

            ItemBuilder builder = ItemBuilder.copyOf(plugin.data.unSelectedItem).transcribe("auto", language);
            unit.unSelectedDisplayName = builder.getName();
            unit.unSelectedLore = builder.getLoreString();

            builder = ItemBuilder.copyOf(plugin.data.selectedItem).transcribe("auto", language);
            unit.selectedDisplayName = builder.getName();
            unit.selectedLore = builder.getLoreString();

            unit.lootSets = new HashMap<>();
            for (Map.Entry<String, LootSet> entry : plugin.data.lootSets.entrySet()) {
                LootSet.Language ll = new LootSet.Language();

                builder = ItemBuilder.copyOf(entry.getValue().itemStack).transcribe("auto", language);
                ll.itemStackDisplayName = builder.getName();
                ll.itemStackLore = builder.getLoreString();

                unit.lootSets.put(entry.getKey(), ll);
            }

            unit.crates = new HashMap<>();
            for (Map.Entry<String, Crate> entry : plugin.data.crates.entrySet()) {
                Crate.Language cl = new Crate.Language();

                builder = ItemBuilder.copyOf(entry.getValue().itemStack).transcribe("auto", language);
                cl.itemStackDisplayName = builder.getName();
                cl.itemStackLore = builder.getLoreString();
                cl.title = GOOG.translate(entry.getValue().title, "auto", language);

                unit.crates.put(entry.getKey(), cl);
            }

            unit.text = new HashMap<>();
            for (Map.Entry<String, String> entry : textDef.entrySet()) {
                unit.text.put(entry.getKey(), GOOG.translate(entry.getValue(), "auto", language));
            }

            return unit;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
