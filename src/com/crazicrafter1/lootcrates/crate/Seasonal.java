package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.util.ItemBuilder;
import com.crazicrafter1.lootcrates.util.ReflectionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.Month;
import java.time.MonthDay;

public enum Seasonal {

    HALLOWEEN("&6&lSpooky",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
                    "Y2NlZDRiY2ZkMjExNjQ2NGRlZGYxNTdiZmM2MmRiMjZjOTU3YTlhNmFjOGJiYzUyNTYzNDY3MDg1YmUyMyJ9fX0=",
            MonthDay.of(Month.OCTOBER, 24), MonthDay.of(Month.OCTOBER, 31)),

    THANKSGIVING("&2&lAwkward",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
                    "ODBmNjljMWQ2MTFkNjQ3MjI4NjZmNTk5MWRlM2JiZDdjNmY3ZjA1ODVjNWVjZjg4YmRiNWY2YWZiNGM3OGQifX19",
            MonthDay.of(Month.NOVEMBER, 23), MonthDay.of(Month.NOVEMBER, 30)), //MonthDay.of(Month.NOVEMBER, 28)),

    CHRISTMAS1("&c&lF&2&le&c&ls&2&lt&c&li&2&lv&c&le",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
                    "ZDQxZmJlYTljMmQxOTA0MGU1NjdmMzg3YWI0NmIyZjhhM2ExZGE4ZWVjOWQzOTllMmU0YWRjZjA1YWRhOGEyYSJ9fX0=",
            MonthDay.of(Month.DECEMBER, 1), MonthDay.of(Month.DECEMBER, 31)), //MonthDay.of(Month.DECEMBER, 25)),

    CHRISTMAS2("&c&lF&2&le&c&ls&2&lt&c&li&2&lv&c&le",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
                    "ZDQxZmJlYTljMmQxOTA0MGU1NjdmMzg3YWI0NmIyZjhhM2ExZGE4ZWVjOWQzOTllMmU0YWRjZjA1YWRhOGEyYSJ9fX0=",
            MonthDay.of(Month.JANUARY, 1), MonthDay.of(Month.JANUARY, 6)), //MonthDay.of(Month.DECEMBER, 25)),

    EASTER("&e&lBountiful",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
                    "YjJjZDVkZjlkN2YxZmE4MzQxZmNjZTJmM2MxMThlMmY1MTdlNGQyZDk5ZGYyYzUxZDYxZDkzZWQ3ZjgzZTEzIn19fQ==",
            MonthDay.of(Month.APRIL, 3), MonthDay.of(Month.APRIL, 5)),

    JULY_FOURTH("&f&lP&4&la&1&lt&f&lr&4&li&1&lo&f&lt&4&li&1&lc",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
                    "N2QxNWQ1NjYyMDJhYzBlNzZjZDg5Nzc1OWRmNWQwMWMxMWY5OTFiZDQ2YzVjOWEwNDM1N2VhODllZTc1In19fQ==",
            MonthDay.of(Month.JUNE, 28), MonthDay.of(Month.JULY, 5)), //MonthDay.of(Month.JULY, 4));

    CAKE_DAY("&2CAKE",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
                    "NmIyNmMxMjJlZGYwZDExZTQ2NWE1OTEyMDkwMDYwYWUyOTI3NGQyM2IxOWZkYjhkNzdiMWQ0YjM3NzNhN2VjZCJ9fX0=",
            MonthDay.of(Month.SEPTEMBER, 7), MonthDay.of(Month.SEPTEMBER, 7));



    //private final String prefix;
    private final MonthDay startTime;
    private final MonthDay endTime;

    private final ItemStack itemStack;

    private static final Seasonal[] values = Seasonal.values();

    Seasonal(String prefix, String base64HeadTexture, MonthDay startTime, MonthDay endTime) {
        //this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        this.startTime = startTime;
        this.endTime = endTime;

        if (!ReflectionUtil.isOldVersion())
            this.itemStack = ItemBuilder.builder(Material.PLAYER_HEAD).skull(base64HeadTexture).toItem();
        else {
            this.itemStack = null;
            Main.getInstance().debug("Using old version compatability for seasonal");
        }
    }

    private boolean isToday() {
        final MonthDay today = MonthDay.now();

        return today.compareTo(startTime) >= 0 && today.compareTo(endTime) <= 0;
    }

    public static ItemStack getSeasonalItem() {
        if (ReflectionUtil.isOldVersion()) return null;
        for (Seasonal seasonal : Seasonal.values) {
            if (seasonal.isToday()) return new ItemStack(seasonal.itemStack);
        }
        return null;
    }
}
