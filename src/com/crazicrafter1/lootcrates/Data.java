package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.GoogleTranslate;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import com.sun.istack.internal.NotNull;
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
        lang = (boolean) args.getOrDefault("lang", false);
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

        loadLanguageFiles();
    }

    /*
     * Defaults, under the assumption that config permanently fails
     * Safe dev-like fallbacks
     */
    public boolean lang;
    public boolean debug;
    public boolean update;
    public int speed;

    public ItemStack unSelectedItem;
    public ItemStack selectedItem;
    public FireworkEffect fireworkEffect;

    public LinkedHashMap<String, Crate> crates;
    public LinkedHashMap<String, LootSet> lootSets;

    public int totalOpens;

    public HashSet<UUID> alertedPlayers = new HashSet<>();

    public ItemStack unSelectedItem(Player p) {
        LanguageUnit dlu = Main.get().getLang(p);

        if (dlu == null) {
            return unSelectedItem;
        }

        return new ItemBuilder(unSelectedItem)
                .name(dlu.unSelectedDisplayName)
                .lore(dlu.unSelectedLore)
                .toItem();
    }

    public ItemStack selectedItem(Player p) {
        LanguageUnit dlu = Main.get().getLang(p);

        if (dlu == null) {
            return selectedItem;
        }

        return new ItemBuilder(selectedItem)
                .name(dlu.selectedDisplayName)
                .lore(dlu.selectedLore)
                .toItem();
    }

    //          lang, translation
    public Map<String, LanguageUnit> translations;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("lang", lang);
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

        saveLanguageFiles();

        return result;
    }

    /**
     * Load all the language files located in lootcrates/lang/
     */
    public boolean loadLanguageFiles() {
        final File langFolder = new File(Main.get().getDataFolder(), "lang");
        langFolder.mkdirs();

        translations = new HashMap<>();

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

    public boolean loadLanguageFile(final String lang) {
        try {
            Main.get().info("Loading language " + lang);

            final File langFolder = new File(Main.get().getDataFolder(), "lang");
            langFolder.mkdirs();

            LanguageUnit unit = new LanguageUnit();

            FileConfiguration langConfig = new YamlConfiguration();
            langConfig.load(new File(langFolder, lang + ".yml"));

            unit.selectedDisplayName = langConfig.getString("selectedDisplayName");
            unit.selectedLore = langConfig.getString("selectedLore");
            unit.unSelectedDisplayName = langConfig.getString("unSelectedDisplayName");
            unit.unSelectedLore = langConfig.getString("unSelectedLore");

            unit.crates = new HashMap<>();
            unit.lootSets = new HashMap<>();

            // load lootsets
            for (String id : lootSets.keySet()) {
                String key = "lootSets." + id + ".";

                LootSet.Language ll = new LootSet.Language();

                ll.itemStackDisplayName = langConfig.getString(key + "itemStackDisplayName");
                ll.itemStackLore = langConfig.getString(key + "itemStackLore");

                unit.lootSets.put(id, ll);
            }

            // now load crates
            for (String id : crates.keySet()) {
                String key = "crates." + id + ".";

                Crate.Language cl = new Crate.Language();

                cl.itemStackDisplayName = langConfig.getString(key + "itemStackDisplayName");
                cl.itemStackLore = langConfig.getString(key + "itemStackLore");
                cl.title = langConfig.getString(key + "title");

                unit.crates.put(id, cl);
            }

            translations.put(lang, unit);

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
        for (LanguageUnit unit : translations.values()) {
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
    public boolean saveLanguageFile(@NotNull final String language) {
        return saveLanguageFile(translations.get(language));
    }

    public boolean saveLanguageFile(@NotNull final LanguageUnit unit) {
        try {
            Main.get().info("Saving language " + unit.language);

            final File langFolder = new File(Main.get().getDataFolder(), "lang");
            langFolder.mkdirs();

            FileConfiguration langConfig = new YamlConfiguration();
            langConfig.set("selectedDisplayName", unit.selectedDisplayName);
            langConfig.set("selectedLore", unit.selectedLore);
            langConfig.set("unSelectedDisplayName", unit.unSelectedDisplayName);
            langConfig.set("unSelectedLore", unit.unSelectedLore);

            // iterate lootsets, inserting into config
            for (String id : lootSets.keySet()) {
                String key = "lootSets." + id + ".";

                LootSet.Language ll = unit.lootSets.get(id);

                langConfig.set(key + "itemStackDisplayName", ll.itemStackDisplayName);
                langConfig.set(key + "itemStackLore", ll.itemStackLore);
            }

            for (String id : crates.keySet()) {
                String key = "crates." + id + ".";

                Crate.Language cl = unit.crates.get(id);

                langConfig.set(key + "itemStackDisplayName", cl.itemStackDisplayName);
                langConfig.set(key + "itemStackLore", cl.itemStackLore);
                langConfig.set(key + "title", cl.title);
            }

            langConfig.save(new File(langFolder, unit.language + ".yml"));

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
    public boolean createLanguageFile(String language) {
        try {
            LanguageUnit dlu = new LanguageUnit();
            GoogleTranslate GOOG = new GoogleTranslate();

            ItemBuilder builder = new ItemBuilder(unSelectedItem).translateLexicals("auto", language);
            dlu.unSelectedDisplayName = builder.getName();
            dlu.unSelectedLore = builder.getLore();

            builder = new ItemBuilder(selectedItem).translateLexicals("auto", language);
            dlu.selectedDisplayName = builder.getName();
            dlu.selectedLore = builder.getLore();

            dlu.lootSets = new HashMap<>();
            for (Map.Entry<String, LootSet> entry : lootSets.entrySet()) {
                LootSet.Language ll = new LootSet.Language();

                builder = new ItemBuilder(entry.getValue().itemStack).translateLexicals("auto", language);
                ll.itemStackDisplayName = builder.getName();
                ll.itemStackLore = builder.getLore();

                dlu.lootSets.put(entry.getKey(), ll);
            }

            dlu.crates = new HashMap<>();
            for (Map.Entry<String, Crate> entry : crates.entrySet()) {
                Crate.Language cl = new Crate.Language();

                builder = new ItemBuilder(entry.getValue().itemStack).translateLexicals("auto", language);
                cl.itemStackDisplayName = builder.getName();
                cl.itemStackLore = builder.getLore();
                cl.title = GOOG.translate(entry.getValue().title, "auto", language);

                dlu.crates.put(entry.getKey(), cl);
            }

            translations.put(language, dlu);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    void populate() {
        lang = false;
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
