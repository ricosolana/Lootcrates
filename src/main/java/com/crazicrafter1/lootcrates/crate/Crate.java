package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Crate implements ConfigurationSerializable {
    public String id;
    public ItemBuilder item;
    public String title;
    public int columns;
    public int picks;
    public Sound sound;
    public WeightedRandomContainer<LootSet> loot = new WeightedRandomContainer<>();

    //public Crate(String id, ItemStack itemStack, String title, int columns, int picks, Sound sound) {
    //    this.id = id;
    //    this.item = ItemBuilder.copyOf(LootCratesAPI.makeCrate(itemStack, id));
    //    this.title = ColorUtil.renderMarkers(title);
    //    this.columns = columns;
    //    this.picks = picks;
    //    this.sound = sound;
    //    this.loot = new WeightedRandomContainer<>();
    //}

    public Crate(String id) {
        this.id = id;
        this.item = ItemBuilder.copyOf(Material.ENDER_CHEST).name("my new crate");
        this.title = "select loot";
        this.columns = 3;
        this.picks = 4;
        this.sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    }

    public Crate(Map<String, Object> args) {
        title = ColorUtil.renderMarkers((String) args.get("title"));

        columns = (int) args.get("columns");
        picks = (int) args.get("picks");
        sound = Sound.valueOf((String) args.get("sound"));

        // TODO eventually remove older revisions
        final int rev = Main.get().rev;
        if (rev < 2)
            item = ItemBuilder.mutable((ItemStack) args.get("itemStack"));
        else
            item = (ItemBuilder) args.get("item");

        if (rev < 4)
            loot = WeightedRandomContainer.cumulative((LinkedHashMap<LootSet, Integer>) args.get("weights"));
        else
            loot = new WeightedRandomContainer<>((Map<LootSet, Integer>) args.get("weights"));
    }

    public String getFormattedPercent(LootSet lootGroup) {
        return String.format("%.02f%%", 100.f * ((float) loot.get(lootGroup)/(float)loot.getWeight()));
    }

    public String getFormattedFraction(LootSet lootGroup) {
        return String.format("%d/%d", loot.get(lootGroup), loot.getWeight());
    }

    /**
     * Return the macro formatted item, or unformatted if player is null
     * @param p player
     * @return the formatted item
     */
    public ItemStack itemStack(@Nullable Player p) {
        return item.copy()
                .replace("crate_picks", "" + picks, '%')
                .placeholders(p)
                .renderAll()
                .build();
    }

    public String getTitle(@Nullable Player p) {
        return ColorUtil.renderAll(Util
                .placeholders(p, this.title
                        .replace("%crate_picks%", "" + picks)
                ));
    }

    @Override
    public String toString() {
        return "id: " + id + "\n" +
                "itemStack: " + item + "\n" +
                "title: " + title + "\n" +
                "size: " + title + "\n" +
                "picks: " + picks + "\n" +
                "sound: " + sound + "\n" +
                "weights: " + loot + "\n";
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("item", item);
        result.put("title", ColorUtil.invertRendered(title));
        result.put("columns", columns);
        result.put("picks", picks);
        result.put("sound", sound.name());
        result.put("weights", loot.getMap());

        return result;
    }

    public AbstractMenu.Builder getBuilder() {
        return new SimpleMenu.SBuilder(5)
                .title(p -> id)
                .background()
                //.onOpen(p -> p.setGameMode(GameMode.CREATIVE))
                //.onClose(p -> {
                //    p.setGameMode();
                //    return Result.PARENT();
                //})
                .parentButton(4, 4)
                // *   *   *
                // Edit Crate ItemStack
                // *   *   *
                .childButton(1, 1, p -> ItemBuilder.copyOf(item).name(Lang.EDIT_ITEM).lore(Lang.LMB_EDIT).build(), new ItemModifyMenu()
                        .build(item.build(), itemStack ->
                                item
                                .apply(itemStack,
                                        ItemBuilder.FLAG_NAME | ItemBuilder.FLAG_LORE | ItemBuilder.FLAG_SKULL | ItemBuilder.FLAG_MATERIAL)
                                .build())
                )
                // Edit Inventory Title
                .childButton(3, 1, p -> ItemBuilder.copyOf(Material.PAPER).name(String.format(Lang.EDIT_TITLE, title)).lore(Lang.LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.TITLE)
                        .leftRaw(p -> title)
                        .onClose((player) -> Result.PARENT())
                        .right(p -> Lang.SPECIAL_FORMATTING, p -> Editor.getColorDem(), ColorUtil.AS_IS)
                        .onComplete((p, s, b) -> {
                            if (!s.isEmpty()) {
                                title = ColorUtil.RENDER_MARKERS.a(s);
                                return Result.PARENT();
                            }

                            return Result.TEXT(Lang.ERR_INVALID);
                        })
                )
                // *   *   *
                // Edit LootSets
                // *   *   *
                .childButton(5, 1, p -> ItemBuilder.fromModernMaterial("EXPERIENCE_BOTTLE").name(Lang.LOOT).lore(Lang.LMB_EDIT).build(), new ParallaxMenu.PBuilder()
                        .title(p -> Lang.LOOT)
                        .parentButton(4, 5)
                        .onClose((player) -> Result.PARENT())
                        .addAll((builder, p) -> {
                            ArrayList<Button> result1 = new ArrayList<>();

                            for (LootSet lootSet : Main.get().data.lootSets.values()) {
                                Integer weight = loot.get(lootSet);
                                Button.Builder btn = new Button.Builder();
                                ItemBuilder b = ItemBuilder.copyOf(lootSet.item.getMaterial()).name("&8" + lootSet.id);
                                if (weight != null) {
                                    b.lore("&7" + getFormattedFraction(lootSet) + "\n" +
                                            "&7" + getFormattedPercent(lootSet) + "\n" +
                                            Lang.LMB_DEC + "\n" +
                                            Lang.RMB_INC + "\n" +
                                            Lang.MMB_TOGGLE + "\n" +
                                            Lang.SHIFT_MUL).glow(true);
                                    btn.mmb(interact -> {
                                        // toggle inclusion
                                        loot.remove(lootSet);
                                        return Result.REFRESH();
                                    }).lmb(interact -> {
                                        // capped decrement
                                        int change = interact.shift ? 5 : 1;

                                        loot.add(lootSet, MathUtil.clamp(weight - change, 1, Integer.MAX_VALUE));

                                        return Result.REFRESH();
                                    }).rmb(interact -> {
                                        // capped increment
                                        int change = interact.shift ? 5 : 1;

                                        loot.add(lootSet, MathUtil.clamp(weight + change, 1, Integer.MAX_VALUE));

                                        return Result.REFRESH();
                                    });
                                } else {
                                    b.lore(Lang.MMB_TOGGLE);
                                    btn.mmb(interact -> {
                                        loot.add(lootSet, 1);
                                        return Result.REFRESH();
                                    });
                                }
                                result1.add(btn.icon(p1 -> b.build()).get());
                            }

                            return result1;
                        })
                )
                // *   *   *
                // Edit Columns
                // *   *   *
                .button(7, 1, new Button.Builder()
                        .icon(p -> ItemBuilder.copyOf(Material.LADDER).name(String.format(Lang.BUTTON_COLUMNS, columns)).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC).amount(columns).build())
                        .lmb(interact -> {
                            // decrease
                            columns = MathUtil.clamp(columns - 1, 1, 6);
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            // decrease
                            columns = MathUtil.clamp(columns + 1, 1, 6);
                            return Result.REFRESH();
                        }))
                // *   *   *
                // Edit Picks
                // *   *   *
                .button(2, 3, new Button.Builder()
                        .icon(p -> ItemBuilder.copyOf(Material.MELON_SEEDS).name(String.format(Lang.BUTTON_PICKS, picks)).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC).amount(picks).build())
                        .lmb(interact -> {
                            // decrease
                            picks = MathUtil.clamp(picks - 1, 1, columns*9);
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            // decrease
                            picks = MathUtil.clamp(picks + 1, 1, columns*9);
                            return Result.REFRESH();
                        }))
                // *   *   *
                // Edit Pick Sound
                // *   *   *
                .childButton(6, 3, p -> ItemBuilder.copyOf(Material.JUKEBOX).name(String.format(Lang.BUTTON_SOUND, sound)).lore(Lang.LMB_EDIT).build(),
                        new TextMenu.TBuilder()
                                .title(p -> Lang.TITLE_SOUND)
                                .leftRaw(p -> Editor.LOREM_IPSUM)
                                .right(p -> Lang.INPUT_SOUND)
                                .onClose((player) -> Result.PARENT())
                                .onComplete((p, s, b) -> {
                                    try {
                                        Sound sound = Sound.valueOf(s.toUpperCase());
                                        this.sound = sound;
                                        p.playSound(p.getLocation(), sound, 1, 1);
                                        return Result.PARENT();
                                    } catch (Exception e) {
                                        return Result.TEXT(Lang.ERR_INVALID);
                                    }
                                })
                );
    }

    public AbstractMenu.Builder getPreview() {
        return new ParallaxMenu.PBuilder()
                .title(p -> "Preview")
                .addAll((self, p) -> {
                    List<Button> buttons = new ArrayList<>();

                    for (Map.Entry<LootSet, Integer> entry : loot.getMap().entrySet()) {
                        LootSet lootSet = entry.getKey();
                        int weight = entry.getValue();
                        double chance = ((double)weight / (double)loot.getWeight()) * 100.;
                        buttons.add(new Button.Builder()
                                .icon(p00 -> ItemBuilder.copyOf(lootSet.item).lore(String.format("&8%.02f%%", chance)).build()).get());
                    }

                    return buttons;
                });
    }

}
