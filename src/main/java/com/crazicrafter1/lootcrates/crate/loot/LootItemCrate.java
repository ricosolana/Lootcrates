package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ui.*;
import com.crazicrafter1.lootcrates.Lang;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.crate.CrateSettings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

public class LootItemCrate implements ILoot {
    public static final ItemStack EDITOR_ICON = ItemBuilder.copy(Material.CHEST).name("&eAdd crate...").build();

    public String id;

    /**
     * Editor template LootItemCrate ctor
     */
    public LootItemCrate() {
        try {
            this.id = LCMain.get().rewardSettings.crates.keySet().iterator().next();
        } catch (NoSuchElementException e) {
            LCMain.get().notifier.severe(Lang.COMMAND_ERROR_CRATES);
        }
    }

    protected LootItemCrate(LootItemCrate other) {
        this.id = other.id;
    }

    public LootItemCrate(Map<String, Object> args) {
        id = (String) args.get("crate");
    }

    @Override
    public boolean execute(@Nonnull CrateInstance activeCrate) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getRenderIcon(@Nonnull Player p) {
        CrateSettings crate = LCMain.get().rewardSettings.crates.get(id);
        return Objects.requireNonNull(crate,
                "Referred a crate by name (" + id + ") " +
                        "which doesn't have a definition in config").itemStack(p);
    }

    @Nonnull
    @Override
    public ItemStack getMenuIcon() {
        return LCMain.get().rewardSettings.crates.get(id).item.clone();
    }

    @Nonnull
    @Override
    public String getMenuDesc() {
        return "&7Crate: &f" + id;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>(); // HashMap used and not LinkedHashMap since only 1 element is added

        result.put("crate", id);

        return result;
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ListMenu.LBuilder()
                .parentButton(4, 5)
                .addAll((self, p00) -> {
                    ArrayList<Button> result = new ArrayList<>();
                    for (Map.Entry<String, CrateSettings> entry : LCMain.get().rewardSettings.crates.entrySet()) {
                        CrateSettings crate = entry.getValue();

                        //ItemStack icon = ItemBuilder.copyOf(Material.LOOM).apply(crate.item).glow(crate.id.equals(id)).build();

                        result.add(new Button.Builder()
                                .icon(p -> ItemBuilder.copy(crate.item).glow(crate.id.equals(id)).build())
                                .lmb(interact -> {
                                    // select as active
                                    id = crate.id;
                                    return Result.refresh();
                                })
                                .get()
                        );
                    }
                    return result;
                });
    }

    @Nonnull
    @Override
    public LootItemCrate copy() {
        return new LootItemCrate(this);
    }
}
