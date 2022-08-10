package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.ui.AbstractMenu;
import com.crazicrafter1.crutils.ui.Button;
import com.crazicrafter1.crutils.ui.ParallaxMenu;
import com.crazicrafter1.crutils.ui.Result;
import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;

// todo migrate to LootSetSettings
@Deprecated
public class LootSet implements ConfigurationSerializable {
    public String id;
    public ItemBuilder item;
    public ArrayList<ILoot> loot;

    public LootSet(String id, ItemStack item, ArrayList<ILoot> loot) {
        this.id = id;
        this.item = ItemBuilder.mutable(item);
        this.loot = loot;
    }

    public LootSet(Map<String, Object> args) {
        // TODO eventually remove older revisions
        int rev = Main.get().rev;
        if (rev < 2)
            item = ItemBuilder.mutable((ItemStack) args.get("itemStack"));
        else
            item = ((ItemBuilder) args.get("item"));
        loot = (ArrayList<ILoot>) args.get("loot");
        for (int i=0; i < loot.size(); i++) {
            if (loot.get(i) == null) {
                Main.get().error("Item in rewards.yml is null (" + i + ")");
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        throw new UnsupportedOperationException("Do not use!");
    }

    //todo remove post-migrate
    public LootSetSettings getSettings() {
        return new LootSetSettings(id, item.build(), loot);
    }
}
