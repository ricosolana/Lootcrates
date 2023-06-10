package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.lootcrates.LCMain;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

// todo migrate to CrateSettings
@Deprecated
public class Crate implements ConfigurationSerializable {
    public String id;
    public ItemBuilder item;
    public String title;
    public int columns;
    public int picks;
    public Sound sound;
    public WeightedRandomContainer<LootSet> loot;

    public Crate(Map<String, Object> args) {
        title = ColorUtil.renderMarkers((String) args.get("title"));

        columns = (int) args.get("columns");
        picks = (int) args.get("picks");
        sound = Sound.valueOf((String) args.get("sound"));

        // TODO eventually remove older revisions
        int rev = LCMain.get().rev;
        if (rev < 2)
            item = ItemBuilder.mut((ItemStack) args.get("itemStack"));
        else
            item = (ItemBuilder) args.get("item");

        if (rev < 4)
            loot = WeightedRandomContainer.cumulative((LinkedHashMap<LootSet, Integer>) args.get("weights"));
        else
            loot = new WeightedRandomContainer<>((Map<LootSet, Integer>) args.get("weights"));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        throw new UnsupportedOperationException("Do not use!");
    }

    //todo remove post-migrate
    public CrateSettings getSettings() {
        // transforms the LootSet map to LootSetSettings map
        // this is the 'magic'
        Map<String, Integer> map = this.loot.getMap().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().id, Map.Entry::getValue));

        return new CrateSettings(id, title, columns, picks, sound, map, item.build(), CrateSettings.RevealType.GOOD_OL_DESTY);
    }
}
