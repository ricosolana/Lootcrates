package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.GoogleTranslate;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;

public class Data implements ConfigurationSerializable {

    public Data() {}

    public Data(Map<String, Object> args) {
        debug = (boolean) args.getOrDefault("debug", false);
        update = (boolean) args.getOrDefault("update", true);
        speed = (int) args.getOrDefault("speed", 4);

        unSelectedItem = (ItemStack) args.get("unSelectedItem");
        selectedItem = (ItemStack) args.get("selectedItem");

        // load in the same way, but need to pass name somehow
        lootSets = (LinkedHashMap<String, LootSet>) args.get("lootSets");
        for (Map.Entry<String, LootSet> entry : lootSets.entrySet()) {
            entry.getValue().id = entry.getKey();
        }

        crates = (LinkedHashMap<String, Crate>) args.get("crates");
        for (Map.Entry<String, Crate> entry : crates.entrySet()) {
            String id = entry.getKey();
            Crate crate = entry.getValue();

            crate.id = id;
            crate.itemStack = LootCratesAPI.makeCrate(crate.itemStack, id);

            // initialize weights
            crate.sumsToWeights();
        }

        fireworkEffect = (FireworkEffect) args.get("fireworkEffect");

        totalOpens = (int) args.getOrDefault("totalOpens", 0);

        try {
            File file = new File(Main.get().getDataFolder(), "players.csv");

            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    alertedPlayers.add(UUID.fromString(line));
                }
                reader.close();
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    /*
     * Defaults, under the assumption that config permanently fails
     * Safe dev-like fallbacks
     */
    public boolean debug;
    public boolean update;
    public int speed;

    /// having many maps for each item category is terrible,
    /// so instead having 1 map for each language is better
    /// partitioning will be difficult though.

    // each class instance will represent a single separate language

    public ItemStack unSelectedItem;
    public ItemStack selectedItem;
    public FireworkEffect fireworkEffect;

    public LinkedHashMap<String, Crate> crates;
    public LinkedHashMap<String, LootSet> lootSets;

    public int totalOpens;

    public HashSet<UUID> alertedPlayers = new HashSet<>();

    public ItemStack unSelectedItem(Player p) {
        Data.LanguageUnit dlu;

        String lang = Util.MCLocaleToGoogleLocale(p.getLocale());

        if (lang == null
                || lang.equals("en")
                || (dlu = Main.get().data.translations.get(lang)) == null) {
            return unSelectedItem;
        }

        return new ItemBuilder(unSelectedItem)
                .name(dlu.unSelectedDisplayName)
                .lore(dlu.unSelectedLore)
                .toItem();
    }

    public ItemStack selectedItem(Player p) {
        Data.LanguageUnit dlu;

        String lang = Util.MCLocaleToGoogleLocale(p.getLocale());

        if (lang == null
                || lang.equals("en")
                || (dlu = Main.get().data.translations.get(lang)) == null) {
            return selectedItem;
        }

        return new ItemBuilder(selectedItem)
                .name(dlu.selectedDisplayName)
                .lore(dlu.selectedLore)
                .toItem();
    }

    public static class LanguageUnit {
        public String selectedDisplayName;
        public String selectedLore;

        public String unSelectedDisplayName;
        public String unSelectedLore;

        // lootSet id, lang
        public Map<String, Crate.LanguageUnit> crates;
        // crate id, lang
        public Map<String, LootSet.LanguageUnit> lootSets;
    }

    //          lang, translation
    public Map<String, LanguageUnit> translations;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("debug", debug);
        result.put("update", update);
        result.put("speed", speed);

        result.put("unSelectedItem", unSelectedItem);
        result.put("selectedItem", selectedItem);

        result.put("lootSets", lootSets);

        result.put("crates", crates);

        result.put("fireworkEffect", fireworkEffect);

        result.put("totalOpens", totalOpens);


        if (!alertedPlayers.isEmpty())
            try {
                File file = new File(Main.get().getDataFolder(), "players.csv");

                BufferedWriter writer = new BufferedWriter(new FileWriter(file));

                for (UUID uuid : alertedPlayers) {
                    writer.write(uuid.toString());
                    writer.newLine();
                }
                writer.close();
            } catch (Exception e) {e.printStackTrace();}

        return result;
    }

    /**
     * Load all the language files located in lootcrates/lang/
     */
    public void loadLanguageFiles() {
        final File langFolder = new File(Main.get().getDataFolder(), "lang");
        langFolder.mkdirs();

        translations = new HashMap<>();

        try {
            // load from lang folder
            for (File file : langFolder.listFiles()) {
                String lang = file.getName();

                if (lang.endsWith(".yml")) {
                    lang = lang.replace(".yml", "");

                    loadLanguageFile(lang);
                }

            }
        } catch (Exception e) {
            Main.get().error("Unable to load language files");
            e.printStackTrace();
        }
    }

    public void loadLanguageFile(final String lang) {
        final File langFolder = new File(Main.get().getDataFolder(), "lang");
        langFolder.mkdirs();

        try {
            Main.get().info("Loading language " + lang);

            LanguageUnit dataLanguageUnit = new Data.LanguageUnit();

            FileConfiguration langConfig = new YamlConfiguration();
            langConfig.load(new File(langFolder, lang + ".yml"));

            dataLanguageUnit.selectedDisplayName = langConfig.getString("selectedDisplayName");
            dataLanguageUnit.selectedLore = langConfig.getString("selectedLore");
            dataLanguageUnit.unSelectedDisplayName = langConfig.getString("unSelectedDisplayName");
            dataLanguageUnit.unSelectedLore = langConfig.getString("unSelectedLore");

            dataLanguageUnit.crates = new HashMap<>();
            dataLanguageUnit.lootSets = new HashMap<>();

            // load lootsets
            for (String id : lootSets.keySet()) {
                String key = "lootSets." + id + ".";

                LootSet.LanguageUnit lootLanguageUnit = new LootSet.LanguageUnit();

                lootLanguageUnit.itemStackDisplayName = langConfig.getString(key + "itemStackDisplayName");
                lootLanguageUnit.itemStackLore = langConfig.getString(key + "itemStackLore");

                dataLanguageUnit.lootSets.put(id, lootLanguageUnit);
            }

            // now load crates
            for (String id : crates.keySet()) {
                String key = "crates." + id + ".";

                Crate.LanguageUnit crateLanguageUnit = new Crate.LanguageUnit();

                crateLanguageUnit.itemStackDisplayName = langConfig.getString(key + "itemStackDisplayName");
                crateLanguageUnit.itemStackLore = langConfig.getString(key + "itemStackLore");
                crateLanguageUnit.title = langConfig.getString(key + "title");

                dataLanguageUnit.crates.put(id, crateLanguageUnit);
            }

            translations.put(lang, dataLanguageUnit);

        } catch (Exception e) {
            Main.get().error("Unable to load language " + lang);
            e.printStackTrace();
        }
    }

    /**
     * Save all the language files to lootcrates/lang/
     */
    public void saveLanguageFiles() {
        Main.get().info("About to save " + translations.size() + " languages");

        try {
            for (String lang : translations.keySet()) {
                saveLanguageFile(lang);
            }
        } catch (Exception e) {
            Main.get().error("Unable to save language files");
            e.printStackTrace();
        }
    }

    public void saveLanguageFile(final String lang) {
        final File langFolder = new File(Main.get().getDataFolder(), "lang" );
        langFolder.mkdirs();

        try {
            LanguageUnit dlu = translations.get(lang);

            if (dlu == null)
                return;

            Main.get().info("Saving language " + lang);

            FileConfiguration langConfig = new YamlConfiguration();
            langConfig.set("selectedDisplayName", dlu.selectedDisplayName);
            langConfig.set("selectedLore", dlu.selectedLore);
            langConfig.set("unSelectedDisplayName", dlu.unSelectedDisplayName);
            langConfig.set("unSelectedLore", dlu.unSelectedLore);

            // iterate lootsets, inserting into config
            for (String id : lootSets.keySet()) {
                String key = "lootSets." + id + ".";

                LootSet.LanguageUnit llu = dlu.lootSets.get(id);

                langConfig.set(key + "itemStackDisplayName", llu.itemStackDisplayName);
                langConfig.set(key + "itemStackLore", llu.itemStackLore);
            }

            for (String id : crates.keySet()) {
                String key = "crates." + id + ".";

                Crate.LanguageUnit clu = dlu.crates.get(id);

                langConfig.set(key + "itemStackDisplayName", clu.itemStackDisplayName);
                langConfig.set(key + "itemStackLore", clu.itemStackLore);
                langConfig.set(key + "title", clu.title);
            }

            langConfig.save(new File(langFolder, lang + ".yml"));

        } catch (Exception e) {
            Main.get().error("Unable to save language " + lang);
            e.printStackTrace();
        }
    }

    /**
     * @param lang language in the form of 'en'
     */
    public void createLanguageFile(String lang) {
        //LanguageUnit dlu = translations.get(lang);
        //if (dlu != null)
        //    return;

        LanguageUnit dlu = new LanguageUnit();
        GoogleTranslate GOOG = new GoogleTranslate();

        if (GOOG.translate("test", "auto", "es") == null)
            return;

        ItemBuilder builder = new ItemBuilder(unSelectedItem).translateLexicals("en", lang);
        dlu.unSelectedDisplayName = builder.getName();
        dlu.unSelectedLore = builder.getLore();
        // now whether lores will work using translate?

        builder = new ItemBuilder(selectedItem).translateLexicals("en", lang);
        dlu.selectedDisplayName = builder.getName();
        dlu.selectedLore = builder.getLore();

        // load LootsetIDS
        dlu.lootSets = new HashMap<>();
        for (Map.Entry<String, LootSet> entry : lootSets.entrySet()) {

            LootSet.LanguageUnit llu = new LootSet.LanguageUnit();

            builder = new ItemBuilder(entry.getValue().itemStack).translateLexicals("en", lang);
            llu.itemStackDisplayName = builder.getName();
            llu.itemStackLore = builder.getLore();

            dlu.lootSets.put(entry.getKey(), llu);
        }

        dlu.crates = new HashMap<>();
        for (Map.Entry<String, Crate> entry : crates.entrySet()) {

            Crate.LanguageUnit clu = new Crate.LanguageUnit();

            builder = new ItemBuilder(entry.getValue().itemStack).translateLexicals("en", lang);
            clu.itemStackDisplayName = builder.getName();
            clu.itemStackLore = builder.getLore();
            clu.title = GOOG.translate(entry.getValue().title, "en", lang);

            dlu.crates.put(entry.getKey(), clu);
        }

        translations.put(lang, dlu);
    }

    void populate() {
        debug = false;
        update = true;
        speed = 4;
        unSelectedItem = new ItemBuilder(Material.CHEST).name("&f&l???").lore("&7Choose 4 mystery chests, and\n&7your loot will be revealed!").toItem();
        selectedItem = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name("&7&l???").lore("&7You have selected this mystery chest").toItem();

        lootSets = new LinkedHashMap<>();
        LootSet lootSet = new LootSet(
                "common",
                new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name("&f&lCommon Reward").toItem(),
                new ArrayList<>(Collections.singletonList(new LootItem())));
        lootSets.put("common", lootSet);

        crates = new LinkedHashMap<>();
        Crate crate = new Crate("peasant",
                new ItemBuilder(Material.CHEST).name("&f&lPeasant Crate").toItem(),
                "select loot",
                3,
                4,
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        crates.put("peasant", crate);

        crate.lootByWeight = new HashMap<>();
        crate.lootByWeight.put(lootSets.get("common"), 10);
        crate.weightsToSums();

        fireworkEffect = FireworkEffect.builder().withColor(Color.RED, Color.BLUE, Color.WHITE).with(FireworkEffect.Type.BURST).build();
    }
}
