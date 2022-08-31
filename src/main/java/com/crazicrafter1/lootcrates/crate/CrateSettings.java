package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.crutils.ui.*;
import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class CrateSettings {
    public final String id;
    public String title;
    public int columns;
    public int picks;
    public Sound sound;
    public WeightedRandomContainer<LootSetSettings> loot;
    public ItemStack item;

    public CrateSettings copy() {
        final String strippedId = Main.NUMBER_AT_END.matcher(id).replaceAll("");
        String newId;
        for (int i=0; Main.get().rewardSettings.crates.containsKey(newId = strippedId + i); i++) {}

        return new CrateSettings(newId, title, columns, picks, sound, new WeightedRandomContainer<>(new HashMap<>(loot.getMap())), item.clone());
    }

    //todo hmmm kinda ugly
    @Deprecated
    public CrateSettings(String id) {
        this.id = id;
        this.title = "select loot";
        this.columns = 3;
        this.picks = 4;
        this.sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        this.loot = new WeightedRandomContainer<>();
        this.item = ItemBuilder.mut(LootcratesAPI.getCrateAsItem(new ItemStack(Material.ENDER_CHEST), id)).name("my new crate").build();
    }

    //todo remove post-migrate
    public CrateSettings(String id, String title, int columns, int picks, Sound sound, WeightedRandomContainer<LootSetSettings> loot, ItemStack item) {
        this.id = id;
        this.title = title;
        this.columns = columns;
        this.picks = picks;
        this.sound = sound;
        this.loot = loot;
        this.item = item;
    }

    //public CrateSettings(String id, Map<String, Object> args) {
    //    this.id = id;
    //    this.title = ColorUtil.renderMarkers((String) args.get("title"));
    //    this.columns = (int) args.get("columns");
    //    this.picks = (int) args.get("picks");
    //    this.sound = Sound.valueOf((String) args.get("sound"));
    //    this.loot = new WeightedRandomContainer<>((Map<LootSetSettings, Integer>) args.get("weights"));
    //    this.item = (ItemStack) args.get("item");
    //}

    @NotNull
    public void serialize(ConfigurationSection section) {
        section.set("item", item);
        section.set("title", ColorUtil.invertRendered(title));
        section.set("columns", columns);
        section.set("picks", picks);
        section.set("sound", sound.name());
        section.set("weights", loot.getMap().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().id, Map.Entry::getValue)));
    }

    private String getFormattedPercent(LootSetSettings lootGroup) {
        return String.format("%.02f%%", 100.f * ((float) loot.get(lootGroup)/(float)loot.getWeight()));
    }

    private String getFormattedFraction(LootSetSettings lootGroup) {
        return String.format("%d/%d", loot.get(lootGroup), loot.getWeight());
    }

    /**
     * Return the macro formatted item, or unformatted if player is null
     * @param p player
     * @return the formatted item
     */
    public ItemStack itemStack(@Nullable Player p) {
        return ItemBuilder.copy(item)
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
                .childButton(1, 1, p -> ItemBuilder.copy(item).name(Lang.EDIT_ITEM).lore(Lang.ED_LMB_EDIT).build(), new ItemModifyMenu()
                        .build(item, itemStack ->
                                ItemBuilder.mut(item)
                                        .apply(itemStack,
                                                ItemBuilder.FLAG_NAME | ItemBuilder.FLAG_LORE | ItemBuilder.FLAG_SKULL | ItemBuilder.FLAG_MATERIAL)
                                        .build())
                )
                // Edit Inventory Title
                .childButton(3, 1, p -> ItemBuilder.copy(Material.PAPER).name(String.format(Lang.EDIT_TITLE, title)).lore(Lang.ED_LMB_EDIT).build(), new TextMenu.TBuilder()
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
                .childButton(5, 1, p -> ItemBuilder.fromModernMaterial("EXPERIENCE_BOTTLE").name(Lang.LOOT).lore(Lang.ED_LMB_EDIT).build(), new ParallaxMenu.PBuilder()
                        .title(p -> Lang.LOOT)
                        .parentButton(4, 5)
                        .onClose((player) -> Result.PARENT())
                        .addAll((builder, p) -> {
                            ArrayList<Button> result1 = new ArrayList<>();

                            for (LootSetSettings lootSet : Main.get().rewardSettings.lootSets.values()) {
                                Integer weight = loot.get(lootSet);
                                Button.Builder btn = new Button.Builder();
                                ItemBuilder b = ItemBuilder.copy(lootSet.itemStack.getType()).name("&8" + lootSet.id);
                                if (weight != null) {
                                    b.lore("&7" + getFormattedFraction(lootSet) + "\n" +
                                            "&7" + getFormattedPercent(lootSet) + "\n" +
                                            Lang.ED_LMB_DEC + "\n" +
                                            Lang.ED_RMB_INC + "\n" +
                                            Lang.ED_MMB_TOGGLE + "\n" +
                                            Lang.ED_SHIFT_MUL).glow(true);
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
                                    b.lore(Lang.ED_MMB_TOGGLE);
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
                        .icon(p -> ItemBuilder.copyOf(Material.LADDER).name(String.format(Lang.ED_Crates_PROTO_BTN_Columns, columns)).lore(Lang.ED_LMB_DEC + "\n" + Lang.ED_RMB_INC).amount(columns).build())
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
                        .icon(p -> ItemBuilder.copyOf(Material.MELON_SEEDS).name(String.format(Lang.ED_Crates_PROTO_BTN_Picks, picks)).lore(Lang.ED_LMB_DEC + "\n" + Lang.ED_RMB_INC).amount(picks).build())
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
                .childButton(6, 3, p -> ItemBuilder.copyOf(Material.JUKEBOX).name(String.format(Lang.ED_Crates_PROTO_BTN_Sound, sound)).lore(Lang.ED_LMB_EDIT).build(),
                        new TextMenu.TBuilder()
                                .title(p -> Lang.ED_Crates_PROTO_Sound_TI)
                                .leftRaw(p -> Editor.LOREM_IPSUM)
                                .right(p -> Lang.ED_Crates_PROTO_Sound_R)
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

                    for (Map.Entry<LootSetSettings, Integer> entry : loot.getMap().entrySet()) {
                        LootSetSettings lootSet = entry.getKey();
                        int weight = entry.getValue();
                        double chance = ((double)weight / (double)loot.getWeight()) * 100.;
                        buttons.add(new Button.Builder()
                                .icon(p00 -> ItemBuilder.copy(lootSet.itemStack).lore(String.format("&8%.02f%%", chance)).build()).get());
                    }

                    return buttons;
                });
    }

}

