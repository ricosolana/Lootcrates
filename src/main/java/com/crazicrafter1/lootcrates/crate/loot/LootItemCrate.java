package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.crazicrafter1.lootcrates.Lang.L;

public class LootItemCrate extends AbstractLootItem {
    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.CHEST).name("&eAdd crate...").build();

    public String id;

    /**
     * Default ctor
     */
    public LootItemCrate() {
        try {
            id = Main.get().data.crates.keySet().iterator().next();
        } catch (NoSuchElementException e) {
            Main.get().error(L("No crates are registered"));
        }
    }

    public LootItemCrate(Map<String, Object> args) {
        super(args);
        id = (String) args.get("crate");
    }

    public LootItemCrate(Crate crate) {
        this.id = crate.id;
    }

    @Override
    public ItemStack getIcon(Player p) {
        Crate crate = Main.get().data.crates.get(id);
        return Objects.requireNonNull(crate,
                "Referred a crate by name (" + id + ") " +
                        "which doesn't have a definition in config").itemStack(p);
    }

    @Override
    public String toString() {
        return "&7crate: &f" + id + "\n" +
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
                .parentButton(4, 5)
                .addAll((self, p00) -> {
                    ArrayList<Button> result = new ArrayList<>();
                    for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {
                        Crate crate = entry.getValue();

                        ItemStack icon = ItemBuilder.copyOf(Material.LOOM).combine(crate.itemStack(null)).glow(crate.id.equals(id)).build();

                        result.add(new Button.Builder()
                                .icon(p -> icon)
                                .lmb(interact -> {
                                    // select as active
                                    id = crate.id;
                                    return Result.REFRESH();
                                })
                                .get()
                        );
                    }
                    return result;
                });
    }
}
