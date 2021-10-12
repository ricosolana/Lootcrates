package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.EnumResult;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;

public class LootItemCrate extends AbstractLootItem {

    // Important to use id, because a referenced Crate object is sort of an
    // small leak
    public String id;

    /**
     * Default ctor
     */
    public LootItemCrate() {}

    public LootItemCrate(Map<String, Object> args) {
        super(args);
        id = (String) args.get("crate");
    }

    public LootItemCrate(Crate crate) {
        this.id = crate.id;
    }

    @Override
    public ItemStack getIcon() {
        return Main.get().data.crates.get(id).itemStack;
    }

    @Override
    public String toString() {
        return "crate: &7" + id + "\n" +
                super.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize(); // = super.serialize();

        result.put("crate", id);

        return result;
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ParallaxMenu.PBuilder()
                .title("LootItemCrate")
                .action(self -> {
                    ArrayList<Button> result = new ArrayList<>();
                    for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {
                        Crate crate = entry.getValue();

                        Main.get().debug("lootitemcrate: " + id + ", iterate: " + crate.id);

                        ItemStack icon = new ItemBuilder(Material.LOOM).mergeLexicals(crate.itemStack).glow(crate.id.equals(id)).toItem();

                        result.add(new Button.Builder()
                                .icon(() -> icon)
                                .lmb(interact -> {
                                    // select as active
                                    id = crate.id;
                                    //return Button.Result.refresh();
                                    //return EnumResult.OPEN(new LMItemCrate(loot, lootSet).menu);
                                    return EnumResult.REFRESH;
                                })
                                .get()
                        );
                    }
                    return result;
                })
                .parentButton(4, 5)
                .validate();
    }
}
