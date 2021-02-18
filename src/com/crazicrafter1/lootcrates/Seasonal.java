package com.crazicrafter1.lootcrates;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.time.Month;
import java.time.MonthDay;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public enum Seasonal {

    HALLOWEEN("&6&lSpooky",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2" +
                    "NlZDRiY2ZkMjExNjQ2NGRlZGYxNTdiZmM2MmRiMjZjOTU3YTlhNmFjOGJiYzUyNTYzNDY3MDg1YmUyMyJ9fX0=",
            MonthDay.of(Month.OCTOBER, 31)),

    THANKSGIVING("&2&lAwkward",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
                    "ODBmNjljMWQ2MTFkNjQ3MjI4NjZmNTk5MWRlM2JiZDdjNmY3ZjA1ODVjNWVjZjg4YmRiNWY2YWZiNGM3OGQifX19",
            MonthDay.of(Month.NOVEMBER, 28)), //MonthDay.of(Month.NOVEMBER, 28)),

    CHRISTMAS("&c&lF&2&le&c&ls&2&lt&c&li&2&lv&c&le",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1c" +
                    "mUvZDQxZmJlYTljMmQxOTA0MGU1NjdmMzg3YWI0NmIyZjhhM2ExZGE4ZWVjOWQzOTllMmU0YWRjZjA1YWRhOGEyYSJ9fX0=",
            MonthDay.of(Month.DECEMBER, 25)), //MonthDay.of(Month.DECEMBER, 25)),

    EASTER("&e&lBountiful",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjJjZ" +
                    "DVkZjlkN2YxZmE4MzQxZmNjZTJmM2MxMThlMmY1MTdlNGQyZDk5ZGYyYzUxZDYxZDkzZWQ3ZjgzZTEzIn19fQ==",
            MonthDay.of(Month.APRIL, 12)),

    JULY_FOURTH("&f&lP&4&la&1&lt&f&lr&4&li&1&lo&f&lt&4&li&1&lc",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cm" +
                    "UvN2QxNWQ1NjYyMDJhYzBlNzZjZDg5Nzc1OWRmNWQwMWMxMWY5OTFiZDQ2YzVjOWEwNDM1N2VhODllZTc1In19fQ==",
            MonthDay.of(Month.JULY, 4)), //MonthDay.of(Month.JULY, 4));

    CAKE_DAY("&2Cake Day",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ2Nzg3YjI2M2Q0NjBiYzdjZGY2ODI1NWQxYzhkNjM3ZGNlNGI5OTM2OGNlNmZhYTczZTdkNzg2YjhkNjVjYyJ9fX0=",
            MonthDay.of(Month.SEPTEMBER, 7));



    private final String prefix;
    private final String base64HeadTexture;
    private final MonthDay monthDay;

    Seasonal(String prefix, String base64HeadTexture, MonthDay monthDay) {
        this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        this.base64HeadTexture = base64HeadTexture;
        this.monthDay = monthDay;
    }

    public String getPrefix() {
        return prefix;
    }

    public ItemStack getHead() {
        return ItemBuilder.builder(SkullUtil.itemFromBase64(base64HeadTexture)).name(prefix).toItem();
    }

    public boolean isToday() {


        //System.out.println("Comparing seasonal times ...");

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        //getTime() returns the current monthDay in default time zone
        Date date = calendar.getTime();
        //int day = calendar.get(Calendar.DATE);
        //Note: +1 the month for current month
        int month = calendar.get(Calendar.MONTH)+1;
        //int year = calendar.get(Calendar.YEAR);
        //int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        //int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        //System.out.println("event day : " + monthDay.getDayOfMonth() + ", today : " + dayOfMonth);
        //System.out.println("event month : " + monthDay.getMonthValue() + ", today : " + month);

        return monthDay.getDayOfMonth() == dayOfMonth && monthDay.getMonthValue() == month;

        //return monthDay.isAfter(MonthDay.of(monthDay.getMonth(), monthDay.getDayOfMonth()-1));
    }

    @Deprecated
    public boolean isNearing(int maxDayDifference) {

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        //getTime() returns the current monthDay in default time zone
        Date date = calendar.getTime();
        //int day = calendar.get(Calendar.DATE);
        //Note: +1 the month for current month
        int month = calendar.get(Calendar.MONTH);
        //int year = calendar.get(Calendar.YEAR);
        //int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        //int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);



        return monthDay.getDayOfMonth() == dayOfMonth && monthDay.getMonthValue() == month;

        //return monthDay.isAfter(MonthDay.of(monthDay.getMonth(), monthDay.getDayOfMonth()-1));
    }

}
