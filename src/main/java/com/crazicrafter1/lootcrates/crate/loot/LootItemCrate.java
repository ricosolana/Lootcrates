package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class LootItemCrate implements ILoot {
    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.CHEST).name("&eAdd crate...").build();

    public String id;

    /**
     * Default ctor
     */
    public LootItemCrate() {
        try {
            id = Main.get().data.crates.keySet().iterator().next();
        } catch (NoSuchElementException e) {
            Main.get().error(Lang.ERR_NO_CRATES);
        }
    }

    public LootItemCrate(Map<String, Object> args) {
        id = (String) args.get("crate");
    }

    public LootItemCrate(Crate crate) {
        this.id = crate.id;
    }

    @Override
    public boolean execute(@NotNull ActiveCrate activeCrate) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        Crate crate = Main.get().data.crates.get(id);
        return Objects.requireNonNull(crate,
                "Referred a crate by name (" + id + ") " +
                        "which doesn't have a definition in config").itemStack(p);
    }

    @NotNull
    @Override
    public ItemStack getMenuIcon() {
        return Main.get().data.crates.get(id).item.buildCopy();
    }

    @NotNull
    @Override
    public String getMenuDesc() {
        return "&7crate: &f" + id;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>(); // HashMap used and not LinkedHashMap since only 1 element is added

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

                        ItemStack icon = ItemBuilder.copyOf(Material.LOOM).apply(crate.item).glow(crate.id.equals(id)).build();

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
